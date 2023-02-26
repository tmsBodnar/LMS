package hu.kalandlabor.carmessenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.Item;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

public class MessengerScreen extends Screen implements DefaultLifecycleObserver {

    Messenger messenger = null;
    Messenger reply = null;
    ServiceConnection connection;
    Intent serviceIntent;
    boolean mBound;
    List<String> buttonTexts = new ArrayList<String>();
    private SpeechToTextSession speechToTextSession;
    Intent speechRecognizerIntent;


    protected MessengerScreen(@NonNull CarContext carContext, SpeechToTextSession speechToTextSession) {
        super(carContext);
        this.speechToTextSession = speechToTextSession;
        this.getLifecycle().addObserver(this);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        connection = new RemoteServiceConnection();
        reply = new Messenger(new IncomingHandler());
        serviceIntent = new Intent();
        serviceIntent.setClassName(
                "com.kalandlabor.ledmessengerstrip",
                "com.kalandlabor.ledmessengerstrip.services.MessagesDataService");
        if (!mBound) {
            getCarContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
        Log.println(Log.INFO, "xxx", "onGetTemplate, bounded:" + mBound);
        getServiceConnection();
        getButtonTexts();
        return createButtons();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        Log.d("TAG", "On pause");
        speechToTextSession.speechRecognizer.cancel();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hu-HU");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Mondd az üzeneted!");

        speechToTextSession.speechRecognizer.cancel();
        speechRecognizerIntent = intent;
    }

    private void getServiceConnection() {
        if (!mBound) {
            getCarContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private void getButtonTexts() {
        Log.println(Log.INFO, "xxx", "getButtonTexts bounded:" + mBound);
        if (mBound) {
            Log.println(Log.INFO, "xxx", "carmessenger bounded:");
            if (buttonTexts.size() < 1) {
                try {
                    Message message = Message.obtain(null, 33, 0, 0);
                    message.replyTo = reply;
                    messenger.send(message);
                } catch (RemoteException e) {
                    Toast.makeText(getCarContext(), "Invocation Failed!!", Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private ListTemplate createButtons() {
        ItemList.Builder itemList = new ItemList.Builder();

        IconCompat refreshIcon = IconCompat.createWithResource(
                getCarContext(), R.mipmap.ic_refresh_round);
        CarIcon refreshCarIcon = new CarIcon.Builder(refreshIcon).build();

        IconCompat micIcon = IconCompat.createWithResource(
                getCarContext(), R.mipmap.ic_mic2_foreground);
        CarIcon microphoneCarIcon = new CarIcon.Builder(micIcon).build();
        Action mic = new Action.Builder()
                .setIcon(microphoneCarIcon)
                .setTitle(getCarContext().getString(R.string.say_something))
                .setOnClickListener(() -> { this.onMicClicked(speechRecognizerIntent);})
                .build();
        if (buttonTexts.size() > 0) {
            for (String text : buttonTexts) {
                Row.Builder gridBuilder = new Row.Builder();
                Item item = gridBuilder.setTitle(text)
                        .build();
                itemList.addItem(item);
            }
            itemList.setOnSelectedListener(this::onTitleClicked);
        } else {

            Row.Builder gridBuilder = new Row.Builder();
            Item item = gridBuilder.setTitle(getCarContext().getString(R.string.refresh_messages))
                    .setImage(refreshCarIcon)
                    .setOnClickListener(this::onRefreshClicked)
                    .build();
            itemList.addItem(item);
        }
        ActionStrip strip = new ActionStrip.Builder().addAction(mic).build();
        return new ListTemplate.Builder()
                .setTitle(getCarContext().getString(R.string.title))
                .setActionStrip(strip)
                .setSingleList(itemList.build())
                .build();
    }

    private void onRefreshClicked() {
        Log.println(Log.INFO, "xxx", "refresh clicked");
        getButtonTexts();
    }

    private void onMicClicked(Intent speechRecognizerIntent) {
        CarToast.makeText(getCarContext(), " Mic clicked", CarToast.LENGTH_LONG)
            .show();
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        int duration = 500;
        toneGen1.startTone(ToneGenerator.TONE_PROP_PROMPT, duration);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speechToTextSession.speechRecognizer.cancel();
                speechToTextSession.speechRecognizer.startListening(speechRecognizerIntent);
            }
        }, duration);

    }
    private void onTitleClicked(int index) {
        if (mBound) {
            try {
                Bundle textBundle = new Bundle();
                textBundle.putString("text", buttonTexts.get(index));
                Message message = Message.obtain(null, 55, textBundle);
                message.replyTo = reply;
                messenger.send(message);
                CarToast.makeText(getCarContext(), getCarContext().getString(R.string.sent), CarToast.LENGTH_LONG)
                        .show();
            } catch (RemoteException e) {
                CarToast.makeText(getCarContext(), getCarContext().getString(R.string.not_sent), CarToast.LENGTH_LONG)
                        .show();
            }
        } else {
            CarToast.makeText(getCarContext(), getCarContext().getString(R.string.no_connect), CarToast.LENGTH_LONG)
                    .show();
        }
    }
    private class RemoteServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            MessengerScreen.this.messenger = new Messenger(service);
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            MessengerScreen.this.messenger = null;
            mBound = false;
        }
    }
    private class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            int what = msg.what;
            if (what == 44) {
                Bundle bundle = (Bundle) msg.obj;
                MessengerScreen.this.buttonTexts = bundle.getStringArrayList("buttonTexts");
                Log.println(Log.INFO, "xxx", "in carmessenger received from service: " + buttonTexts.size());
                invalidate();
            }
        }
    }
}