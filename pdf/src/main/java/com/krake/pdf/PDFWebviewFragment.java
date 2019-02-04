package com.krake.pdf;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.krake.pdf.utilities.PDFConstants;

/**
 * Fragment that loads a webview with a pdf shown through Google Docs.
 * It doesn't need to download it
 */
public class PDFWebviewFragment extends Fragment {
    public PDFWebviewFragment() {
        // empty constructor
    }

    /**
     * Create a new fragment with default bundle
     *
     * @param url url to pass via bundle
     * @return new PDFWebviewFragment with default arguments
     */
    /* package */ static PDFWebviewFragment getInstance(String url) {
        Bundle toSend = new Bundle();
        toSend.putString(PDFConstants.ARG_PDF_URI, url);
        PDFWebviewFragment fragment = new PDFWebviewFragment();
        fragment.setArguments(toSend);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_webview_fragment, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle b = getArguments();
        if (b != null &&
                b.containsKey(PDFConstants.ARG_PDF_URI) &&
                !TextUtils.isEmpty(b.getString(PDFConstants.ARG_PDF_URI))) {

            String pdfUri = b.getString(PDFConstants.ARG_PDF_URI);

            final ProgressBar mProgress = view.findViewById(android.R.id.progress);
            WebView wv = view.findViewById(R.id.wv_PDF);

            wv.getSettings().setJavaScriptEnabled(true);
            wv.getSettings().setPluginState(WebSettings.PluginState.ON);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // fix for a bug with api 19 that opens the browser automatically
                    return false;
                }

                public void onPageFinished(WebView view, String url) {
                    if (mProgress != null) {
                        mProgress.setVisibility(View.GONE);
                    }
                }
            });
            wv.loadUrl(getString(R.string.pdf_google_docs_url) + pdfUri);
        }
    }
}