package hu.kalandlabor.carmessenger;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.Item;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.Template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessengerScreen extends Screen {

    Messenger messenger = null;
    Messenger reply = null;
    ServiceConnection connection;
    boolean mBound;
    List<String> buttonTexts = new ArrayList<String>();
    protected MessengerScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        connection = new RemoteServiceConnection();
        reply = new Messenger(new IncomingHandler());
        Intent servIntent = new Intent();
        servIntent.setClassName(
                "com.kalandlabor.ledmessengerstrip",
                "com.kalandlabor.ledmessengerstrip.services.MessagesDataService");
        getCarContext().bindService(servIntent, connection, Context.BIND_AUTO_CREATE);
        getButtonTexts();
        return createButtons();
    }

    private void getButtonTexts() {
        if (mBound) {
            Log.println(Log.INFO, "xxx", "carmessenger bounded");
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

    private ListTemplate createButtons(){
        ItemList.Builder itemList = new ItemList.Builder();

        IconCompat refreshIcon = IconCompat.createWithResource(
                getCarContext(), R.mipmap.ic_refresh_foreground);
        CarIcon refreshCarIcon = new CarIcon.Builder(refreshIcon).build();

        IconCompat micIcon = IconCompat.createWithResource(
                getCarContext(), R.mipmap.ic_mic2_foreground);
        CarIcon microphoneCarIcon = new CarIcon.Builder(micIcon).build();
        Action mic = new Action.Builder()
                .setIcon(microphoneCarIcon)
                .setTitle("Mondd el")
                .setOnClickListener(this::onMicClicked)
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
            Item item = gridBuilder.setTitle("nincs üzenet")
                    .setImage(refreshCarIcon)
                    .setOnClickListener(this::onRefreshClicked)
                    .build();
            itemList.addItem(item);
        }
        ActionStrip strip = new ActionStrip.Builder().addAction(mic).build();
        return new ListTemplate.Builder()
                .setTitle("Messages")
                .setActionStrip(strip)
                .setSingleList(itemList.build())
                .build();
    }

    private void onRefreshClicked() {
        getButtonTexts();
    }

    private void onMicClicked() {
        CarToast.makeText(getCarContext(), " Mic clicked", CarToast.LENGTH_LONG)
                .show();
    }

    private void onTitleClicked(int index) {
        CarToast.makeText(getCarContext(), buttonTexts.get(index) +" clicked", CarToast.LENGTH_LONG)
                .show();
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