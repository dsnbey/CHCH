package com.example.ch.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ch.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FCM received", "onMessageReceived: ");

        Map<String, String> dataReceived = message.getData();

        Random rand = new Random();
        int ID = rand.nextInt();
        if (dataReceived != null) {
                Common.showNotification(this, ID,
                        dataReceived.get(Common.NOT_TITLE),
                        dataReceived.get(Common.NOT_CONTENT),
                        dataReceived.get(Common.NOT_SENDER),
                        dataReceived.get(Common.NOT_ROOM_ID),
                        null);

        }
    }
}