package com.krake.core.media;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.krake.core.media.support.VideoFragment;
import com.krake.core.util.UriGsonSerializer;
import com.krake.core.widget.CachedFragmentPagerAdapter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by joel on 04/12/15.
 */
public class CameraPickedFullScreenActivity extends MediasFullscreenActivity {

    private static final String EXTRA_INTENT_INDEX = "selectedIndex";
    private static final String EXTRA_CAMERA_INFOS = "cameraInfos";


    public static Intent newStartIntent(Context context, List<UploadableMediaInfo> mediaParts, int position) {

        Intent intent = new Intent(context, CameraPickedFullScreenActivity.class);

        intent.putExtra(EXTRA_INTENT_INDEX, position);

        Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriGsonSerializer()).create();


        intent.putExtra(EXTRA_CAMERA_INFOS, gson.toJson(mediaParts));

        return intent;
    }

    @Override
    protected void startDataLoading(Intent intent) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriGsonSerializer()).create();

        List infos = gson.fromJson(intent.getStringExtra(EXTRA_CAMERA_INFOS), new TypeToken<LinkedList<UploadableMediaInfo>>() {
        }.getType());

        showLoadedData(infos, infos.get(intent.getIntExtra(EXTRA_INTENT_INDEX, 0)));
    }

    @Override
    protected FragmentPagerAdapter createPagerAdapter() {
        return new CachedFragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment createFragment(int position) {
                UploadableMediaInfo mediaPart = (UploadableMediaInfo) getMediasList().get(position);
                if (mediaPart.getType() == MediaType.IMAGE)
                    return ZoomableMediaFragment.Companion.newInstance(mediaPart.getUri());
                else
                    return VideoFragment.newInstance(mediaPart.getUri().toString(), position);
            }

            @Override
            public int getCount() {
                return getMediasList().size();
            }
        };
    }
}
