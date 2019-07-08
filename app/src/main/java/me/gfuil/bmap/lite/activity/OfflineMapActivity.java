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

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.ViewPagerAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.fragment.AmapDownloadMapFragment;
import me.gfuil.bmap.lite.fragment.BaiduDownloadMapFragment;
import me.gfuil.bmap.lite.fragment.OfflineMapFragment;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;

/**
 * 离线地图
 *
 * @author gfuil
 */

public class OfflineMapActivity extends BaseActivity {
    private TabLayout mTab;
    private ViewPager mPager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_offline_map);

        getData();
    }

    private void getData() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.offline_map, menu);
        return true;
    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTab = getView(R.id.tab);
        mPager = getView(R.id.pager);


        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new OfflineMapFragment());
        fragments.add(new BaiduDownloadMapFragment());
        fragments.add(new AmapDownloadMapFragment());

        List<String> titles = new ArrayList<>();
        titles.add("全部");
        titles.add("已下载百度包");
        titles.add("已下载高德包");
        mPager.setAdapter(new ViewPagerAdapter(getFragmentManager(), fragments, titles));
        mPager.setOffscreenPageLimit(fragments.size());
        mTab.setupWithViewPager(mPager);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        } else if (R.id.action_update == id) {
            checkUpdate();
        } else if (R.id.action_setting == id) {
            try {
                chooseDir();
            } catch (Exception e) {
                e.printStackTrace();
                onMessage("抱歉，您的手机不支持存储目录设置");
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUpdate() {
        int count = 0;
        final MKOfflineMap mOffline = new MKOfflineMap();
        mOffline.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int type, int state) {
                switch (type) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
                        break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:
                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE:
                        // 版本更新提示
                        // 版本更新
                        final MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                        showAlertDialog("提示", e.cityName + "有新版本了，是否更新？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOffline.update(e.cityID);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        });


        List<MKOLUpdateElement> baiduList = mOffline.getAllUpdateInfo();
        if (null != baiduList && !baiduList.isEmpty()) {
            for (final MKOLUpdateElement city : baiduList) {
                if (city.update) {
                    count++;
                    showAlertDialog("提示", city.cityName + "有新版本了，是否更新？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mOffline.update(city.cityID);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                }

            }
        }


        final OfflineMapManager amapManager = new OfflineMapManager(this, new OfflineMapManager.OfflineMapDownloadListener() {
            @Override
            public void onDownload(int i, int i1, String s) {

            }

            @Override
            public void onCheckUpdate(boolean b, String s) {

            }

            @Override
            public void onRemove(boolean b, String s, String s1) {

            }
        });
        List<OfflineMapCity> cityList = amapManager.getDownloadOfflineMapCityList();
        if (null != cityList && !cityList.isEmpty()) {
            for (final OfflineMapCity city : cityList) {
                if (city.getState() == OfflineMapStatus.NEW_VERSION || city.getState() == OfflineMapStatus.CHECKUPDATES) {
                    count++;
                    showAlertDialog("提示", city.getCity() + "有新版本了，是否更新？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                amapManager.updateOfflineCityByCode(city.getCode());
                            } catch (AMapException e) {
                                e.printStackTrace();
                                onMessage("更新失败");
                            }
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                }

            }
        }

        if (0 == count) {
            onMessage("离线地图已是最新");
        }
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

    @Override
    public void onMessage(String msg) {
        Snackbar.make(mPager, msg, Snackbar.LENGTH_SHORT).show();
    }
}
