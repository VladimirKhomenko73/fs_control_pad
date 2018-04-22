package com.yarolegovich.planesimulatormanager;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by yarolegovich on 23.05.2015.
 * Класс - экран настроек
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /*
     * Ключи, под которыми приложение хранит наши настройки
     */
    public static final String SERVER_IP = "pref_key_server_ip";
    public static final String SERVER_PORT = "pref_key_server_port";

    public static final String SOURCE_NAME = "pref_key_source_name";
    public static final String DEST_NAME = "pref_key_dest_name";

    public static final String DEVICE_IP = "pref_key_device_ip";
    public static final String DEVICE_PORT = "pref_key_device_port";

    /*
     * Практически все операции в этом классе проводятся из необходимости совместимости с ранними
     * версиями Android (планшет работает на  Gingerbread 2.3.3). Для этого используется класс
     * AppCompatDelegate библиотеки AppCompat, объекту которого мы делигируем вызовы.
     * В приложениях, не ориентированных на поддержку таких ранних версий ОС следовало бы использовать
     * PreferenceFragment класс вместо PreferenceActivity
     */
    private AppCompatDelegate mAppCompatDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkPrimaryColor));
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setSummary(prefs, SERVER_IP);
        setSummary(prefs, SERVER_PORT);
        setSummary(prefs, SOURCE_NAME);
        setSummary(prefs, DEST_NAME);
        setSummary(prefs, DEVICE_PORT);

        //Определяем IP адрес устройства в текущей сети
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        findPreference(DEVICE_IP).setSummary(ip);
    }

    private void setSummary(SharedPreferences prefs, String key) {
        Preference preference = findPreference(key);
        preference.setSummary(prefs.getString(key, ""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    /*
     * При изменении какой-то из настроек обновлеям её описание
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(sharedPreferences, key);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    @Override
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        return mAppCompatDelegate != null ? mAppCompatDelegate
                : (mAppCompatDelegate = AppCompatDelegate.create(this, null));
    }

}
