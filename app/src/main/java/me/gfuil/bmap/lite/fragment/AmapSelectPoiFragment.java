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
import android.location.Location;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.SuggestionCity;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.SelectPoiActivity;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.utils.AppUtils;

/**
 * 高德地图选择地图上的点
 *
 * @author gfuil
 */

public class AmapSelectPoiFragment extends BaseFragment implements View.OnClickListener, AMap.OnMapLoadedListener, AMap.OnMyLocationChangeListener, OnSearchResultListener, AMap.OnCameraChangeListener {
    private FloatingActionButton btnLocation;
    private CardView mCardZoom;
    private ImageView mImageCompass;
    private MapView mMapView;
    private AMap mAmap;
    private MyLocationStyle mLocClient;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private MyPoiModel clickMapPoiNow;
    private TypePoi mTypePoi;
    private List<Marker> markerList = new ArrayList<>();
    private SearchInteracter mSearchInteracter;
    private Button mBtnZoomIn, mBtnZoomOut;


    public static AmapSelectPoiFragment newInstance() {
        return new AmapSelectPoiFragment();
    }

    public AmapSelectPoiFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mMapView) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_amap, container, false);
        initView(view);
        mMapView.onCreate(savedInstanceState);
        getDate();
        return view;
    }

    private void getDate() {
        mAmap = mMapView.getMap();
        mSearchInteracter = new SearchInteracter(getActivity(), TypeMap.TYPE_AMAP);


        mAmap.setOnMapLoadedListener(this);
        mAmap.setOnCameraChangeListener(this);


        // 开启定位图层
        mAmap.setMyLocationEnabled(true);
        // 开启室内图
        mAmap.showIndoorMap(true);
        mAmap.setOnMyLocationChangeListener(this);
        mAmap.getUiSettings().setMyLocationButtonEnabled(false);


    }

    @Override
    public void onDestroyView() {
        // 关闭定位图层
        if (null != mAmap) {
            mAmap.setMyLocationEnabled(false);
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
        mAmap.setMyLocationEnabled(true);
        if (null != mLocClient) {
            mLocClient.radiusFillColor(Color.argb(50, 0, 0, 180));
            mLocClient.strokeColor(Color.argb(50, 0, 0, 255));
            mLocClient.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_location_marker));
            mAmap.setMyLocationStyle(mLocClient);
        }
        configMap();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        mAmap.setMyLocationEnabled(false);
        super.onPause();
    }


    @Override
    protected void initView(View view) {
        mMapView = getView(view, R.id.map_amap);
        btnLocation = getView(view, R.id.btn_location);
        mBtnZoomIn = getView(view, R.id.btn_zoom_in);
        mBtnZoomOut = getView(view, R.id.btn_zoom_out);
        mCardZoom = getView(view, R.id.card_zoom);
        mImageCompass = getView(view, R.id.image_compass);


        btnLocation.setOnClickListener(this);
        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        mImageCompass.setOnClickListener(this);

    }

    public void initAmapSdk() {

        // 定位初始化
        mLocClient = new MyLocationStyle();
        mLocClient.interval(3000);
        mLocClient.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        mLocClient.radiusFillColor(Color.argb(50, 0, 0, 180));
        mLocClient.strokeColor(Color.argb(50, 0, 0, 255));
        mLocClient.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_location_marker));
        mAmap.setMyLocationStyle(mLocClient);

    }

    int clickLocNum = 0;

    public void requestLoc() {
        if (clickLocNum++ > 1) {
            clickLocNum = 0;
        }

        isRequest = true;
        if (null != mLocClient) {


            if (clickLocNum == 2) {
                btnLocation.setImageResource(R.drawable.ic_explore_24dp);
                mLocClient.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
                mAmap.setMyLocationStyle(mLocClient);
                Toast.makeText(getActivity(), "罗盘模式", Toast.LENGTH_SHORT).show();
            } else {
                btnLocation.setImageResource(R.drawable.ic_my_location_24dp);
                mLocClient.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
                mAmap.setMyLocationStyle(mLocClient);

                mAmap.moveCamera(CameraUpdateFactory.changeBearing(0));
                mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));

            }

            if (null != BApp.MY_LOCATION) {
                mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())));
            }


        } else {
            initAmapSdk();
        }
    }


    @Override
    public void onMapLoaded() {

        configMap();
        setCacheMapStatus();

        initAmapSdk();
    }

    private void configMap() {
        ConfigInteracter configInteracter = new ConfigInteracter(getActivity());

        mAmap.getUiSettings().setScaleControlsEnabled(configInteracter.isShowScaleControl());
        mAmap.getUiSettings().setZoomGesturesEnabled(configInteracter.isZoomGesturesEnabled());
        mAmap.getUiSettings().setTiltGesturesEnabled(configInteracter.isOverlookEnable());
        mAmap.getUiSettings().setRotateGesturesEnabled(configInteracter.isRotateEnable());
        mAmap.setTrafficEnabled(configInteracter.isTrafficEnable());
        mAmap.getUiSettings().setZoomControlsEnabled(false);
        mAmap.getUiSettings().setIndoorSwitchEnabled(false);
        if (configInteracter.getNightMode() == 2) {
            mAmap.setMapType(AMap.MAP_TYPE_NIGHT);
        } else {
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (configInteracter.getZoomControlsPosition()) {
            params.rightMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        } else {
            params.leftMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }
        mCardZoom.setLayoutParams(params);

        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(AppUtils.dip2Px(getActivity(), 40), AppUtils.dip2Px(getActivity(), 40));
        params3.leftMargin = AppUtils.dip2Px(getActivity(), 10);
        params3.topMargin = AppUtils.dip2Px(getActivity(), 50);
        mImageCompass.setLayoutParams(params3);

    }

    private void setCacheMapStatus() {
//        CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
//        LatLng latLng = new LatLng(cacheInteracter.getLatitude(), cacheInteracter.getLongitude());
//
//        mAmap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
        mAmap.moveCamera(CameraUpdateFactory.zoomTo(17));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_location:
                requestLoc();
                break;
            case R.id.btn_zoom_in:
                if (mAmap.getMaxZoomLevel() > mAmap.getCameraPosition().zoom) {
                    mAmap.animateCamera(CameraUpdateFactory.zoomIn());
                }
                break;
            case R.id.btn_zoom_out:
                if (mAmap.getMinZoomLevel() < mAmap.getCameraPosition().zoom) {
                    mAmap.animateCamera(CameraUpdateFactory.zoomOut());
                }
                break;
            case R.id.image_compass:
                if (mAmap.getCameraPosition().bearing != 0) {
                    mAmap.animateCamera(CameraUpdateFactory.changeBearing(0));
                }
                break;
        }
    }


    @Override
    public void onMyLocationChange(Location location) {
        if (null != location && null != mMapView) {
            if (null == BApp.MY_LOCATION) {
                BApp.MY_LOCATION = new MyPoiModel(BApp.TYPE_MAP);
            }
            BApp.MY_LOCATION.setLongitude(location.getLongitude());
            BApp.MY_LOCATION.setLatitude(location.getLatitude());
            BApp.MY_LOCATION.setName("我的位置");

            if (isFirstLoc || isRequest) {

                if (location.getLatitude() == 0 || location.getLongitude() == 0
                        || location.getLatitude() == 4.9E-324 || location.getLongitude() == 4.9E-324) {
                    onMessage("无法获取到位置信息，请连接网络后再试");
                    return;
                }

                mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())));


                CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
                cacheInteracter.setLatitude(location.getLatitude());
                cacheInteracter.setLongitude(location.getLongitude());

                isRequest = false;
                isFirstLoc = false;
            }

        }

    }


    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (null != list && !list.isEmpty()) {
            ((SelectPoiActivity) getActivity()).setPoi(list.get(0));
        }
    }

    @Override
    public void setSuggestCityList(List<SuggestionCity> cities) {

    }

