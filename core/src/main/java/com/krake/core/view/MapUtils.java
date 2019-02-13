package com.krake.core.view;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.krake.core.R;

/**
 * Classe per dare un aspetto coerente alle Mappe utilizzate dalle App.
 * La classe deve essere chiamata ogni volta che si alloca una mappa.
 * Nel caso in cui siano presenti TileOverlay sarà necessario assegnare lo stile corretto
 * alla mappa anche ogni volta che ne viene fatto il clean, perché andrebbe ad eliminare anche gli overlay
 * che sostituiscono i tiles.
 * In caso sia necessario mostrare i copyright per l'uso di una determinata cartografia è possibile
 * modificare la stringa {@llink R.string#osm_copyrights}
 */
public class MapUtils {

    public static void styleMap(@NonNull GoogleMap map, @NonNull Context context) {

        map.getUiSettings().setMapToolbarEnabled(false);

        if (map.getMapType() == GoogleMap.MAP_TYPE_NONE) {
            map.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(new OSMTileProvider(context))
                    .zIndex(-1));
        }
    }

    public static String loadOSMCopyrights(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("<body style=\"background-color:");
        //noinspection ResourceType
        sb.append(context.getString(R.color.details_background_color).replaceFirst("#ff", "#"));
        sb.append("\">");
        sb.append(context.getString(R.string.osm_copyrights));
        sb.append("</body>");

        return sb.toString();
    }
}
