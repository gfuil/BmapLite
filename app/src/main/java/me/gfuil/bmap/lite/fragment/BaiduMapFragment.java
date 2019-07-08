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
import android.graphics.Point;
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

import com.amap.api.services.core.SuggestionCity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
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
import me.gfuil.bmap.lite.listener.MyOrientationListener;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.StatusBarUtils;

/**
 * 百度地图
 *
 * @author gfuil
 */

public class BaiduMapFragment extends BaseFragment implements BaiduMap.OnMapClickListener, BaiduMap.OnMapLoadedCallback, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapLongClickListener, MyOrientationListener.OnOrientationListener, OnGetBusLineSearchResultListener, View.OnClickListener, OnSearchResultListener, BaiduMap.OnMyLocationClickListener, BaiduMap.OnMapStatusChangeListener, BDLocationListener, BaiduMap.OnBaseIndoorMapListener, AdapterView.OnItemClickListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private CardView mCardZoom, mCardFloor;
    private ListView mListFloors;
    private Button mBtnZoomIn, mBtnZoomOut;
    private FloatingActionButton btnLocation;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private MyOrientationListener myOrientationListener;
    private int mXDirection = -1;
    private MyPoiModel clickMapPoiNow;
    public TypePoi mTypePoi;
    private SearchInteracter mSearchInteracter;
    private List<Overlay> mFavMarkerList = new ArrayList<>();
    private List<MyPoiModel> mPoiList = new ArrayList<>();
    private List<Overlay> mOverlayList = new ArrayList<>();
    private double mTotal = 0;

    private FavoriteInteracter mFavoriteInteracter;


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


    public static BaiduMapFragment newInstance() {
        return new BaiduMapFragment();
    }

    public BaiduMapFragment() {
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
            MapView.setMapCustomEnable(false);
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

                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(clickMapPoiNow.getLatitude(), clickMapPoiNow.getLongitude())));

                    }
                } else if (null != poiAll && !poiAll.isEmpty()) {
                    clearMarker();

                    ((MainActivity) getActivity()).showPoiLay(null, -1);

//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

//                    int length = 10 > poiAll.size() ? poiAll.size() : 10;
//                    for (int i = 0; i < length; i++) {
//                        if (poiAll.get(i).getTypePoi() == TypePoi.POINT || (poiAll.get(i).getLatitude() != 0 && poiAll.get(i).getLongitude() != 0)) {
//                            makeMarker(poiAll.get(i), false, i + 1);
//                            LatLng ll = new LatLng(poiAll.get(i).getLatitude(), poiAll.get(i).getLongitude());
//                            builder.include(ll);
//                        }
//
//                    }
                    mTypePoi = null;
                    clickMapPoiNow = poiAll.get(position);

                    makeMarker(clickMapPoiNow, false);

                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(clickMapPoiNow.getLatitude(), clickMapPoiNow.getLongitude())));


