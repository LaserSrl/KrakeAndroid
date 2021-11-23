package com.krake.core.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.krake.core.R;
import com.krake.core.app.ContentItemDetailActivity;
import com.krake.core.app.KrakeApplication;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.component.module.ThemableComponentModule;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by joel on 27/04/16.
 */
public class OrchardContentNotifier {
    private OrchardContentNotifier() {

    }

    public static void showNotification(@NonNull Context context,
                                        @Nullable String text,
                                        @Nullable Object resultObject,
                                        @Nullable String displayPath,
                                        @Nullable Map<String, String> connectionExtras,
                                        @Nullable Map<String, String> remoteMessageExtras) {
        showNotification(context,text,resultObject,displayPath,connectionExtras,null, KrakeApplication.KRAKE_NOTIFICATION_CHANNEL);
    }

    public static void showNotification(@NonNull Context context,
                                        @Nullable String text,
                                        @Nullable Object resultObject,
                                        @Nullable String displayPath,
                                        @Nullable Map<String, String> connectionExtras,
                                        @Nullable Map<String, String> remoteMessageExtras,
                                        String channelId) {
        if (!TextUtils.isEmpty(text)) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId);
            notificationBuilder.setSmallIcon(R.drawable.ic_push_icon);

            notificationBuilder.setContentTitle(context.getString(R.string.app_name));
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            notificationBuilder.setVibrate(new long[]{500, 500});
            int maxTextLength = context.getResources().getInteger(R.integer.push_max_text_length);
            if (text.length() > maxTextLength) {
                notificationBuilder.setContentText(text.substring(0, maxTextLength).concat("..."));
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text));
            } else {
                notificationBuilder.setContentText(text);
            }

            Class contentClass = null;
            Intent activityIntent = null;

            KrakeApplication app = (KrakeApplication) context.getApplicationContext();

            if (resultObject != null && displayPath != null) {
                if (resultObject instanceof List) {
                    List list = (List) resultObject;

                    if (list.size() > 0) {
                        Object projectionContent = list.get(0);

                        if (app.isDataClassRegisteredForProjection(projectionContent.getClass())) {
                            contentClass = projectionContent.getClass();
                            activityIntent = app.registeredActivityIntentForContentClass(projectionContent.getClass());
                        }
                    }
                } else {
                    if (app.isDataClassMappedForDetails(resultObject.getClass())) {
                        contentClass = resultObject.getClass();
                        activityIntent = new Intent(context, ContentItemDetailActivity.class);
                    }
                }
            }
            Intent upIntent = null;
            if (contentClass != null && activityIntent != null) {
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                OrchardComponentModule orchardComponentModule = new OrchardComponentModule();
                orchardComponentModule.dataClass(contentClass);
                orchardComponentModule.displayPath(displayPath);
                if (connectionExtras != null)
                    orchardComponentModule.extraParameters(connectionExtras);

                activityIntent.putExtras(orchardComponentModule.writeContent(context));
                PackageManager pm = context.getPackageManager();

                if (((KrakeApplication) context.getApplicationContext()).getForegroundActivitiesCount() == 0) {

                    upIntent =
                            pm.getLaunchIntentForPackage(context.getApplicationContext().getPackageName());
                    ThemableComponentModule module = new ThemableComponentModule().upIntent(upIntent);

                    activityIntent.putExtras(module.writeContent(context));
                }
            } else if (displayPath == null || !displayPath.startsWith("http")) {
                PackageManager pm = context.getPackageManager();

                activityIntent = pm.getLaunchIntentForPackage(context.getApplicationContext().getPackageName());
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else {
                activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(displayPath));
            }

            activityIntent = app.updateCloudMessageNotification(notificationBuilder,
                    activityIntent,
                    text,
                    displayPath,
                    resultObject,
                    remoteMessageExtras);

            activityIntent.putExtra(KrakeApplication.OPEN_PUSH_NOTIFICATION, context.getResources().getBoolean(R.bool.enable_open_push_notification_event));

            PendingIntent contentIntent;
            if (upIntent == null) {
                contentIntent = PendingIntent.getActivity(context, new Random().nextInt(), activityIntent, PendingIntent.FLAG_ONE_SHOT);
            } else {
                contentIntent = TaskStackBuilder.create(context)
                        .addNextIntent(upIntent)
                        .addNextIntent(activityIntent)
                        .getPendingIntent(new Random().nextInt(), PendingIntent.FLAG_ONE_SHOT);
            }
            notificationBuilder.setContentIntent(contentIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
        }
    }
}
