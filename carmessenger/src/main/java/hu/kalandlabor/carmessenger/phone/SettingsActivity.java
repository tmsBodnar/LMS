package hu.kalandlabor.carmessenger.phone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.connection.CarConnection;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import hu.kalandlabor.carmessenger.R;

public class SettingsActivity extends AppCompatActivity {

    boolean isConnected;
    BluetoothConnectionService btc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        btc = new BluetoothConnectionService();
        startService(new Intent(this, BluetoothConnectionService.class));
        new CarConnection(this).getType().observe(this, this::onConnectionStateUpdated);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button checkButton = findViewById(R.id.check_button);
        checkButton.setOnClickListener(this::onCheckButtonClicked);
    }

    private void onCheckButtonClicked(View view) {
        checkPermissions();
    }

    public void checkPermissions() {
        int rec = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int net = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int bt = 0;ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int btScan = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            btScan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        } else {
            bt = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        }
        int btConnect = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            btConnect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        }
        if ( rec != PackageManager.PERMISSION_GRANTED ||
                net != PackageManager.PERMISSION_GRANTED ||
                btScan != PackageManager.PERMISSION_GRANTED ||
                bt != PackageManager.PERMISSION_GRANTED ||
                btConnect != PackageManager.PERMISSION_GRANTED) {
            String[] codes = {Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.INTERNET,
                    Manifest.permission.BLUETOOTH};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    codes[3] = Manifest.permission.BLUETOOTH_SCAN;
                    codes[4] = Manifest.permission.BLUETOOTH_CONNECT;
            }
            requestPermissions(codes, 1);
        } else {
            Toast.makeText(this, R.string.permissions_ok, Toast.LENGTH_LONG).show();
        }
    }

    private void onConnectionStateUpdated(Integer connectionState) {
        String message;
        switch (connectionState) {
            case CarConnection.CONNECTION_TYPE_NOT_CONNECTED:
                message = "Not connected to a head unit";
                isConnected = false;
                break;
            case CarConnection.CONNECTION_TYPE_NATIVE:
                message = "Connected to Android Automotive OS";
                Toast.makeText(this, R.string.open_app_error, Toast.LENGTH_LONG).show();
             //   this.finish();
                isConnected = true;
                break;
            case CarConnection.CONNECTION_TYPE_PROJECTION:
                message = "Connected to Android Auto";
                Toast.makeText(this, R.string.open_app_error, Toast.LENGTH_LONG).show();
               //this.finish();
                break;
            default:
                message = "Unknown car connection type";
                isConnected = false;
                break;
        }
        Log.d("Connection", message);
    }

    @Override
    public void onResume() {
        if (isConnected) {
            Toast.makeText(this, R.string.open_app_error, Toast.LENGTH_LONG).show();
            //this.finish();
        }
        super.onResume();
    }


}