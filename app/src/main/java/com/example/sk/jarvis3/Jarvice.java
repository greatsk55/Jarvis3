package com.example.sk.jarvis3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sk on 2015-02-28.
 */
public class Jarvice extends Service implements SpeechActivationListener {

    private static final String TAG = "SpeechActivatorStartStop";

    private final int REQ_CODE_SPEECH_INPUT = 100;

    /**
     * store if currently listening
     */
    private boolean isListeningForActivation;

    /**
     * if paused, store what was happening so that onResume can restart it
     */
    private boolean wasListeningForActivation;

    private boolean isStop;

    private SpeechActivator speechActivator;
    private static final String WAS_LISTENING_STATE = "WAS_LISTENING";

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d(TAG,"자비스 생성");

        //unregisterRestartAlarm();

        isStop=false;
        isListeningForActivation = false;
        speechActivator = new WordActivator(this, this);

        startActivator();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if( !isStop ) {
            Log.d(TAG,"자비스 반복");
            isStop = true;
            isListeningForActivation = false;
            speechActivator = new WordActivator(this, this);

            startActivator();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){

        isStop = false;
        isListeningForActivation = false;
        speechActivator = null;

        //registerRestartAlarm();

        super.onDestroy();
    }


    public void startActivator()
    {
        if (isListeningForActivation)
        {
            Toast.makeText(this, "Not started: already started",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "not started, already started");
            // only activate once
            return;
        }

        if (speechActivator != null)
        {
            isListeningForActivation = true;
            Toast.makeText(this, "Started movement activator",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "started");
            speechActivator.detectActivation();
        }
    }

    private void stopActivator()
    {
        if (speechActivator != null)
        {
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "stopped");
            speechActivator.stop();
        }
        isListeningForActivation = false;
    }


    @Override
    public void activated(SpeechRecognizer recognizer)
    {
        Log.d(TAG, "activated...");
        stopActivator();
/*
        Intent itBroadcast = new Intent();
        itBroadcast.setAction("com.example.sk.jarvis3.ACTION");
        getApplicationContext().sendBroadcast(itBroadcast);
*/
        Intent intent = new Intent(this, CommandActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    void registerRestartAlarm() {
        Log.d(TAG, "registerRestartAlarm");
        Intent intent = new Intent(Jarvice.this, RestartService.class);
        intent.setAction(RestartService. ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(Jarvice.this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 5000; // 10초 후에 알람이벤트 발생
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1000, sender);
    }


    void unregisterRestartAlarm() {
        Log.d(TAG, "unregisterRestartAlarm");
        Intent intent = new Intent(Jarvice.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(Jarvice.this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
}
