package com.krake.core.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.krake.core.app.OpenWebContentFragment;

import java.lang.ref.WeakReference;

/**
 * Created by joel on 17/11/14.
 */
public class DescriptionWebViewClient extends WebViewClient {

    private WeakReference<FragmentActivity> mActivity;

    /**
     * @param activity se il parametro è nulla non sarà mostrata la richiesta per aprire le pagine selezionate
     */
    public DescriptionWebViewClient(@Nullable FragmentActivity activity) {
        if (activity != null)
            mActivity = new WeakReference<>(activity);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if (mActivity != null && mActivity.get() != null)
            OpenWebContentFragment.newInstance(url).show(mActivity.get().getSupportFragmentManager(), "alert");

        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        if (mActivity != null && mActivity.get() != null)
            OpenWebContentFragment.newInstance(request.getUrl().toString()).show(mActivity.get().getSupportFragmentManager(), "alert");

        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        view.setVisibility(View.VISIBLE);
    }
}
