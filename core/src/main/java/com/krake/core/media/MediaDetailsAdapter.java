package com.krake.core.media;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.krake.core.R;
import com.krake.core.widget.ImageViewHolder;

/**
 * Adapter utilizzato per gestire larghezze differenti a seconda della posizione nella lista.
 * Se la lista ha solo un elemento, la larghezza sarà uguale a quella dello schermo.
 * Nel caso in cui ci sia più di un elemento, la larghezza del primo sarà uguale a 3/2 di quella dichiarata nel layout, quella degli altri rimarrà invariata.
 * <br/>
 * Nel caso in cui si volesse gestire la larghezza degli elementi in modo differente, bisogna creare una classe che estende {@link MediaPartAdapter}.
 */
public class MediaDetailsAdapter extends MediaPartAdapter {
    private int mMediaCellWidth;

    public MediaDetailsAdapter(Fragment fragment, int resource) {
        super(fragment, resource);

        // dimensione di default della cella dei media
        mMediaCellWidth = getContext().getResources().getDimensionPixelSize(R.dimen.detail_media_cell_width);
    }

    public MediaDetailsAdapter(@NonNull Activity activity, @LayoutRes int resource) {
        super(activity, resource);
        mMediaCellWidth = getContext().getResources().getDimensionPixelSize(R.dimen.detail_media_cell_width);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        super.onBindViewHolder(imageViewHolder, i);

        View view = imageViewHolder.itemView;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (getItemCount() == 1) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            if (i == 0) {
                params.width = (int) (mMediaCellWidth * 1.5);
            }
        }
    }
}