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

package me.gfuil.bmap.lite;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;

import com.amap.api.maps.MapsInitializer;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.activity.MainActivity;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.utils.FileUtils;
import me.gfuil.bmap.lite.utils.LogUtils;

/**
 * @author gfuil
 */
public class BApp extends Application {

  public static  TypeMap        TYPE_MAP;
  public static  MyPoiModel     MY_LOCATION;
  private static List<Activity> activityList = new ArrayList<>();

  @Override
  public void onCreate() {
    super.onCreate();

    LogUtils.setDebug(BuildConfig.DEBUG);

    initMap();

    setNightMode();

  }

  public void setNightMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      ConfigInteracter configInteracter = new ConfigInteracter(this);
      if (configInteracter.getNightMode() == 2) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
      } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
      }
    }

  }

  /**
   * 初始化地图sdk
   */
  private void initMap() {
    ConfigInteracter configInteracter = new ConfigInteracter(this);
    TYPE_MAP = configInteracter.getTypeMap();
    String dirPath = configInteracter.getDirectory();

    try {
      SDKInitializer.initialize(dirPath, getApplicationContext());
    } catch (Exception e) {
      e.printStackTrace();
      SDKInitializer.initialize(getApplicationContext());
    }
    SDKInitializer.setCoordType(CoordType.GCJ02);

    try {
      MapsInitializer.initialize(getApplicationContext());
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    MapsInitializer.sdcardDir = dirPath + File.separator + "Amap";

    File dir = new File(getExternalFilesDir(""), "Theme");
    if (!dir.exists()) {
      dir.mkdir();
    }
    final File fileBaidu = new File(dir, "night_baidu.data");
    if (!fileBaidu.exists()) {
      FileUtils.writeFileToSDCard(fileBaidu, FileUtils.readFileFromAsset(this, "night_baidu.data"));
    }
    MapView.setCustomMapStylePath(fileBaidu.getPath());
    MapView.setMapCustomEnable(true);

  }


  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  public static void addActivity(Activity activity) {
    activityList.add(activity);
  }

  public static void removeActivity(Activity activity) {
    activityList.remove(activity);
  }

  public static List<Activity> getActivityList() {
    return activityList;
  }

  public static void exitApp() {
    for (int i = activityList.size() - 1; i >= 0; i--) {
      Activity activity = activityList.get(i);
      if (activity instanceof MainActivity) {
        MainActivity.isExit = true;
      }
      activity.finish();
    }

  }
}
