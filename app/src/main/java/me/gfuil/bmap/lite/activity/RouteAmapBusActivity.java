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
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.mapapi.overlay.AMapUtil;
import com.amap.mapapi.overlay.BusRouteOverlay;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.RouteDetailsAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.TimeUtils;

/**
 * 高德地图公交路线
 *
 * @author gfuil
 */

public class RouteAmapBusActivity extends BaseActivity implements AMap.OnMapLoadedListener, AMap.OnMyLocationChangeListener, OnSearchResultListener, View.OnClickListener, AMap.OnMarkerClickListener, AMap.OnCameraChangeListener {
    private FloatingActionButton btnLocation;
    private TextView mTextInfo, mTextRoute;
    private ImageView mImageCompass;
    private RecyclerView mRecyclerDetails;
    private FrameLayout mLayBusInfo;
    private CardView mCardZoom;
    private Button mBtnZoomIn, mBtnZoomOut;
    private BottomSheetBehavior mBehavior;
    private MapView mMapView;
    private AMap mAmap;
    private MyLocationStyle mLocClient;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private BusPath mBusRoute;
    private MyPoiModel mPoiFirst;
    private SearchInteracter mSearchInteracter;
    private BusRouteResult mBusRouteResult;
    private RouteDetailsAdapter mRouteDetailsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView(R.layout.activity_route_bus_amap);
        mMapView.onCreate(savedInstanceState);

