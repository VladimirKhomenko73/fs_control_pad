package com.yarolegovich.planesimulatormanager;


import android.content.Context;
import android.content.Intent;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by yarolegovich on 22.05.2015.
 * Главный экран нашего приложения
 */
public class MainActivity extends ActionBarActivity {

    /*
     * Метод жизненного цикла класса Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container);
        if (fragment == null) {
            fragment = new ControlFragment();
            fm.beginTransaction().replace(R.id.container, fragment).commit();
        }
        startService(new Intent(this, NetworkingService.class));
    }

    /*
     * Получаем событие - нажатие кнопки меню. Здесь отслеживаем подключение, отключение и вход
     * в настройке. Выполняем соответствующие действия
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.reconnect:
                intent = new Intent(this, NetworkingService.class);
                if (NetworkingService.isRunning) {
                    stopService(intent);
                }
                startService(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Метод жизненного цикла, выключаем сервис
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, NetworkingService.class));
    }

    /*
     * Метод, вызываемый при создании меню. Выполняем стандартные действия и в зависимости от статуса
     * сервиса - делаем одну из кнопок Подключиться/Отключиться невидимой.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
