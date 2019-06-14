package com.example.batla.arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class Devices extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> pairedDeviceArrayList;
    ArrayAdapter<String> pairedDeviceAdapter;
    ListView listViewPairedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        listViewPairedDevice = (ListView) findViewById(R.id.listview);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this hardware platform", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Devices.this, MainActivity.class);
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
            return;
        }
    } // END onCreate


    @Override
    protected void onStart() { // Запрос на включение Bluetooth
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        setup();
    }

    private void setup() {
        // Создание списка сопряжённых Bluetooth-устройств
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) { // Если есть сопряжённые устройства
            pairedDeviceArrayList = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) { // Добавляем сопряжённые устройства - Имя + MAC-адресс
                pairedDeviceArrayList.add(device.getName() + "\n" + device.getAddress());
            }

            //цвет текста в listview
            pairedDeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDeviceArrayList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    tv.setTextColor(Color.WHITE);
                    return view;
                }
            };

            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() { // Клик по нужному устройству
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listViewPairedDevice.setVisibility(View.GONE); // После клика скрываем список
                    String itemValue = (String) listViewPairedDevice.getItemAtPosition(position);
                    String MAC = itemValue.substring(itemValue.length() - 17); // MAC-адрес

                    //передаем MAC в MainActivity для подключения
                    Intent intent = new Intent(Devices.this, MainActivity.class);
                    intent.putExtra("device2MAC", MAC);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) { // Если разрешили включить Bluetooth, тогда void setup()
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else { // Если не разрешили, тогда закрываем activity
                Toast.makeText(this, "Bluetooth не включён", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Devices.this, MainActivity.class);
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        }
    }
}
