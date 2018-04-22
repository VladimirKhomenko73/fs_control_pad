package com.yarolegovich.planesimulatormanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yarolegovich on 28.05.2015.
 * Класс необходим для приема события ACTION_BOOT_COMPLETED, возникающего при включении телефона.
 * Когда событие зарегистрировано - класс запускает главное окно приложения
 */
public class ApplicationBootLauncher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
