package com.kalandlabor.ledmessengerstrip.managers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * initialize the bluetooth connection with HC-05 device
 * and when the socket is OK, sends a message.
 */
public class BluetoothMessenger {

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    BluetoothSocket clientSocket;
    OutputStream os;

    public BluetoothMessenger(){
    }

    // check the socket, and sends a messege
    public void sendMessage(String textToSend) {
        if ( clientSocket!= null && os != null) {
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
    public void write(String textToSend) {
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
