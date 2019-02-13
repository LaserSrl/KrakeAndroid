package com.krake.core.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.krake.core.app.KrakeApplication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Versione che supporat più thread.
 * La classe permette di avere N code a priorità normale, cui saranno associati i lavori in modo casuale,
 * e una coda di lavori a priorità o lunghezza diversa (ad esempio per l'upload più lenti, così da non
 * bloccare i caricamenti dei dati)
 * Created by joel on 24/01/14.
 */
public abstract class MessengerAndWorkerMultithreadService extends Service {
    final Messenger mMessenger;
    private List<Messenger> mClients = new ArrayList<Messenger>();
    private int numberOfThreads;

    private List<Looper> mLoopers;
    private List<Handler> mHandlers;

    public MessengerAndWorkerMultithreadService(int numberOfThreads) {
        super();
        this.numberOfThreads = numberOfThreads;
        mMessenger = new Messenger(new IncomingHandler());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLoopers = new LinkedList<>();
        mHandlers = new LinkedList<>();

        for (int i = 0; i < numberOfThreads; ++i) {
            HandlerThread thread = new HandlerThread("IntentService[" + i + "]");
            thread.start();

            Looper looper = thread.getLooper();
            mLoopers.add(looper);
            ServiceHandler serviceHandler = new ServiceHandler(looper);

            mHandlers.add(serviceHandler);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler handler = mHandlers.get(threadIndexToHandleIntent(intent, numberOfThreads));
        Message msg = handler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        handler.sendMessage(msg);
        return START_NOT_STICKY;
    }

    protected abstract void onHandleIntent(Intent intent, Handler handler);

    protected abstract int threadIndexToHandleIntent(Intent intent, int numberOfThreads);

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Looper looper : mLoopers)
            looper.quit();
    }

    protected abstract Message getStatusMessage();

    protected abstract boolean handleClientMessage(Message message);

    //Bind con activities e fragment
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public void sendMessageToClients(Message message) {
        for (int i = 0; i < mClients.size(); ++i) {
            Messenger client = mClients.get(i);
            Message messageCopy = new Message();
            messageCopy.copyFrom(message);
            try {
                client.send(messageCopy);
            } catch (RemoteException ignored) {
            }
        }
    }

    protected Notification getOnGoingStatusNotification(String title, String text, int drawableID) {
        return getOnGoingStatusNotification(title, text, drawableID, 0, 0, null);
    }

    protected Notification getOnGoingStatusNotification(String title, String text, int drawableID, long progress, long maxProgress, @Nullable NotificationCompat.Action action) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), KrakeApplication.KRAKE_NOTIFICATION_CHANNEL);

        float progressScaled = 100.0f * progress / maxProgress;

        builder.setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(drawableID)
                .setContentTitle(title)
                .setPriority(Notification.PRIORITY_MAX)
                .setProgress(100, Math.round(progressScaled), progress == 0 && maxProgress == 0)
                .setContentText(text)
                .setWhen(0);

        if (action != null) {
            builder.addAction(action);
        }

        return builder.build();
    }

    public static class InputMessageCodes {
        public static final int REGISTER_UPDATE = 1102;
        public static final int UNREGISTER_UPDATE = 1101;

        public static final int UPDATE_STATUS = 1103;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                onHandleIntent((Intent) msg.obj, this);
            }
            stopSelf(msg.arg1);
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case InputMessageCodes.REGISTER_UPDATE:

                    mClients.add(msg.replyTo);
                    try {
                        msg.replyTo.send(getStatusMessage());

                    } catch (RemoteException e) {

                    }
                    break;

                case InputMessageCodes.UNREGISTER_UPDATE:
                    mClients.remove(msg.replyTo);
                    break;

                case InputMessageCodes.UPDATE_STATUS:
                    try {
                        msg.replyTo.send(getStatusMessage());

                    } catch (RemoteException e) {

                    }
                    break;

                default:
                    if (!MessengerAndWorkerMultithreadService.this.handleClientMessage(msg))
                        super.handleMessage(msg);
            }
        }
    }
}
