package com.krake.contentcreation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.krake.core.*;
import com.krake.core.app.ContentItemDetailActivity;
import com.krake.core.app.KrakeApplication;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.DetailComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.media.UploadableMediaInfo;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.util.List;
import java.util.Random;

/**
 * Listener per gestire la fine dell'upload del file multimediale durante la creazione di un contenuto.
 * <p/>
 * Il listener si occupa una volta terminato l'upload della foto di avviare il caricamento degli altri dati dell'oggetto.
 * Il caricamento effettivo del contenuto è effettuato sfruttando la chiamata
 * con apiPath impostato a R.string.orchard_api_path_new_content
 */
public class CreateContentUploadMediaEndListener implements UploadCompleteEndListener,
        OrchardApiEndListener,
        Function2<RemoteResponse, OrchardError, Unit> {
    public static final String EXTRA_CONTENT_PARAMETERS_JSON = "mediaParameters";
    public static final String EXTRA_CONTENT_CREATION_INTENT_EXTRA = "CCIntentExtra";
    public static final String EXTRA_CONTENT_CREATION_INTENT_CLASS_NAME = "CCIntentClass";
    private static final String TAG = CreateContentUploadMediaEndListener.class.getSimpleName();

    @Override
    public void onUploadCompleted(@NonNull Context context,
                                  @NonNull KeyListMap<UploadableMediaInfo> keyMediaListMap,
                                  @Nullable OrchardError e,
                                  @Nullable Bundle endApiCallBundle) {
        if (endApiCallBundle != null) {
            if (e == null) {
                ArrayMap<String, JsonArray> mediasForGallery = new ArrayMap<>();
                for (int i = 0; i < keyMediaListMap.size(); i++) {
                    String key = keyMediaListMap.keyAt(i);
                    List<UploadableMediaInfo> mediaIds = keyMediaListMap.get(key);
                    JsonArray array = new JsonArray();
                    for (int j = 0; j < mediaIds.size(); j++) {
                        array.add(mediaIds.get(j).getId());
                    }
                    mediasForGallery.put(key, array);
                }

                JsonObject parameters = new Gson().fromJson(endApiCallBundle.getString(EXTRA_CONTENT_PARAMETERS_JSON), JsonObject.class);

                for (String key : mediasForGallery.keySet()) {
                    parameters.add(key, mediasForGallery.get(key));
                }

                RemoteRequest request = new RemoteRequest(context)
                        .setMethod(RemoteRequest.Method.POST)
                        .setPath(context.getString(R.string.orchard_api_path_content_modify))
                        .setBody(parameters);

                RemoteResponse response = null;
                OrchardError error = null;
                try {
                    response = Signaler.shared.executeAPI(context, request, endApiCallBundle.getBoolean(OrchardUploadService.EXTRA_LOGIN_REQUIRED), endApiCallBundle);
                } catch (OrchardError orchardError) {
                    error = orchardError;
                }

                onApiInvoked(context, request, response, error, endApiCallBundle);

            } else {
                notifyError(context, e, endApiCallBundle);
            }
        }
    }

    private void notifyError(Context context, OrchardError e, Bundle endApiCallBundle) {
        NotificationManager notification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, KrakeApplication.KRAKE_NOTIFICATION_CHANNEL);

        Intent intent = endApiCallBundle.getParcelable(EXTRA_CONTENT_CREATION_INTENT_CLASS_NAME);

        String errorMessage = e.getReactionCode() == OrchardError.REACTION_LOGIN ? context.getString(R.string.authentication_failed) : e.getUserFriendlyMessage(context);

        builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.new_content_creation_icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(String.format(context.getString(R.string.content_creation_notification_title_format),
                        endApiCallBundle.getString(Constants.REQUEST_CONTENT_TYPE)))
                .setContentText(errorMessage)
                .setContentIntent(PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0));

        notification.notify(new Random().nextInt(), builder.build());
    }

    public void onApiInvoked(@NonNull Context context,
                             @NonNull RemoteRequest remoteRequest,
                             @Nullable RemoteResponse remoteResponse,
                             @Nullable OrchardError e,
                             @Nullable Object endListenerParameters) {

        Bundle endApiCallBundle = (Bundle) endListenerParameters;
        if (e == null) {

            NotificationManager notification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, KrakeApplication.KRAKE_NOTIFICATION_CHANNEL);

            String contentType = endApiCallBundle.getString(Constants.REQUEST_CONTENT_TYPE);
            builder.setAutoCancel(true)
                    .setSmallIcon(R.drawable.new_content_creation_icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(String.format(context.getString(R.string.content_creation_notification_title_format),
                            endApiCallBundle.getString(Constants.REQUEST_CONTENT_TYPE)))
                    .setContentText(context.getString(R.string.new_content_created));

            Class dataClass = TextUtils.isEmpty(contentType) ? null : ClassUtils.dataClassForName(contentType);

            JsonObject result = remoteResponse.jsonObject();
            String alias = null;
            if (result.has("Data")) {
                alias = result.getAsJsonObject("Data").get("DisplayAlias").getAsString();
            }

            if (dataClass != null && alias != null && ((KrakeApplication) context.getApplicationContext()).isDataClassMappedForDetails(dataClass)) {
                Intent resultIntent = ComponentManager.createIntent()
                        .from(context)
                        .to(ContentItemDetailActivity.class)
                        .with(new DetailComponentModule(context),
                                new OrchardComponentModule()
                                        .dataClass(dataClass)
                                        .displayPath(alias))
                        .build();
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                        );

                builder.setContentIntent(resultPendingIntent);
            }

            notification.notify(new Random().nextInt(), builder.build());

            ContentCreationSaveManager.saveContentCreationInfos(context,
                    endApiCallBundle.getString(Constants.REQUEST_CONTENT_TYPE),
                    null);

        } else {
            notifyError(context, e, endApiCallBundle);
        }
    }

    @Override
    public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
        return null;
    }
}
