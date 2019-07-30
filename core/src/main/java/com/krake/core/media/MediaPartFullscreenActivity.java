package com.krake.core.media;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import com.krake.core.R;
import com.krake.core.app.KrakeApplication;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.MediaComponentModule;
import com.krake.core.media.streaming.StreamingProvider;
import com.krake.core.media.support.VideoFragment;
import com.krake.core.model.*;
import com.krake.core.widget.CachedFragmentPagerAdapter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MediaPartFullscreenActivity extends MediasFullscreenActivity implements RealmChangeListener<RealmResults> {
    @BundleResolvable
    public MediaComponentModule mediaComponentModule;
    private List mMediasIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ComponentManager.resolveIntent(this);
        super.onCreate(savedInstanceState);

        mMediaPartTextView = findViewById(R.id.media_part_title_text_view);
    }

    @Override
    protected void startDataLoading(Intent intent) {
        List<Long> mediaIds = mediaComponentModule.getMediaIds();
        List<String> mediaStringIds = mediaComponentModule.getMediaStringIds();
        List<String> mediaUrls = mediaComponentModule.getMediaUrls();
        Class mediaClass = mediaComponentModule.getMediaPartClass();
        if (mediaIds != null) {
            Realm.getDefaultInstance()
                    .where(mediaClass)
                    .in(RecordWithIdentifier.IdentifierFieldName, mediaIds.toArray(new Long[mediaIds.size()]))
                    .findAllAsync()
                    .addChangeListener(this);
            mMediasIds = mediaIds;
        } else if (mediaStringIds != null) {
            Realm.getDefaultInstance()
                    .where(mediaClass)
                    .in(RecordWithStringIdentifier.StringIdentifierFieldName, mediaStringIds.toArray(new String[mediaStringIds.size()]))
                    .findAllAsync()
                    .addChangeListener(this);
            mMediasIds = mediaStringIds;
        } else {
            Realm.getDefaultInstance()
                    .where(mediaClass)
                    .in(mediaComponentModule.getUrlColumnName(), mediaUrls.toArray(new String[mediaUrls.size()]))
                    .findAllAsync()
                    .addChangeListener(this);
            mMediasIds = mediaUrls;
        }
    }

    @Override
    protected FragmentPagerAdapter createPagerAdapter() {
        return new CachedFragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment createFragment(int position) {
                MediaPart mediaPart = (MediaPart) getMediasList().get(position);

                if (mediaPart.getMediaType() == MediaType.IMAGE) {
                    return ZoomableMediaFragment.Companion.newInstance(MediaPartFullscreenActivity.this, mediaPart);
                } else {
                    String mimeType;
                    String mediaUrl = mediaPart.getMediaUrl();

                    if ((mimeType = mediaPart.getMimeType()) != null && mimeType.equals(MediaPart.MIME_TYPE_TEXT_HTML) && mediaUrl != null) {
                        StreamingProvider provider = ((KrakeApplication) getApplication()).getStreamingProvider(mediaUrl);
                        if (provider != null) {
                            mediaUrl = provider.retrieveVideoUrl(MediaPartFullscreenActivity.this, mediaUrl);
                        }
                    }
                    return VideoFragment.newInstance(mediaUrl, position);
                }
            }

            @Override
            public int getCount() {
                return getMediasList().size();
            }
        };
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        if (getMediasList() != null) {
            MediaPart mediaPart = (MediaPart) getMediasList().get(position);
            mMediaPartTextView.setText(mediaPart.getTitle());

        }
    }

    @Override
    public void onChange(RealmResults element) {
        element.removeChangeListener(this);

        LinkedList mMediasList = new LinkedList<>(element);

        if (mMediasList.isEmpty() || !(mMediasList.get(0) instanceof MediaPartUrlSerializable)) {
            Collections.sort(mMediasList, new Comparator<MediaPart>() {
                @Override
                public int compare(MediaPart mediaPart,
                                   MediaPart mediaPart2) {
                    Integer idIndex = mMediasIds.indexOf(RecordKt.getIdentifierOrStringIdentifier(mediaPart));
                    Integer id2Index = mMediasIds.indexOf(RecordKt.getIdentifierOrStringIdentifier(mediaPart2));
                    return idIndex.compareTo(id2Index);
                }
            });

            LinkedList filteredList = new LinkedList();

            int index = 0;

            for (index = 0; index < mMediasList.size(); ++index) {
                MediaPart part = (MediaPart) mMediasList.get(index);
                int internalIndex = 0;
                boolean found = false;
                for (internalIndex = 0; internalIndex < filteredList.size(); ++internalIndex) {
                    MediaPart internalMedia = (MediaPart) filteredList.get(internalIndex);
                    if (internalMedia.getMediaUrl().equals(part.getMediaUrl())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    filteredList.add(part);
                }
            }

        } else {
            Collections.sort(mMediasList, new Comparator<MediaPart>() {
                @Override
                public int compare(MediaPart mediaPart,
                                   MediaPart mediaPart2) {
                    Integer idIndex = mMediasIds.indexOf(mediaPart.getMediaUrl());
                    Integer id2Index = mMediasIds.indexOf(mediaPart2.getMediaUrl());
                    return idIndex.compareTo(id2Index);
                }
            });
        }

        showLoadedData(mMediasList, mMediasList.get(mediaComponentModule.getMediaIndex()));
    }
}