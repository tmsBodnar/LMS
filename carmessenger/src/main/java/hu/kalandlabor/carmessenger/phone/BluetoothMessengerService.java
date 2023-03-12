package hu.kalandlabor.carmessenger.phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * initialize the bluetooth connection with HC-05 device
 * and when the socket is OK, sends a message.
 */
public class BluetoothMessengerService {

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    BluetoothSocket clientSocket;
    OutputStream os;
    BluetoothConnectionService bluetoothConnectionService;
    SettingsActivity settingsActivity;

    public BluetoothMessengerService(BluetoothConnectionService bluetoothConnectionService, SettingsActivity activity) {
        this.bluetoothConnectionService = bluetoothConnectionService;
        this.settingsActivity = activity;
    }

    // check the socket, and sends a messege
    public void sendMessage(String textToSend) {
        if (clientSocket != null && os != null) {
            write(textToSend);
        } else {
            initBluetooth();
            sendMessage(textToSend);
        }
    }

    // initialize the connection, and sends a result in String
    public String initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return "NOT support bluetooth";
        } else if (!bluetoothAdapter.isEnabled()) {
            return "BLUETOOTH ERROR";
        } else {
            try {
                int bt = 0;ContextCompat.checkSelfPermission(this.settingsActivity, Manifest.permission.BLUETOOTH);
                int btScan = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    btScan = ContextCompat.checkSelfPermission(this.settingsActivity, Manifest.permission.BLUETOOTH_SCAN);
                } else {
                    bt = ContextCompat.checkSelfPermission(this.settingsActivity, Manifest.permission.BLUETOOTH);
                }
                int btConnect = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    btConnect = ContextCompat.checkSelfPermission(this.settingsActivity, Manifest.permission.BLUETOOTH_CONNECT);
                }
                if ( btScan != PackageManager.PERMISSION_GRANTED ||
                        bt != PackageManager.PERMISSION_GRANTED ||
                        btConnect != PackageManager.PERMISSION_GRANTED) {
                    String[] codes = {Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.INTERNET,
                            Manifest.permission.BLUETOOTH};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        codes[3] = Manifest.permission.BLUETOOTH_SCAN;
                        codes[4] = Manifest.permission.BLUETOOTH_CONNECT;
                    }
                    this.settingsActivity.requestPermissions(codes, 1);
                }
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    String addr = "";
                    for (BluetoothDevice dev : pairedDevices) {
                        String deviceName = dev.getName();
                        if (deviceName.equals("HC-05")) {
                            bluetoothAdapter.cancelDiscovery();
                            addr = dev.getAddress();
                            device = bluetoothAdapter.getRemoteDevice(addr);
                            try {
                                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                                clientSocket = (BluetoothSocket) m.invoke(device, 1);
                                clientSocket.connect();
                                os = clientSocket.getOutputStream();
                                return "OK";
                            } catch (Exception e) {
                                return "clientsocket Error";
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "BLUETOOTH ERROR";
            }
            return "NOK";
        }
    }

    // converts the string to byte array
    // (some special characters need to set to other),
    // because the device can not showing correctly
    // then creates a byte array and sends it with outputstream
    private void write(String textToSend) {
        byte[] bytes = new byte[textToSend.length()];
        try {
            for ( int i = 0;i < textToSend.length(); i++) {
                int temp = Character.codePointAt(textToSend.toCharArray(), i);
                switch (temp){
                    case 336: temp = 213; break;
                    case 337: temp = 245; break;
                    case 368: temp = 219; break;
                    case 369: temp = 251; break;
                    default: ;
                }
                bytes[i]= (byte)temp;
            }
            os.write(bytes);
        } catch (IOException e) {
            Log.d("xxx", "write exception " + e.getMessage());
        }
    }

    public BluetoothSocket getClientSocket(){
        return clientSocket;
    }
}
