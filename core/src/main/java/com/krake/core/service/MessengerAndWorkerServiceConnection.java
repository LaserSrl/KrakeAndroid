package com.krake.core.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Created by joel on 24/09/14.
 */
public class MessengerAndWorkerServiceConnection implements ServiceConnection, LifecycleObserver {
    private @NonNull
    Context context;
    private @NonNull
    Class serviceClass;

    private @NonNull
    Messenger mServiceResponseClient;
    private Messenger mServiceMessenger = null;

    public MessengerAndWorkerServiceConnection(@NonNull Context context,
                                               @NonNull Class serviceClass,
                                               @NonNull Handler serviceHandler) {
        this.context = context;

        this.serviceClass = serviceClass;
        mServiceResponseClient = new Messenger(serviceHandler);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        context.bindService(new Intent(context, serviceClass),
                this,
                Context.BIND_AUTO_CREATE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onStop() {
        if (mServiceMessenger != null) {
            sendMessageToService(MessengerAndWorkerMultithreadService.InputMessageCodes.UNREGISTER_UPDATE, null);
            context.unbindService(this);
            mServiceMessenger = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        sendMessageToService(MessengerAndWorkerMultithreadService.InputMessageCodes.REGISTER_UPDATE, null);
    }

    public void sendMessageToService(int what, Bundle data) {
        Message message = new Message();
        message.what = what;
        message.replyTo = mServiceResponseClient;
        message.setData(data);
        try {
            mServiceMessenger.send(message);
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceMessenger = null;
    }
}
