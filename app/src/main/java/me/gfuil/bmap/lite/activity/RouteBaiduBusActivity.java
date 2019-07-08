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
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.SuggestionCity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.MassTransitRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.RouteDetailsAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.MyOrientationListener;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.BusRouteModel;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;
import me.gfuil.bmap.lite.utils.StringUtils;

/**
 * 百度地图公交路线
 *
 * @author gfuil
 */

public class RouteBaiduBusActivity extends BaseActivity implements View.OnClickListener, MyOrientationListener.OnOrientationListener, BaiduMap.OnMapLoadedCallback, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener, OnSearchResultListener, BaiduMap.OnMapStatusChangeListener, BDLocationListener, RouteDetailsAdapter.OnItemClickListener, OnGetBusLineSearchResultListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private FloatingActionButton btnLocation;
    private CardView mCardZoom;
    private Button mBtnZoomIn, mBtnZoomOut;
    private AppBarLayout mLayAppBar;
    private TextView mTextInfo, mTextRoute;
    private RecyclerView mRecyclerDetails;
    private FrameLayout mLayBusInfo;
    private BottomSheetBehavior mBehavior;
    private BusRouteModel mBusRoute;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private MyOrientationListener myOrientationListener;
    private int mXDirection = -1;
    private MyPoiModel mPoiFirst;
    private SearchInteracter mSearchInteracter;
    private RouteDetailsAdapter mRouteDetailsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_route_bus_baidu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onDestroy() {
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

        super.onDestroy();
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
        Bundle bundle = getExtras();
        if (null != bundle) {
            mBusRoute = bundle.getParcelable("bus");
        }
        if (null != mBusRoute) {
            try {
                setMassTransitOverlay(mBusRoute.getLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            onMessage("没有路线");
            finish();
            return;
        }

        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


        String info = "";
        int duration = mBusRoute.getDuration() / 60;
        int distance = mBusRoute.getDistance();

        if (duration > 60) {
            info += duration / 60 + "小时" + duration % 60 + "分钟 - ";
        } else {
            info += duration + "分钟 - ";
        }

        if (1000 > distance) {
            info += distance + "米";
        } else {
            info += String.format("%.1f", (double) distance / 1000) + "公里";
        }


        mTextRoute.setText(info);

        MassTransitRouteLine line = mBusRoute.getLine();
        String msg = "";
        int walk = 0;
        List<String> details = new ArrayList<>();
        if (mBusRoute.isSameCity()) {
            if (line.getNewSteps() != null) {
                for (List<MassTransitRouteLine.TransitStep> transitStepList : line.getNewSteps()) {
                    if (transitStepList.size() == 1) {
                        MassTransitRouteLine.TransitStep step = transitStepList.get(0);
                        details.add(step.getInstructions());
                        if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS) {
                            msg += step.getBusInfo().getName() + " > ";
                            details.add("<b>" + step.getBusInfo().getName() + "  " + step.getBusInfo().getDepartureTime() + "-" + step.getBusInfo().getArriveTime() + "</b> [点击查询信息]");
                        } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_TRAIN) {
                            msg += step.getTrainInfo().getName() + " > ";
                            details.add(step.getTrainInfo().getName() + "  " + step.getTrainInfo().getDepartureTime() + "-" + step.getTrainInfo().getArriveTime() + "</b> [点击查询信息]");
                        } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                            walk += step.getDistance();
                        }
                    } else if (transitStepList.size() > 1) {
                        details.add(transitStepList.get(0).getInstructions());
                        for (MassTransitRouteLine.TransitStep step : transitStepList) {
                            if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS) {
                                msg += step.getBusInfo().getName() + " / ";
                                details.add("<b>" + step.getBusInfo().getName() + "  " + step.getBusInfo().getDepartureTime() + "-" + step.getBusInfo().getArriveTime() + "</b> [点击查询信息]");
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_TRAIN) {
                                msg += step.getTrainInfo().getName() + " / ";
                                details.add("<b>" + step.getTrainInfo().getName() + "  " + step.getTrainInfo().getDepartureTime() + "-" + step.getTrainInfo().getArriveTime() + "</b> [点击查询信息]");
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                                walk += step.getDistance();
                            }
                        }

                        if (msg.endsWith(" / ")) {
                            msg = msg.substring(0, msg.length() - 3) + " > ";
                        }
                    }
                }
            }

        } else {
            if (line.getNewSteps() != null) {
                for (List<MassTransitRouteLine.TransitStep> transitStepList : line.getNewSteps()) {
                    for (MassTransitRouteLine.TransitStep step : transitStepList) {
                        details.add(step.getInstructions());
                        if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS) {
                            msg += step.getBusInfo().getName() + " > ";
                            details.add( "<b>" + step.getBusInfo().getName() + "  " + step.getBusInfo().getDepartureTime() + "-" + step.getBusInfo().getArriveTime() + " </b> [点击查询信息]");
                        } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_TRAIN) {
                            msg += step.getTrainInfo().getName() + " > ";
                            details.add("<b>" + step.getTrainInfo().getName() + "  " + step.getTrainInfo().getDepartureTime() + "-" + step.getTrainInfo().getArriveTime() + " </b> [点击查询信息]");
                        } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_PLANE) {
                            msg += step.getPlaneInfo().getName() + " > ";
                        } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_COACH) {
                            msg += step.getCoachInfo().getName() + " > ";
                        } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                            walk += step.getDistance();
                        }
                    }

                }

            }

        }

        if (null != line.getNewSteps() && !line.getNewSteps().isEmpty()
                && null != line.getNewSteps().get(0) && !line.getNewSteps().get(0).isEmpty()
                && null != line.getNewSteps().get(0).get(0).getEndLocation()
                && line.getNewSteps().get(0).get(0).getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
            mPoiFirst = new MyPoiModel(TypeMap.TYPE_BAIDU);
            mPoiFirst.setLongitude(line.getNewSteps().get(0).get(0).getEndLocation().longitude);
            mPoiFirst.setLatitude(line.getNewSteps().get(0).get(0).getEndLocation().latitude);
        }

        if (1000 > walk) {
            info += " - 步行" + walk + "米";
        } else {
            info += " - 步行" + String.format("%.1f", (double) walk / 1000) + "公里";
        }

        double price = mBusRoute.getPrice();
        if (price > 0) {
            info += " - " + String.format("%.2f", price) + "元";
        }
        mTextRoute.setText(info);

        if (msg.endsWith(" / ")) {
            mTextInfo.setText(msg.substring(0, msg.length() - 3));
        } else if (msg.endsWith(" > ")) {
            mTextInfo.setText(msg.substring(0, msg.length() - 3));
        } else {
            mTextInfo.setText(msg);
        }
        setRouteDetailsAdapter(details);
    }

    public void setRouteDetailsAdapter(List<String> list) {
        if (null == mRouteDetailsAdapter) {
            mRouteDetailsAdapter = new RouteDetailsAdapter(this, list);
            mRouteDetailsAdapter.setOnItemClickListener(this);
            mRecyclerDetails.setAdapter(mRouteDetailsAdapter);
        } else {
            mRouteDetailsAdapter.setList(list);
            mRouteDetailsAdapter.notifyDataSetChanged();
        }
    }

    private void setTransitOverlay(TransitRouteLine line) {
        mBaiduMap.clear();
        TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
        mBaiduMap.setOnMarkerClickListener(overlay);
        overlay.setData(line);
        overlay.addToMap();
        if (null != mBaiduMap.getLocationConfigeration() && mBaiduMap.getLocationConfigeration().locationMode != MyLocationConfiguration.LocationMode.COMPASS) {
            overlay.zoomToSpan();
        }

    }

    private void setMassTransitOverlay(MassTransitRouteLine line) {
        mBaiduMap.clear();
        MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mBaiduMap);
        if (null != mBusRoute) {
            overlay.setSameCity(mBusRoute.isSameCity());
        }
        mBaiduMap.setOnMarkerClickListener(overlay);
        overlay.setData(line);
        overlay.addToMap();

