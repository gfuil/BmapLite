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

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.ArrayList;

import me.gfuil.bmap.lite.adapter.OfflineCityExpandAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.utils.LogUtils;
import me.gfuil.bmap.lite.utils.PermissionUtils;

/**
 * @author gfuil
 */

public class OfflineMapFragment extends BaseFragment implements MKOfflineMapListener, OfflineMapManager.OfflineMapDownloadListener, ExpandableListView.OnChildClickListener {
    private static final String[] PERMISSIONS_STOEAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_STOEAGE = 200;
    private ExpandableListView mExpandListCity;
    private MKOfflineMap mOffline = null;
    private OfflineMapManager amapManager;
    private ArrayList<MKOLSearchRecord> offlineCityList;
    private OfflineCityExpandAdapter mOfflineCityExpandAdapter;
    private int mGroupPosition = -1, mChildPosition = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mExpandListCity = new ExpandableListView(getActivity());
        initView(mExpandListCity);
        getData();
        return mExpandListCity;
    }

    private void getData() {
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        amapManager = new OfflineMapManager(getActivity(), this);

//        try {
//            MapsInitializer.initialize(getActivity());
//            File dir = getActivity().getExternalFilesDir("amap");
//            if (!dir.exists()){
//                dir.mkdir();
//            }
//            MapsInitializer.sdcardDir = dir.getPath();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

        offlineCityList = mOffline.getOfflineCityList();

        if (null != offlineCityList && !offlineCityList.isEmpty()) {

            for (MKOLSearchRecord record : offlineCityList) {
                if (null == record.childCities || record.childCities.isEmpty()) {
                    ArrayList<MKOLSearchRecord> child = new ArrayList<>();
                    child.add(record);
                    record.childCities = child;
                }
                if (1 == record.cityType) {
                    record.childCities.add(0, record);
                }

            }


            mOfflineCityExpandAdapter = new OfflineCityExpandAdapter(getActivity(), offlineCityList);
            mExpandListCity.setAdapter(mOfflineCityExpandAdapter);
        }

    }

    @Override
    protected void initView(View view) {
        mExpandListCity.setDividerHeight(1);
        mExpandListCity.setOnChildClickListener(this);
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
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                break;
            default:
                break;
        }
    }


    private void download(int groupPosition, int childPosition) {
        final MKOLSearchRecord record = (MKOLSearchRecord) mOfflineCityExpandAdapter.getChild(groupPosition, childPosition);

        String msg = "下载" + record.cityName + "的离线地图包?";
//        if (record.cityName.contains("全国")) {
//            msg += "\nPS：" + record.cityName + "暂时有BUG，不推荐下载";
//        }
//        showAlertDialog("下载提示", msg, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        }, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("下载提示");
        builder.setMessage(msg);
        builder.setNegativeButton("下载百度包", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MKOfflineMap mkOfflineMap = new MKOfflineMap();
                mkOfflineMap.init(OfflineMapFragment.this);
                mkOfflineMap.start(record.cityID);
            }
        });
        builder.setPositiveButton("下载高德包", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if ("全球基础包".equals(record.cityName) || "全国基础包".equals(record.cityName)) {
                        amapManager.downloadByCityCode("000");
                    } else {
                        if (null != record.childCities && !record.childCities.isEmpty()){
                            amapManager.downloadByProvinceName(record.cityName);
                        }else {
                            amapManager.downloadByCityName(record.cityName);
                        }
                    }
                } catch (AMapException e) {
                    e.printStackTrace();
                    onMessage("没有找到" + record.cityName + "的离线地图包");
                }

            }
        });
        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }


    public void verifyPermissions(int groupPosition, int childPosition) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            download(groupPosition, childPosition);
        }
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

            showAlertDialog("权限申请", "感谢使用，为了您更好的使用体验，请授予应用读写SD卡的权限，否则您可能无法正常使用，谢谢您的支持。", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STOEAGE, REQUEST_STOEAGE);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        } else {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STOEAGE, REQUEST_STOEAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STOEAGE) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
                if (0 < mGroupPosition && 0 < mChildPosition) {
                    download(mGroupPosition, mChildPosition);
                }
            } else {
                onMessage("您没有授予所需权限");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDownload(int status, int completeCode, String downName) {
        LogUtils.debug("status=" + status + ",completeCode=" + completeCode + ",downName=" + downName);
    }

    @Override
    public void onCheckUpdate(boolean hasNew, String name) {

    }

    @Override
    public void onRemove(boolean success, String name, String describe) {

    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(mExpandListCity, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mGroupPosition = groupPosition;
//            mChildPosition = childPosition;
//            verifyPermissions(groupPosition, childPosition);
//        } else {
//        }
        download(groupPosition, childPosition);
        return true;
    }
}
