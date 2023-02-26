package hu.kalandlabor.carmessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.ScreenManager;
import androidx.car.app.Session;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpeechToTextSession extends Session {
    public static final String TAG = "xxx";
    public static final Integer RecordAudioRequestCode = 1;
    public MessengerService context;
    SpeechRecognizer speechRecognizer;

    Loader.OnLoadCompleteListener<Cursor> listener = (loader, cursor) -> {
        ScreenManager screenManager = getCarContext().getCarService(ScreenManager.class);
    };

    public SpeechToTextSession(MessengerService context, SpeechRecognizer speechRecognizer) {
        this.context = context;
        this.speechRecognizer = speechRecognizer;
    }

    @Override
    @NonNull
    public Screen onCreateScreen(@NonNull Intent intent) {

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                showToast(getCarContext().getResources().getString(R.string.recording_in_progress));
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
                showToast(getCarContext().getResources().getString(R.string.recording_complete));
            }

            @Override
            public void onError(int i) {
                String description = "";
                switch (i) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        description = "ERROR_AUDIO";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        showToast(getCarContext().getResources().getString(R.string.speech_recongition_cancelled));
                        return;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        description = "ERROR_INSUFFICIENT_PERMISSIONS";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        description = "ERROR_NETWORK";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        description = "ERROR_NETWORK_TIMEOUT";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        showToast(getCarContext().getResources().getString(R.string.voice_command_not_recognized));
                        return;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        description = "ERROR_RECOGNIZER_BUSY";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        description = "ERROR_SERVER";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        description = "ERROR_SPEECH_TIMEOUT";
                        break;
                }
                showToast("onError " + i + " " + description);
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d(TAG, "the string is   " + data);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        return new MessengerScreen(getCarContext(), this);
    }

    private void showToast(String message) {

        CarToast

                .makeText(getCarContext(), message, CarToast.LENGTH_LONG)
                .show();
    }


}