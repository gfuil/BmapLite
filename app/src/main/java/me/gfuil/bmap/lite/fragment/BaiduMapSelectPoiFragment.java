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

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amap.api.services.core.SuggestionCity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.SelectPoiActivity;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.MyOrientationListener;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.utils.AppUtils;

/**
 * @author gfuil
 */

public class BaiduMapSelectPoiFragment extends BaseFragment implements BaiduMap.OnMapLoadedCallback, MyOrientationListener.OnOrientationListener, View.OnClickListener, OnSearchResultListener,  BaiduMap.OnMapStatusChangeListener, BDLocationListener {
    private MapView        mMapView;
    private BaiduMap       mBaiduMap;
    private LocationClient mLocClient;
    private CardView       mCardZoom;
    private Button         mBtnZoomIn, mBtnZoomOut;
    private FloatingActionButton btnLocation;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest  = false;//是否手动触发请求定位
    private MyOrientationListener myOrientationListener;
    private int mXDirection = -1;
    private TypePoi          mTypePoi;
    private SearchInteracter mSearchInteracter;


    private boolean mIsModeRanging = false;

    public static BaiduMapSelectPoiFragment newInstance() {
        return new BaiduMapSelectPoiFragment();
    }

    public BaiduMapSelectPoiFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_baidu, container, false);
        initView(view);
        getData();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        // 关闭定位图层
        if (null != mBaiduMap) {
            mBaiduMap.hideInfoWindow();
            mBaiduMap.setMyLocationEnabled(false);
        }

        if (null != mMapView) {
            mMapView.onDestroy();
            mMapView = null;
        }

        if (null != mSearchInteracter) {
            mSearchInteracter.destroy();
        }

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        if (null != mLocClient && !mLocClient.isStarted()) {
            mLocClient.start();
        }
        myOrientationListener.start();
        configMap();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();

        if (null != mLocClient && mLocClient.isStarted()) {
            mLocClient.stop();
        }
        myOrientationListener.stop();
        super.onPause();
    }

    private void getData() {
        myOrientationListener = new MyOrientationListener(getActivity());
        myOrientationListener.setOnOrientationListener(this);

        mSearchInteracter = new SearchInteracter(getActivity(), TypeMap.TYPE_BAIDU);
    }

    @Override
    protected void initView(View view) {
        btnLocation = getView(view, R.id.btn_location);
        mMapView = getView(view, R.id.map_baidu);
        mBtnZoomIn = getView(view, R.id.btn_zoom_in);
        mBtnZoomOut = getView(view, R.id.btn_zoom_out);
        mCardZoom = getView(view, R.id.card_zoom);

        btnLocation.setOnClickListener(this);
        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);

        // 地图初始化
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启室内图
        mBaiduMap.setIndoorEnable(true);
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMapStatusChangeListener(this);

    }

    public void initBaiduSdk() {
        // 定位初始化
        mLocClient = new LocationClient(getActivity());
        mLocClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("gcj02");
        option.setOpenGps(true);
        option.setScanSpan(1000);
        option.setIsNeedAltitude(true);
        option.setIsNeedAddress(false);

        mLocClient.setLocOption(option);

        MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
        mBaiduMap.setMyLocationConfiguration(configuration);

        mLocClient.start();

    }

    int clickLocNum = 0;

    public void requestLoc() {
        if (clickLocNum++ > 1) {
            clickLocNum = 0;
        }
        if (null != BApp.MY_LOCATION) {
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())).zoom(18f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
        if (null != mBaiduMap.getLocationConfiguration()) {
            if (mBaiduMap.getLocationConfiguration().locationMode == MyLocationConfiguration.LocationMode.COMPASS) {
                mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
                btnLocation.setImageResource(R.drawable.ic_my_location_24dp);

                MapStatus.Builder builder = new MapStatus.Builder().rotate(0).overlook(90);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

            } else {
                if (clickLocNum == 2) {
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, null));
                    btnLocation.setImageResource(R.drawable.ic_explore_24dp);
                    Toast.makeText(getActivity(), "罗盘模式", Toast.LENGTH_SHORT).show();
                }

            }
        }

        isRequest = true;
        if (null != mLocClient) {
            mLocClient.start();
        } else {
            initBaiduSdk();
        }
    }


    @Override
    public void onMapLoaded() {

        mBaiduMap.setCompassPosition(new Point(AppUtils.dip2Px(getActivity(), 30), AppUtils.dip2Px(getActivity(), 70)));

        configMap();
        setCacheMapStatus();


        initBaiduSdk();
    }

    private void configMap() {
        ConfigInteracter configInteracter = new ConfigInteracter(getActivity());
        mBaiduMap.getUiSettings().setZoomGesturesEnabled(configInteracter.isZoomGesturesEnabled());
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(configInteracter.isOverlookEnable());
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(configInteracter.isRotateEnable());
        mMapView.showScaleControl(configInteracter.isShowScaleControl());
        mBaiduMap.showMapPoi(configInteracter.isMapPoiEnable());
        mBaiduMap.setTrafficEnabled(configInteracter.isTrafficEnable());
        if (configInteracter.getNightMode() == 2){
            MapView.setMapCustomEnable(true);
        }else {
            MapView.setMapCustomEnable(false);
        }

        mMapView.showZoomControls(false);
        mBaiduMap.setMaxAndMinZoomLevel(20f, 3f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (configInteracter.getZoomControlsPosition()) {
            params.rightMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        } else {
            params.leftMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }
        mCardZoom.setLayoutParams(params);

    }

    private void setCacheMapStatus() {
        CacheInteracter cacheInteracter = new CacheInteracter(getActivity());

        LatLng latLng = new LatLng(cacheInteracter.getLatitude(), cacheInteracter.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng).zoom(14.5f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_location:
                requestLoc();
                break;
            case R.id.btn_zoom_in:
                if (mBaiduMap.getMaxZoomLevel() > mBaiduMap.getMapStatus().zoom) {
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.zoom(mBaiduMap.getMapStatus().zoom + 1f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                break;
            case R.id.btn_zoom_out:
                if (mBaiduMap.getMinZoomLevel() < mBaiduMap.getMapStatus().zoom) {
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.zoom(mBaiduMap.getMapStatus().zoom - 1f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                break;
        }
    }

    @Override
    public void onOrientationChanged(float x) {
        mXDirection = (int) x;

        if (null != mBaiduMap && null != mBaiduMap.getLocationData()) {
            MyLocationData locData = new MyLocationData.Builder()
                    .direction(mXDirection)
                    .latitude(mBaiduMap.getLocationData().latitude)
                    .longitude(mBaiduMap.getLocationData().longitude)
                    .accuracy(mBaiduMap.getLocationData().accuracy)
                    .build();
            mBaiduMap.setMyLocationData(locData);
        }
    }

    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (null != list && !list.isEmpty()) {
            ((SelectPoiActivity)getActivity()).setPoi(list.get(0));
        }
    }

    @Override
    public void setSuggestCityList(List<SuggestionCity> cities) {

    }


    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

        if (mBaiduMap.getMaxZoomLevel() <= mapStatus.zoom) {
            mBtnZoomIn.setTextColor(Color.parseColor("#bbbbbb"));
            mBtnZoomIn.setEnabled(false);
        } else if (mBaiduMap.getMinZoomLevel() >= mapStatus.zoom) {
            mBtnZoomOut.setTextColor(Color.parseColor("#bbbbbb"));
            mBtnZoomOut.setEnabled(false);
        } else {
            mBtnZoomOut.setTextColor(Color.parseColor("#757575"));
            mBtnZoomOut.setEnabled(true);
            mBtnZoomIn.setTextColor(Color.parseColor("#757575"));
            mBtnZoomIn.setEnabled(true);
        }

        mSearchInteracter.searchLatLng(mapStatus.target.latitude, mapStatus.target.longitude, 2, this);

    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(btnLocation, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMapView == null) {
            return;
        }

        if (null == BApp.MY_LOCATION) {
            BApp.MY_LOCATION = new MyPoiModel(TypeMap.TYPE_BAIDU);
        }

        BApp.MY_LOCATION.setName("我的位置");
        BApp.MY_LOCATION.setLatitude(location.getLatitude());
        BApp.MY_LOCATION.setLongitude(location.getLongitude());
        BApp.MY_LOCATION.setAltitude(location.getAltitude());
        BApp.MY_LOCATION.setAccuracy(location.getRadius());

        CacheInteracter interacter = new CacheInteracter(getActivity());

        if (BApp.MY_LOCATION.getLongitude() == 0 || BApp.MY_LOCATION.getLatitude() == 0
                ||BApp.MY_LOCATION.getLongitude() == 4.9E-324 || BApp.MY_LOCATION.getLatitude() == 4.9E-324){
            BApp.MY_LOCATION.setLatitude(interacter.getLatitude());
            BApp.MY_LOCATION.setLongitude(interacter.getLongitude());

        }

        MyLocationData locData = new MyLocationData.Builder()
                .direction(mXDirection)
                .latitude(BApp.MY_LOCATION.getLatitude())
                .longitude(BApp.MY_LOCATION.getLongitude())
                .accuracy((float) BApp.MY_LOCATION.getAccuracy())
                .build();
        mBaiduMap.setMyLocationData(locData);

        if (isFirstLoc || isRequest) {

            if (location.getLatitude() == 0 || location.getLongitude() == 0
                    || location.getLatitude() == 4.9E-324 || location.getLongitude() == 4.9E-324) {
                onMessage("无法获取到位置信息，请连接网络后再试");
            }

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())).zoom(18f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));


            interacter.setLatitude(BApp.MY_LOCATION.getLatitude());
            interacter.setLongitude(BApp.MY_LOCATION.getLongitude());

            isRequest = false;
            isFirstLoc = false;

        }

    }

}
