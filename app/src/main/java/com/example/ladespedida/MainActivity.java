package com.example.ladespedida;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private TextView sensorValueTextView, timerTextView;

    private Handler handler = new Handler();
    private StringBuilder data = new StringBuilder();
    private boolean timerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorValueTextView = findViewById(R.id.sensorValueTextView);
        timerTextView = findViewById(R.id.timerTextView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            return;
        }

        // Dirección MAC del módulo Bluetooth
        String address = "00:00:00:00:00:00"; // Reemplazar con la dirección del ESP32/ESP8266

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            socket = device.createRfcommSocketToServiceRecord(uuid);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            socket.connect();
            inputStream = socket.getInputStream();

            listenToData();
            startTimer(); // Comienza el temporizador al conectarse exitosamente
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToData() {
        final byte delimiter = 10; // El carácter '\n' es el delimitador

        Thread workerThread = new Thread(new Runnable() {
            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);

                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String receivedData = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            processData(receivedData.trim());
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        workerThread.start();
    }

    private void processData(String receivedData) {
        if (timerRunning) {
            data.append(receivedData).append("\n");
        } else {
            // Mostrar el último valor después de que el temporizador haya expirado
            sensorValueTextView.setText("Último valor: " + receivedData);
        }
    }

    private void startTimer() {
        timerRunning = true;

        new CountDownTimer(10000, 1000) { // Temporizador de 10 segundos (10000 ms) con actualización cada segundo (1000 ms)
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Tiempo restante: " + millisUntilFinished / 1000 + " segundos");
            }

            public void onFinish() {
                timerRunning = false;
                // Mostrar el último valor después de que el temporizador haya expirado
                if (data.length() > 0) {
                    String lastValue = data.toString().trim().split("\n")[data.toString().trim().split("\n").length - 1];
                    sensorValueTextView.setText("Último valor después del tiempo: " + lastValue);
                }
                timerTextView.setText("Tiempo terminado");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close(); // Cierra la conexión al destruir la actividad
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
