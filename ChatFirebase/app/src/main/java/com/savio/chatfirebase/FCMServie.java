package com.savio.chatfirebase;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMServie extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull  RemoteMessage remoteMessage) {

        Log.i("teste",remoteMessage.getMessageId());
    }
}
