package com.yarolegovich.planesimulatormanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

/**
 * Created by yarolegovich on 22.05.2015.
 * Сервис, работаюший в background режиме и обеспечиваюший общений с сервером.
 */
public class NetworkingService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = NetworkingService.class.getSimpleName();

    public static final String MESSAGE_RECEIVED = "com.yaroleogovich.planesimulator.NEW_MESSAGE";
    public static final String MESSAGE_EXTRA = "com.yaroleogovich.planesimulator.SERVER_MESSAGE";
    public static final String SEND_MESSAGE = "com.yaroleogovich.planesimulator.SEND_TO_SERVER";

    //Счетчик полетов, добавляется в head отправляемого пакета
    private static int mFlyCount;

    //Переменная, хранящая информацию о том, работает ли сейчас сервис
    public static boolean isRunning;

    private String mHostIP;
    private int mServerPort;
    private int mDevicePort;
    private DatagramSocket mSocket;

    //Объекты, обеспечиваюшие двустороннее общение с сервером
    private Thread mServerListener;
    private BroadcastReceiver mMessageSender;

    /*
     * Метод должен быть переопределен, но в данном контексте не используется нами, поэтому - null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * Метод жизненного цикла, вызывается при запуске сервиса один раз. В нем извлекаем из настроек
     * IP сервера и порт, на котором он слушает и создаем сокет.
     * Далее запускаем mServerListener поток, который будет принимать сообщения от сервера и
     * регистрируем mMessageSender (он принимает broadcast из окна нашего приложения, отправляемый
     * при нажатии кнопки).
     */
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mHostIP = prefs.getString(SettingsActivity.SERVER_IP, "");
        mServerPort = Integer.parseInt(prefs.getString(SettingsActivity.SERVER_PORT, "9842"));
        mDevicePort = Integer.parseInt(prefs.getString(SettingsActivity.DEVICE_PORT, "9842"));
        openConnection();
        mMessageSender = new MessageSender();
        registerReceiver(mMessageSender, new IntentFilter(SEND_MESSAGE));
        isRunning = true;
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    /*
     * Метод жизненного цикла, вызывается при уничтожении сервиса. Закрываем сокет и прерываем поток
     */
    @Override
    public void onDestroy() {
        closeConnection();
        unregisterReceiver(mMessageSender);
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        isRunning = false;
        super.onDestroy();
    }

    private void openConnection() {
        try {
            mSocket = new DatagramSocket(mDevicePort);
            mServerListener = new Thread(new ServerListener());
            mServerListener.start();
            new Thread(new SendCommandTask("test")).start();
        } catch (SocketException e) {
            Log.e(LOG_TAG, "Socket creation error: " + e.getMessage());
            sendMessageToLog("Ошибка при открытии сокета: " + e.getMessage());
        }
    }

    private void sendMessageToLog(String message) {
        Intent intent = new Intent(MESSAGE_RECEIVED);
        intent.putExtra(MESSAGE_EXTRA, message);
        sendBroadcast(intent);
    }

    private void closeConnection() {
        if (mSocket != null) {
            mSocket.close();
            mServerListener.interrupt();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        startService(new Intent(this, NetworkingService.class));
        stopSelf();
    }

    /*
     * Runnable, который будет исполняться в отдельном потоке. mSocket.receive(byte[] arr) -
     * блокирующий вызов, ожидающий пакет от сервера. При получении парсим сообщение. Если пакет
     * не удовлетворяет требования (неверная структура или получатель/отправитель), то
     * parseMessage() вернет пустую строку. Если вернется не пустая строка, значит сообщение валидно
     * и оно будет передано на экран приложения
     */
    private class ServerListener implements Runnable {

        @Override
        public void run() {
            byte[] receivedData = new byte[576];
            try {
                InetAddress ip = InetAddress.getByName(mHostIP);
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length, ip, mServerPort);
                    mSocket.receive(packet);
                    String receivedMessage = new String(packet.getData(), Charset.forName("cp866"));
                    String parsedMessage = JsonUtil.with(NetworkingService.this).parseMessage(receivedMessage);
                    if (parsedMessage != null && !parsedMessage.equals("")) {
                        sendMessageToLog(parsedMessage);
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while receiving data: " + e.getMessage());
            }
        }
    }

    /*
     * Класс для получения broadcast-ов от главного экрана, отправляемых при нажатии на кнопки
     * Network операции нельзя проводить в главном потоке, поэтому запускаем новый поток для отправки
     * команды на сервер.
     * Если команда "start", то наращиваем счетчик полетов
     */
    private class MessageSender extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MESSAGE_EXTRA);
            if (message.equals("start")) {
                mFlyCount++;
            }
            new Thread(new SendCommandTask(message)).start();
        }
    }

    /*
     * Runnable, отправляюший сообщения на сервер.
     */
    private class SendCommandTask implements Runnable {

        private String mCommand;

        public SendCommandTask(String command) {
            mCommand = command;
        }

        @Override
        public void run() {
            try {
                String jsonMessage = JsonUtil.with(NetworkingService.this).composeMessage(mFlyCount, mCommand);
                byte[] data = jsonMessage.getBytes("UTF-8");
                InetAddress ip = InetAddress.getByName(mHostIP);
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, mServerPort);
                mSocket.send(packet);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while sending message: " + e.getMessage());
            }
        }
    }
}