//        if (null != mBaiduMap.getLocationConfigeration() && mBaiduMap.getLocationConfigeration().locationMode != MyLocationConfiguration.LocationMode.COMPASS) {
        overlay.zoomToSpan();
//        }

    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLayAppBar = getView(R.id.lay_app_bar);
        mTextInfo = getView(R.id.text_info);
        mTextRoute = getView(R.id.text_route);
        mRecyclerDetails = getView(R.id.recycler_details);
        mMapView = getView(R.id.map_baidu);
        btnLocation = getView(R.id.btn_location);
        mBtnZoomIn = getView(R.id.btn_zoom_in);
        mBtnZoomOut = getView(R.id.btn_zoom_out);
        mCardZoom = getView(R.id.card_zoom);

        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        btnLocation.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerDetails.setLayoutManager(layoutManager);
        mRecyclerDetails.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 地图初始化
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启室内图
        mBaiduMap.setIndoorEnable(true);
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapStatusChangeListener(this);

        mSearchInteracter = new SearchInteracter(this, TypeMap.TYPE_BAIDU);

        myOrientationListener = new MyOrientationListener(this);
        myOrientationListener.setOnOrientationListener(this);

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
//                    btnLocation.setTranslationY(-slideOffset * AppUtils.dip2Px(RouteBaiduBusActivity.this, 200));
//                    btnNavigation.setTranslationY(-slideOffset * AppUtils.dip2Px(RouteBaiduBusActivity.this, 200));
//                }
            }
        });
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

    public void initLocationSDK() {
// 定位初始化
        mLocClient = new LocationClient(this);
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

        isFirstLoc = false;

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
                    Toast.makeText(this, "罗盘模式", Toast.LENGTH_SHORT).show();
                }

            }
        }

        isRequest = true;
        if (null != mLocClient) {
            mLocClient.start();
        }
    }

    @Override
    public void onMapLoaded() {
        mBaiduMap.setCompassPosition(new Point(AppUtils.dip2Px(this, 30), AppUtils.dip2Px(this, 30)));

        configMap();
        //setCacheMapStatus();

        initLocationSDK();

        getData();

    }

    private void configMap() {
        ConfigInteracter configInteracter = new ConfigInteracter(this);
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
        mBaiduMap.setMaxAndMinZoomLevel(20f, 3f);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (configInteracter.getZoomControlsPosition()) {
            params.rightMargin = AppUtils.dip2Px(this, 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        } else {
            params.leftMargin = AppUtils.dip2Px(this, 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }
        mCardZoom.setLayoutParams(params);

//        if (configInteracter.getZoomControlsPosition()) {
//            mMapView.setZoomControlsPosition(new Point(mMapView.getWidth() - 150, mMapView.getHeight() / 2));
//        } else {
//            mMapView.setZoomControlsPosition(new Point(20, mMapView.getHeight() / 2));
//        }
    }

    private void setCacheMapStatus() {
        CacheInteracter cacheInteracter = new CacheInteracter(this);

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
    public boolean onMarkerClick(Marker marker) {
        if (null == marker.getTitle() || marker.getTitle().isEmpty()) {
            return true;
        }
        showAlertDialog("提示", marker.getTitle(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, null);
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        if (mLayAppBar.getVisibility() == View.GONE) {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            animation.setDuration(300);
            mLayAppBar.startAnimation(animation);
            mLayAppBar.setVisibility(View.VISIBLE);
        } else {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
            animation.setDuration(300);
            mLayAppBar.startAnimation(animation);
            mLayAppBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (null != list && !list.isEmpty()) {
            if (list.get(0).getTypePoi() == TypePoi.POINT) {
                if (null == BApp.MY_LOCATION) {
                    BApp.MY_LOCATION = new MyPoiModel(BApp.TYPE_MAP);
                }
                BApp.MY_LOCATION.setCity(list.get(0).getCity());
                BApp.MY_LOCATION.setName("我的位置");

                CacheInteracter cacheInteracter = new CacheInteracter(this);
                cacheInteracter.setCity(BApp.MY_LOCATION.getCity());
            } else if (list.get(0).getTypePoi() == TypePoi.BUS_LINE || list.get(0).getTypePoi() == TypePoi.SUBWAY_LINE) {
                BusLineSearch busLineSearch = BusLineSearch.newInstance();
                busLineSearch.setOnGetBusLineSearchResultListener(this);
                busLineSearch.searchBusLine(new BusLineSearchOption().city(list.get(0).getCity()).uid(list.get(0).getUid()));
            }

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

        CacheInteracter interacter = new CacheInteracter(this);

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

            isRequest = false;
            isFirstLoc = false;

        }
    }

    @Override
    public void onItemClick(int position, String route) {
            String busName = StringUtils.filterHtml(route);
            if (busName.contains("点击查询")) {
                busName = busName.substring(0, route.lastIndexOf("  ") - 3);
                LogUtils.debug(busName);
                showProgress();
                if (mBusRoute.isSameCity()) {
                    mSearchInteracter.searchInCity(busName, mBusRoute.getOriginCity(), 0, this);
                } else {
                    mSearchInteracter.searchInCity(busName, mBusRoute.getOriginCity(), 0, this);
                }
            }
    }

    @Override
    public void onGetBusLineResult(BusLineResult busLineResult) {
        if (null != busLineResult && null != busLineResult.getStations() && !busLineResult.getStations().isEmpty()) {
            CharSequence[] sequences = new CharSequence[busLineResult.getStations().size()];
            for (int i = 0; i < busLineResult.getStations().size(); i++) {
                sequences[i] = busLineResult.getStations().get(i).getTitle();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(busLineResult.getBusLineName());
            builder.setItems(sequences, null);
            builder.create().show();
        }
    }
}
