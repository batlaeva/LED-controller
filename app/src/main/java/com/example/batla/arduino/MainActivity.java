package com.example.batla.arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button buttonDevices;
    ColorPicker colorPicker;
    Switch ledOnOff;
//    LedsView ledsView;
//    SeekBar seekBar;

    BluetoothAdapter bluetoothAdapter;
    ThreadConnectBTdevice myThreadConnectBTdevice; //поток для соединения
    static ThreadConnected myThreadConnected; //поток для данных

    private static StringBuilder sb = new StringBuilder();
    private UUID myUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonDevices = (Button) findViewById(R.id.buttonDevices);
        colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        ledOnOff = (Switch) findViewById(R.id.switch1);
//        ledsView = (LedsView) findViewById(R.id.ledsView);
//        seekBar = (SeekBar) findViewById(R.id.seekBar);

        ledOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ledOnOff.setText("LED OFF");
                    myThreadConnected.write("ON\r".getBytes());
                } else {
                    ledOnOff.setText("LED ON");
                    myThreadConnected.write("OFF\n".getBytes());
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myThreadConnectBTdevice!=null) myThreadConnectBTdevice.cancel();
    }

    public void onClickDevices(View view) {
        Intent intent = new Intent(this, Devices.class);
        startActivityForResult(intent, 1);
    }

    //рисвание lED
    public void onClickCreateLed(View view) throws UnsupportedEncodingException {
//        ledsView.drawLed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
                myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                //возвращаем MAC подключаемого устройства
                String MAC = data.getStringExtra("device2MAC");
                Log.i("mytag","mac="+MAC);
                BluetoothDevice device2 = bluetoothAdapter.getRemoteDevice(MAC);
                try {
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myThreadConnectBTdevice.start();  // Запускаем поток для подключения Bluetooth
            }
        }
    }

    public class ThreadConnectBTdevice extends Thread {
        // Поток для коннекта с Bluetooth
        private BluetoothSocket bluetoothSocket = null;

        private ThreadConnectBTdevice(BluetoothDevice device) throws IOException {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() { // Коннект
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            }
            catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Не получается подключиться!", Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (success) {  // Если законнектились, тогда запускаем поток приёма и отправки данных
                Log.i("mytag","success connect");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Подключено", Toast.LENGTH_LONG).show();
                    }
                });
                myThreadConnected = new ThreadConnected(bluetoothSocket);
                myThreadConnected.start(); // запуск потока приёма и отправки данных
            }
        }

        public void cancel() {
            Toast.makeText(getApplicationContext(), "Close - BluetoothSocket", Toast.LENGTH_LONG).show();
            try {
                bluetoothSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    } // END ThreadConnectBTdevice:



    public static class ThreadConnected extends Thread {    // Поток - приём и отправка данных

        private InputStream connectedInputStream;
        private OutputStream connectedOutputStream;

        private String sbprint;

        public ThreadConnected(BluetoothSocket socket) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() { // Приём данных
            while (true) {
                try {
                    byte[] buffer = new byte[1];
                    int bytes = connectedInputStream.read(buffer);
                    String strIncom = new String(buffer, 0, bytes);
                    sb.append(strIncom); // собираем символы в строку
                    int endOfLineIndex = sb.indexOf("\r\n"); // определяем конец строки
//                    Log.i("mytag", "READING...");

                    if (endOfLineIndex > 0) {
                        sbprint = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());
                        Log.i("mytag", sbprint);

//                        runOnUiThread(new Runnable() { // Вывод данных
//                            @Override
//                            public void run() {
//
//                                }
//                            }
//                        });
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

