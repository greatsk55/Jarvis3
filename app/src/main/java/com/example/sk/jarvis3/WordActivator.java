package com.example.sk.jarvis3;

/**
 * Created by sk on 2015-02-20.
 */

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * Uses direct speech recognition to activate when the user speaks
 * one of the target words
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class WordActivator implements RecognitionListener,SpeechActivator
{
    private static final String TAG = "WordActivator";
    private static final String WORD = "자비스";

    private AudioManager mAudioManager;
    private int mStreamVolume = 0;

    private Context context;
    private SpeechRecognizer recognizer;

    private SpeechActivationListener resultListener;

    public WordActivator(Context context,
                         SpeechActivationListener resultListener, String... targetWords)
    {
        this.context = context;
        this.resultListener = resultListener;
        this.mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void detectActivation()
    {
        recognizeSpeechDirectly();
    }

    private void recognizeSpeechDirectly()
    {
        Intent recognizerIntent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // accept partial results if they come
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        SpeechRecognitionUtil.recognizeSpeechDirectly(context,
                recognizerIntent, this, getSpeechRecognizer());


    }

    public void stop()
    {
        if (getSpeechRecognizer() != null)
        {
            //AudioManager amanager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            //amanager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
            getSpeechRecognizer().stopListening();
            getSpeechRecognizer().cancel();
            getSpeechRecognizer().destroy();
        }
    }

    @Override
    public void onResults(Bundle results)
    {
        Log.d(TAG, "full results");
        receiveResults(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "partial results");
        receiveResults(partialResults);
    }

    /**
     * common method to process any results bundle from {@link SpeechRecognizer}
     */
    private void receiveResults(Bundle results)
    {
        if ((results != null)
                && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION))
        {
            List<String> heard =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores =
                    results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            receiveWhatWasHeard(heard, scores);

            String str = new String();
            for( int i=0; i<heard.size(); i++) {
                str += heard.get(i);
            }
            Log.d(TAG,"result"+str);
        }
        else
        {
            Log.d(TAG, "no results");
        }
    }

    private void receiveWhatWasHeard(List<String> heard, float[] scores)
    {
        boolean heardTargetWord = false;
        // find the target word
        for (String possible : heard)
        {
            if ( possible.equals( WORD )  )
            {
                Log.d(TAG, "HEARD IT!");
                heardTargetWord = true;
                break;
            }
        }

        if (heardTargetWord)
        {
            stop();
            resultListener.activated(recognizer);
        }
        else
        {
            // keep going
            recognizeSpeechDirectly();
        }
    }

    @Override
    public void onError(int errorCode)
    {
        if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH)
                || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        {
            Log.d(TAG, "didn't recognize anything");
            // keep going
            recognizeSpeechDirectly();
        }
        else
        {
            Log.d(TAG,
                    "FAILED "
                            + SpeechRecognitionUtil
                            .diagnoseErrorCode(errorCode));
        }
    }

    /**
     * lazy initialize the speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer()
    {
        if (recognizer == null)
        {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        }
        return recognizer;
    }

    // other unused methods from RecognitionListener...

    @Override
    public void onReadyForSpeech(Bundle params) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0); // again setting the system volume back to the original, un-mutting
        //
    }

    @Override
    public void onEndOfSpeech()
    {

    }

    /**
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
    @Override
    public void onBeginningOfSpeech()
    {
    }

    @Override
    public void onBufferReceived(byte[] buffer)
    {
    }

    @Override
    public void onRmsChanged(float rmsdB)
    {
    }

    @Override
    public void onEvent(int eventType, Bundle params)
    {
    }
}