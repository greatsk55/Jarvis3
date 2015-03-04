package com.example.sk.jarvis3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {
    public static final String TAG="MAIN";
    public static final int SPEECH_INPUT=100;
    public static final int COMMAND_INPUT=101;
    private static final int READY=1;
    public static final int END=2;
    public static final int RESTART=3;

    public static String command;

    public BroadcastReceiver receiver;
    public TextView txt1;
    public TextView txt2;

    public ComponentName service;
    public Intent voiceService;
    public Intent recoIntent;
    public String recieveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt1 = (TextView) findViewById(R.id.logger);
        txt2 = (TextView) findViewById(R.id.textView);

        command="자비스";
        stopService(new Intent(this, Jarvice.class));

        recoIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recoIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recoIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recoIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "명령어를 입력하세요");

        receiver = new LocalReceiver();

        IntentFilter mainFilter = new IntentFilter("com.example.sk.jarvis3.ACTION");
        registerReceiver(receiver, mainFilter);
        onService();
    }

    public void onService(){
        //voiceService = new Intent("service.VoiceService");
        stopService(new Intent(this, Jarvice.class));
        if(voiceService!=null)
            stopService(voiceService);

        voiceService = new Intent(this, Jarvice.class);
        service = startService(voiceService);
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"받았다");
            startActivityForResult(recoIntent, SPEECH_INPUT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d(TAG, result.get(0));
                    txt2.setText(result.get(0));
                }
                break;
            }
        }
        mHdrVoiceRecoState.sendEmptyMessage(READY);
    }

    private Handler mHdrVoiceRecoState = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READY:
                    sendEmptyMessageDelayed(END,100);
                    break;
                case END: {
                    Log.d(TAG, "END");
                    getApplicationContext().stopService(voiceService);
                    sendEmptyMessageDelayed(RESTART,1000);
                    break;
                }
                case RESTART:
                    Log.d(TAG,"RESTART");
                    onService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