        getData();
    }

    private void getData() {
        mAmap = mMapView.getMap();
        mSearchInteracter = new SearchInteracter(this, TypeMap.TYPE_AMAP);

        mAmap.setOnMarkerClickListener(this);
        mAmap.setOnMapLoadedListener(this);
        mAmap.setOnCameraChangeListener(this);


        // 开启定位图层
        mAmap.setMyLocationEnabled(true);
        // 开启室内图
        mAmap.showIndoorMap(true);
        mAmap.setOnMyLocationChangeListener(this);
        mAmap.getUiSettings().setMyLocationButtonEnabled(false);

        Bundle bundle = getExtras();
        if (null != bundle) {
            mBusRoute = bundle.getParcelable("bus");
            mBusRouteResult = bundle.getParcelable("route");
        }

        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTextInfo = getView(R.id.text_info);
        mTextRoute = getView(R.id.text_route);
        mRecyclerDetails = getView(R.id.recycler_details);
        mImageCompass = getView(R.id.image_compass);
        mMapView = getView(R.id.map_amap);
        btnLocation = getView(R.id.btn_location);
        mBtnZoomIn = getView(R.id.btn_zoom_in);
        mBtnZoomOut = getView(R.id.btn_zoom_out);
        mCardZoom = getView(R.id.card_zoom);

        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        mImageCompass.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerDetails.setLayoutManager(layoutManager);
        mRecyclerDetails.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mLayBusInfo = getView(R.id.lay_bus_info);
        mBehavior = BottomSheetBehavior.from(mLayBusInfo);
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    btnLocation.setVisibility(View.VISIBLE);
                    mCardZoom.setVisibility(View.VISIBLE);
                } else if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_SETTLING) {
                    btnLocation.setVisibility(View.GONE);
                    mCardZoom.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                if (0 <= slideOffset && 1 >= slideOffset) {
//                    btnLocation.setTranslationY(-slideOffset * AppUtils.dip2Px(RouteAmapBusActivity.this, 200));
//                    btnNavigation.setTranslationY(-slideOffset * AppUtils.dip2Px(RouteAmapBusActivity.this, 200));
//                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mMapView) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
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


        super.onDestroy();
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
    public void onMapLoaded() {

        if (null != mBusRoute && null != mBusRouteResult) {
            configMap();
            //setCacheMapStatus();
            initAmapSdk();
            setBusOverlay();
        } else {
            onMessage("没有路线");
            finish();
        }

    }

    private void setBusOverlay() {

        BusRouteOverlay overlay = new BusRouteOverlay(this, mAmap, mBusRoute, mBusRouteResult.getStartPos(), mBusRouteResult.getTargetPos());
        overlay.addToMap();
        overlay.zoomToSpan();

        mTextInfo.setText(AMapUtil.getBusPathTitle(mBusRoute));
        mTextRoute.setText(AMapUtil.getBusPathDes(mBusRoute));

        List<String> details = new ArrayList<>();
        if (null != mBusRoute.getSteps() && !mBusRoute.getSteps().isEmpty()) {
//            for (BusStep path : mBusRoute.getSteps()) {
            for (int i = 0; i < mBusRoute.getSteps().size(); i++) {
                BusStep path = mBusRoute.getSteps().get(i);
                if (null != path.getWalk() && null != path.getWalk().getSteps() && !path.getWalk().getSteps().isEmpty()) {
                    details.add(path.getWalk().getSteps().get(0).getRoad() + overlay.getWalkSnippet(path.getWalk().getSteps()));
                }
                if (null != path.getBusLines() && !path.getBusLines().isEmpty()) {
                    for (RouteBusLineItem item : path.getBusLines()) {
                        details.add("<b>" + item.getDepartureBusStation().getBusStationName() + "</b>站 乘<b>" + item.getBusLineName() + "</b>经过" + (item.getPassStationNum() + 1) + "站到达<b> " + item.getArrivalBusStation().getBusStationName() + "</b>站");
                        if (null != item.getFirstBusTime() && null != item.getLastBusTime()) {
                            details.add("<b>" + item.getBusLineName() + "  " + TimeUtils.convertTime(item.getFirstBusTime().getTime(), "HH:mm") + "-" + TimeUtils.convertTime(item.getLastBusTime().getTime(), "HH:mm") + "</b>");
                        }
                        details.add("<b>&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;" + item.getDepartureBusStation().getBusStationName() + "</b>");

                        if (null != item.getPassStations() && !item.getPassStations().isEmpty()) {
                            for (BusStationItem stationItem : item.getPassStations()) {
                                details.add("&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;" + stationItem.getBusStationName());
                            }
                        }
                        details.add("<b>&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;" + item.getArrivalBusStation().getBusStationName() + "</b>");

                        if (1 == i || 0 == i) {
                            mPoiFirst = new MyPoiModel(TypeMap.TYPE_AMAP);
                            mPoiFirst.setName(item.getDepartureBusStation().getBusStationName());
                            mPoiFirst.setLatitude(item.getDepartureBusStation().getLatLonPoint().getLatitude());
                            mPoiFirst.setLongitude(item.getDepartureBusStation().getLatLonPoint().getLongitude());
                        }
                    }

                }
            }
        }

        setRouteDetailsAdapter(details);

    }

    public void setRouteDetailsAdapter(List<String> list) {
        if (null == mRouteDetailsAdapter) {
            mRouteDetailsAdapter = new RouteDetailsAdapter(this, list);
            mRecyclerDetails.setAdapter(mRouteDetailsAdapter);
        } else {
            mRouteDetailsAdapter.setList(list);
            mRouteDetailsAdapter.notifyDataSetChanged();
        }
    }


    private void configMap() {
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        mAmap.setTrafficEnabled(configInteracter.isTrafficEnable());
        mAmap.getUiSettings().setScaleControlsEnabled(configInteracter.isShowScaleControl());
        mAmap.getUiSettings().setZoomGesturesEnabled(configInteracter.isZoomGesturesEnabled());
        mAmap.getUiSettings().setTiltGesturesEnabled(configInteracter.isOverlookEnable());
        mAmap.getUiSettings().setRotateGesturesEnabled(configInteracter.isRotateEnable());
        if (configInteracter.getNightMode() == 2) {
            mAmap.setMapType(AMap.MAP_TYPE_NIGHT);
        } else {
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
        }

        mAmap.getUiSettings().setZoomControlsEnabled(false);
        mAmap.getUiSettings().setIndoorSwitchEnabled(false);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (configInteracter.getZoomControlsPosition()) {
            params.rightMargin = AppUtils.dip2Px(this, 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        } else {
            params.leftMargin = AppUtils.dip2Px(this, 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }
        mCardZoom.setLayoutParams(params);
        CoordinatorLayout.LayoutParams params3 = new CoordinatorLayout.LayoutParams(AppUtils.dip2Px(this, 40), AppUtils.dip2Px(this, 40));
        params3.leftMargin = AppUtils.dip2Px(this, 10);
        params3.topMargin = AppUtils.dip2Px(this, 10);
        mImageCompass.setLayoutParams(params3);
    }

    private void setCacheMapStatus() {
        mAmap.moveCamera(CameraUpdateFactory.zoomTo(14));
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
        isFirstLoc = false;

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
                Toast.makeText(this, "罗盘模式", Toast.LENGTH_SHORT).show();
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

//                mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())));
                mSearchInteracter.searchLatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude(), 1, this);

                CacheInteracter cacheInteracter = new CacheInteracter(this);
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
            if (null == BApp.MY_LOCATION) {
                BApp.MY_LOCATION = new MyPoiModel(BApp.TYPE_MAP);
            }
            BApp.MY_LOCATION.setCity(list.get(0).getCity());
            BApp.MY_LOCATION.setName("我的位置");

            CacheInteracter cacheInteracter = new CacheInteracter(this);
            cacheInteracter.setCity(BApp.MY_LOCATION.getCity());

        }
    }

    @Override
    public void setSuggestCityList(List<SuggestionCity> cities) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (null == marker.getTitle() || marker.getTitle().isEmpty() || null == marker.getSnippet() || marker.getSnippet().isEmpty()) {
            return true;
        }
        showAlertDialog(marker.getTitle(), marker.getSnippet(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, null);
        return true;
    }

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

}
