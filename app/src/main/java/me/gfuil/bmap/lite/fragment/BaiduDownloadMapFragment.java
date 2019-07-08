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

package me.gfuil.bmap.lite.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.gfuil.bmap.lite.adapter.BaiduDownloadCityAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.utils.LogUtils;

/**
 * 百度离线地图
 *
 * @author gfuil
 */

public class BaiduDownloadMapFragment extends BaseFragment implements MKOfflineMapListener, AdapterView.OnItemClickListener, BaiduDownloadCityAdapter.OnClickBadiduDownloadOptionsListener {

    private final static int TIME_UP = 1;
    private ListView listOffline;
    private MKOfflineMap mOffline = null;
    private BaiduDownloadCityAdapter cityAdapter;
    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_UP:
                    getList();
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listOffline = new ListView(getActivity());
        initView(listOffline);
        getData();
        return listOffline;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != timer) {
            timer.cancel();
        }
    }

    private void getData() {
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(TIME_UP);
            }
        };

        timer = new Timer(true);
        timer.schedule(task, 1000, 1000);
    }

    private void getList() {
        if (null != mOffline) {
            List<MKOLUpdateElement> list = mOffline.getAllUpdateInfo();

            if (null == cityAdapter) {
                cityAdapter = new BaiduDownloadCityAdapter(getActivity(), list);
                cityAdapter.setOnClickBadiduDownloadOptionsListener(this);
                listOffline.setAdapter(cityAdapter);
            } else {
                cityAdapter.setList(list, true);
                cityAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    protected void initView(View view) {
        listOffline.setDividerHeight(1);
        listOffline.setOnItemClickListener(this);
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    LogUtils.debug(String.format("%s : %d%%", update.cityName, update.ratio));
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                LogUtils.debug(String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新
                final MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                showAlertDialog("提示", e.cityName + "有新版本了，是否更新？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOffline.update(e.cityID);
                    }
                }, null);

                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MKOLUpdateElement element = (MKOLUpdateElement) listOffline.getAdapter().getItem(position);
        delCity(element);
    }

    private void delCity(final MKOLUpdateElement element) {
        showAlertDialog("温馨提示", "您要删除" + element.cityName + "的离线地图包吗?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOffline.remove(element.cityID);
                getList();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    @Override
    public void onClickBaiduDownloadOptions(String option, MKOLUpdateElement city) {
        if ("暂停".equals(option)){
            mOffline.pause(city.cityID);
        }else if ("删除".equals(option)){
            delCity(city);
        }else if ("开始".equals(option)){
            mOffline.start(city.cityID);
        }else if ("更新".equals(option)){
            mOffline.update(city.cityID);
        }
    }
}
