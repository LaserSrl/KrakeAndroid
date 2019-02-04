package com.krake.core;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.krake.core.app.KrakeApplication;
import com.krake.core.media.KeyMediaListMap;
import com.krake.core.media.UploadableMediaInfo;
import com.krake.core.network.RemoteClient;
import com.krake.core.service.MessengerAndWorkerMultithreadService;

import java.util.List;

/**
 * Service per la gestione del sercizi di Orchard.
 * Il servizio utilizza diversi thread per evitare di bloccare il caricamento dati nell'attesa di
 * un'operazione più lenta come il caricamento di un video.
 * <p/>
 */
public class OrchardUploadService extends MessengerAndWorkerMultithreadService {
    public static final String INTENT_ACTION_UPLOAD_FILE = "UploadFile";
    public static final String INTENT_ACTION_CANCEL_UPLOAD = "CancelUpload";

    public static final String EXTRA_LOGIN_REQUIRED = "LoginRequired";
    private static final String EXTRA_UPLOAD_PARAMETERS = "uploadParams";
    private static final String EXTRA_API_PATH = "apiPath";
    private static final String EXTRA_JSON_KEY_MEDIA_MAP = "jsonLocalMediaMap";
    private static final String EXTRA_API_END_CALL_BUNDLE = "endCallBundle";
    private static final String EXTRA_UPLOAD_CODE = "UploadCode";
    private static final int UPLOAD_CONTENT_NOTIFICATION_ID = 4324;
    /**
     * Bool: indica se il caricamento dei dati deve essere effettuato anche se la stessa chiamata è
     * stata effettuata poco tempo prima.
     * Da utilizzare solo per il refresh layout.
     */
    private static final String TAG = OrchardUploadService.class.getSimpleName();

    private final Gson gson = new Gson();
    private UploadStatus mSignalApiStatus = UploadStatus.WAITING;
    private Integer mUploadFileCode = null;

    private KrakeApplication mApp;

    public OrchardUploadService() {
        super(1);
    }

