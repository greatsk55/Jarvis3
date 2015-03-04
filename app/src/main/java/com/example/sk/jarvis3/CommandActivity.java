package com.example.sk.jarvis3;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sk on 2015-03-02.
 */
public class CommandActivity extends Activity{
    public static final String TAG="COMMAND";
    public static final int SPEECH_INPUT=100;

    private static final int READY=1;
    public static final int START=2;
    public static final int RESTART=3;

    public ComponentName adminComponent;
    public DevicePolicyManager devicePolicyManager;

    public TextView txt1;
    public TextView txt2;

    public Intent recoIntent;
    public Intent jarvis;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        txt1 = (TextView) findViewById(R.id.logger);
        txt2 = (TextView) findViewById(R.id.textView);


        jarvis = new Intent(getApplicationContext(), Jarvice.class);
        getApplicationContext().stopService(jarvis);

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //if( !devicePolicyManager.isAdminActive(adminComponent))
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            /*
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            */
            //devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            //devicePolicyManager.resetPassword("", 1);
        }
        recoIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recoIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recoIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recoIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "명령어를 입력하세요");

        mHdrVoiceRecoState.sendEmptyMessage(READY);
    }

    private void inputSpeech(){
        try {
            startActivityForResult(recoIntent, SPEECH_INPUT);
        }catch(ActivityNotFoundException e){

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
        mHdrVoiceRecoState.sendEmptyMessageDelayed(RESTART,500);
    }

    public void onService()
    {
        startService(jarvis);
    }


    private Handler mHdrVoiceRecoState = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READY:
                    sendEmptyMessageDelayed(START,100);
                    break;
                case START:
                    inputSpeech();
                    break;
                case RESTART:
                    onService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

}
