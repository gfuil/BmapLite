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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.IndoorBuildingInfo;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.SuggestionCity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.MainActivity;
import me.gfuil.bmap.lite.adapter.BaiduIndoorInfoAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.FavoriteInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;

/**
 * 高德地图
 * @author gfuil
 */

public class AmapFragment extends BaseFragment implements AMap.OnMapClickListener, View.OnClickListener, AMap.OnMapLoadedListener, AMap.OnMarkerClickListener, AMap.OnMapLongClickListener, AMap.OnMyLocationChangeListener, AMap.OnPOIClickListener, OnSearchResultListener, AMap.OnCameraChangeListener, AdapterView.OnItemClickListener, AMap.OnIndoorBuildingActiveListener {
    private FloatingActionButton btnLocation;
    private CardView mCardZoom, mCardFloor;
    private ListView mListFloors;
    private ImageView mImageCompass;
    private MapView mMapView;
    private AMap mAmap;
    private MyLocationStyle mLocClient;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private MyPoiModel clickMapPoiNow;
    private TypePoi mTypePoi;
    private List<Marker> markerList = new ArrayList<>();
    private List<Marker> mFavMarkerList = new ArrayList<>();
    private SearchInteracter mSearchInteracter;
    private FavoriteInteracter mFavoriteInteracter;
    private Button mBtnZoomIn, mBtnZoomOut;

    private List<MyPoiModel> mPoiList = new ArrayList<>();
    private List<Marker> mRangingMarkerList = new ArrayList<>();
    private List<Polyline> mLineList = new ArrayList<>();
    private double mTotal = 0;

    private boolean mIsModeRanging = false;
    private boolean mIsModeEDog = false;

    public boolean isModeRanging() {
        return mIsModeRanging;
    }

    public void setModeRanging(boolean enabled) {
        mIsModeRanging = enabled;
        clearRangingPoi();
    }

    public boolean isModeEDog() {
        return mIsModeEDog;
    }


    public static AmapFragment newInstance() {
        return new AmapFragment();
    }