//    float downX = 0, downY = 0;
//
//    @Override
//    public void onTouch(MotionEvent motionEvent) {
//
//        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//            //LogUtils.debug("getX"+ motionEvent.getX() + "getY"+motionEvent.getY());
//            //LogUtils.debug("getRawX"+ motionEvent.getRawX() + "getRawY"+motionEvent.getRawY());
//
//            float x = downX - motionEvent.getX();
//            float y = downY - motionEvent.getY();
//
//            float distance = x * x + y * y;
//            LogUtils.debug("distance" + distance);
//            if (distance > 50000) {
//                if (mAmap.getMyLocationStyle().getMyLocationType() != MyLocationStyle.LOCATION_TYPE_SHOW && mAmap.getMyLocationStyle().getMyLocationType() != MyLocationStyle.LOCATION_TYPE_MAP_ROTATE) {
//                    mLocClient.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
//                    mLocClient.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_location_marker_no_arrow));
//                    btnLocation.setImageResource(R.drawable.ic_my_location_24dp);
//                    mAmap.setMyLocationStyle(mLocClient);
//
//                }
//            }
//        } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//            downX = motionEvent.getX();
//            downY = motionEvent.getY();
//        }
//    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if (0 == cameraPosition.bearing) {
            mImageCompass.setVisibility(View.GONE);
        } else {
            if (mImageCompass.getVisibility() == View.GONE) {
                mImageCompass.setVisibility(View.VISIBLE);
            }
            mImageCompass.setRotation(360 - cameraPosition.bearing);
        }
        if (mAmap.getMaxZoomLevel() <= cameraPosition.zoom) {
            mBtnZoomIn.setTextColor(Color.parseColor("#bbbbbb"));
            mBtnZoomIn.setEnabled(false);
        } else if (mAmap.getMinZoomLevel() >= cameraPosition.zoom) {
            mBtnZoomOut.setTextColor(Color.parseColor("#bbbbbb"));
            mBtnZoomOut.setEnabled(false);
        } else {
            mBtnZoomOut.setTextColor(Color.parseColor("#757575"));
            mBtnZoomOut.setEnabled(true);
            mBtnZoomIn.setTextColor(Color.parseColor("#757575"));
            mBtnZoomIn.setEnabled(true);
        }

        mSearchInteracter.searchLatLng(cameraPosition.target.latitude, cameraPosition.target.longitude, 1, this);
    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(btnLocation, msg, Snackbar.LENGTH_SHORT).show();
    }
}
