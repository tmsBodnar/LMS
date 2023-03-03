package hu.kalandlabor.carmessenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.connection.CarConnection;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new CarConnection(this).getType().observe(this, this::onConnectionStateUpdated);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button checkButton = findViewById(R.id.check_button);
        checkButton.setOnClickListener(this::ocCheckButtonClicked);
    }

    private void ocCheckButtonClicked(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) !=
                        PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission_group.MICROPHONE) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]
                    {android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.INTERNET,
                            Manifest.permission_group.MICROPHONE}, 1);
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
                this.finish();
                isConnected = true;
                break;
            case CarConnection.CONNECTION_TYPE_PROJECTION:
                message = "Connected to Android Auto";
                Toast.makeText(this, R.string.open_app_error, Toast.LENGTH_LONG).show();
                this.finish();
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
            this.finish();
        }
        super.onResume();
    }


}