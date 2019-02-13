package com.krake.pdf;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.krake.pdf.utilities.PDFConstants;

/**
 * Fullscreen Activity that switch two fragments:
 * PDFWebviewFragment: show webview with pdf not downloaded
 * PDFRendererFragment: it renders pdf in app
 */

public class PDFActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        Bundle b = getIntent().getExtras();
        String pdfUri = getIntent().getStringExtra(PDFConstants.ARG_PDF_URI);

        // if it's true show the fragment with the webview, else show PDFRendererFragment
        if (b.getBoolean(PDFConstants.ARG_SHOW_IN_WEBVIEW)) {
            showPdfWebviewFragment(pdfUri);
        } else {
            showPdfRendererFragment(pdfUri);
        }
    }

    /**
     * This method will put a PDFWebviewFragment that will show a pdf through Google Docs
     *
     * @param url url of your pdf
     */
    @SuppressLint("CommitTransaction")
    private void showPdfWebviewFragment(String url) {
        PDFWebviewFragment pdfFragment = (PDFWebviewFragment) getSupportFragmentManager().findFragmentByTag(PDFConstants.FRAGMENT_PDF_WEBVIEW);
        if (pdfFragment == null)
            pdfFragment = PDFWebviewFragment.getInstance(url);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_pdf,
                        pdfFragment,
                        PDFConstants.FRAGMENT_PDF_WEBVIEW);

        transaction.commit();
    }

    /**
     * This method will put a PDFRendererFragment that will show a local pdf in a container
     *
     * @param path path of pdf file
     */
    @SuppressLint("CommitTransaction")
    private void showPdfRendererFragment(String path) {
        PDFRendererFragment pdfFragment = (PDFRendererFragment) getSupportFragmentManager().findFragmentByTag(PDFConstants.FRAGMENT_PDF_RENDERER);
        if (pdfFragment == null)
            pdfFragment = PDFRendererFragment.getInstance(path);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_pdf,
                        pdfFragment,
                        PDFConstants.FRAGMENT_PDF_RENDERER);

        transaction.commit();
    }
}