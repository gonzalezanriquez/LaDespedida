package com.example.ladespedida;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends sinBarraSuperior {
    private BluetoothGatt bluetoothGatt;  // Almacena la instancia de BluetoothGatt para la conexión BLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToESP32();  // Método para conectar con el ESP32
            }
        });
    }

    private void connectToESP32() {
        // Verifica si el dispositivo admite BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // El dispositivo no admite BLE
            // Puedes manejar esto según tus necesidades
            return;
        }

        // Solicita permisos de Bluetooth
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        // Inicializa el adaptador Bluetooth
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Verifica si el Bluetooth está habilitado
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
            return;
        }

        // Escanea y encuentra el dispositivo ESP32 por su nombre o dirección MAC
        BluetoothDevice esp32Device = bluetoothAdapter.getRemoteDevice("Nombre del dispositivo o dirección MAC");

        // Conecta con el dispositivo
        bluetoothGatt = esp32Device.connectGatt(this, false, new BluetoothGattCallback() {
            // Implementa los métodos callback para manejar la conexión y la comunicación
        });
    }
}
