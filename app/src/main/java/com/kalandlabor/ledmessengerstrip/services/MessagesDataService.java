package com.kalandlabor.ledmessengerstrip.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.kalandlabor.ledmessengerstrip.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class MessagesDataService extends Service {

   // private final IBinder binder = new LocalBinder();

    private Messenger messenger;
    public ArrayList<String> buttonTexts = new ArrayList<String>();

    public MessagesDataService() {
    }
    public ArrayList<String> getButtonTexts(){
        return buttonTexts;
    }
    public void setButtonTexts(ArrayList<String> texts) {
        buttonTexts = texts;
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */


    @Override
    public IBinder onBind(Intent intent) {
        if(this.messenger == null)
        {
            synchronized(MessagesDataService.class)
            {
                if(this.messenger == null)
                {
                    this.messenger = new Messenger(new IncomingHandler());
                }
            }
        }
        //Return the proper IBinder instance
        return this.messenger.getBinder();
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            int what = msg.what;

            Message message = Message.obtain(null, 2, 0, 0);
            if (what == 1) {
                Bundle bundle = (Bundle) msg.obj;
                setButtonTexts(bundle.getStringArrayList("buttonTexts"));
                Log.println(Log.INFO, "xxx", "in service received from app: " + buttonTexts.size());
            }
            if (what == 33 && buttonTexts != null) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("buttonTexts", buttonTexts);
                Log.println(Log.INFO, "xxx", "in service received from carmessenger: " + buttonTexts.size());
                message = Message.obtain(null, 44, 0, 0, bundle);
                try {
                    //make the RPC invocation
                    Messenger replyTo = msg.replyTo;
                    replyTo.send(message);
                } catch (RemoteException rme) {
                    //Show an Error Message
                    Toast.makeText(MessagesDataService.this, "Invocation failed", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}