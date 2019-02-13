package com.krake.core.media;

import android.widget.ImageView;
import androidx.annotation.DrawableRes;

/**
 * Utilizzata per definire una classe che può caricare un'immagine tramite il {@link com.krake.core.media.loader.MediaLoader}
 * <br/>
 * Per gli attributi custom guardare {@link WidgetLoadableHelper}
 */
public interface MediaLoadable {
    /**
     * Getter per le configurazione del ws
     *
     * @return options da passare al WS
     */
    ImageOptions getOptions();

    /**
     * Getter per il placeholder delle foto
     *
     * @return risorsa del placeholder delle foto
     */
    @DrawableRes
    int getPhotoPlaceholder();

    /**
     * Getter per il placeholder dei video
     *
     * @return risorsa del placeholder dei video
     */
    @DrawableRes
    int getVideoPlaceholder();

    /**
     * Getter per il placeholder degli audio
     *
     * @return risorsa del placeholder degli audio
     */
    @DrawableRes
    int getAudioPlaceholder();

    /**
     * Getter per il tipo di ScaleType del placeholder
     *
     * @return enum che rappresenta lo ScaleType del placeholder
     */
    ImageView.ScaleType getPlaceholderScaleType();

    /**
     * Getter per il tipo di ScaleType del media da caricare
     *
     * @return enum che rappresenta lo ScaleType del media da caricare
     */
    ImageView.ScaleType getMediaScaleType();

    /**
     * Utilizzato da Glide per capire se utilizzare l'animazione o meno per il caricamento dell'immagine
     *
     * @return true se l'animazione è abilitata
     */
    boolean isAnimated();

    /**
     * Utilizzato dal {@link com.krake.core.media.loader.MediaLoader} e dagli adapter per capire se mostrare il placeholder o meno
     *
     * @return true se nel widget si vuole mostrare un placeholder
     */
    boolean showPlaceholder();
}