    /**
     * Permette l'upload di un file sul WS.
     * Questo caricamento permette di gestire anche upload di grosse dimensioni.
     * <br/>
     * L'upload viene gestito da un {@link UploadInterceptor} che viene scelto per ogni file in base al filtro impostato con il metodo
     * {@link KrakeApplication#getUploadInterceptor(UploadableMediaInfo, String, Bundle)} e scelto se presente nella lista degli interceptors nell'application.
     * Per aggiungerne uno, usare il metodo {@link KrakeApplication#addUploadInterceptor(UploadInterceptor)}.
     * Alla fine della chiamata del segnale saranno chiamati {@link UploadCompleteEndListener} prelevati da
     * {@link KrakeApplication#registerUploadCompleteListener(int, Class)}.
     * Inoltre sarà inviato il messaggio {@link Output#MESSAGE_FILE_UPLOADED}.
     * Il WS si occuperà di spedire i messaggi per i cambiamenti di stato {@link Output#MESSAGE_STATUS}
     * all'inizio e termine della chiamata
     *
     * @param context               context dell'App
     * @param keyMediaListMap       ArrayMap di String - {@link UploadableMediaInfo} per identificare i file da caricare
     * @param uploadCode            identificativo del caricamento del file
     * @param uploadParameters      parametri da passare all'{@link UploadInterceptor} durante il processo di upload
     * @param endListenerParameters parametri da passare a fine richiesta
     * @param loginRequired         se il caricamento è da effettuare loggato.
     */
    static public void startServiceToUploadFiles(@NonNull Context context,
                                                 @NonNull KeyMediaListMap keyMediaListMap,
                                                 @NonNull Integer uploadCode,
                                                 @Nullable Bundle uploadParameters,
                                                 @Nullable Bundle endListenerParameters,
                                                 boolean loginRequired) {
        Bundle bundle = new Bundle();

        bundle.putString(EXTRA_JSON_KEY_MEDIA_MAP, keyMediaListMap.serialize());

        bundle.putInt(EXTRA_UPLOAD_CODE, uploadCode);
        if (uploadParameters != null) {
            bundle.putBundle(EXTRA_UPLOAD_PARAMETERS, uploadParameters);
        }

        if (endListenerParameters != null) {
            bundle.putBundle(EXTRA_API_END_CALL_BUNDLE, endListenerParameters);
        }

        bundle.putBoolean(EXTRA_LOGIN_REQUIRED, loginRequired);

        Intent intent = new Intent(context, OrchardUploadService.class);

        intent.putExtras(bundle);
        intent.setAction(INTENT_ACTION_UPLOAD_FILE);

        intent.addFlags(Service.START_STICKY_COMPATIBILITY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = (KrakeApplication) getApplication();
    }

    @Override
    protected Message getStatusMessage() {
        Message message = new Message();
        message.what = Output.MESSAGE_STATUS;

        Bundle data = new Bundle();

        data.putString(Output.UPLOAD_STATUS, mSignalApiStatus.toString());

        switch (mSignalApiStatus) {
            case UPLOADING_FILE:
                data.putInt(Output.UPLOAD_CODE, mUploadFileCode);
                break;
        }

        message.setData(data);
        return message;
    }

    @Override
    protected boolean handleClientMessage(Message message) {
        return false;
    }

    @Override
    protected void onHandleIntent(final Intent intent, Handler handler) {

        final String action = intent.getAction();
        if (action != null) {
            switch (action) {

                case INTENT_ACTION_UPLOAD_FILE:
                    uploadFile(intent);
                    break;

                case INTENT_ACTION_CANCEL_UPLOAD:
                    List<UploadInterceptor> interceptors = mApp.getUploadInterceptors();
                    boolean closeNotification = true;
                    for (UploadInterceptor interceptor : interceptors) {
                        // viene interrotto l'upload per ogni interceptor
                        if (!interceptor.cancelUpload()) {
                            closeNotification = false;
                        }
                    }
                    if (closeNotification) {
                        stopForeground(true);
                    }
                    break;
            }
        }
    }

    @Override
    protected int threadIndexToHandleIntent(Intent intent, int numberOfThreads) {
        return 0;
    }

    private void uploadFile(Intent intent) {
        final Integer uploadCode = intent.getIntExtra(EXTRA_UPLOAD_CODE, 0);

        final String invokeAPI = intent.getStringExtra(EXTRA_API_PATH);

        final boolean loginRequired = intent.getBooleanExtra(EXTRA_LOGIN_REQUIRED, false);
        final KeyListMap<UploadableMediaInfo> keyMediaListMap = new KeyMediaListMap().deserialize(intent.getStringExtra(EXTRA_JSON_KEY_MEDIA_MAP));

        final int mediaNumber = ((KeyMediaListMap) keyMediaListMap).numberOfUploadableMedias();

        mUploadFileCode = uploadCode;
        mSignalApiStatus = UploadStatus.UPLOADING_FILE;
        sendMessageToClients(getStatusMessage());


        RemoteClient remoteClient = RemoteClient.Companion.client(loginRequired);

        OrchardError error = null;

        final String notificationTitle = getString(R.string.uploading_medias);
        final String notificationText = null;
        final int notificationDrawable = R.drawable.media_upload_logo;
        long notificationProgressStart = 0;
        final long maxProgress = 100;

        Intent cancelIntent = new Intent(this, OrchardUploadService.class);
        cancelIntent.setAction(INTENT_ACTION_CANCEL_UPLOAD);
        cancelIntent.addFlags(START_STICKY_COMPATIBILITY);

        final NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.media_upload_cancel_icon, getString(android.R.string.cancel),
                PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        startForeground(UPLOAD_CONTENT_NOTIFICATION_ID, getOnGoingStatusNotification(notificationTitle, notificationText, notificationDrawable, notificationProgressStart, maxProgress, action));

        // viene calcolata la percentuale in base al numero di file
        final long singleFilePercentage = 100 / mediaNumber;

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        final Bundle uploadParams = intent.getBundleExtra(EXTRA_UPLOAD_PARAMETERS);

        boolean cancelled = false;

        for (int i = 0; i < keyMediaListMap.size() && !cancelled; i++) {
            String key = keyMediaListMap.keyAt(i);
            // ottiene la lista di media abbinata ad una chiave
            List<UploadableMediaInfo> mediaList = keyMediaListMap.get(key);

            for (int j = 0; j < mediaList.size(); j++) {
                final UploadableMediaInfo mediaInfo = mediaList.get(j);
                if (!mediaInfo.isUploaded()) {
                    final UploadInterceptor interceptor = mApp.getUploadInterceptor(mediaInfo, key, uploadParams);
                    if (interceptor == null) {
                        error = new OrchardError(getString(R.string.error_uploading_file));
                        break;
                    }

                    final long finalNotificationProgressStart = notificationProgressStart;

                    try {
                        // upload del file oppure throw dell'eccezione
                        interceptor.uploadFile(remoteClient, this, mediaInfo, uploadParams);
                    } catch (OrchardError e) {
                        error = e;
                        // se l'upload è stato cancellato a mano, si interrompono tutti gli upload, altrimenti il processo continua
                        if (interceptor.isCancelled()) {
                            cancelled = true;
                            break;
                        }
                    }

                    notificationProgressStart += singleFilePercentage;
                }
            }
        }

        stopForeground(true);

        Message message = new Message();
        message.what = Output.MESSAGE_FILE_UPLOADED;
        Bundle extras = new Bundle();

        extras.putInt(Output.UPLOAD_CODE, uploadCode);
        extras.putBoolean(Output.SUCCESS, error == null);
        extras.putString(Output.UPDATED_MEDIA_LIST, keyMediaListMap.serialize());

        if (error != null) {
            Log.w(TAG, "uploadFile: error during the upload. " + error.toString());
            handleOrchardError(error, extras);
        }
        message.setData(extras);

        final Bundle apiEndCallBundle = intent.getBundleExtra(EXTRA_API_END_CALL_BUNDLE);
        if (apiEndCallBundle != null) {
            extras.putAll(apiEndCallBundle);
        }

        sendMessageToClients(message);

        callUploadEndListener(uploadCode, keyMediaListMap, error, apiEndCallBundle);

        mUploadFileCode = null;
        mSignalApiStatus = UploadStatus.WAITING;
        sendMessageToClients(getStatusMessage());
    }

    protected void handleOrchardError(OrchardError error, Bundle extras) {

        extras.putString(Output.ERROR_ORCHARD, gson.toJson(error));

        extras.putInt(Output.ERROR_REACTION_CODE, error.getReactionCode());
    }

    private void callUploadEndListener(Integer uploadCode, KeyListMap<UploadableMediaInfo> mediaIdsMap, OrchardError error, Bundle extras) {
        Class endListenerClass = mApp.getUploadCompletedListener(uploadCode);
        if (endListenerClass != null) {
            try {

                UploadCompleteEndListener endListener = (UploadCompleteEndListener) endListenerClass.getConstructor().newInstance();
                endListener.onUploadCompleted(this, mediaIdsMap, error, extras);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public enum UploadStatus {
        WAITING,
        UPLOADING_FILE
    }

    public static class Output {
        /**
         * Boolean
         */
        public static final String SUCCESS = "Success";

        /**
         * String: serialization of Orchard Error
         */
        public static final String ERROR_ORCHARD = "Error";

        /**
         * Int: codice della reazione che è necessaria per correggere l'errore.
         * in tutti gli altri caso ha valore 0
         */
        public final static String ERROR_REACTION_CODE = "ErrorCode";

        /**
         * Long: identificativo dell'upload
         */
        public static final String UPLOAD_CODE = "UploadCode";

        /**
         * String: toString della enum {@link OrchardUploadService.UploadStatus}
         */
        public static final String UPLOAD_STATUS = "UploadStatus";

        /**
         * JsonArray: array {@link JsonObject} o {@link JsonArray}.
         * In base al ritorno delle chiamate
         */
        public static final String UPLOAD_OUTPUTS = "UploadOutputs";


        /**
         * ArrayList<String>: Uri toString. Contiene l'URI del file inviati
         */
        public static final String UPDATED_MEDIA_LIST = "updatedMediaList";

        /**
         * <ol>
         * <li>{@link OrchardUploadService.Output#UPLOAD_STATUS}</li>
         * <li>{@link OrchardUploadService.Output#UPLOAD_CODE}</li>
         * </ol>
         */
        public final static int MESSAGE_STATUS = 660;

        /**
         * Messaggio che indica che l'upload di un file è terminato
         * Contiene:
         * <ol>
         * <li>{@link OrchardUploadService.Output#SUCCESS}</li>
         * <li>{@link OrchardUploadService.Output#ERROR_ORCHARD}</li>
         * <li>{@link OrchardUploadService.Output#ERROR_REACTION_CODE}</li>
         * <li>{@link OrchardUploadService.Output#UPDATED_MEDIA_LIST}</li>
         * <li>{@link OrchardUploadService.Output#UPLOAD_OUTPUTS}</li>
         * <li>{@link OrchardUploadService.Output#EXTRA_API_END_CALL_BUNDLE}</li>
         * </ol>
         */
        public final static int MESSAGE_FILE_UPLOADED = 670;

    }
}
