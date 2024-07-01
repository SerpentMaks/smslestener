package com.example.smslistener;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static final String PHRASE = "MIR-";
    private static final String SERVER_URL = "http://ewko.ru/ind69.php";
    private static final int NOTIFICATION_ID_MESSAGE_FOUND = 1;
    private static final int NOTIFICATION_ID_REQUEST_SENT = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey("pdus")) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = message.getDisplayOriginatingAddress();
                    String body = message.getMessageBody();
                    if (body != null && body.contains(PHRASE)) {
                        showNotification(context, "Сообщение найдено");
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        int registrationCode = MainActivity.getRegistrationCode(context);
                        sendHttpRequest(context, timestamp, sender, body, registrationCode);
                    }
                }
            }
        } else {
            Log.i(TAG, "SmsReceiver started");
        }
    }

    private void sendHttpRequest(Context context, String timestamp, String sender, String message, int registrationCode) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("par1", timestamp)
                .add("par2", sender)
                .add("par3", message)
                .add("par4", String.valueOf(registrationCode))
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "HTTP request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "HTTP request failed: " + response);
                } else {
                    Log.i(TAG, "HTTP request successful: " + response);
                    showNotification(context, "Запрос отправлен");
                }
            }
        });
    }

    private void showNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channel is required for Android Oreo (API 26) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            String channelName = "Default Channel";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default_channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Уведомление от SmsReceiver")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        notificationManager.notify(getNotificationId(message), builder.build());
    }

    private int getNotificationId(String message) {
        if ("Сообщение найдено".equals(message)) {
            return NOTIFICATION_ID_MESSAGE_FOUND;
        } else if ("Запрос отправлен".equals(message)) {
            return NOTIFICATION_ID_REQUEST_SENT;
        }
        return 0;
    }
}
