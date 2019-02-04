package com.krake.core.media;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Opzioni per caricare le immagini da Orcahrd.
 * Tutte le operazioni di ridimensionamento, crop ecc sono effettuate da server.
 */
public class ImageOptions {
    private int width;
    private int height;
    @Mode
    private String mode;
    @Alignment
    private String alignment;
    private static ImageOptions DEFAULT;

    /**
     * Versione di default delle opzioni per il caricamento delle immagini.
     * Tramite il singleton pattern verrà mantenuta solo un'istanza di ImageOptions per app per evitare di occupare altro spazio in memoria
     * Attualmente i parametri di default sono:
     * 0,0 per altezza e larghezza
     * {@link com.krake.core.media.ImageOptions.Mode#MAX}
     * {@link com.krake.core.media.ImageOptions.Alignment#TOPCENTER}
     *
     * @return le image option di default
     */
    public synchronized static ImageOptions getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ImageOptions(0, 0);
        }
        return DEFAULT;
    }

    /**
     * Creazione di opzioni per l'immagine solo altezza e larghezza.
     * Sono impostati gli altri parametri ai valori di
     * {@link com.krake.core.media.ImageOptions.Mode#MAX}
     * {@link com.krake.core.media.ImageOptions.Alignment#TOPCENTER}
     *
     * @param width  larghezza dell'immagine richiesta
     * @param height altezza dell'immagine richiesta
     */
    public ImageOptions(int width, int height) {
        this(width, height, Mode.MAX, Alignment.MIDDLECENTER);
    }

    /**
     * * Creazione di opzioni per l'immagine solo altezza e larghezza.
     * Sono impostati gli altri parametri ai valori di
     * {@link com.krake.core.media.ImageOptions.Alignment#TOPCENTER}
     *
     * @param width  larghezza dell'immagine richiesta
     * @param height altezza dell'immagine richiesta
     * @param mode   indicazione per il modo per tagliare o ridimensionare l'immagine
     */
    public ImageOptions(int width, int height, @Mode String mode) {
        this(width, height, mode, Alignment.MIDDLECENTER);
    }

    /**
     * Creazione di opzioni per l'immagine indicando tutte i parametri
     *
     * @param width     larghezza dell'immagine richiesta
     * @param height    altezza dell'immagine richiesta
     * @param mode      indicazione per il modo per tagliare o ridimensionare l'immagine
     * @param alignment allineamento della parte dell'immagine da tenere
     */
    public ImageOptions(int width, int height, @Mode String mode, @Alignment String alignment) {
        this.width = width;
        this.height = height;
        this.mode = mode;
        this.alignment = alignment;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Mode
    public String getMode() {
        return mode;
    }

    public void setMode(@Mode String mode) {
        this.mode = mode;
    }

    @Alignment
    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(@Alignment String alignment) {
        this.alignment = alignment;
    }

    @StringDef({Mode.MAX, Mode.CROP, Mode.STRETCH, Mode.PAN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        String MAX = "max";
        String CROP = "crop";
        String STRETCH = "stretch";
        String PAN = "pan";
    }

    @StringDef({Alignment.TOPLEFT, Alignment.TOPCENTER, Alignment.TOPRIGHT, Alignment.MIDDLELEFT, Alignment.MIDDLECENTER, Alignment.MIDDLERIGHT, Alignment.BOTTOMLEFT, Alignment.BOTTOMCENTER, Alignment.BOTTOMRIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Alignment {
        String TOPLEFT = "topleft";
        String TOPCENTER = "topcenter";
        String TOPRIGHT = "topright";

        String MIDDLELEFT = "middleleft";
        String MIDDLECENTER = "middlecenter";
        String MIDDLERIGHT = "middleright";

        String BOTTOMLEFT = "bottomleft";
        String BOTTOMCENTER = "bottomcenter";
        String BOTTOMRIGHT = "bottomright";
    }
}