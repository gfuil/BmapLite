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
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.RouteActivity;
import me.gfuil.bmap.lite.adapter.RouteDetailsAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.MyOrientationListener;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypeNavigation;
import me.gfuil.bmap.lite.utils.AppUtils;

/**
 * 百度地图路线
 *
 * @author gfuil
 */

public class BaiduMapRouteFragment extends BaseFragment implements BaiduMap.OnMapLoadedCallback, MyOrientationListener.OnOrientationListener, OnGetRoutePlanResultListener, View.OnClickListener, BaiduMap.OnMapClickListener, OnSearchResultListener, BaiduMap.OnMapStatusChangeListener, BDLocationListener, BaiduMap.OnMapLongClickListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private FloatingActionButton btnLocation;
    private LinearLayout mLayPlan0, mLayPlanAll;
    private FrameLayout mLayNavigation;
    private CardView mCardZoom;
    private Button mBtnZoomIn, mBtnZoomOut;

    private BottomSheetBehavior mBehavior;
    private TextView mTextInfo, mTextDuration;
    private RecyclerView mRecyclerDetails;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private MyOrientationListener myOrientationListener;
    private int mXDirection = -1;
    private MyPoiModel mPoiStart, mPoiEnd;
    private TypeNavigation mType;
    private SearchInteracter mSearchInteracter;
    private RouteDetailsAdapter mRouteDetailsAdapter;

    public static BaiduMapRouteFragment newInstance() {
        return new BaiduMapRouteFragment();
    }

    public BaiduMapRouteFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_baidu_route, container, false);
        initView(view);
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
        reRoute(getArguments());
    }


    public void reRoute(Bundle bundle) {
        if (null != bundle) {
            mPoiStart = bundle.getParcelable("start");
            mPoiEnd = bundle.getParcelable("end");
            mType = (TypeNavigation) bundle.getSerializable("type");
        }
        if (null != mPoiStart && "我的位置".equals(mPoiStart.getName()) && null != BApp.MY_LOCATION) {
            mPoiStart = BApp.MY_LOCATION;
        }
        routeLine(mType);
    }

    private void routeLine(TypeNavigation type) {
        setRouteDetailsAdapter(null);
        mTextInfo.setText("");
        mTextDuration.setText("");
        mLayPlan0.setVisibility(View.GONE);
        mLayPlanAll.setVisibility(View.GONE);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (null != mPoiStart && null != mPoiEnd) {
            if (null != mBaiduMap) {
                mBaiduMap.clear();

            }
            PlanNode stNode = PlanNode.withLocation(new LatLng(mPoiStart.getLatitude(), mPoiStart.getLongitude()));
            PlanNode enNode = PlanNode.withLocation(new LatLng(mPoiEnd.getLatitude(), mPoiEnd.getLongitude()));
            RoutePlanSearch planSearch = RoutePlanSearch.newInstance();
            planSearch.setOnGetRoutePlanResultListener(this);
            if (type == TypeNavigation.WALK) {
                planSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
            } else if (type == TypeNavigation.BUS) {
                planSearch.transitSearch(new TransitRoutePlanOption().from(stNode).to(enNode).city(mPoiStart.getCity()));
            } else if (type == TypeNavigation.BIKE) {
                planSearch.bikingSearch(new BikingRoutePlanOption().from(stNode).to(enNode));
            } else if (type == TypeNavigation.DRIVE) {
                planSearch.drivingSearch(new DrivingRoutePlanOption().from(stNode).to(enNode)
                        .trafficPolicy(DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH_AND_TRAFFIC)
                        .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_AVOID_JAM));
            }
        }
    }

    @Override
    protected void initView(View view) {
        btnLocation = getView(view, R.id.btn_location);
        mMapView = getView(view, R.id.map_baidu);
        mLayPlan0 = getView(view, R.id.lay_plan_0);
        mLayPlanAll = getView(view, R.id.lay_plan_all);
        mTextInfo = getView(view, R.id.text_info);
        mRecyclerDetails = getView(view, R.id.recycler_details);
        mTextDuration = getView(view, R.id.text_duration);
        mBtnZoomIn = getView(view, R.id.btn_zoom_in);
        mBtnZoomOut = getView(view, R.id.btn_zoom_out);
        mCardZoom = getView(view, R.id.card_zoom);

        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        btnLocation.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerDetails.setLayoutManager(layoutManager);
        mRecyclerDetails.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        // 地图初始化
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启室内图
        mBaiduMap.setIndoorEnable(true);
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLongClickListener(this);
        mBaiduMap.setOnMapStatusChangeListener(this);

        mSearchInteracter = new SearchInteracter(getActivity(), TypeMap.TYPE_BAIDU);

        myOrientationListener = new MyOrientationListener(getActivity());
        myOrientationListener.setOnOrientationListener(this);

        mLayNavigation = getView(view, R.id.lay_navigation);
        mBehavior = BottomSheetBehavior.from(mLayNavigation);
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                    btnLocation.setVisibility(View.VISIBLE);
                    mCardZoom.setVisibility(View.VISIBLE);
                }else if (newState == BottomSheetBehavior.STATE_DRAGGING|| newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_SETTLING){
                    btnLocation.setVisibility(View.GONE);
                    mCardZoom.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                if (0 <= slideOffset && 1 >= slideOffset) {
//                    btnLocation.setTranslationY(-slideOffset * AppUtils.dip2Px(getActivity(), 200));
//                    btnNavigation.setTranslationY(-slideOffset * AppUtils.dip2Px(getActivity(), 200));
//                }
            }
        });
    }

    public void initLocationSDK() {
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
                    Toast.makeText(getActivity(), "罗盘模式", Toast.LENGTH_SHORT).show();
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

        mBaiduMap.setCompassPosition(new Point(AppUtils.dip2Px(getActivity(), 30), AppUtils.dip2Px(getActivity(), 30)));

        configMap();
        setCacheMapStatus();

        initLocationSDK();

        getData();

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
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                    builder.zoom(mBaiduMap.getMapStatus().zoom + 5f);
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
        ((RouteActivity) getActivity()).showToolbar();
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        final MyPoiModel poiClickNow = new MyPoiModel(TypeMap.TYPE_BAIDU);
        poiClickNow.setName(mapPoi.getName());
        poiClickNow.setLatitude(mapPoi.getPosition().latitude);
        poiClickNow.setLongitude(mapPoi.getPosition().longitude);
        poiClickNow.setUid(mapPoi.getUid());
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("您要干什么");
        builder.setMessage(mapPoi.getName());
        builder.setPositiveButton("到这里去", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((RouteActivity) getActivity()).resetEnd(poiClickNow);
            }
        });
        builder.setNegativeButton("从这里出发", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((RouteActivity) getActivity()).resetStart(poiClickNow);
            }
        });
        builder.create().show();


        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        final MyPoiModel poiClickNow = new MyPoiModel(TypeMap.TYPE_BAIDU);
        poiClickNow.setName("您长按的位置");
        poiClickNow.setLatitude(latLng.latitude);
        poiClickNow.setLongitude(latLng.longitude);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("您要干什么");
        builder.setMessage(poiClickNow.getName());
        builder.setPositiveButton("到这里去", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((RouteActivity) getActivity()).resetEnd(poiClickNow);
            }
        });
        builder.setNegativeButton("从这里出发", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((RouteActivity) getActivity()).resetStart(poiClickNow);
            }
        });
        builder.create().show();
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

    public void setRouteDetailsAdapter(List<String> list){
            if (null == mRouteDetailsAdapter) {
                mRouteDetailsAdapter = new RouteDetailsAdapter(getActivity(), list);
                mRecyclerDetails.setAdapter(mRouteDetailsAdapter);
            } else {
                mRouteDetailsAdapter.setList(list);
                mRouteDetailsAdapter.notifyDataSetChanged();
            }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        hideProgress();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            onMessage("未找到路线");
        } else {
            if (null != result.getRouteLines() && !result.getRouteLines().isEmpty()) {
                setWalkingOverlay(result.getRouteLines().get(0));

                WalkingRouteLine line = result.getRouteLines().get(0);
                String info = "";
                String durationString = "";
                List<String> details = new ArrayList<>();
                for (int i = 0; i < line.getAllStep().size(); i++) {
                    details.add(line.getAllStep().get(i).getInstructions());
                }

                int duration = line.getDuration() / 60;
                int distance = line.getDistance();
                if (1000 > distance) {
                    info = distance + "米";
                } else {
                    info = String.format("%.1f", (double) distance / 1000) + "公里";
                }

                if (duration > 60) {
                    durationString = duration / 60 + "小时" + duration % 60 + "分钟";
                } else {
                    durationString = duration + "分钟";
                }

                mTextInfo.setText(info);
                mTextDuration.setText(durationString);
                setRouteDetailsAdapter(details);

                mLayPlanAll.setVisibility(View.GONE);
                mLayPlan0.setVisibility(View.VISIBLE);
            } else {
                onMessage("未找到路线");
            }
        }

    }

    @Override
    public void onGetTransitRouteResult(final TransitRouteResult result) {

    }


    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {

    }

    @Override
    public void onGetDrivingRouteResult(final DrivingRouteResult result) {
        hideProgress();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            onMessage("未找到路线");
        } else {
            if (null != result.getRouteLines() && !result.getRouteLines().isEmpty()) {
                if (result.getRouteLines().size() > 1) {
                    mLayPlanAll.removeAllViews();

                    for (int i = 0; i < result.getRouteLines().size(); i++) {
                        final DrivingRouteLine line = result.getRouteLines().get(i);
                        String msg = "";
                        int distance = line.getDistance();
                        if (1000 > distance) {
                            msg += distance + "米\n";
                        } else if (1000 <= distance) {
                            msg += String.format("%.1f", (double) distance / 1000) + "公里\n";
                        }

                        msg += ((line.getLightNum() > 0) ? (line.getLightNum() + "个") : "没有") + "红绿灯\n";

                        msg += (line.getDuration() / 60) + "分钟";
                        if (null != getActivity()) {
                            final TextView textView = new TextView(getActivity());
                            textView.setText(msg);
                            textView.setTextSize(12);
                            textView.setBackgroundResource(R.drawable.bg_btn_poi_selector);
                            textView.setGravity(Gravity.CENTER);

                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    for (int j = 0; j < mLayPlanAll.getChildCount(); j++) {
                                        if (textView.equals(mLayPlanAll.getChildAt(j))) {
                                            mLayPlanAll.getChildAt(j).setBackgroundResource(R.color.colorPressed);
                                        } else if (mLayPlanAll.getChildAt(j) instanceof TextView) {
                                            mLayPlanAll.getChildAt(j).setBackgroundResource(R.drawable.bg_btn_poi_selector);
                                        }
                                    }

                                    List<String> details = new ArrayList<>();
                                    for (int k = 0; k < line.getAllStep().size(); k++) {
                                        details.add(line.getAllStep().get(k).getInstructions());
                                    }

                                    setRouteDetailsAdapter(details);

                                    setDrivingOverlay(line);
                                }
                            });

                            LinearLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                            layoutParams.weight = 1;

                            mLayPlanAll.addView(textView, layoutParams);

                            if (i < result.getRouteLines().size() - 1) {
                                View lineView = new View(getActivity());
                                lineView.setBackgroundResource(R.color.colorPressed);
                                LinearLayout.LayoutParams layoutParams2 = new AppBarLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
                                mLayPlanAll.addView(lineView, layoutParams2);
                            }
                        }
                        mLayPlanAll.setVisibility(View.VISIBLE);
                        mLayPlan0.setVisibility(View.GONE);

                        mLayPlanAll.getChildAt(0).setBackgroundResource(R.color.colorPressed);

                        List<String> details = new ArrayList<>();
                        for (int k = 0; k < result.getRouteLines().get(0).getAllStep().size(); k++) {
                            details.add(result.getRouteLines().get(0).getAllStep().get(k).getInstructions());
                        }
                        setRouteDetailsAdapter(details);
                    }
                } else {
                    DrivingRouteLine line = result.getRouteLines().get(0);
                    String msg = "";

                    int distance = line.getDistance();
                    if (1000 > distance) {
                        msg += distance + "米 - ";
                    } else if (1000 <= distance) {
                        msg += String.format("%.1f", (double) distance / 1000) + "公里 - ";
                    }

                    msg += ((line.getLightNum() > 0) ? (line.getLightNum() + "个") : "没有") + "红绿灯";

                    List<String> details = new ArrayList<>() ;
                    for (int k = 0; k < result.getRouteLines().get(0).getAllStep().size(); k++) {
                        details.add(result.getRouteLines().get(0).getAllStep().get(k).getInstructions());
                    }

                    String durationString = (line.getDuration() / 60) + "分钟";
//                    int duration = line.getDuration();
//                    if (duration > 60) {
//                        durationString = duration / 60 + "小时" + duration % 60 + "分钟";
//                    } else {
//                        durationString = duration + "分钟";
//                    }
                    mTextInfo.setText(msg);
                    mTextDuration.setText(durationString);
                    setRouteDetailsAdapter(details);

                    mLayPlanAll.setVisibility(View.GONE);
                    mLayPlan0.setVisibility(View.VISIBLE);
                }
                setDrivingOverlay(result.getRouteLines().get(0));


            } else {
                onMessage("未找到路线");
            }
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult result) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        hideProgress();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            onMessage("未找到路线");
        } else {
            if (null != result.getRouteLines() && !result.getRouteLines().isEmpty()) {
                setBikeOverlay(result.getRouteLines().get(0));
                BikingRouteLine line = result.getRouteLines().get(0);
                String info = "";
                List<String> details = new ArrayList<>();
                String durationString = "";

                for (int i = 0; i < line.getAllStep().size(); i++) {
                    details.add(line.getAllStep().get(i).getInstructions());
                }

                int duration = line.getDuration() / 60;
                int distance = line.getDistance();
                if (1000 > distance) {
                    info = distance + "米";
                } else {
                    info = String.format("%.1f", (double) distance / 1000) + "公里";
                }

                if (duration > 60) {
                    durationString = duration / 60 + "小时" + duration % 60 + "分钟";
                } else {
                    durationString = duration + "分钟";
                }

                setRouteDetailsAdapter(details);
                mTextInfo.setText(info);
                mTextDuration.setText(durationString);
                mLayPlanAll.setVisibility(View.GONE);
                mLayPlan0.setVisibility(View.VISIBLE);
            } else {
                onMessage("未找到路线");
            }
        }
    }


    private void setDrivingOverlay(DrivingRouteLine line) {
        mBaiduMap.clear();
        DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
        mBaiduMap.setOnMarkerClickListener(overlay);
        overlay.setData(line);
        overlay.addToMap();
        if (null != mMapView && null != mBaiduMap.getLocationConfiguration() && mBaiduMap.getLocationConfiguration().locationMode != MyLocationConfiguration.LocationMode.COMPASS) {
            overlay.zoomToSpan();
        }

    }

    private void setBikeOverlay(BikingRouteLine line) {
        mBaiduMap.clear();
        BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
        mBaiduMap.setOnMarkerClickListener(overlay);
        overlay.setData(line);
        overlay.addToMap();
        if (null != mMapView && null != mBaiduMap.getLocationConfiguration() && mBaiduMap.getLocationConfiguration().locationMode != MyLocationConfiguration.LocationMode.COMPASS) {
            overlay.zoomToSpan();
        }

    }

    private void setWalkingOverlay(WalkingRouteLine line) {
        mBaiduMap.clear();
        WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
        mBaiduMap.setOnMarkerClickListener(overlay);
        overlay.setData(line);
        overlay.addToMap();
        if (null != mMapView && null != mBaiduMap.getLocationConfiguration() && mBaiduMap.getLocationConfiguration().locationMode != MyLocationConfiguration.LocationMode.COMPASS) {
            overlay.zoomToSpan();
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
                    || location.getLatitude() == 4.9E-324 || location.getLongitude() == 4.9E-324){
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
    public void onMessage(String msg) {
        Snackbar.make(btnLocation, msg, Snackbar.LENGTH_SHORT).show();
    }
}
