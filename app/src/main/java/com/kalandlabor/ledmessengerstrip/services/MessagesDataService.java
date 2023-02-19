package com.kalandlabor.ledmessengerstrip.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class MessagesDataService extends Service {

    private final IBinder binder = new LocalBinder();
    private static List<String> buttonTexts;

    public MessagesDataService() {
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MessagesDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MessagesDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /** method for clients */
    public List<String> getMessageTexts() {
        return buttonTexts;
    }
    public void setButtonTexts(List<String> texts) {
        buttonTexts = texts;
    }
}