package com.krake.pdf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.krake.core.permission.PermissionListener;
import com.krake.core.permission.PermissionManager;

import org.jetbrains.annotations.NotNull;

import kotlin.collections.ArraysKt;

/**
 * Helper for lifecycle methods in a ContentItemDetailFragment
 */
public class PDFDetailFragmentHandler implements PermissionListener {
    private Activity activity;
    private String url;
    private PermissionManager mPermissionManager;

    public PDFDetailFragmentHandler(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        mPermissionManager = new PermissionManager(activity)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationalMsg(activity.getString(R.string.pdf_permission_denied))
                .addListener(this);
        mPermissionManager.create();
    }

    /**
     * Set the url and call invalidate for the options menu to change the visibility
     *
     * @param url url of the pdf
     */
    public void setUrl(String url) {
        this.url = url;
        activity.invalidateOptionsMenu();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_open_pdf, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_pdf).setVisible(url != null);
    }

    /**
     * Request runtime permissions if the version is Marshmallow
     *
     * @param item menu item
     * @return true
     */
    @SuppressLint("NewApi")
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pdf) {
            mPermissionManager.request();
            return true;
        }
        return false;
    }

    public void onDestroy() {
        activity = null;
    }

    @Override
    public void onPermissionsHandled(@NotNull String[] acceptedPermissions) {
        if (ArraysKt.contains(acceptedPermissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PDFHelper.showPdf(activity, url);
        }
    }
}