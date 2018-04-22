package com.yarolegovich.planesimulatormanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.MalformedJsonException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.zip.GZIPOutputStream;

/**
 * Created by yarolegovich on 24.05.2015.
 */
public class JsonUtil {

    private static final String LOG_TAG = JsonUtil.class.getSimpleName();

    /*
     * Контекст приложения - позволит нам обратиться к указанным в настройках именам отправителя
     * и получателя, чтобы проверить валидность пакета.
     */
    private Context mContext;

    public static JsonUtil with(Context context) {
        return new JsonUtil(context);
    }

    private JsonUtil(Context context) {
        mContext = context;
    }

    /*
     * Конструируем пакет для отправки и преобразовываем его в JSON-формат средствами библиотеки Gson
     * SharedPreferences - сохраненные настройки
     */
    public String composeMessage(int flyNumber, String command) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Package pac = new Package.PackageBuilder()
                .source(sharedPreferences.getString(SettingsActivity.SOURCE_NAME, ""))
                .destination(sharedPreferences.getString(SettingsActivity.DEST_NAME, ""))
                .flyNumber(flyNumber)
                .command(command)
                .build();
        return new Gson().toJson(pac);
    }

    /*
     * Парсим сообщение, используя JsonReader с флагом Lenitent, что позволяет нам извлечь JSON из
     * сроки, содержащей так же другие символы (в нашем случае пакет может не содержать 576 байт
     * информации, и конструктор String(byte[] arr) вернет нам строку, дополненную
     * ненужными символами)
     */
    public String parseMessage(String message) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String source = sharedPreferences.getString(SettingsActivity.DEST_NAME, "");
            String destination = sharedPreferences.getString(SettingsActivity.SOURCE_NAME, "");
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(message));
            reader.setLenient(true);
            Package pac = gson.fromJson(reader, Package.class);
            return pac.isValid(source, destination) ? pac.getBody().getMes() : "";
        } catch (Exception e) {
            Log.e(LOG_TAG, "Probably malformed package: " + e.getMessage());
        }
        return "";
    }
}
