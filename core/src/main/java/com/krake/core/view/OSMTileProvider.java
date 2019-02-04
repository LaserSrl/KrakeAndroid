package com.krake.core.view;

import android.content.Context;

import com.google.android.gms.maps.model.UrlTileProvider;
import com.krake.core.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by joel on 08/01/15.
 */
public class OSMTileProvider extends UrlTileProvider {

    String mServerTileFormat;

    public OSMTileProvider(Context context) {
        super(256, 256);

        mServerTileFormat = context.getString(R.string.tile_server_url);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        try {
            //http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/${z}/${y}/${x}.jpg
            return new URL(String.format(mServerTileFormat, zoom, x, y));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
