package com.krake.pdf;

import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.krake.pdf.utilities.PDFConstants;

import java.io.IOException;

/**
 * Fragment that displays pdf above api 21.
 * There's also a bottom bar with two styleable buttons and a TextView that indicates number of pages
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PDFRendererFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    /**
     * PdfRenderer to render the PDF.
     */
    private PdfRenderer mPdfRenderer;
    /**
     * ViewPager to display pdf pages
     */
    private ViewPager mPdfPager;
    /**
     * adapter that contains pdf rendering
     */
    private PDFPagerAdapter mPagerAdapter;
    /**
     * Button to move to the previous page.
     */
    private ImageButton mButtonPrevious;

    /**
     * Button to move to the next page.
     */
    private ImageButton mButtonNext;

    /**
     * TextView to show page number
     */
    private TextView mPageNumberTv;

    public PDFRendererFragment() {
        // empty constructor
    }

    /**
     * Create a new fragment with default bundle
     *
     * @param uri uri of pdf to pass via bundle
     * @return new PDFRendererFragment with default arguments
     */
    public static PDFRendererFragment getInstance(String uri) {
        Bundle toSend = new Bundle();
        toSend.putString(PDFConstants.ARG_PDF_URI, uri);
        PDFRendererFragment fragment = new PDFRendererFragment();
        fragment.setArguments(toSend);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_renderer_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // obtain pdf path from bundle
        Bundle b = getArguments();
        if (b != null &&
                b.containsKey(PDFConstants.ARG_PDF_URI) &&
                !TextUtils.isEmpty(b.getString(PDFConstants.ARG_PDF_URI))) {

            // display pdf pages
            mPageNumberTv = view.findViewById(R.id.tv_pdf_page_number);
            mPageNumberTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.pdf_actions_color));

            mButtonPrevious = view.findViewById(R.id.btn_previous_page);
            mButtonNext = view.findViewById(R.id.btn_next_page);

            // set color to ImageButton programmatically
            mButtonPrevious.setColorFilter(ContextCompat.getColor(getActivity(), R.color.pdf_actions_color),
                    PorterDuff.Mode.SRC_ATOP);
            mButtonNext.setColorFilter(ContextCompat.getColor(getActivity(), R.color.pdf_actions_color),
                    PorterDuff.Mode.SRC_ATOP);

            mButtonPrevious.setOnClickListener(this);
            mButtonNext.setOnClickListener(this);

            Uri pdfUri = Uri.parse(b.getString(PDFConstants.ARG_PDF_URI));
            try {
                ParcelFileDescriptor descriptor = getActivity().getContentResolver().openFileDescriptor(pdfUri, "r");
                mPdfRenderer = new PdfRenderer(descriptor);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mPdfRenderer != null) {
                mPagerAdapter = new PDFPagerAdapter(getActivity(), mPdfRenderer);
                mPdfPager = view.findViewById(R.id.pdf_pager);
                // listener to know the state of the page
                mPdfPager.addOnPageChangeListener(this);
                mPdfPager.setVisibility(View.VISIBLE);
                // it loads current fragment and next one not shown
                mPdfPager.setOffscreenPageLimit(1);
                // set a PDFPagerAdapter
                mPdfPager.setAdapter(mPagerAdapter);
                int index = 0;
                // if there's something in savedInstanceState Bundle, load correct page
                if (null != savedInstanceState) {
                    index = savedInstanceState.getInt(PDFConstants.STATE_CURRENT_PAGE_INDEX, 0);
                }
                // updateUi with correct page
                updateUi(index);
            }
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onDetach() {
        try {
            if (mPagerAdapter != null) {
                // closeRenderer method in PDFPagerAdapter
                mPagerAdapter.closeRenderer();
            }
            if (mPdfPager != null) {
                // force destroyItem of adapter
                mPdfPager.setAdapter(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the state of the page
        if (mPdfPager != null && mPagerAdapter != null)
            outState.putInt(PDFConstants.STATE_CURRENT_PAGE_INDEX, mPdfPager.getCurrentItem());
    }

    public void updateUi(int position) {
        toggleImageButton(mButtonPrevious, position <= 0);
        toggleImageButton(mButtonNext, position >= mPagerAdapter.getCount() - 1);

        mPageNumberTv.setText(String.format("%s / %s", String.valueOf(position + 1), String.valueOf(mPagerAdapter.getCount())));
    }

    /**
     * Method that will toggle state of imagebutton applying a color alpha filter
     *
     * @param button    imagebutton that you want to toggle
     * @param condition if true, disable imagebutton, if false, enable it
     */
    private void toggleImageButton(ImageButton button, boolean condition) {
        if (condition) {
            button.setAlpha(0.3f);
            button.setEnabled(false);
        } else {
            button.setAlpha(1f);
            button.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        // set next or previous page in PDFPagerAdapter
        if (i == R.id.btn_previous_page) {
            mPdfPager.setCurrentItem(mPdfPager.getCurrentItem() - 1, true);
        } else if (i == R.id.btn_next_page) {
            mPdfPager.setCurrentItem(mPdfPager.getCurrentItem() + 1, true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // update buttons state and textview with PDFPagerAdapter and ViewPager
        updateUi(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}