//                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(poiAll.get(position).getLatitude()-0.001, poiAll.get(position).getLongitude())));

                }

            }
        }
    }

    private void getData() {
        myOrientationListener = new MyOrientationListener(getActivity());
        myOrientationListener.setOnOrientationListener(this);

        mSearchInteracter = new SearchInteracter(getActivity(), TypeMap.TYPE_BAIDU);
        mFavoriteInteracter = new FavoriteInteracter(getActivity());
    }

    @Override
    protected void initView(View view) {
        btnLocation = getView(view, R.id.btn_location);
        mMapView = getView(view, R.id.map_baidu);
        mBtnZoomIn = getView(view, R.id.btn_zoom_in);
        mBtnZoomOut = getView(view, R.id.btn_zoom_out);
        mCardZoom = getView(view, R.id.card_zoom);
        mCardFloor = getView(view, R.id.card_floor);
        mListFloors = getView(view, R.id.list_floors);

        btnLocation.setOnClickListener(this);
        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        mListFloors.setOnItemClickListener(this);

        // 地图初始化
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启室内图
        mBaiduMap.setIndoorEnable(true);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapLongClickListener(this);
        mBaiduMap.setOnMyLocationClickListener(this);
        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnBaseIndoorMapListener(this);


    }

    public void initLocationSdk() {
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
        if (null != BApp.MY_LOCATION) {
//            ((MainActivity)getActivity()).showPoiLay(BApp.MY_LOCATION, -1);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 授权
                ((MainActivity) getActivity()).verifyPermissions();
            } else {
                initLocationSdk();
            }
        }
    }

    public void searchBusLine(String city, String uid) {
        BusLineSearch busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(this);
        busLineSearch.searchBusLine(new BusLineSearchOption().city(city).uid(uid));
    }

    @Override
    public void onMapLoaded() {

        int statusHeight = StatusBarUtils.getStatusBarHeight(getActivity());
        mBaiduMap.setCompassPosition(new Point(AppUtils.dip2Px(getActivity(), 30), statusHeight + AppUtils.dip2Px(getActivity(), 90)));

        configMap();
        setCacheMapStatus();
        clearMarker();
        getFavoriteList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((MainActivity) getActivity()).verifyPermissions();
        } else {
            initLocationSdk();
        }
    }

    public void getFavoriteList() {
        if (null != mFavMarkerList && !mFavMarkerList.isEmpty()) {
            for (Overlay o : mFavMarkerList) {
                o.remove();
            }
            mFavMarkerList.clear();
        }
        if (null != mFavoriteInteracter) {
            List<MyPoiModel> favoriteList = mFavoriteInteracter.getFavoriteList();
            if (null != favoriteList && !favoriteList.isEmpty()) {
                for (MyPoiModel poi : favoriteList) {
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_grade_point);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions().title(poi.getName()).position(new LatLng(poi.getLatitude(), poi.getLongitude())).icon(bitmap).animateType(MarkerOptions.MarkerAnimateType.none).anchor(0.5f, 0.5f);
                    //在地图上添加Marker，并显示
                    mFavMarkerList.add(mBaiduMap.addOverlay(option));
                }
            }
        }
    }

    private void configMap() {
        ConfigInteracter configInteracter = new ConfigInteracter(getActivity());
        mBaiduMap.getUiSettings().setZoomGesturesEnabled(configInteracter.isZoomGesturesEnabled());
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(configInteracter.isOverlookEnable());
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(configInteracter.isRotateEnable());
        mMapView.showScaleControl(configInteracter.isShowScaleControl());
        mBaiduMap.showMapPoi(configInteracter.isMapPoiEnable());
        mBaiduMap.setTrafficEnabled(configInteracter.isTrafficEnable());
        if (configInteracter.getNightMode() == 2) {
            MapView.setMapCustomEnable(true);
        } else {
            MapView.setMapCustomEnable(false);
        }

        mMapView.showZoomControls(false);
//        mBaiduMap.setMaxAndMinZoomLevel(20f, 3f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(AppUtils.dip2Px(getActivity(), 36), ViewGroup.LayoutParams.WRAP_CONTENT);
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
        mCardZoom.setLayoutParams(params);
        mCardFloor.setLayoutParams(params2);

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
    public void onMapClick(LatLng latLng) {
        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_BAIDU);
            poi.setLatitude(latLng.latitude);
            poi.setLongitude(latLng.longitude);

            if (null == mPoiList) {
                mPoiList = new ArrayList<>();
            }
            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            ((MainActivity) getActivity()).showPoiLay(null, -1);
        }

    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_BAIDU);
            poi.setLatitude(mapPoi.getPosition().latitude);
            poi.setLongitude(mapPoi.getPosition().longitude);

            if (null == mPoiList) {
                mPoiList = new ArrayList<>();
            }
            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            if (null == clickMapPoiNow) {
                clickMapPoiNow = new MyPoiModel(TypeMap.TYPE_BAIDU);
            }

            clickMapPoiNow.setTypeMap(TypeMap.TYPE_BAIDU);
            clickMapPoiNow.setName(mapPoi.getName());
            clickMapPoiNow.setUid(mapPoi.getUid());
            clickMapPoiNow.setLatitude(mapPoi.getPosition().latitude);
            clickMapPoiNow.setLongitude(mapPoi.getPosition().longitude);
            makeMarker(clickMapPoiNow, true);
        }
        ((MainActivity) getActivity()).showSearchResultLay(false);

        return true;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_BAIDU);
            poi.setLatitude(marker.getPosition().latitude);
            poi.setLongitude(marker.getPosition().longitude);

            if (null == mPoiList) {
                mPoiList = new ArrayList<>();
            }
            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            int distance = 0;
            if (null != BApp.MY_LOCATION) {
                distance = (int) DistanceUtil.getDistance(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude()), marker.getPosition());
            }

            if (null == clickMapPoiNow) {
                clickMapPoiNow = new MyPoiModel(TypeMap.TYPE_BAIDU);
            }
            clickMapPoiNow.setTypeMap(TypeMap.TYPE_BAIDU);
            clickMapPoiNow.setName(marker.getTitle());
            clickMapPoiNow.setLongitude(marker.getPosition().longitude);
            clickMapPoiNow.setLatitude(marker.getPosition().latitude);
            ((MainActivity) getActivity()).showPoiLay(clickMapPoiNow, distance);

            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(clickMapPoiNow.getLatitude(), clickMapPoiNow.getLongitude())));
        }


        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if (mIsModeRanging) {
            MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_BAIDU);
            poi.setLatitude(latLng.latitude);
            poi.setLongitude(latLng.longitude);

            if (null == mPoiList) {
                mPoiList = new ArrayList<>();
            }
            mPoiList.add(poi);

            makeRangingMarker(poi);
            setRangingPolyLine();
        } else {
            if (null == clickMapPoiNow) {
                clickMapPoiNow = new MyPoiModel(TypeMap.TYPE_BAIDU);
            }
            clickMapPoiNow.setTypeMap(TypeMap.TYPE_BAIDU);
            clickMapPoiNow.setName("地图上的点");
            clickMapPoiNow.setLatitude(latLng.latitude);
            clickMapPoiNow.setLongitude(latLng.longitude);
            makeMarker(clickMapPoiNow, true);
        }

    }

    private void makeMarker(MyPoiModel poi, boolean isClear, int number) {
        if (isClear) {
            clearMarker();
        }

        if (0 < number && 11 > number) {
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark" + number + ".png");
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().title(poi.getName()).position(new LatLng(poi.getLatitude(), poi.getLongitude())).icon(bitmap).animateType(MarkerOptions.MarkerAnimateType.none);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);

            if (1 == number) {
                int distance = 0;
                if (null != BApp.MY_LOCATION) {
                    distance = (int) DistanceUtil.getDistance(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude()),
                            new LatLng(poi.getLatitude(), poi.getLongitude()));
                }

                ((MainActivity) getActivity()).showPoiLay(poi, distance);
            }
        } else {
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().title(poi.getName()).position(new LatLng(poi.getLatitude(), poi.getLongitude())).icon(bitmap).animateType(MarkerOptions.MarkerAnimateType.none);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(new LatLng(poi.getLatitude(), poi.getLongitude()));
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

    }

    private void makeMarker(MyPoiModel poi, boolean isClear) {
//        if (isClear) {
//            clearMarker();
//        }
//
//        //构建Marker图标
//        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
//        //构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions().title(poi.getName()).position(poi.getLatLngBaidu(getActivity())).icon(bitmap).animateType(MarkerOptions.MarkerAnimateType.none);
//        //在地图上添加Marker，并显示
//        mBaiduMap.addOverlay(option);
//
//        MapStatus.Builder builder = new MapStatus.Builder();
//        builder.target(poi.getLatLngBaidu(getActivity()));
//        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        makeMarker(poi, isClear, 0);

        int distance = 0;
        if (null != BApp.MY_LOCATION) {
            distance = (int) DistanceUtil.getDistance(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude()),
                    new LatLng(poi.getLatitude(), poi.getLongitude()));
        }

        ((MainActivity) getActivity()).showPoiLay(poi, distance);

    }

    public void clearMarker() {
        mBaiduMap.clear();
        getFavoriteList();
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
    public void onGetBusLineResult(BusLineResult result) {
        hideProgress();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            onMessage("未找到路线");
        } else {
            clearMarker();
            BusLineOverlay overlay = new BusLineOverlay(mBaiduMap, PoiInfo.POITYPE.fromInt(mTypePoi.getInt()));
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();


        }
    }

    public int getMapType() {
        return mBaiduMap.getMapType();
    }

    public void setMapType(int mapType) {
        mBaiduMap.setMapType(mapType);
    }

    public MapStatus getMapStatus() {
        return mBaiduMap.getMapStatus();
    }

    public void animateMapStatus(MapStatusUpdate mapStatusUpdate) {
        mBaiduMap.animateMapStatus(mapStatusUpdate);
    }

    public boolean isTrafficEnabled() {
        return mBaiduMap.isTrafficEnabled();
    }

    public void setTrafficEnabled(boolean trafficEnabled) {
        mBaiduMap.setTrafficEnabled(trafficEnabled);
    }


    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (null != list && !list.isEmpty()) {
            if (null == BApp.MY_LOCATION) {
                BApp.MY_LOCATION = new MyPoiModel(BApp.TYPE_MAP);
            }
            BApp.MY_LOCATION.setCity(list.get(0).getCity());
            BApp.MY_LOCATION.setName("我的位置");

            if (null != getActivity()) {
                CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
                cacheInteracter.setCity(BApp.MY_LOCATION.getCity());
            }

        }
    }

    @Override
    public void setSuggestCityList(List<SuggestionCity> cities) {

    }

    @Override
    public boolean onMyLocationClick() {
        ((MainActivity) getActivity()).showPoiLay(BApp.MY_LOCATION, 0);
        return true;
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
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().title(poi.getName())
                .position(new LatLng(poi.getLatitude(), poi.getLongitude())).icon(bitmap)
                .animateType(MarkerOptions.MarkerAnimateType.none).anchor(0.5f, 0.5f);
        //在地图上添加Marker，并显示

        Overlay marker = mBaiduMap.addOverlay(option);

        mOverlayList.add(marker);
    }

    public void setRangingPolyLine() {
        if (null == mPoiList || mPoiList.size() < 2) {
            mTotal = 0;
            ((MainActivity) getActivity()).setRangingDistance(mTotal);
            return;
        }

        MyPoiModel end = mPoiList.get(mPoiList.size() - 1);
        MyPoiModel last = mPoiList.get(mPoiList.size() - 2);

        mTotal += DistanceUtil.getDistance(new LatLng(end.getLatitude(), end.getLongitude()),
                new LatLng(last.getLatitude(), last.getLongitude()));

        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(end.getLatitude(), end.getLongitude()));
        points.add(new LatLng(last.getLatitude(), last.getLongitude()));

        OverlayOptions ooPolyline = new PolylineOptions().width(6).color(Color.BLUE).points(points);
        Overlay line = mBaiduMap.addOverlay(ooPolyline);

        if (mOverlayList == null) {
            mOverlayList = new ArrayList<>();
        }
        mOverlayList.add(line);

        ((MainActivity) getActivity()).setRangingDistance(mTotal);
    }

    public void deleteRangingPoi() {
        if (null == mPoiList || mOverlayList == null) {
            return;
        }
        if (mPoiList.size() > 1) {
            MyPoiModel end = mPoiList.get(mPoiList.size() - 1);
            MyPoiModel pre = mPoiList.get(mPoiList.size() - 2);

            mTotal -= DistanceUtil.getDistance(new LatLng(end.getLatitude(), end.getLongitude()), new LatLng(pre.getLatitude(), pre.getLongitude()));

            mPoiList.remove(mPoiList.size() - 1);
            ((MainActivity) getActivity()).setRangingDistance(mTotal);
        } else if (mPoiList.size() == 1) {
            mPoiList.remove(mPoiList.size() - 1);
            mTotal = 0;
            ((MainActivity) getActivity()).setRangingDistance(mTotal);
        }

        if (!mOverlayList.isEmpty()) {
            mOverlayList.get(mOverlayList.size() - 1).remove();
            mOverlayList.remove(mOverlayList.size() - 1);
        }

        if (!mOverlayList.isEmpty()) {
            mOverlayList.get(mOverlayList.size() - 1).remove();
            mOverlayList.remove(mOverlayList.size() - 1);
        }
    }


    public void clearRangingPoi() {
        mBaiduMap.clear();
        if (mPoiList != null) {
            mPoiList.clear();
        }
        if (mOverlayList != null) {
            mOverlayList.clear();
        }
        mTotal = 0;
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
                || BApp.MY_LOCATION.getLongitude() == 4.9E-324 || BApp.MY_LOCATION.getLatitude() == 4.9E-324) {
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

            mSearchInteracter.searchLatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude(), 1, this);

            interacter.setLatitude(BApp.MY_LOCATION.getLatitude());
            interacter.setLongitude(BApp.MY_LOCATION.getLongitude());

            if (isFirstLoc) {
                ((MainActivity) getActivity()).firstLocationComplete();
                isFirstLoc = false;
            }

            isRequest = false;

        }
    }


    public void setModeEDog(boolean isChecked) {

        if (isChecked) {

            if (null != mBaiduMap.getLocationConfiguration() && mBaiduMap.getLocationConfiguration().locationMode != MyLocationConfiguration.LocationMode.COMPASS) {
                mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, null));
                btnLocation.setImageResource(R.drawable.ic_explore_24dp);
            }
            mIsModeEDog = true;
        } else {
            if (null != mBaiduMap.getLocationConfiguration() && mBaiduMap.getLocationConfiguration().locationMode == MyLocationConfiguration.LocationMode.COMPASS) {
                mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
                btnLocation.setImageResource(R.drawable.ic_my_location_24dp);
                MapStatus.Builder builder = new MapStatus.Builder().rotate(0).overlook(90);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            mIsModeEDog = false;

        }
    }

    private BaiduIndoorInfoAdapter mIndoorInfoAdapter;

    @Override
    public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo info) {
        if (b && null != info) {
            //进入室内图
            mCardFloor.setVisibility(View.VISIBLE);
            if (mIndoorInfoAdapter == null) {
                mIndoorInfoAdapter = new BaiduIndoorInfoAdapter(getActivity(), info.getFloors(), info.getCurFloor(), info.getID());
                mListFloors.setAdapter(mIndoorInfoAdapter);
            } else {
                mIndoorInfoAdapter.setFloorId(info.getID());
                mIndoorInfoAdapter.setList(info.getFloors());
                mIndoorInfoAdapter.setCurFloor(info.getCurFloor());
                mIndoorInfoAdapter.notifyDataSetChanged();
            }
        } else {
            //移出室内图
            mCardFloor.setVisibility(View.GONE);
            mIndoorInfoAdapter = null;
            mListFloors.setAdapter(mIndoorInfoAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.list_floors) {
            mIndoorInfoAdapter.setCurFloor((String) mIndoorInfoAdapter.getItem(position));
            mIndoorInfoAdapter.notifyDataSetChanged();
            ((MainActivity) getActivity()).showPoiLay(null, 0);
            if (null != mIndoorInfoAdapter.getFloorId()) {
                mBaiduMap.switchBaseIndoorMapFloor(mIndoorInfoAdapter.getCurFloor(), mIndoorInfoAdapter.getFloorId());
            }
        }
    }

    public void showOtherPoi(MyPoiModel poi) {
        isFirstLoc = false;
        makeMarker(poi, true);
    }
}
