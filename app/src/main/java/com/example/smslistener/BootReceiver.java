package com.example.smslistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i("BootReceiver", "Boot completed received, starting SmsListenerService");
            Intent serviceIntent = new Intent(context, SmsListenerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
                Log.i("BootReceiver", "Started service using startForegroundService");
            } else {
                context.startService(serviceIntent);
                Log.i("BootReceiver", "Started service using startService");
            }
        } else {
            Log.i("BootReceiver", "Received unexpected intent: " + intent.getAction());
        }
    }
}