    public AmapFragment() {

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
        mFavoriteInteracter = new FavoriteInteracter(getActivity());

//        mAmap.setCustomMapStylePath(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/night_amap.data");
//        mAmap.setMapCustomEnable(true);
        mAmap.setOnMapClickListener(this);
        mAmap.setOnMapLongClickListener(this);

        mAmap.setOnMarkerClickListener(this);
        mAmap.setOnPOIClickListener(this);
        mAmap.setOnMapLoadedListener(this);
        mAmap.setOnCameraChangeListener(this);


        // 开启定位图层
        mAmap.setMyLocationEnabled(true);
        // 开启室内图
        mAmap.showIndoorMap(true);
        mAmap.setOnMyLocationChangeListener(this);
        mAmap.getUiSettings().setMyLocationButtonEnabled(false);
        mAmap.setOnIndoorBuildingActiveListener(this);

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
        if (null != mFavoriteInteracter) {
            mFavoriteInteracter.destroy();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (MainActivity.REQUEST_SEARCH == resultCode) {
            if (null != data && null != data.getExtras()) {
                List<MyPoiModel> poiAll = data.getExtras().getParcelableArrayList("poiAll");
                MyPoiModel poiInfo = data.getExtras().getParcelable("poi");
                int position = data.getExtras().getInt("position");

                if (null != poiInfo) {
                    if (poiInfo.getTypePoi() == TypePoi.BUS_LINE || poiInfo.getTypePoi() == TypePoi.SUBWAY_LINE) {
                        mTypePoi = poiInfo.getTypePoi();
                        searchBusLine(poiInfo.getCity(), poiInfo.getUid());
                        ((MainActivity) getActivity()).showPoiLay(null, -1);
                    } else {
                        mTypePoi = null;
                        clickMapPoiNow = poiInfo;

                        makeMarker(clickMapPoiNow, true);

                        mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(clickMapPoiNow.getLatitude(), clickMapPoiNow.getLongitude())));

                    }
                } else if (null != poiAll && !poiAll.isEmpty()) {
                    clearMarker();

                    ((MainActivity) getActivity()).showPoiLay(null, -1);

                    mTypePoi = null;
                    clickMapPoiNow = poiAll.get(position);

                    makeMarker(clickMapPoiNow, true);

                    mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(clickMapPoiNow.getLatitude(), clickMapPoiNow.getLongitude())));

//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
//                    for (int i = 0; i < poiAll.size(); i++) {
//                        if (poiAll.get(i).getTypePoi() == TypePoi.POINT) {
//                            ImageView imageView = new ImageView(getActivity());
//                            imageView.setImageResource(R.drawable.shape_point_poi);
//                            MyPoiModel poi = poiAll.get(i);
//                            Marker marker = mAmap.addMarker(new MarkerOptions().position(new LatLng(poi.getLatitude(), poi.getLongitude())).title(poi.getName()).icon(BitmapDescriptorFactory.fromView(imageView)));
//                            markerList.add(marker);
//                        }
//                        LatLng ll = new LatLng(poiAll.get(i).getLatitude(), poiAll.get(i).getLongitude());
//                        builder.include(ll);
//                    }
//
//                    mAmap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
                }


            }
        }
    }


    @Override
    protected void initView(View view) {
        mMapView = getView(view, R.id.map_amap);
        btnLocation = getView(view, R.id.btn_location);
        mBtnZoomIn = getView(view, R.id.btn_zoom_in);
        mBtnZoomOut = getView(view, R.id.btn_zoom_out);
        mCardZoom = getView(view, R.id.card_zoom);
        mCardFloor = getView(view, R.id.card_floor);
        mListFloors = getView(view, R.id.list_floors);
        mImageCompass = getView(view, R.id.image_compass);

        btnLocation.setOnClickListener(this);
        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        mImageCompass.setOnClickListener(this);
        mListFloors.setOnItemClickListener(this);


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


        try {
            LocationManager locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                onMessage(getResources().getString(R.string.gps_enabled_false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
//                ((MainActivity)getActivity()).showPoiLay(BApp.MY_LOCATION, -1);
                mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())));
            }


        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 授权
                ((MainActivity) getActivity()).verifyPermissions();
            } else {
                initAmapSdk();
            }
        }
    }

    public void searchBusLine(String city, String uid) {

    }

    @Override
    public void onMapLoaded() {

        configMap();
        setCacheMapStatus();
        getFavoriteList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((MainActivity) getActivity()).verifyPermissions();
        } else {
            initAmapSdk();
        }
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
        mAmap.getUiSettings().setCompassEnabled(false);
        if (configInteracter.getNightMode() == 2) {
            setMapType(AMap.MAP_TYPE_NIGHT);
        } else {
            setMapType(AMap.MAP_TYPE_NORMAL);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(AppUtils.dip2Px(getActivity(), 36), ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(AppUtils.dip2Px(getActivity(), 40), AppUtils.dip2Px(getActivity(), 40));

        if (configInteracter.getZoomControlsPosition()) {
            params.rightMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            params2.leftMargin = AppUtils.dip2Px(getActivity(), 10);
            params2.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        } else {
            params.leftMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            params2.rightMargin = AppUtils.dip2Px(getActivity(), 10);
            params2.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        }
        params3.leftMargin = AppUtils.dip2Px(getActivity(), 10);
        params3.topMargin = AppUtils.dip2Px(getActivity(), 90);
        mCardZoom.setLayoutParams(params);
        mCardFloor.setLayoutParams(params2);
        mImageCompass.setLayoutParams(params3);

    }

    private void setCacheMapStatus() {
        CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
        LatLng latLng = new LatLng(cacheInteracter.getLatitude(), cacheInteracter.getLongitude());

        mAmap.moveCamera(CameraUpdateFactory.zoomTo(17));
        mAmap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
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
                if (mAmap.getCameraPosition().bearing != 0){
                    mAmap.animateCamera(CameraUpdateFactory.changeBearing(0));
                }
                break;
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_AMAP);
            poi.setLatitude(latLng.latitude);
            poi.setLongitude(latLng.longitude);

            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            if (null == clickMapPoiNow) {
                clickMapPoiNow = new MyPoiModel(TypeMap.TYPE_AMAP);
            }
            clickMapPoiNow.setTypeMap(TypeMap.TYPE_AMAP);
            clickMapPoiNow.setName("地图上的点");
            clickMapPoiNow.setLatitude(latLng.latitude);
            clickMapPoiNow.setLongitude(latLng.longitude);

            makeMarker(clickMapPoiNow, true);
        }

    }

    @Override
    public void onPOIClick(Poi poi) {
        if (mIsModeRanging) {
            MyPoiModel mypoi = new MyPoiModel(TypeMap.TYPE_AMAP);
            mypoi.setLatitude(poi.getCoordinate().latitude);
            mypoi.setLongitude(poi.getCoordinate().longitude);

            mPoiList.add(mypoi);

            makeRangingMarker(mypoi);
            setRangingPolyLine();
        } else {
            if (null == clickMapPoiNow) {
                clickMapPoiNow = new MyPoiModel(TypeMap.TYPE_AMAP);
            }
            clickMapPoiNow.setTypeMap(TypeMap.TYPE_AMAP);
            clickMapPoiNow.setName(poi.getName());
            clickMapPoiNow.setUid(poi.getPoiId());
            clickMapPoiNow.setLatitude(poi.getCoordinate().latitude);
            clickMapPoiNow.setLongitude(poi.getCoordinate().longitude);

            makeMarker(clickMapPoiNow, true);

        }
        ((MainActivity) getActivity()).showSearchResultLay(false);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        LogUtils.debug("onMapClick");
        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_AMAP);
            poi.setLatitude(latLng.latitude);
            poi.setLongitude(latLng.longitude);

            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            ((MainActivity) getActivity()).showPoiLay(null, -1);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_AMAP);
            poi.setLatitude(marker.getPosition().latitude);
            poi.setLongitude(marker.getPosition().longitude);

            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            int distance = 0;
            if (null != BApp.MY_LOCATION) {
                distance = (int) AMapUtils.calculateLineDistance(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude()), marker.getPosition());
            }

            if (null == clickMapPoiNow) {
                clickMapPoiNow = new MyPoiModel(TypeMap.TYPE_AMAP);
            }
            if (null != marker.getTitle() && !marker.getTitle().isEmpty()) {
                clickMapPoiNow.setTypeMap(TypeMap.TYPE_AMAP);
                clickMapPoiNow.setName(marker.getTitle());
                clickMapPoiNow.setLongitude(marker.getPosition().longitude);
                clickMapPoiNow.setLatitude(marker.getPosition().latitude);
                mAmap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(clickMapPoiNow.getLatitude(), clickMapPoiNow.getLongitude())));
                ((MainActivity) getActivity()).showPoiLay(clickMapPoiNow, distance);
            } else {
                ((MainActivity) getActivity()).showPoiLay(BApp.MY_LOCATION, distance);
            }

        }
        return true;
    }

    private void makeMarker(MyPoiModel poi, boolean isClear) {
        if (isClear) {
            clearMarker();
        }

        int distance = 0;
        if (null != BApp.MY_LOCATION) {
            distance = (int) AMapUtils.calculateLineDistance(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude()),
                    new LatLng(poi.getLatitude(), poi.getLongitude()));
        }
        Marker marker = mAmap.addMarker(new MarkerOptions().position(new LatLng(poi.getLatitude(), poi.getLongitude())).title(poi.getName()).snippet(distance + "").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding_2)));
        markerList.add(marker);

        ((MainActivity) getActivity()).showPoiLay(poi, distance);

    }

    public void clearMarker() {
        for (Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
        getFavoriteList();
    }


    public void changeTilt(float bearing) {
        mAmap.animateCamera(CameraUpdateFactory.changeTilt(bearing));
    }

    public int getMapType() {
        return mAmap.getMapType();
    }

    public void setMapType(int mapType) {
        mAmap.setMapType(mapType);
    }


    public boolean isTrafficEnabled() {
        return mAmap.isTrafficEnabled();
    }

    public void setTrafficEnabled(boolean trafficEnabled) {
        mAmap.setTrafficEnabled(trafficEnabled);
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
            BApp.MY_LOCATION.setAltitude(location.getAltitude());
            BApp.MY_LOCATION.setAccuracy(location.getAccuracy());

            if (isFirstLoc || isRequest) {

                if (location.getLatitude() == 0 || location.getLongitude() == 0
                        || location.getLatitude() == 4.9E-324 || location.getLongitude() == 4.9E-324) {
                    onMessage("无法获取到位置信息，请连接网络后再试");
                    return;
                }

                mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())));
                mSearchInteracter.searchLatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude(), 1, this);

                CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
                cacheInteracter.setLatitude(location.getLatitude());
                cacheInteracter.setLongitude(location.getLongitude());

                if (isFirstLoc) {
                    ((MainActivity) getActivity()).firstLocationComplete();
                    isFirstLoc = false;
                }

                isRequest = false;
            }

        }

    }


    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (null != list && !list.isEmpty()) {
            if (null == BApp.MY_LOCATION) {
                BApp.MY_LOCATION = new MyPoiModel(BApp.TYPE_MAP);
            }
            BApp.MY_LOCATION.setCity(list.get(0).getCity());
            BApp.MY_LOCATION.setName("我的位置");

            CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
            cacheInteracter.setCity(BApp.MY_LOCATION.getCity());

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
    }

    public void setBtnLocationTranslationY(float v) {
        if (null != btnLocation) {
            btnLocation.setTranslationY(v);
        }
    }


    private void makeRangingMarker(MyPoiModel poi) {
        //构建Marker图标
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(R.drawable.shape_point);

        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(imageView);

        Marker marker = mAmap.addMarker(new MarkerOptions().icon(bitmap).anchor(0.5f, 0.5f).position(new LatLng(poi.getLatitude(), poi.getLongitude())));

        if (null == mRangingMarkerList) {
            mRangingMarkerList = new ArrayList<>();
        }
        mRangingMarkerList.add(marker);
    }

    public void setRangingPolyLine() {
        if (mPoiList.size() < 2) {
            mTotal = 0;
            ((MainActivity) getActivity()).setRangingDistance(mTotal);
            return;
        }

        MyPoiModel end = mPoiList.get(mPoiList.size() - 1);
        MyPoiModel last = mPoiList.get(mPoiList.size() - 2);

        mTotal += AMapUtils.calculateLineDistance(new LatLng(end.getLatitude(), end.getLongitude()),
                new LatLng(last.getLatitude(), last.getLongitude()));

        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(end.getLatitude(), end.getLongitude()));
        points.add(new LatLng(last.getLatitude(), last.getLongitude()));

        Polyline ooPolyline = mAmap.addPolyline(new PolylineOptions().addAll(points).width(6).color(Color.BLUE));

        if (null == mLineList) {
            mLineList = new ArrayList<>();
        }
        mLineList.add(ooPolyline);

        ((MainActivity) getActivity()).setRangingDistance(mTotal);
    }

    public void deleteRangingPoi() {
        if (mPoiList.size() > 1) {
            MyPoiModel end = mPoiList.get(mPoiList.size() - 1);
            MyPoiModel pre = mPoiList.get(mPoiList.size() - 2);

            mTotal -= AMapUtils.calculateLineDistance(new LatLng(end.getLatitude(), end.getLongitude()),
                    new LatLng(pre.getLatitude(), pre.getLongitude()));

            mPoiList.remove(mPoiList.size() - 1);
            ((MainActivity) getActivity()).setRangingDistance(mTotal);
        } else if (mPoiList.size() == 1) {
            mPoiList.remove(mPoiList.size() - 1);
            mTotal = 0;
            ((MainActivity) getActivity()).setRangingDistance(mTotal);
        }

        if (!mRangingMarkerList.isEmpty()) {
            mRangingMarkerList.get(mRangingMarkerList.size() - 1).remove();
            mRangingMarkerList.remove(mRangingMarkerList.size() - 1);
        }

        if (!mLineList.isEmpty()) {
            mLineList.get(mLineList.size() - 1).remove();
            mLineList.remove(mLineList.size() - 1);
        }
    }


    public void clearRangingPoi() {
        clearMarker();

        for (Marker marker : mRangingMarkerList) {
            marker.remove();
        }
        mRangingMarkerList.clear();

        for (Polyline polyline : mLineList) {
            polyline.remove();
        }
        mLineList.clear();

        mPoiList.clear();
        mTotal = 0;
    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(btnLocation, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void getFavoriteList() {
        if (null != mFavMarkerList && !mFavMarkerList.isEmpty()) {
            for (Marker o : mFavMarkerList) {
                o.remove();
            }
            mFavMarkerList.clear();
        }
        if (null != mFavoriteInteracter) {
            List<MyPoiModel> favoriteList = mFavoriteInteracter.getFavoriteList();
            if (null != favoriteList && !favoriteList.isEmpty()) {
                for (MyPoiModel poi : favoriteList) {
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_grade_point_2);
                    Marker marker = mAmap.addMarker(new MarkerOptions().position(new LatLng(poi.getLatitude(), poi.getLongitude())).title(poi.getName()).snippet("").anchor(0.5f, 0.5f).icon(bitmapDescriptor));
                    mFavMarkerList.add(marker);
                }
            }
        }
    }

    public void setModeEDog(boolean isChecked) {
        if (isChecked) {
            btnLocation.setImageResource(R.drawable.ic_explore_24dp);
            mLocClient.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
            mAmap.setMyLocationStyle(mLocClient);
            mIsModeEDog = true;
        } else {
            btnLocation.setImageResource(R.drawable.ic_my_location_24dp);
            mLocClient.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            mAmap.setMyLocationStyle(mLocClient);

            mAmap.moveCamera(CameraUpdateFactory.changeBearing(0));
            mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));
            mIsModeEDog = false;
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.list_floors) {
            mIndoorInfoAdapter.setCurFloor((String) mIndoorInfoAdapter.getItem(position));
            mIndoorInfoAdapter.notifyDataSetChanged();
            ((MainActivity) getActivity()).showPoiLay(null, 0);
            if (null != mIndoorBuildingInfo) {
                mIndoorBuildingInfo.activeFloorName = mIndoorBuildingInfo.floor_names[position];
                mIndoorBuildingInfo.activeFloorIndex = mIndoorBuildingInfo.floor_indexs[position];
                mAmap.setIndoorBuildingInfo(mIndoorBuildingInfo);
            }
        }
    }

    BaiduIndoorInfoAdapter mIndoorInfoAdapter;
    IndoorBuildingInfo mIndoorBuildingInfo;

    @Override
    public void OnIndoorBuilding(IndoorBuildingInfo info) {
        if (null != info) {
            mIndoorBuildingInfo = info;
            //进入室内图
            mCardFloor.setVisibility(View.VISIBLE);
            if (null == mIndoorInfoAdapter) {
                mIndoorInfoAdapter = new BaiduIndoorInfoAdapter(getActivity(), Arrays.asList(info.floor_names), info.activeFloorName, info.poiid);
                mListFloors.setAdapter(mIndoorInfoAdapter);
            } else {
                mIndoorInfoAdapter.setFloorId(info.poiid);
                mIndoorInfoAdapter.setList(Arrays.asList(info.floor_names));
                mIndoorInfoAdapter.setCurFloor(info.activeFloorName);
                mIndoorInfoAdapter.notifyDataSetChanged();
            }
        } else {
            //移出室内图
            mCardFloor.setVisibility(View.GONE);
            mIndoorInfoAdapter = null;
            mListFloors.setAdapter(mIndoorInfoAdapter);
        }
    }

    public void showOtherPoi(MyPoiModel poi) {
        isFirstLoc = false;
        makeMarker(poi, true);
        mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(poi.getLatitude(), poi.getLongitude())));
    }
}
