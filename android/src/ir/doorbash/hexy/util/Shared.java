package ir.doorbash.hexy.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Shared {
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    public static Shared getInstance(Context c) {
        return new Shared(c);
    }

    private Shared(Context c) {
        mSettings = c.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mSettings.edit();
    }

    public void commit() {
        mEditor.commit();
    }

    public Shared apply() {
        mEditor.apply();
        return this;
    }

    public boolean getBoolean(String key, boolean default_value) {
        return mSettings.getBoolean(key, default_value);

    }

    public Shared setBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        return this;
    }


    public int getInt(String key, int default_value) {

        return mSettings.getInt(key, default_value);

    }

    public Shared setInt(String key, int value) {
        mEditor.putInt(key, value);
        return this;
    }


    public long getLong(String key, long default_value) {
        return mSettings.getLong(key, default_value);

    }

    public Shared setLong(String key, long value) {
        mEditor.putLong(key, value);
        return this;

    }

    public float getFloat(String key, float default_value) {
        return mSettings.getFloat(key, default_value);
    }

    public Shared setFloat(String key, float value) {
        mEditor.putFloat(key, value);
        return this;
    }

    public String getString(String key, String default_value) {
        return mSettings.getString(key, default_value);
    }

    public Shared setString(String key, String value) {
        mEditor.putString(key, value);
        return this;
    }
}
