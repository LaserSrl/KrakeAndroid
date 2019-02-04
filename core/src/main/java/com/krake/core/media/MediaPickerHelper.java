package com.krake.core.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.krake.core.R;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Helper utilizzato per l'accesso alle azioni principali sui media:
 * <ul>
 * <li>scattare una foto (tramite app esterna)</li>
 * <li>scegliere una foto dalla galleria</li>
 * <li>registrare un video (tramite app esterna)</li>
 * <li>scegliere un video dalla galleria</li>
 * <li>registrare un audio (tramite app esterna o, se non disponibile, con registratore interno)</li>
 * <li>scegliere un audio dallo storage</li>
 * </ul>
 * <br/>
 * Nel fragment che lo integra è necessario collegare la chiamata: {@link #handleOnActivityResult(int, int, Intent)}
 */
public class MediaPickerHelper {
    public static final int HANDLED = 0;
    public static final int USELESS = 1;
    public static final int WAITING = 2;
    public static final int New_Photo = 12001;
    public static final int Local_Photo = 12002;
    public static final int New_Video = 12003;
    public static final int Local_Video = 12004;
    public static final int New_Audio = 12005;
    public static final int Local_Audio = 12006;
    private static final String TAG = MediaPickerHelper.class.getSimpleName();
    protected Fragment mFragment;
    private Context mContext;
    private boolean singleMediaMode;
    private UploadableMediaInfo lastMediaInfo;
    private UploadableMediaInfo oldMediaInfo;

    /**
     * Istanzia un MediaPickerHelper che viene inizializzato con il Context del Fragment e il parametro singleMediaMode
     *
     * @param fragment        Fragment corrente
     * @param singleMediaMode booleano che definisce se sarà presente un unico media o meno
     */
    public MediaPickerHelper(@NonNull Fragment fragment, boolean singleMediaMode) {
        this(fragment.getActivity(), singleMediaMode);
        mFragment = fragment;
    }

    /**
     * Istanzia un MediaPickerHelper che lo inizializza con un Context generico e il parametro singleMediaMode
     *
     * @param context         Context generico
     * @param singleMediaMode booleano che definisce se sarà presente un unico media o meno
     */
    private MediaPickerHelper(@NonNull Context context, boolean singleMediaMode) {
        mContext = context;
        this.singleMediaMode = singleMediaMode;
    }

    /**
     * Verifica se la fotocamera è disponibile nell'App.
     * La verifica viene effettuata verificando la presenza di PackageManager.FEATURE_CAMERA
     *
     * @param context context corrente
     * @return true se disponibile una fotocamera accessibile tramite intent, false negli altri casi
     */
    public static boolean isCameraAvailable(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && isIntentAvailable(context, new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
    }

    /**
     * Definisce se è disponibile o meno il picker per un certo tipo di media
     *
     * @param context   context corrente
     * @param mediaType tipo del media
     * @return true se è possibile ottenere un media del tipo specificato come parametro tramite l'Intent lanciato con ACTION_GET_CONTENT
     */
    @SuppressLint("SwitchIntDef")
    public static boolean isPickerAvailable(@NonNull Context context, @MediaType int mediaType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // setta il filtro sul MimeType
        intent.setType(MediaProvider.mimeTypeForMedia(mediaType));
        return isIntentAvailable(context, intent);
    }

    /**
     * Definisce se è possibile lanciare l'Intent passato come parametro
     *
     * @param context context corrente
     * @param intent  Intent da verificare
     * @return true se è presente almeno un'app in grado di gestire l'Intent passato come parametro
     */
    private static boolean isIntentAvailable(@NonNull Context context, @NonNull Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public boolean isSingleMediaMode() {
        return singleMediaMode;
    }

    @Nullable
    public UploadableMediaInfo getLastMediaInfo() {
        return lastMediaInfo;
    }

    public void setLastMediaInfo(@Nullable UploadableMediaInfo info) {
        lastMediaInfo = info;
    }

    @Nullable
    public UploadableMediaInfo getOldMediaInfo() {
        return oldMediaInfo;
    }

    /**
     * Crea un File inizialmente vuoto in cui verrà scritta la nuova foto.
     * <br/>
     * Lancia l'Intent per aprire un'app in grado di scattare una foto.
     */
    public void takeNewPhoto() {
        File mediaFile = MediaProvider.createMediaFile(mContext, MediaType.IMAGE);
        String mediaPath = mediaFile.getAbsolutePath();
        Uri mediaUri = MediaProvider.getUriForFile(mContext, mediaFile);
        Log.d(TAG, "takeNewPhoto with Uri: " + mediaUri.toString());
        oldMediaInfo = lastMediaInfo;
        lastMediaInfo = new UploadableMediaInfo(null, mediaUri, mediaPath, MediaType.IMAGE);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        mFragment.startActivityForResult(intent, New_Photo);
    }

    /**
     * Crea un File inizialmente vuoto in cui verrà scritto il nuovo video.
     * <br/>
     * Lancia l'Intent per aprire un'app in grado di registrare un video.
     * <br/>
     * La durata del video viene definita a priori tramite l'Integer <i>video_max_duration</i>
     */
    public void takeNewVideo() {
        File mediaFile = MediaProvider.createMediaFile(mContext, MediaType.VIDEO);
        String mediaPath = mediaFile.getAbsolutePath();
        Uri mediaUri = MediaProvider.getUriForFile(mContext, mediaFile);
        Log.d(TAG, "takeNewVideo with Uri: " + mediaUri.toString());
        oldMediaInfo = lastMediaInfo;
        lastMediaInfo = new UploadableMediaInfo(null, mediaUri, mediaPath, MediaType.VIDEO);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, mContext.getResources().getInteger(R.integer.video_max_duration));
        mFragment.startActivityForResult(intent, New_Video);
    }

    public void destroy() {
        // Empty by default.
    }

    /**
     * Considerando che non tutti i telefono hanno installata un'app di default per la registrazione di file audio,
     * viene fornita un'Activity interna unicamente utilizzata per questa funzionalità (nel caso in cui ci sia almeno un'app presente, quest'Activity viene ignorata).
     * <br/>
     * Nel caso in cui si apra un'app esterna per la registrazione, viene creato un File inizialmente vuoto in cui verrà scritto il nuovo audio.
     * <br/>
     * Lancia l'Intent per aprire un'app in grado di registrare un audio, oppure per aprire l'Activity interna di tipo {@link AudioRecorderActivity}.
     */
    public void takeNewAudio() {
        // crea l'Intent per un'app esterna in grado di registrare File di tipo audio
        Intent externalIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        boolean externalRecordApp = isIntentAvailable(mContext, externalIntent);

        // definisce l'
        Intent intent = externalRecordApp ? externalIntent : new Intent(mContext, AudioRecorderActivity.class);

        Uri mediaUri = null;
        String mediaPath = null;

        if (!externalRecordApp) {
            File mediaFile = MediaProvider.createMediaFile(mContext, MediaType.AUDIO);
            mediaPath = mediaFile.getAbsolutePath();
            mediaUri = MediaProvider.getUriForFile(mContext, mediaFile);
            Log.d(TAG, "takeNewAudio with Uri: " + mediaUri.toString());

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        }

        oldMediaInfo = lastMediaInfo;
        lastMediaInfo = new UploadableMediaInfo(null, mediaUri, mediaPath, MediaType.AUDIO);

        mFragment.startActivityForResult(intent, New_Audio);
    }

    /**
     * Elimina il File dal dispositivo e reimposta la variabili {@link #oldMediaInfo} e {@link #lastMediaInfo} a null se assegnate al {@link UploadableMediaInfo} di questo File
     *
     * @param mediaInfo UploadableMediaInfo che contiene le informazioni del File da eliminare
     * @return true se il File è stato eliminato
     */
    public boolean deleteFile(@Nullable UploadableMediaInfo mediaInfo) {
        boolean deleted = false;
        if (mediaInfo != null) {
            deleted = mediaInfo.delete(mContext);
            if (mediaInfo == oldMediaInfo) {
                oldMediaInfo = null;
            } else if (mediaInfo == lastMediaInfo) {
                lastMediaInfo = null;
            }
        }
        return deleted;
    }

    /**
     * Fa partire l'Intent per ottenere un media dallo storage filtrando i risultati in base al MimeType.
     * <br/>
     * All'Intent vengono assegnati i permessi di lettura e scrittura sull'Uri per evitare che app come Google Photo, Google Camera, ecc.. blocchino questo comportamento.
     *
     * @param mediaType   tipo del media
     * @param requestCode request code della richiesta e della callback
     */
    public void pickMediaFromGallery(@MediaType int mediaType, @RequestCode int requestCode) {
        oldMediaInfo = lastMediaInfo;
        // crea un media vuoto, i cui campi verranno valorizzati dopo averlo ricevuto nella callback
        lastMediaInfo = new UploadableMediaInfo(mediaType);

        Uri externalUri;
        switch (mediaType) {
            case MediaType.IMAGE:
                externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case MediaType.VIDEO:
                externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case MediaType.AUDIO:
                externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            default:
                externalUri = null;
                break;
        }
        if (externalUri != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, externalUri);
            // setta il filtro sul MimeType
            intent.setType(MediaProvider.mimeTypeForMedia(mediaType));
            // assegna i permessi di lettura e scrittura sull'uri
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            mFragment.startActivityForResult(intent, requestCode);
        }
    }

    @HandleResult
    public int handleOnActivityResult(@RequestCode int requestCode, int resultCode, Intent data) {
        Uri mediaUri = data != null && data.getData() != null ? data.getData() : null;
        if (resultCode == Activity.RESULT_OK) {
            if (singleMediaMode) {
                deleteFile(oldMediaInfo);
            }
            switch (requestCode) {
                case New_Photo:
                    loadDataFromNewPhoto();
                    return HANDLED;

                case New_Video:
                    return HANDLED;

                case New_Audio:
                    if (mediaUri != null) {
                        Log.d(TAG, "handleOnActivityResult, audio with Uri: " + mediaUri.toString());
                        lastMediaInfo.setUri(mediaUri);
                    }
                    return HANDLED;

                case Local_Photo:
                    loadDataFromStorage(mediaUri, MediaType.IMAGE);
                    return HANDLED;

                case Local_Video:
                    loadDataFromStorage(mediaUri, MediaType.VIDEO);
                    return HANDLED;

                case Local_Audio:
                    loadDataFromStorage(mediaUri, MediaType.AUDIO);
                    return HANDLED;
            }
        } else {
            lastMediaInfo = oldMediaInfo;
        }

        return USELESS;
    }

    /**
     * Definisce un comportamento custom che viene eseguito dopo aver scattato una foto
     */
    protected void loadDataFromNewPhoto() {
        // Empty by default.
    }

    /**
     * Carica un media dallo storage del telefono.
     * <br/>
     * In questo processo viene creato un File temporaneo che copia il file ottenuto nella callback del Intent con action ACTION_GET_CONTENT.
     *
     * @param mediaUri  uri del media
     * @param mediaType tipo del media
     */
    protected void loadDataFromStorage(@Nullable Uri mediaUri, @MediaType int mediaType) {
        if (mediaUri != null) {
            // crea il file temporaneo
            File tempFile = MediaProvider.createTempFile(mContext, mediaUri);
            // setta le nuove informazione che ora non saranno criptate
            lastMediaInfo.setPath(tempFile.getAbsolutePath());
            lastMediaInfo.setUri(MediaProvider.getUriForFile(mContext, tempFile));
            Log.d(TAG, "loadDataFromStorage: MEDIA URI: " + mediaUri.toString());
            Log.d(TAG, "loadDataFromStorage: TEMP URI: " + lastMediaInfo.getUri().toString());
            oldMediaInfo = lastMediaInfo;
        }
    }

    @IntDef({HANDLED, USELESS, WAITING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HandleResult {
    }

    @IntDef({New_Photo, Local_Photo, New_Video, Local_Video, New_Audio, Local_Audio})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface RequestCode {
    }
}