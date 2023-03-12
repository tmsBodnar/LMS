package hu.kalandlabor.carmessenger.phone;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import androidx.annotation.Nullable;

public class BluetoothConnectionService extends Service {

    BluetoothMessengerService bms;
    private Messenger messenger;
    SettingsActivity settingsActivity;
    public BluetoothConnectionService() {
        this.settingsActivity = null;
        bms = new BluetoothMessengerService(this, settingsActivity);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendMessage(String text) {
        this.bms.sendMessage(text);
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 999) {
                Bundle bundle = (Bundle) msg.obj;
                String textToSend = bundle.getString("textToSend");
                bms.sendMessage(textToSend);
            }
        }
    }


}