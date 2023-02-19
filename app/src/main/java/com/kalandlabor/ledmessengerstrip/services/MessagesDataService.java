package com.kalandlabor.ledmessengerstrip.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class MessagesDataService extends Service {

   // private final IBinder binder = new LocalBinder();

    private Messenger messenger;
    private static List<String> buttonTexts;

    public MessagesDataService() {
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
//    public class LocalBinder extends Binder {
//        public MessagesDataService getService() {
//            // Return this instance of LocalService so clients can call public methods
//            return MessagesDataService.this;
//        }
//    }

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
            List<String> data = (List<String>) msg.obj;
            Log.println(Log.INFO, "xxx", data.get(0));
            //Setup the reply message
            Message message = Message.obtain(null, 2, data.size(), 0);
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