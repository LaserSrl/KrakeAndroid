package com.krake.core.media.watermark;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.krake.core.media.MediaPickerHelper;
import com.krake.core.media.MediaType;
import com.krake.core.media.task.ImageWatermarkTask;

/**
 * Estensione di {@link MediaPickerHelper} che permette di applicare un Watermark alla foto.
 * Il Watermark viene applicato direttamente in fase di caricamento della foto.
 */
public class WatermarkCameraHelper extends MediaPickerHelper implements ImageWatermarkTask.Listener {

    /**
     * Listener per le callbacks
     */
    private Listener mListener;
    private ImageWatermarkTask imageWatermarkTask;

    /**
     * Istanzia un {@link WatermarkCameraHelper} che viene inizializzato con il Context del Fragment, il parametro singleMediaMode e il listener per le callback
     *
     * @param fragment        Fragment corrente
     * @param singleMediaMode booleano che definisce se sarà presente un unico media o meno
     * @param listener        listener al quale viene notificata la fine del processo
     */
    public WatermarkCameraHelper(@NonNull Fragment fragment, boolean singleMediaMode, @NonNull Listener listener) {
        super(fragment, singleMediaMode);
        mListener = listener;
        imageWatermarkTask = new ImageWatermarkTask(fragment.getActivity(), this);
    }

    @Override
    protected void loadDataFromNewPhoto() {
        super.loadDataFromNewPhoto();
        if (mListener.getWatermark() != null && mListener.getWatermark().isAvailable())
            applyWatermark();
    }

    @Override
    protected void loadDataFromStorage(Uri mediaUri, int mediaType) {
        super.loadDataFromStorage(mediaUri, mediaType);
        if (mediaType == MediaType.IMAGE && mListener.getWatermark() != null && mListener.getWatermark().isAvailable())
            applyWatermark();
    }

    @HandleResult
    public int handleOnActivityResult(@RequestCode int requestCode, int resultCode, Intent data) {
        @HandleResult int result = super.handleOnActivityResult(requestCode, resultCode, data);
        if (result == HANDLED && mListener.getWatermark() != null && mListener.getWatermark().isAvailable() && (requestCode == New_Photo || requestCode == Local_Photo)) {
            result = WAITING;
        }

        return result;
    }

    private void applyWatermark() {
        if (getLastMediaInfo() != null && mFragment != null) {
            // il loader viene fatto partire per ogni nuovo media
            imageWatermarkTask.load(mListener.getWatermark(), getLastMediaInfo());
        }
    }

    @Override
    public void onWatermarkApplied() {
        mListener.onPhotoAppliedWaterMark();
    }

    @Override
    public void destroy() {
        super.destroy();
        imageWatermarkTask.release();
    }


    /**
     * Listener per le callback da assegnare al Fragment che utilizza il {@link WatermarkCameraHelper}
     */
    public interface Listener {
        /**
         * @return {@link Watermark} dalla class che implementa il listener
         */
        Watermark getWatermark();

        /**
         * Notifica il listener quando il processo di generazione dell'immagine con Watermark è completato
         */
        void onPhotoAppliedWaterMark();
    }
}