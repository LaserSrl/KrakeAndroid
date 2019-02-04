package com.krake.pdf.utilities;

/**
 * Common constants for pdf
 */
public class PDFConstants {
    private PDFConstants() {
        // private constructor to avoid instantiation
    }

    /**
     * extension for pdf
     */
    public static final String PDF_EXTENSION = ".pdf";
    /**
     * default mime type for pdf
     */
    public static final String PDF_MIME_TYPE = "application/pdf";
    /**
     * Key string to pass via bundle url or path of pdf
     */
    public static final String ARG_PDF_URI = "pdfUriOrPath";
    /**
     * SharedPreferences name for PDF
     */
    public static final String PDF_PREFS = "PDFPrefs";
    /**
     * Key string to pass via bundle a boolean:
     * true: will add PDFWebviewFragment
     * false: will add PDFRendererFragment
     */
    public static final String ARG_SHOW_IN_WEBVIEW = "showPdfInWebview";
    /**
     * Key string for saving the state of current page index
     */
    public static final String STATE_CURRENT_PAGE_INDEX = "currentPdfPageIndex";
    /**
     * Key string for the tag of pdfRendererFragment
     */
    public static final String FRAGMENT_PDF_RENDERER = "pdfRendererFragment";
    /**
     * Key string for the tag of pdfWebviewFragment
     */
    public static final String FRAGMENT_PDF_WEBVIEW = "pdfWebviewFragment";
}