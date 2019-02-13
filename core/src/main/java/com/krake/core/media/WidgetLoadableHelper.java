package com.krake.core.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.krake.core.R;

import static android.widget.ImageView.ScaleType;
import static com.krake.core.media.ImageOptions.Alignment;
import static com.krake.core.media.ImageOptions.Mode;

/**
 * Classe helper utilizzata dai widget che implementano l'interfaccia {@link MediaLoadable} per centralizzarne il comportamento
 */
public class WidgetLoadableHelper {
    /**
     * Utilizzato per le proprietà dell'immagine da passare in querystring al WS
     * <br>
     * Abbinato agli attr:
     * <ul>
     * <li>optionsWidth: larghezza in pixel, default: 0</li>
     * <li>optionsHeight: altezza in pixel, default: 0</li>
     * <li>optionsMode: {@link Mode}, default: {@link Mode#MAX}</li>
     * <li>optionsAlignment: {@link Alignment}, default: {@link Alignment#MIDDLECENTER}</li>
     * </ul>
     */
    private ImageOptions mOptions;

    /**
     * Specifica il placeholder delle immagini
     * <br>
     * Abbinato all'attr: placeholderPhotoSrc
     * <br>
     * Default: R.drawable.photo_placeholder
     */
    @DrawableRes
    private int mPhotoPlaceholder;

    /**
     * Specifica il placeholder dei video
     * <br>
     * Abbinato all'attr: placeholderVideoSrc
     * <br>
     * Default: R.drawable.video_placeholder
     */
    @DrawableRes
    private int mVideoPlaceholder;

    /**
     * Specifica il placeholder degli audio
     * <br>
     * Abbinato all'attr: placeholderAudioSrc
     * <br>
     * Default: R.drawable.audio_placeholder
     */
    @DrawableRes
    private int mAudioPlaceholder;

    /**
     * ScaleType che viene applicato esclusivamente al placeholder
     * <br>
     * Abbinato all'attr: placeholderScaleType
     * <br>
     * Default: ScaleType.FIT_CENTER
     */
    private ScaleType mPlaceholderScaleType;

    /**
     * ScaleType che viene applicato al media da caricare
     * <br>
     * Abbinato all'attr: mediaScaleType
     * <br>
     * Default: ScaleType.CENTER_CROP
     */
    private ScaleType mMediaScaleType;

    /**
     * Viene utilizzato da Glide per gestire l'animazione
     * <br>
     * Abbinato all'attr: animated
     * <br>
     * Default: true
     */
    private boolean animated;

    /**
     * Utilizzato per gestire la visibilità del placeholder
     * <br>
     * Abbinato all'attr: showPlaceholder
     * <br>
     * Default: true
     */
    private boolean showPlaceholder;

    /**
     * Crea un nuovo helper legato ad un widget
     *
     * @param context context corrente
     * @param attrs   attrs da xml se disponibili dal costruttore
     */
    public WidgetLoadableHelper(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            // legge attributi da xml
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Loadable, 0, 0);

