package com.kalandlabor.ledmessengerstrip;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Debug;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kalandlabor.ledmessengerstrip.adapters.CustomGridAdapter;
import com.kalandlabor.ledmessengerstrip.managers.BluetoothMessenger;
import com.kalandlabor.ledmessengerstrip.services.MessagesDataService;

/**
 * App for controlling LED messenger Strip device
 * manages the connection and send messages to the device with BluetoothMessenger class
 * save the defined Buttons in SharedPreferences
 */
public class MainActivity extends AppCompatActivity {

    public Context context;
    List<Button> buttonList;
    EditText newButtonsText;
    GridView gridView;
    CustomGridAdapter gridAdapter;
    SharedPreferences sPrefs;
    List<String> buttonTexts;
    String textToSend;
    MessagesDataService mService;
    Messenger messenger = null;
    Messenger reply = null;
    ServiceConnection connection;
    boolean mBound = false;
    static BluetoothMessenger btm;
    public static MyBluetoothTask btt = null;
    final int ADD_TYPE = 1;
    final int ERROR_TYPE = 2;
    final int DEV_ERROR_TYPE = 3;

    ActivityResultLauncher<Intent> startSpeechActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && null != result.getData()) {
                    ArrayList<String> res = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textToSend = res.get(0);
                    sendToBluetooth(textToSend);
                }
            });

    ActivityResultLauncher<Intent> startBluetoothActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                btt.cancel(true);
                btt.restartMyBluetoothTask();
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::addNewMessage);
        buttonList = new ArrayList<>();
        gridView = findViewById(R.id.grid_view);
        gridAdapter = new CustomGridAdapter(MainActivity.this, buttonList);
        gridView.setAdapter(gridAdapter);
        btm = new BluetoothMessenger();
        btt = new MyBluetoothTask(MainActivity.this);
        btt.execute();
        connection = new RemoteServiceConnection();
        reply = new Messenger(new IncomingHandler());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent();
        intent.setClassName(
                "com.kalandlabor.ledmessengerstrip",
                "com.kalandlabor.ledmessengerstrip.services.MessagesDataService");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(btm.getClientSocket() == null) {
            btt.restartMyBluetoothTask();
        }
            sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> buttonTextSet = new HashSet<>();
            buttonTextSet =  sPrefs.getStringSet("buttonText", buttonTextSet);
            buttonTexts= new ArrayList<>();
            buttonTexts.addAll(buttonTextSet);
            for ( String text : buttonTexts) {
                addNewButton(text);
            }
            if(mBound) {
                try {
                    Message message = Message.obtain(null, 1, buttonTexts);
                    message.replyTo = reply;
                    messenger.send(message);
                } catch (RemoteException e) {
                    Toast.makeText(MainActivity.this, "Invocation Failed!!", Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);
                }
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sPrefs.edit();
        Set<String> buttonTextSet = new HashSet<>(buttonTexts);
        buttonTextSet.addAll(buttonTexts);
        editor.putStringSet("buttonText",buttonTextSet).apply();
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    private void addNewMessage(View view) {
        openDialog(ADD_TYPE);
    }

    //opens a dialog based on dialog type
    private void openDialog( int type) {
        if(type == ADD_TYPE) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View dialogView = factory.inflate(R.layout.add_message_dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setView(dialogView);
            dialogView.findViewById(R.id.new_button_text).requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialogView.findViewById(R.id.save_new_message).setOnClickListener(v -> {
                newButtonsText = dialogView.findViewById(R.id.new_button_text);
                buttonTexts.add(newButtonsText.getText().toString());
                String newText = newButtonsText.getText().toString();
                addNewButton(newText);
                Set<String> buttonTextSet = new HashSet<>();
                buttonTextSet =  sPrefs.getStringSet("buttonText", buttonTextSet);
                buttonTextSet.add(newText);
                SharedPreferences.Editor editor = sPrefs.edit();
                editor.clear();
                editor.putStringSet("buttonText",buttonTextSet).apply();
                if(mBound) {
                    try {
                        Message message = Message.obtain(null, 1, buttonTexts);
                        message.replyTo = reply;
                        messenger.send(message);
                    } catch (RemoteException e) {
                        Toast.makeText(MainActivity.this, "Invocation Failed!!", Toast.LENGTH_LONG).show();
                        throw new RuntimeException(e);
                    }
                 //   mService.setButtonTexts(buttonTexts);
                }
                dialog.dismiss();
            });
            dialog.show();
        }if (type == ERROR_TYPE){
            LayoutInflater factory = LayoutInflater.from(this);
            final View dialogView = factory.inflate(R.layout.check_bluetooth_dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setView(dialogView);
            dialogView.findViewById(R.id.OK_btn).setOnClickListener(v -> {
                btt.cancel(true);
                btt.restartMyBluetoothTask();
                dialog.dismiss();
            });
            dialogView.findViewById(R.id.Cancel_btn).setOnClickListener(v -> {
                dialog.dismiss();
                finishAndRemoveTask();
            });
            dialog.show();
        }if (type == DEV_ERROR_TYPE) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View dialogView = factory.inflate(R.layout.check_bluetooth_dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setView(dialogView);
            TextView tv = dialogView.findViewById(R.id.error_dialog_text);
            tv.setText(R.string.bluetooth_settings_message);
            dialogView.findViewById(R.id.OK_btn).setOnClickListener(v -> {
                btt.cancel(true);
                openBluetoothSettings();
                dialog.dismiss();
            });
            dialogView.findViewById(R.id.Cancel_btn).setOnClickListener(v -> {
                dialog.dismiss();
                finishAndRemoveTask();
            });
            dialog.show();
        }
    }

    private void addNewButton(String text) {
        boolean exist = false;
        for ( Button btn : buttonList) {
            if (btn.getText().toString().equals(text)){
                exist = true;
            }
        }if (! exist ) {
            Button b = new Button(context);
            b.setText(text);
            buttonList.add(b);
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bluetooth_settings) {
            openBluetoothSettings();
            return true;
        }
        if (id == R.id.faq) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openBluetoothSettings(){
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        //startActivityForResult(intentOpenBluetoothSettings, REQ_CODE_BLUETOOTH);
        startBluetoothActivityIntent.launch(intentOpenBluetoothSettings);
    }

    // opens the google speech to text activity
    public void speechToText(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hu-HU");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Mondd az üzeneted!");
        try {
       //     startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            startSpeechActivityIntent.launch(intent);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    // sends message with BluetoothMessenger class
    private void sendToBluetooth(String textToSend) {
        if(btm.getClientSocket() != null) {
            showToast("elküldve: " + textToSend);
            btm.sendMessage(textToSend);
        } else {
            openDialog(ERROR_TYPE);
        }
    }

    //gets a message text from button list's clicked item
    public void itemClicked(int position){
        sendToBluetooth(buttonList.get(position).getText().toString());
    }

    //remove item from buttonList
    public boolean removeItem(Button button) {
        for ( Button b: buttonList ) {
            if(b.getText() == button.getText()){
                buttonList.remove(b);
                gridAdapter.notifyDataSetChanged();
                return removeButtonText(button);
            }
        }

        return true;
    }

    //remove text from buttonTexts ( and from SharedPreferences in OnPause )
    private boolean removeButtonText(Button button) {
        for (String s : buttonTexts) {
            if (s.contentEquals(button.getText())) {
                buttonTexts.remove(s);
                return true;
            }
        }
        return false;
    }

    // showing custom Toast
    private void showToast(String message){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // Class for separate AsyncTask for initializing Bluetooth connection
    // in BluetoothManager class,
    static class MyBluetoothTask extends AsyncTask<Void, Void, String> {
        private final WeakReference<MainActivity> weakActivity;
        ProgressDialog progressDialog;
        final int ERROR_TYPE = 2;
        final int DEV_ERROR_TYPE = 3;
        MyBluetoothTask myBtt = null;

        MyBluetoothTask(MainActivity myActivity) {
            this.weakActivity = new WeakReference<>(myActivity);
        }

        @Override
        public String doInBackground(Void... params) {
                return btm.initBluetooth();
        }

        @Override
        public void onPostExecute(String result) {
            MainActivity activity = weakActivity.get();
            if (activity == null
                    || activity.isFinishing()
                    || activity.isDestroyed()) {
            } else {
                if (result.equals("BLUETOOTH ERROR") && !isCancelled()) {
                    progressDialog.dismiss();
                    activity.openDialog(DEV_ERROR_TYPE);
                } else if (result.equals("Clientsocket Error") && ! isCancelled()){
                    progressDialog.dismiss();
                    activity.openDialog(ERROR_TYPE);
                } else {
                    progressDialog.dismiss();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            myBtt = this;
            progressDialog = ProgressDialog.show(weakActivity.get(),
                    weakActivity.get().getResources().getString(R.string.bluetooth_check),
                    "");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            myBtt = null;
        }

        public void restartMyBluetoothTask(){
            myBtt = new MyBluetoothTask(weakActivity.get());
            myBtt.execute();
        }
    }

    private class RemoteServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            MainActivity.this.messenger = new Messenger(service);
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            MainActivity.this.messenger = null;
            mBound = false;
        }
    }

    private class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            System.out.println("*****************************************");
           Log.println(Log.INFO, "xxx","Return successfully received!!!!!!");
            System.out.println("*****************************************");

            int what = msg.what;


            Toast.makeText(MainActivity.this.getApplicationContext(), "Remote Service replied-("+msg.arg1+")", Toast.LENGTH_LONG).show();
        }
    }
}


