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

package me.gfuil.bmap.lite.interacter;

import android.content.Context;

import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.utils.PreferenceUtils;

/**
 * @author gfuil
 */

public class ConfigInteracter {
    private Context mContext;
    PreferenceUtils preferenceUtils;

    public ConfigInteracter(Context context) {
        this.mContext = context;
        preferenceUtils = new PreferenceUtils(context, "config");
    }

    public void setTypeMap(TypeMap type) {
        preferenceUtils.setIntPreference("type_map", type.getInt());
    }

    public TypeMap getTypeMap() {
        return TypeMap.fromInt(preferenceUtils.getIntPreference("type_map", 0));
    }

    public void setScreenLightAlways(boolean isAlways) {
        preferenceUtils.setBooleanPreference("screenLightAlways", isAlways);
    }

    public boolean isScreenLightAlways() {
        return preferenceUtils.getBooleanPreference("screenLightAlways", false);
    }

    public void setTrafficEnable(boolean enabled) {
        preferenceUtils.setBooleanPreference("traffic", enabled);
    }

    public boolean isTrafficEnable() {
        return preferenceUtils.getBooleanPreference("traffic", true);
    }

    public void setZoomGesturesEnabled(boolean enabled) {
        preferenceUtils.setBooleanPreference("zoom", enabled);
    }

    public boolean isZoomGesturesEnabled() {
        return preferenceUtils.getBooleanPreference("zoom", true);
    }

    public void setRotateEnable(boolean enabled) {
        preferenceUtils.setBooleanPreference("rotate", enabled);
    }

    public boolean isRotateEnable() {
        return preferenceUtils.getBooleanPreference("rotate", true);
    }

    public void setOverlookEnable(boolean enabled) {
        preferenceUtils.setBooleanPreference("overlook", enabled);
    }

    public boolean isOverlookEnable() {
        return preferenceUtils.getBooleanPreference("overlook", true);
    }

    public void setMapPoiEnable(boolean enabled) {
        preferenceUtils.setBooleanPreference("poi", enabled);
    }

    public boolean isMapPoiEnable() {
        return preferenceUtils.getBooleanPreference("poi", true);
    }

    public void setShowScaleControl(boolean enabled) {
        preferenceUtils.setBooleanPreference("scale", enabled);
    }

    public boolean isShowScaleControl() {
        return preferenceUtils.getBooleanPreference("scale", true);
    }

    public void setZoomControlsPosition(boolean isRight) {
        preferenceUtils.setBooleanPreference("control", isRight);
    }

    public boolean getZoomControlsPosition() {
        return preferenceUtils.getBooleanPreference("control", true);
    }

    public void setLocationPosition(boolean isRight) {
        preferenceUtils.setBooleanPreference("location", isRight);
    }

    public boolean getLocationPosition() {
        return preferenceUtils.getBooleanPreference("location", false);
    }

    public void setDirectory(String path) {
        preferenceUtils.setStringPreference("directory", path);
    }

    public String getDirectory() {
        return preferenceUtils.getStringPreference("directory", mContext.getExternalFilesDir("").getPath());
    }

    public void setSearchNearby(boolean enabled) {
        preferenceUtils.setBooleanPreference("search_nearby", enabled);
    }

    public boolean isSearchNearby() {
        return preferenceUtils.getBooleanPreference("search_nearby", false);
    }


    public void setNightMode(int mode) {
        preferenceUtils.setIntPreference("nightMode", mode);
    }

    public int getNightMode() {
        return preferenceUtils.getIntPreference("nightMode", 0);
    }

    public boolean isNeverHint() {
        return preferenceUtils.getBooleanPreference("never_hint", false);
    }

    public void setNeverHint(boolean b) {
        preferenceUtils.setBooleanPreference("never_hint", b);
    }

    public boolean isNeverHintBaidu() {
        return preferenceUtils.getBooleanPreference("never_hint_baidu", false);
    }

    public void setNeverHintBaidu(boolean b) {
        preferenceUtils.setBooleanPreference("never_hint_baidu", b);
    }

    public int getTypeRoute() {
        return preferenceUtils.getIntPreference("type_route", 0);
    }

    public void setTypeRoute(int index) {
        preferenceUtils.setIntPreference("type_route", index);
    }
}
