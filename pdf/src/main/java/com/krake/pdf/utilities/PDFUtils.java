package com.krake.pdf.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

/**
 * Utilities for this pdf library.
 */
public class PDFUtils {
    private PDFUtils() {
        // private constructor to avoid instantiation
    }

    /**
     * @param url url of pdf
     * @return title from partial url e.g. www.example.com/dummy.pdf will return "dummy"
     */
    public static String extractedFilenamFromUrl(@NonNull String url) {
        String[] partialUrl = url.split("/");
        if (partialUrl.length > 0) {
            String titleWithExtension = partialUrl[partialUrl.length - 1];
            if (titleWithExtension.contains(PDFConstants.PDF_EXTENSION)) {
                return titleWithExtension.replace(PDFConstants.PDF_EXTENSION, "");
            }
        }
        return null;
    }

    /**
     * @param url url of pdf
     * @return filename equal to title extracted from partial url + .pdf
     */
    public static String extractedFilenameWithExtFromUrl(@NonNull String url) {
        return extractedFilenamFromUrl(url) + PDFConstants.PDF_EXTENSION;
    }

    /**
     * @param filename of your pdf
     * @return title trimming .pdf extension
     */
    public static String filenameToTitle(@NonNull String filename) {
        return filename.replace(PDFConstants.PDF_EXTENSION, "");
    }

    /**
     * Check if there's an app installed to open pdf files
     *
     * @param context context of the app
     * @return true if there's at least one app that can open pdf
     */
    public static boolean canDisplayPdf(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType(PDFConstants.PDF_MIME_TYPE);
        return packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }
}