            try {
                int requestWidth = a.getDimensionPixelSize(R.styleable.Loadable_optionsWidth, 0);
                int requestHeight = a.getDimensionPixelSize(R.styleable.Loadable_optionsHeight, 0);
                mOptions = new ImageOptions(requestWidth, requestHeight);

                int modeVal = a.getInt(R.styleable.Loadable_optionsMode, 0);
                @Mode String mode;
                switch (modeVal) {
                    case 1:
                        mode = Mode.CROP;
                        break;
                    case 2:
                        mode = Mode.STRETCH;
                        break;
                    case 3:
                        mode = Mode.PAN;
                        break;
                    default:
                        mode = Mode.MAX;
                        break;
                }
                mOptions.setMode(mode);

                int alignmentVal = a.getInt(R.styleable.Loadable_optionsAlignment, 0);
                @Alignment String alignment;
                switch (alignmentVal) {
                    case 1:
                        alignment = Alignment.TOPLEFT;
                        break;
                    case 2:
                        alignment = Alignment.TOPCENTER;
                        break;
                    case 3:
                        alignment = Alignment.TOPRIGHT;
                        break;
                    case 4:
                        alignment = Alignment.MIDDLELEFT;
                        break;
                    case 5:
                        alignment = Alignment.MIDDLERIGHT;
                        break;
                    case 6:
                        alignment = Alignment.BOTTOMLEFT;
                        break;
                    case 7:
                        alignment = Alignment.BOTTOMCENTER;
                        break;
                    case 8:
                        alignment = Alignment.BOTTOMRIGHT;
                        break;
                    default:
                        alignment = Alignment.MIDDLECENTER;
                        break;

                }
                mOptions.setAlignment(alignment);

                mPhotoPlaceholder = a.getResourceId(R.styleable.Loadable_placeholderPhotoSrc, R.drawable.photo_placeholder);
                mVideoPlaceholder = a.getResourceId(R.styleable.Loadable_placeholderVideoSrc, R.drawable.video_placeholder);
                mAudioPlaceholder = a.getResourceId(R.styleable.Loadable_placeholderAudioSrc, R.drawable.audio_placeholder);

                showPlaceholder = a.getBoolean(R.styleable.Loadable_showPlaceholder, true);
                if (showPlaceholder) {
                    int placeholderScaleTypeVal = a.getInt(R.styleable.Loadable_placeholderScaleType, 6);
                    mPlaceholderScaleType = intToScaleType(placeholderScaleTypeVal, ScaleType.CENTER_CROP);
                }

                int mediaScaleTypeVal = a.getInt(R.styleable.Loadable_mediaScaleType, 6);
                mMediaScaleType = intToScaleType(mediaScaleTypeVal, ScaleType.CENTER_CROP);

                animated = a.getBoolean(R.styleable.Loadable_animated, true);

            } finally {
                a.recycle();
            }
        }

        // setta i valori di default nel caso in cui non siano stati settati
        if (mOptions == null) {
            mOptions = ImageOptions.getDefault();
            mPhotoPlaceholder = R.drawable.photo_placeholder;
            mVideoPlaceholder = R.drawable.video_placeholder;
            mAudioPlaceholder = R.drawable.audio_placeholder;
            animated = true;
        }
    }

    /**
     * Trasforma un valore ottenuto da xml come intero in {@link ScaleType}.
     * <br/>
     * Nel caso in cui lo {@link ScaleType} non venga trovato, ne verrà usato uno di default
     *
     * @param resolvedXmlVal   valore ottenuto dall'attributo in xml
     * @param defaultScaleType {@link ScaleType} di default nel caso in cui non sia disponibile nessuno {@link ScaleType}
     * @return {@link ScaleType} da utilizzare
     */
    private ScaleType intToScaleType(int resolvedXmlVal, @NonNull ScaleType defaultScaleType) {
        ScaleType scaleType;
        switch (resolvedXmlVal) {
            case 1:
                scaleType = ScaleType.FIT_XY;
                break;
            case 2:
                scaleType = ScaleType.FIT_START;
                break;
            case 3:
                scaleType = ScaleType.FIT_CENTER;
                break;
            case 4:
                scaleType = ScaleType.FIT_END;
                break;
            case 5:
                scaleType = ScaleType.CENTER;
                break;
            case 6:
                scaleType = ScaleType.CENTER_CROP;
                break;
            case 7:
                scaleType = ScaleType.CENTER_INSIDE;
                break;
            default:
                scaleType = defaultScaleType;
                break;
        }
        return scaleType;
    }

    /**
     * Getter per le configurazione del ws
     *
     * @return options da passare al WS
     */
    public ImageOptions getOptions() {
        return mOptions;
    }

    /**
     * Getter per il placeholder delle foto
     *
     * @return risorsa del placeholder delle foto
     */
    @DrawableRes
    public int getPhotoPlaceholder() {
        return mPhotoPlaceholder;
    }

    /**
     * Getter per il placeholder dei video
     *
     * @return risorsa del placeholder dei video
     */
    @DrawableRes
    public int getVideoPlaceholder() {
        return mVideoPlaceholder;
    }

    /**
     * Getter per il placeholder degli audio
     *
     * @return risorsa del placeholder degli audio
     */
    @DrawableRes
    public int getAudioPlaceholder() {
        return mAudioPlaceholder;
    }

    /**
     * Getter per il tipo di ScaleType del placeholder
     *
     * @return enum che rappresenta lo ScaleType del placeholder
     */
    public ScaleType getPlaceholderScaleType() {
        return mPlaceholderScaleType;
    }

    /**
     * Getter per il tipo di ScaleType del media da caricare
     *
     * @return enum che rappresenta lo ScaleType del media da caricare
     */
    public ScaleType getMediaScaleType() {
        return mMediaScaleType;
    }

    /**
     * Utilizzato da Glide per capire se utilizzare l'animazione o meno per il caricamento dell'immagine
     *
     * @return true se l'animazione è abilitata
     */
    public boolean isAnimated() {
        return animated;
    }

    /**
     * Utilizzato dal {@link com.krake.core.media.loader.MediaLoader} e dagli adapter per capire se mostrare il placeholder o meno
     *
     * @return true se nel widget si vuole mostrare un placeholder
     */
    public boolean showPlaceholder() {
        return showPlaceholder;
    }
}