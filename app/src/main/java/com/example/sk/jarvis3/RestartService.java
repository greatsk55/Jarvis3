package com.example.sk.jarvis3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by sk on 2015-02-28.
 */
public class RestartService extends BroadcastReceiver {
    public static final String ACTION_RESTART_PERSISTENTSERVICE = "ACTION.Restart. PersistentService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RestartService", "RestartService called!@!@@@@@#$@$@#$@#$@#");
        if (intent.getAction().equals(ACTION_RESTART_PERSISTENTSERVICE)) {
            Intent i = new Intent("service.VoiceService");
            context.startService(i);
        }
    }
}