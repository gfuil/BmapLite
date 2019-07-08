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

package me.gfuil.bmap.lite.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.utils.AppUtils;

/**
 * 设置
 *
 * @author gfuil
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {
    private SwitchCompat mSwitchTraffic, mSwitchZoom, mSwitchRotate, mSwitchOverlook, mSwitchScale, mSwitchPoi;
    private TextView mTextMap, mTextDir, mTextRoute;
    private CheckBox mCheckScreenLight;
    private Spinner mSpinnerZoom, mSpinnerLocation;
    private RadioGroup mGroupZoom;
    private RadioGroup mGroupMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_setting);

        getData();
    }

    private void getData() {
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        mCheckScreenLight.setChecked(configInteracter.isScreenLightAlways());
        mSwitchTraffic.setChecked(configInteracter.isTrafficEnable());
        mSwitchZoom.setChecked(configInteracter.isZoomGesturesEnabled());
        mSwitchRotate.setChecked(configInteracter.isRotateEnable());
        mSwitchOverlook.setChecked(configInteracter.isOverlookEnable());
        mSwitchScale.setChecked(configInteracter.isShowScaleControl());
        mSwitchPoi.setChecked(configInteracter.isMapPoiEnable());
        mTextMap.setText(BApp.TYPE_MAP == TypeMap.TYPE_BAIDU ? "百度地图" : "高德地图");
        mTextDir.setText(configInteracter.getDirectory());
        mTextRoute.setText(getResources().getStringArray(R.array.type_route)[configInteracter.getTypeRoute()]);
//        mSpinnerZoom.setSelection(configInteracter.getZoomControlsPosition() ? 0 : 1);
//        mSpinnerLocation.setSelection(configInteracter.getLocationPosition() ? 0 : 1);


        if (configInteracter.getZoomControlsPosition()) {
            mGroupZoom.check(R.id.radio_zoom_right);
        } else {
            mGroupZoom.check(R.id.radio_zoom_left);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (configInteracter.getNightMode() == 2) {
                mGroupMode.check(R.id.radio_black);
            } else {
                mGroupMode.check(R.id.radio_white);
            }
        }
    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getView(R.id.lay_app_details).setOnClickListener(this);
        getView(R.id.lay_setting_map).setOnClickListener(this);

        mCheckScreenLight = getView(R.id.check_screen_light);
        mSwitchTraffic = getView(R.id.switch_traffic);
        mSwitchZoom = getView(R.id.switch_zoom);
        mSwitchRotate = getView(R.id.switch_rotate);
        mSwitchOverlook = getView(R.id.switch_overlook);
        mSwitchScale = getView(R.id.switch_scale);
        mSwitchPoi = getView(R.id.switch_poi);
        mSpinnerZoom = getView(R.id.spinner_zoom);
        mSpinnerLocation = getView(R.id.spinner_location);
        mGroupZoom = getView(R.id.group_zoom);
        mTextMap = getView(R.id.text_map);
        mTextDir = getView(R.id.text_dir);
        mTextRoute = getView(R.id.text_route);
        mGroupMode = getView(R.id.group_mode);

        mCheckScreenLight.setOnCheckedChangeListener(this);
        mSwitchTraffic.setOnCheckedChangeListener(this);
        mSwitchZoom.setOnCheckedChangeListener(this);
        mSwitchRotate.setOnCheckedChangeListener(this);
        mSwitchOverlook.setOnCheckedChangeListener(this);
        mSwitchScale.setOnCheckedChangeListener(this);
        mSwitchPoi.setOnCheckedChangeListener(this);
        mGroupZoom.setOnCheckedChangeListener(this);
        mGroupMode.setOnCheckedChangeListener(this);

        getView(R.id.lay_screen_light).setOnClickListener(this);
        getView(R.id.lay_traffic).setOnClickListener(this);
        getView(R.id.lay_zoom).setOnClickListener(this);
        getView(R.id.lay_rotate).setOnClickListener(this);
        getView(R.id.lay_overlook).setOnClickListener(this);
        getView(R.id.lay_about).setOnClickListener(this);
        getView(R.id.lay_setting_dir).setOnClickListener(this);
        getView(R.id.lay_setting_default_route).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getView(R.id.lay_mode).setVisibility(View.VISIBLE);
        } else {
            getView(R.id.lay_mode).setVisibility(View.GONE);
        }
//        mSpinnerZoom.setOnItemSelectedListener(this);
//        mSpinnerLocation.setOnItemSelectedListener(this);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, getResources().getStringArray(R.array.zoom_control));
//        mSpinnerZoom.setAdapter(adapter);
//        mSpinnerLocation.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lay_app_details:
                try {
                    AppUtils.openApplicationDetails(this, getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                    onMessage("请自行到 设置 > 应用程序 > Bamp 查看");
                }
                break;
            case R.id.lay_setting_map:
                chooseMap();
                break;
            case R.id.lay_screen_light:
                mCheckScreenLight.setChecked(!mCheckScreenLight.isChecked());
                break;
            case R.id.lay_traffic:
                mSwitchTraffic.setChecked(!mSwitchTraffic.isChecked());
                break;
            case R.id.lay_zoom:
                mSwitchZoom.setChecked(!mSwitchZoom.isChecked());
                break;
            case R.id.lay_rotate:
                mSwitchRotate.setChecked(!mSwitchRotate.isChecked());
                break;
            case R.id.lay_overlook:
                mSwitchOverlook.setChecked(!mSwitchOverlook.isChecked());
                break;
            case R.id.lay_about:
                openActivity(AboutActivity.class);
                break;
            case R.id.lay_setting_dir:
                try {
                    chooseDir();
                } catch (Exception e) {
                    e.printStackTrace();
                    onMessage("抱歉，您的手机不支持存储目录设置");
                }
                break;
            case R.id.lay_setting_default_route:
                chooseRoute();
                break;
        }
    }

    private void chooseRoute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("默认出行方式");
        builder.setItems(R.array.type_route, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ConfigInteracter(SettingActivity.this).setTypeRoute(which);
                mTextRoute.setText(getResources().getStringArray(R.array.type_route)[which]);
            }
        });

        builder.create().show();
    }

    private int mIndexDir = 0;

    private void chooseDir() {
        File[] dirs;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            dirs = getExternalFilesDirs("");
        } else {
            dirs = new File[]{getExternalFilesDir("")};
        }

        final ConfigInteracter configInteracter = new ConfigInteracter(this);
        String dirPath = configInteracter.getDirectory();

        List<CharSequence> list = new ArrayList<>();
        for (int i = 0; i < dirs.length; i++) {
            File f = dirs[i];
            if (null == f) {
                break;
            }
            list.add(f.getPath());
            if (f.getPath().equals(dirPath)) {
                mIndexDir = i;
            }
        }

        final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("存储位置");
        builder.setSingleChoiceItems(charSequences, mIndexDir, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIndexDir = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                configInteracter.setDirectory(charSequences[mIndexDir].toString());

                showAlertDialog("提示", "重启后才生效。是否退出？\n退出后请自行启动APP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BApp.exitApp();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            }
        });
        builder.setNegativeButton("取消", null);

        builder.create().show();
    }

    int mIndex = BApp.TYPE_MAP.getInt();

    private void chooseMap() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("基础地图");
        builder.setSingleChoiceItems(R.array.type_map, mIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIndex = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mIndex != BApp.TYPE_MAP.getInt()) {

                    BApp.MY_LOCATION = null;
                    BApp.TYPE_MAP = TypeMap.fromInt(mIndex);
                    ConfigInteracter configInteracter = new ConfigInteracter(SettingActivity.this);
                    configInteracter.setTypeMap(BApp.TYPE_MAP);

                    BApp.exitApp();
                    openActivity(MainActivity.class);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        switch (buttonView.getId()) {
            case R.id.check_screen_light:
                configInteracter.setScreenLightAlways(isChecked);
                break;
            case R.id.switch_traffic:
                configInteracter.setTrafficEnable(isChecked);
                break;
            case R.id.switch_zoom:
                configInteracter.setZoomGesturesEnabled(isChecked);
                break;
            case R.id.switch_rotate:
                configInteracter.setRotateEnable(isChecked);
                break;
            case R.id.switch_overlook:
                configInteracter.setOverlookEnable(isChecked);
                break;
            case R.id.switch_scale:
                configInteracter.setShowScaleControl(isChecked);
                break;
            case R.id.switch_poi:
                configInteracter.setMapPoiEnable(isChecked);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner_zoom) {
            ConfigInteracter interacter = new ConfigInteracter(this);
            if (position == 0) {
                interacter.setZoomControlsPosition(true);
            } else {
                interacter.setZoomControlsPosition(false);
            }
        } else if (parent.getId() == R.id.spinner_location) {
            ConfigInteracter interacter = new ConfigInteracter(this);
            if (position == 0) {
                interacter.setLocationPosition(true);
            } else {
                interacter.setLocationPosition(false);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        ConfigInteracter interacter = new ConfigInteracter(this);
        if (group.getId() == R.id.group_zoom) {
            if (checkedId == R.id.radio_zoom_left) {
                interacter.setZoomControlsPosition(false);
            } else if (checkedId == R.id.radio_zoom_right) {
                interacter.setZoomControlsPosition(true);
            }
        } else if (group.getId() == R.id.group_mode) {
            if (checkedId == R.id.radio_white) {
                interacter.setNightMode(1);
            } else if (checkedId == R.id.radio_black) {
                if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
                    Toast.makeText(this, "夜间模式下百度地图可能需要重启应用后生效", Toast.LENGTH_LONG).show();
                }
                interacter.setNightMode(2);
            } else {
                interacter.setNightMode(0);
            }
            ((BApp) getApplication()).setNightMode();
        }

    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(getView(R.id.lay_app_bar), msg, Snackbar.LENGTH_SHORT).show();
    }
}
