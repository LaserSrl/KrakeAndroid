package com.krake.pdf;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.krake.pdf.utilities.PDFConstants;
import com.krake.pdf.utilities.PDFUtils;

import java.io.FileNotFoundException;

/**
 * Created by laser on 26/08/15.
 * Core methods of this pdf library like download or intent types.
 */
public class PDFHelper {
    private static final String TAG = PDFHelper.class.getSimpleName();

    private PDFHelper() {
        // empty constructor to avoid instantiation
    }

    /**
     * Shows a pdf downloaded from a url with three possibilities:
     * - In-app rendering above Lollipop
     * - App chooser below Lollipop if the device has app that can open PDF
     * - Webview with Google Docs as the last choice
     *
     * @param context your context
     * @param url     the url of the pdf
     */
    public static void showPdf(Context context, String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createRendererDownloadRequest(context, url);
        } else {
            if (PDFUtils.canDisplayPdf(context)) {
                createChooserDownloadRequest(context, url);
            } else {
                displayPdfThroughWebview(context, url);
            }
        }
    }

    /**
     * This method will download pdf from url and will open a new activity with PDFRendererFragment using its download path.
     * Filename will be generated from partial url
     *
     * @param context your current context
     * @param url     url of your pdf
     */
    private static void createRendererDownloadRequest(Context context, String url) {
        Uri existingUri = pdfInCache(context, url);
        if (existingUri == null) {
            downloadPdfFromUrl(context, url);
            context.registerReceiver(onCompleteToRender, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } else {
            Log.d(TAG, "createRendererDownloadRequest: uri = " + existingUri.toString());
            openPdfRenderer(context, existingUri);
        }
    }

    /**
     * This method will download a pdf from url and will open it with an app chooser
     * Filename will be extracted from url
     *
     * @param context Context of your Activity in which you start this intent
     * @param url     url of your pdf
     */
    private static void createChooserDownloadRequest(Context context, String url) {
        Uri existingUri = pdfInCache(context, url);
        if (existingUri == null) {
            downloadPdfFromUrl(context, url);
            context.registerReceiver(onCompleteToChooser, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } else {
            openAppChooser(context, existingUri);
        }
    }

    /**
     * Check if a pdf file is downloaded and if the file is in cache (3 days)
     *
     * @param context current context
     * @param url     url of the file to extract the filename
     * @return path of pdf file if is downloaded and in cache, null instead
     */
    private static Uri pdfInCache(Context context, final String url) {
        String fileName = PDFUtils.extractedFilenamFromUrl(url);
        Uri pdfUri = getUriFromPrefs(context, fileName);
        if (pdfUri != null) {
            try {
                ParcelFileDescriptor descriptor = context.getContentResolver().openFileDescriptor(pdfUri, "r");
                if (descriptor != null && descriptor.getFileDescriptor().valid()) {
                    return pdfUri;
                }
            } catch (FileNotFoundException ignored) {
                // ignoring the exception if thrown
            }
        }
        saveUriInPrefs(context, fileName, null);
        return null;
    }

    /**
     * This method will start another activity in your application with a webview to display pdf from google docs.
     * The pdf will not be downloaded and the user can see it live.
     *
     * @param context Context of your Activity in which you start this intent
     * @param url     Url of your pdf
     */
    private static void displayPdfThroughWebview(Context context, String url) {
        Intent pdfIntent = new Intent(context, PDFActivity.class);
        pdfIntent.putExtra(PDFConstants.ARG_PDF_URI, url);
        pdfIntent.putExtra(PDFConstants.ARG_SHOW_IN_WEBVIEW, true);
        context.startActivity(pdfIntent);
    }

    /**
     * This method will download a pdf from url and will put it in download folder
     *
     * @param context Context of your Activity in which you start this intent
     * @param url     Url of pdf to download
     */
    private static void downloadPdfFromUrl(Context context, String url) {
        final String fileName = PDFUtils.extractedFilenameWithExtFromUrl(url);
        // use Download manager to start Download
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));

        // set request parameters
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(PDFUtils.filenameToTitle(fileName))
                .setAllowedOverRoaming(false)
                // show a notification while downloading and after pdf is downloaded
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDescription(context.getString(R.string.pdf_download_file_label) + " " + PDFUtils.filenameToTitle(fileName))
                // destination in download folder
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType(PDFConstants.PDF_MIME_TYPE);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        // enqueue the request to start it
        downloadManager.enqueue(req);
    }

    /**
     * This method receive the bundle of download manager request and handles different intents
     *
     * @param context   Context obtained from onReceive(Context context, Intent intent)
     * @param intent    Intent obtained from onReceive(Context context, Intent intent)
     * @param toChooser boolean that indicates if you are querying a download to open app chooser
     *                  or you want to display it in app
     */
    private static void queryRequestParameters(Context context, Intent intent, boolean toChooser) {
        // get request bundle
        Bundle extras = intent.getExtras();
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri pdfUri = manager.getUriForDownloadedFile(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));

        if (pdfUri != null) {
            String displayName = null;
            Cursor c = context.getContentResolver().query(pdfUri, null, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    displayName = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
                c.close();
            } else {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
                c = manager.query(q);
                if (c != null) {
                    if (c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            displayName = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        }
                    }
                    c.close();
                }
            }

            if (displayName != null) {
                saveUriInPrefs(context, displayName, pdfUri);
            }

            if (toChooser) {
                // display app chooser
                openAppChooser(context, pdfUri);
            } else {
                // open pdf activity and show PDFRendererFragment
                openPdfRenderer(context, pdfUri);
            }
        }

        try {
            if (toChooser) {
                context.unregisterReceiver(onCompleteToChooser);
            } else {
                context.unregisterReceiver(onCompleteToRender);
            }
        } catch (IllegalArgumentException ignored) {
            // receiver wasn't registered
        }
    }

    /**
     * This method will open app chooser to open a local pdf file
     *
     * @param context Context of your Activity in which you start this intent
     * @param uri     pdf content uri
     */
    private static void openAppChooser(Context context, Uri uri) {
        try {
            Intent install = new Intent();
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setAction(Intent.ACTION_VIEW);
            install.setDataAndType(uri, PDFConstants.PDF_MIME_TYPE);
            Intent chooser = Intent.createChooser(install, context.getString(R.string.pdf_open_file_label));
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, R.string.no_app_available, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method will open an activity with a PDFRenderer fragment
     *
     * @param context your current context
     * @param uri     pdf content uri
     */
    private static void openPdfRenderer(Context context, Uri uri) {
        // open pdf activity and show PDFRendererFragment
        Intent pdfIntent = new Intent(context, PDFActivity.class);
        // pass pdf path via bundle
        pdfIntent.putExtra(PDFConstants.ARG_PDF_URI, uri.toString());
        // pass 'false' for key ARG_SHOW_IN_WEBVIEW to open PDFRendererFragment and not WebView
        pdfIntent.putExtra(PDFConstants.ARG_SHOW_IN_WEBVIEW, false);
        // start the activity
        context.startActivity(pdfIntent);
    }

    @SuppressLint("CommitPrefEdits")
    private static void saveUriInPrefs(Context context, String fileName, Uri uri) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PDFConstants.PDF_PREFS, Context.MODE_PRIVATE).edit();
        if (uri == null) {
            editor.remove(fileName);
        } else {
            editor.putString(fileName, uri.toString());
        }
        editor.commit();
    }

    private static Uri getUriFromPrefs(Context context, String fileName) {
        SharedPreferences prefs = context.getSharedPreferences(PDFConstants.PDF_PREFS, Context.MODE_PRIVATE);
        String fileUri = prefs.getString(fileName, null);
        if (fileUri != null) {
            return Uri.parse(fileUri);
        }
        return null;
    }

    /**
     * if showChooser is true, after download will be opened automatically the file chooser
     */
    private static BroadcastReceiver onCompleteToChooser = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            queryRequestParameters(context, intent, true);
        }
    };

    /**
     * receiver to open activity with pdf displayed in a pager
     */
    private static BroadcastReceiver onCompleteToRender = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            queryRequestParameters(context, intent, false);
        }
    };
}