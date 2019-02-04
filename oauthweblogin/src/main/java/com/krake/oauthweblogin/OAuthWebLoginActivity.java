package com.krake.oauthweblogin;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.krake.core.R;

import java.lang.ref.WeakReference;

public class OAuthWebLoginActivity extends AppCompatActivity {

    public static final String EXTRA_COMPLETE_CALL_URL = "CallUrl";
    public static final String EXTRA_RESULT_COMPLETE_CALL_URL = "ResultUrl";


    private WebView webView;
    private String redirectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauthwebview_login);

        redirectUri = getString(R.string.orchard_base_service_url) + getString(R.string.orchard_login_redirect_path);

        webView = findViewById(R.id.loginWebView);
        webView.setWebViewClient(new LoginWebViewClient(this));

        webView.loadUrl(getIntent().getStringExtra(EXTRA_COMPLETE_CALL_URL));

    }

    private static class LoginWebViewClient extends WebViewClient {
        WeakReference<OAuthWebLoginActivity> activity;

        protected LoginWebViewClient(OAuthWebLoginActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (!handleRedirectUrl(request.getUrl()))
                return super.shouldOverrideUrlLoading(view, request);

            return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!handleRedirectUrl(Uri.parse(url)))
                return super.shouldOverrideUrlLoading(view, url);

            return true;
        }

        private boolean handleRedirectUrl(Uri uri) {

            if (uri.toString().startsWith(activity.get().redirectUri)) {

                Intent resultIntent = new Intent();

                resultIntent.putExtra(EXTRA_RESULT_COMPLETE_CALL_URL, uri.toString());

                activity.get().setResult(RESULT_OK, resultIntent);
                activity.get().finish();
                return true;
            }

            return false;
        }
    }
}
