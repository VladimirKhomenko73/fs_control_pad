package com.yarolegovich.planesimulatormanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by yarolegovich on 22.05.2015.
 * Класс, представляющий UI главного окна проекта
 */
public class ControlFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = ControlFragment.class.getSimpleName();

    private static final String LOG_TEXT = "saved_log";

    /*
     * Объект, принимающий обработанные сервисом сообщения и отображающий их в лог
     */
    private BroadcastReceiver mReceiver;

    private TextView mLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new ServiceMessagesReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control_panel, container, false);

        //Присваиваем каждой кнопке тэг-команду и назначаем класс ControlFragment слушателем нажатий
        initButton(v.findViewById(R.id.fly_command), "start");
        initButton(v.findViewById(R.id.pause_command), "pause");
        initButton(v.findViewById(R.id.stop_command), "stop");
        initButton(v.findViewById(R.id.light_command), "light");
        initButton(v.findViewById(R.id.fog_command), "fog");
        initButton(v.findViewById(R.id.volup_command), "volup");
        initButton(v.findViewById(R.id.voldwn_command), "voldwn");
        initButton(v.findViewById(R.id.test_command), "test");
        initButton(v.findViewById(R.id.off_command), "off");

        mLog = (TextView) v.findViewById(R.id.log);

        //Если у нас есть сохраненные записи лога, то восстанавливаем их
        if (savedInstanceState != null) {
            mLog.setText(savedInstanceState.getString(LOG_TEXT));
        }

        return v;
    }

    private void initButton(View view, String tag) {
        view.setOnClickListener(this);
        view.setTag(tag);
    }

    /*
     * Метод жизненного цикла. Вызывается при появлении интерфейса на экране. Регистрируем слушателя,
     * который будет отображать сообщения в лог
     */
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(NetworkingService.MESSAGE_RECEIVED));
    }

    /*
     * Метод жизненного цикла. Вызывается, когда интерфейс уходит с экрана.
     */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    /*
     * При смене ориентации экрана (или при долгом отсутствии на экране) наше View уничтожается
     * и все элементы рисуются заново.
     * Чтобы не потерять записи лога, мы сохраняем их в Bundle и извлекаем позже в методе onCreateView
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOG_TEXT, mLog.getText().toString());
    }

    /*
     * Слушатель нажатий на кнопки. Извлекам присвоенный кнопке тэг и отправляем его сервису
     */
    @Override
    public void onClick(View view) {
        playSound();
        String command = (String) view.getTag();
        Intent intent = new Intent(NetworkingService.SEND_MESSAGE);
        intent.putExtra(NetworkingService.MESSAGE_EXTRA, command);
        getActivity().sendBroadcast(intent);
    }

    private void playSound() {
        final MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.button_click);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.release();
            }
        });
        mp.start();
    }

    /*
     * Класс-наследник BroadcastReceiver, получает сообщения от сервиса и добавляет их в лог
     */
    private class ServiceMessagesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NetworkingService.MESSAGE_RECEIVED:
                    playNotificationSound();
                    String message = intent.getStringExtra(NetworkingService.MESSAGE_EXTRA);
                    mLog.setText(message + "\n" + mLog.getText());
                    break;
            }
        }

        private void playNotificationSound() {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
            r.play();
        }
    }

}
