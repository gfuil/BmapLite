/*
 * This file is part of the BmapLite.
 * Copyright (C) 2019 gfuil 刘风广 <3021702005@qq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.gfuil.bmap.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 读写SharedPreferences
 *
 * @author gfuil
 */
public class PreferenceUtils {
    private String file = "default";
    private SharedPreferences preference;

    public PreferenceUtils(Context context) {
        this(context, null);
    }

    public PreferenceUtils(Context context, String preferenceFile) {
        if (preferenceFile != null)
            file = preferenceFile;
        preference = context.getApplicationContext().getSharedPreferences(file, 0);
    }

    public String getPreferenceFile() {
        return file;
    }

    public void clear(){
        Editor ed = preference.edit();
        ed.clear();
        ed.apply();
    }

    public void setStringPreference(String key, String value) {
        Editor ed = preference.edit();
        ed.putString(key, value);
        ed.apply();
    }

    public String getStringPreference(String key, String defaultValue) {
        String str = defaultValue;
        if (preference.contains(key)) {
            str = preference.getString(key, defaultValue);
        }
        return str;
    }

    public void setIntPreference(String key, int value) {
        Editor ed = preference.edit();
        ed.putInt(key, value);
        ed.apply();

    }

    public int getIntPreference(String key, int defaultValue) {
        int num = defaultValue;
        if (preference.contains(key)) {
            num = preference.getInt(key, defaultValue);
        }
        return num;
    }

    public void setBooleanPreference(String key, boolean value) {
        Editor ed = preference.edit();
        ed.putBoolean(key, value);
        ed.apply();

    }

    public boolean getBooleanPreference(String key, boolean defaultValue) {
        boolean is = defaultValue;
        if (preference.contains(key)) {
            is = preference.getBoolean(key, defaultValue);
        }
        return is;
    }

    public void setFloatPreference(String key, float value) {
        Editor ed = preference.edit();
        ed.putFloat(key, value);
        ed.apply();

    }

    public float getFloatPreference(String key, float defaultValue) {
        float val = defaultValue;
        if (preference.contains(key)) {
            val = preference.getFloat(key, defaultValue);
        }
        return val;
    }

    public void setLongPreference(String key, long value) {
        Editor ed = preference.edit();
        ed.putLong(key, value);
        ed.apply();
    }

    public long getLongPreference(String key, long defaultValue) {
        long val = defaultValue;
        if (preference.contains(key)) {
            val = preference.getLong(key, defaultValue);
        }

        return val;
    }

}
