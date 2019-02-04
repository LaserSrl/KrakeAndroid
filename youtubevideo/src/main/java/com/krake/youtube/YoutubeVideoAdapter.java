package com.krake.youtube;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.krake.core.widget.ObjectsRecyclerViewAdapter;
import com.krake.youtube.model.YoutubeVideo;
import com.krake.youtube.widget.YoutubeVideoHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Se l'adaoter è utilizzato direttamente è necessario che nel metodo onDestroyView() del fragment o activity chiamante
 * sia impostato a null l'adapter della lista che si appoggia all'adapter.
 * Altrimenti non sarà possibile rilasciare correttament i loader per youtube causando dei leak.
 * Created by joel on 14/11/14.
 */
public class YoutubeVideoAdapter extends ObjectsRecyclerViewAdapter<YoutubeVideo, YoutubeVideoHolder> implements YoutubeVideoHolder.OnThumbnailLoaderAvailable {

    private List<YoutubeVideoHolder> mHolders = new ArrayList<>();

    public YoutubeVideoAdapter(Context context, int layout, List<YoutubeVideo> objects, Class<YoutubeVideoHolder> holderClass) {
        super(context, layout, objects, holderClass);
    }

    @NonNull
    @Override
    protected YoutubeVideoHolder instantiateViewHolder(@NonNull ViewGroup viewGroup, @LayoutRes int viewLayout) {
        YoutubeVideoHolder h = super.instantiateViewHolder(viewGroup, viewLayout);
        mHolders.add(h);
        return h;
    }

    @Override
    public void onBindViewHolder(YoutubeVideoHolder youtubeVideoHolder, int i) {
        youtubeVideoHolder.setListener(this);
        youtubeVideoHolder.setIndex(i);
        if (youtubeVideoHolder.getThumbnailLoader() != null && getItem(i) != null) {
            youtubeVideoHolder.getThumbnailLoader().setVideo(YoutubeVideoUtils.extractVideoIdentifier(getItem(i)));
            youtubeVideoHolder.getThumbnailViewTitle().setText(getItem(i).getTitlePartTitle());
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        for (YoutubeVideoHolder holder : mHolders) {
            holder.releaseLoader();
        }
        mHolders.clear();
    }

    @Override
    public void onThumbnailAvailable(int index) {
        notifyItemChanged(index);
    }
}