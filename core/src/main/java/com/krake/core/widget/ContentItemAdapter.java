package com.krake.core.widget;

import android.content.Context;
import android.widget.ImageView;

import com.krake.core.media.MediaLoadable;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.model.ContentItem;
import com.krake.core.model.ContentItemWithGallery;
import com.krake.core.model.MediaPart;

/**
 * Created by joel on 30/11/15.
 */
public class ContentItemAdapter extends ObjectsRecyclerViewAdapter<ContentItem, ImageTextCellHolder> {

    public ContentItemAdapter(Context context, int layout, Class holderClass) {
        super(context, layout, null, holderClass);
    }

    @Override
    public void onBindViewHolder(final ImageTextCellHolder holder, final int i) {
        if (i < getItemCount()) {
            final ContentItem content = getItem(i);
            if (content != null) {
                holder.getTitleTextView().setText(content.getTitlePartTitle());

                MediaPart photo = null;
                if (content instanceof ContentItemWithGallery)
                    photo = ((ContentItemWithGallery) content).getFirstMedia();

                ImageView imageView = holder.getImageView();
                Context context = getContext();
                if (imageView instanceof MediaLoadable && context != null) {
                    MediaLoader loader = MediaLoader.Companion.with(context, ((MediaLoadable) imageView));

                    if (photo != null)
                        loader.mediaPart(photo);

                    loader.load();
                }
            }
        }
    }
}