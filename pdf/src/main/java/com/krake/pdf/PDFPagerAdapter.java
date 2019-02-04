package com.krake.pdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.IOException;

/**
 * PagerAdapter for ViewPager with api >= 21 with Lollipop PdfRenderer
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PDFPagerAdapter extends PagerAdapter {
    /**
     * Context of your fragment/activity
     */
    private Context context;
    /**
     * layout inflater to inflate child view
     */
    private LayoutInflater mLayoutInflater;
    /**
     * PdfRenderer to render the PDF.
     */
    private PdfRenderer mPdfRenderer;
    /**
     * Page that is currently shown on the screen.
     */
    private PdfRenderer.Page mCurrentPage;
    /**
     * Imageview that displays current pdf page through bitmap
     */
    private ImageView mImageView;
    /**
     * PdfViewAttacher by Chris Banes to zoom the view
     */
    private PhotoViewAttacher mPva;

    public PDFPagerAdapter(Context context, PdfRenderer pdfRenderer) {
        this.context = context;
        this.mPdfRenderer = pdfRenderer;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final View itemView = mLayoutInflater.inflate(R.layout.pdf_pager_cell, container, false);

        mImageView = itemView.findViewById(R.id.image);
        // attach the zoom to the view
        mPva = new PhotoViewAttacher(mImageView);
        // set maximum zoom (default is 3.0f)
        mPva.setMaximumScale(5.0f);
        // set the scaleType
        mPva.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        showPage(mImageView, position);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public int getCount() {
        return mPdfRenderer.getPageCount();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // remove the current view from viewpager
        container.removeView((View) object);
        // deallocate memory from bitmap to reuse it
        recycleBitmap(mImageView);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
    }

    private void showPage(final ImageView iv, int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (mCurrentPage != null) {
            mCurrentPage.close();
        }

        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        final int width = mCurrentPage.getWidth();
        final int height = mCurrentPage.getHeight();
        try {
            // Change the width and height to increase quality.
            Bitmap bitmap = Bitmap.createBitmap(
                    resizePdfByDensity(width),
                    resizePdfByDensity(height),
                    Bitmap.Config.ARGB_8888);
            // Here, it renders the page onto the Bitmap.
            // To render a portion of the page, use the second and third parameter. Pass nulls to get
            // the default result.
            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
            mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            // show the Bitmap to user.
            iv.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }


    /**
     * Closes the PdfRenderer and related resources.
     *
     * @throws IOException When the PDF file cannot be closed.
     */
    /* package */ void closeRenderer() throws IOException {
        if (mCurrentPage != null) {
            // detach the zoom view
            //TODO: verificare se ancora utile mPva.cleanup();
            // close the page following Google guidelines
            mCurrentPage.close();
        }
        // close the renderer
        mPdfRenderer.close();
    }

    /**
     * Method that resize pdf according to screen size. Only three conditions are used to avoid too big bitmaps
     *
     * @param size size (width or height) of pdf that you want to resize
     * @return resized size as int
     */
    private int resizePdfByDensity(int size) {
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        float multiplier;

        if (dpi > 440) {
            multiplier = 3.5f;
        } else if (dpi > 300) {
            multiplier = 2.5f;
        } else {
            multiplier = 2;
        }

        return (int) (size * multiplier);
    }


    /**
     * Method that recycle drawables and bitmap (e.g. in onDestroyItem in adapter) and release heap allocation
     *
     * @param view view with bitmap as background to release
     */
    private void recycleBitmap(View view) {
        if (view.getBackground() != null) {
            try {
                view.getBackground().setCallback(null);
                ((BitmapDrawable) view.getBackground()).getBitmap().recycle();
                view.destroyDrawingCache();
                view.notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}