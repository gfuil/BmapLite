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
import android.location.Location;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.amap.mapapi.overlay.DrivingRouteOverlay;
import com.amap.mapapi.overlay.RideRouteOverlay;
import com.amap.mapapi.overlay.WalkRouteOverlay;

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
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypeNavigation;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;

/**
 * 高德地图路线
 *
 * @author gfuil
 */

public class AmapRouteFragment extends BaseFragment implements View.OnClickListener, AMap.OnMapClickListener, AMap.OnMapLongClickListener, AMap.OnMarkerClickListener, AMap.OnPOIClickListener, AMap.OnMapLoadedListener, AMap.OnCameraChangeListener, AMap.OnMyLocationChangeListener, OnSearchResultListener, RouteSearch.OnRouteSearchListener {
    private MapView mMapView;
    private FloatingActionButton btnLocation;
    private Button mBtnZoomIn, mBtnZoomOut;
    private CardView mCardZoom;
    private ImageView mImageCompass;
    private LinearLayout mLayPlan0, mLayPlanAll;
    private FrameLayout mLayNavigation;
    private BottomSheetBehavior mBehavior;
    private TextView mTextInfo, mTextDuration;
    private RecyclerView mRecyclerDetails;
    private AMap mAmap;
    private MyLocationStyle mLocClient;
    private MyPoiModel mPoiStart, mPoiEnd;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isRequest = false;//是否手动触发请求定位
    private SearchInteracter mSearchInteracter;
    private TypeNavigation mType;
    private RouteDetailsAdapter mRouteDetailsAdapter;

    public static AmapRouteFragment newInstance() {
        return new AmapRouteFragment();
    }

    public AmapRouteFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_amap_route, container, false);
        initView(view);
        mMapView.onCreate(savedInstanceState);
        getDate();
        return view;
    }

    private void getDate() {
        mAmap = mMapView.getMap();
        mSearchInteracter = new SearchInteracter(getActivity(), TypeMap.TYPE_AMAP);

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

        mAmap.clear();
        routeLine(mType);

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
        mLayPlan0 = getView(view, R.id.lay_plan_0);
        mLayPlanAll = getView(view, R.id.lay_plan_all);
        mTextInfo = getView(view, R.id.text_info);
        mRecyclerDetails = getView(view, R.id.recycler_details);
        mTextDuration = getView(view, R.id.text_duration);
        mBtnZoomIn = getView(view, R.id.btn_zoom_in);
        mBtnZoomOut = getView(view, R.id.btn_zoom_out);
        mCardZoom = getView(view, R.id.card_zoom);
        mImageCompass = getView(view, R.id.image_compass);

        btnLocation.setOnClickListener(this);
        mBtnZoomIn.setOnClickListener(this);
        mBtnZoomOut.setOnClickListener(this);
        mImageCompass.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerDetails.setLayoutManager(layoutManager);
        mRecyclerDetails.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mLayNavigation = getView(view, R.id.lay_navigation);
        mBehavior = BottomSheetBehavior.from(mLayNavigation);
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
//                    btnLocation.setTranslationY(-slideOffset * AppUtils.dip2Px(getActivity(), 200));
//                    btnNavigation.setTranslationY(-slideOffset * AppUtils.dip2Px(getActivity(), 200));
//                }
            }
        });
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
        if (configInteracter.getNightMode() == 2){
            mAmap.setMapType(AMap.MAP_TYPE_NIGHT);
        }else {
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
        }

        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (configInteracter.getZoomControlsPosition()) {
            params.rightMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        } else {
            params.leftMargin = AppUtils.dip2Px(getActivity(), 10);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }
        mCardZoom.setLayoutParams(params);
        CoordinatorLayout.LayoutParams params3 = new CoordinatorLayout.LayoutParams(AppUtils.dip2Px(getActivity(), 40), AppUtils.dip2Px(getActivity(), 40));
        params3.leftMargin = AppUtils.dip2Px(getActivity(), 10);
        params3.topMargin = AppUtils.dip2Px(getActivity(), 10);
        mImageCompass.setLayoutParams(params3);
    }

    private void setCacheMapStatus() {
//        CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
//        LatLng latLng = new LatLng(cacheInteracter.getLatitude(), cacheInteracter.getLongitude());
//
        if (null != BApp.MY_LOCATION) {
            mAmap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude())));
        }
        mAmap.moveCamera(CameraUpdateFactory.zoomTo(17));
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
        if (null == mPoiStart || null == mPoiEnd) {
            return;
        }

        setRouteDetailsAdapter(null);
        mTextInfo.setText("");
        mTextDuration.setText("");
        mLayPlan0.setVisibility(View.GONE);
        mLayPlanAll.setVisibility(View.GONE);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        RouteSearch routeSearch = new RouteSearch(getActivity());
        routeSearch.setRouteSearchListener(this);
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                new LatLonPoint(mPoiStart.getLatitude(), mPoiStart.getLongitude()),
                new LatLonPoint(mPoiEnd.getLatitude(), mPoiEnd.getLongitude())
        );
        if (type == TypeNavigation.WALK){
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo);
            routeSearch.calculateWalkRouteAsyn(query);
        }else if (type == TypeNavigation.BIKE){
            RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo);
            routeSearch.calculateRideRouteAsyn(query);
        }else if (type == TypeNavigation.DRIVE){
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_MULTI_CHOICE_AVOID_CONGESTION, null, null, null);
            routeSearch.calculateDriveRouteAsyn(query);
        }
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
    public void onMapClick(LatLng latLng) {
        ((RouteActivity) getActivity()).showToolbar();
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
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
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onPOIClick(Poi poi) {
        final MyPoiModel poiClickNow = new MyPoiModel(TypeMap.TYPE_BAIDU);
        poiClickNow.setName(poi.getName());
        poiClickNow.setLatitude(poi.getCoordinate().latitude);
        poiClickNow.setLongitude(poi.getCoordinate().longitude);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("您要干什么");
        builder.setMessage(poi.getName());
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
    public void onMapLoaded() {
        configMap();
        setCacheMapStatus();
        initAmapSdk();

        reRoute(getArguments());
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
                mSearchInteracter.searchLatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude(), 1, this);

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

    public void setRouteDetailsAdapter(List<String> list) {
        if (null == mRouteDetailsAdapter) {
            mRouteDetailsAdapter = new RouteDetailsAdapter(getActivity(), list);
            mRecyclerDetails.setAdapter(mRouteDetailsAdapter);
        } else {
            mRouteDetailsAdapter.setList(list);
            mRouteDetailsAdapter.notifyDataSetChanged();
        }
    }

    private void setLineInfo(int distance, int duration) {
        String info = "";
        String durationString = "";

        if (1000 > distance) {
            info = distance + "米";
        } else {
            info = String.format("%.1f", (double) distance / 1000) + "公里";
        }

        if (duration > 60 * 60) {
            durationString = duration / 60  + "小时" + duration % 60 + "分钟";
        } else {
            durationString = duration + "分钟";
        }

        mTextInfo.setText(info);
        mTextDuration.setText(durationString);
        mLayPlanAll.setVisibility(View.GONE);
        mLayPlan0.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessage(String msg) {
        Snackbar.make(btnLocation, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int code) {

    }

    @Override
    public void onDriveRouteSearched(final DriveRouteResult result, int code) {
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                LogUtils.debug("result.getPaths().size()=" + result.getPaths().size());
                if (result.getPaths().size() > 1) {
                    mLayPlanAll.removeAllViews();
                    for (int i = 0; i < result.getPaths().size(); i++) {
                        final DrivePath line = result.getPaths().get(i);
                        String msg = "";
                        int distance = (int) line.getDistance();
                        if (1000 > distance) {
                            msg += distance + "米\n";
                        } else if (1000 <= distance) {
                            msg += String.format("%.1f", (double) distance / 1000) + "公里\n";
                        }

                        msg += ((line.getTotalTrafficlights() > 0) ? (line.getTotalTrafficlights() + "个") : "没有") + "红绿灯\n";

                        msg += (line.getDuration() / 60) + "分钟";
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

                                setDrivingOverlay(line, result.getStartPos(), result.getTargetPos());

                                List<String> details = new ArrayList<>();
                                if (null != line.getSteps() && !line.getSteps().isEmpty()) {
                                    for (DriveStep step : line.getSteps()) {
                                        details.add(step.getInstruction());
                                    }
                                }
                                setRouteDetailsAdapter(details);
                            }
                        });

                        LinearLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams.weight = 1;

                        mLayPlanAll.addView(textView, layoutParams);

                        if (i < result.getPaths().size() - 1) {
                            View lineView = new View(getActivity());
                            lineView.setBackgroundResource(R.color.colorPressed);
                            LinearLayout.LayoutParams layoutParams2 = new AppBarLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
                            mLayPlanAll.addView(lineView, layoutParams2);
                        }
                    }
                    mLayPlanAll.setVisibility(View.VISIBLE);
                    mLayPlan0.setVisibility(View.GONE);

                    mLayPlanAll.getChildAt(0).setBackgroundResource(R.color.colorPressed);

                    DrivePath drivePath = result.getPaths().get(0);
                    setDrivingOverlay(drivePath, result.getStartPos(), result.getTargetPos());

                    List<String> details = new ArrayList<>();
                    if (null != drivePath.getSteps() && !drivePath.getSteps().isEmpty()) {
                        for (DriveStep step : drivePath.getSteps()) {
                            details.add(step.getInstruction());
                        }
                    }
                    setRouteDetailsAdapter(details);

                } else if (result.getPaths().size() == 1) {
                    final DrivePath drivePath = result.getPaths().get(0);
                    setDrivingOverlay(drivePath, result.getStartPos(), result.getTargetPos());

                    setLineInfo((int) drivePath.getDistance(), (int) drivePath.getDuration() / 60);

                    List<String> details = new ArrayList<>();

                    if (null != drivePath.getSteps() && !drivePath.getSteps().isEmpty()) {
                        for (DriveStep step : drivePath.getSteps()) {
                            details.add(step.getInstruction());
                        }
                    }
                    setRouteDetailsAdapter(details);

                }
            }
        }
    }

    private void setDrivingOverlay(DrivePath drivePath, LatLonPoint start, LatLonPoint target) {
        mAmap.clear();// 清理地图上的所有覆盖物
        DrivingRouteOverlay overlay = new DrivingRouteOverlay(getActivity(), mAmap, drivePath,
                start, target, null);
        overlay.setNodeIconVisibility(false);//设置节点marker是否显示
        overlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
        overlay.removeFromMap();
        overlay.addToMap();
        overlay.zoomToSpan();
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int code) {
        mAmap.clear();// 清理地图上的所有覆盖物
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    final WalkPath walkPath = result.getPaths()
                            .get(0);
                    WalkRouteOverlay overlay = new WalkRouteOverlay(getActivity(), mAmap, walkPath,
                            result.getStartPos(),
                            result.getTargetPos());
                    overlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    overlay.removeFromMap();
                    overlay.addToMap();
                    overlay.zoomToSpan();

                    setLineInfo((int) walkPath.getDistance(), (int) walkPath.getDuration() / 60);

                    List<String> details = new ArrayList<>();
                    if (null != walkPath.getSteps() && !walkPath.getSteps().isEmpty()) {
                        for (WalkStep step : walkPath.getSteps()) {
                            details.add(step.getInstruction());
                        }
                    }
                    setRouteDetailsAdapter(details);
                }
            }
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int code) {
        mAmap.clear();// 清理地图上的所有覆盖物
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    final RidePath ridePath = result.getPaths()
                            .get(0);
                    RideRouteOverlay overlay = new RideRouteOverlay(getActivity(), mAmap, ridePath,
                            result.getStartPos(),
                            result.getTargetPos());
                    overlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    overlay.removeFromMap();
                    overlay.addToMap();
                    overlay.zoomToSpan();

                    setLineInfo((int) ridePath.getDistance(), (int) ridePath.getDuration() / 60);

                    List<String> details = new ArrayList<>();

                    if (null != ridePath.getSteps() && !ridePath.getSteps().isEmpty()) {
                        for (RideStep step : ridePath.getSteps()) {
                            details.add(step.getInstruction());
                        }
                    }
                    setRouteDetailsAdapter(details);
                }
            }
        }
    }
}
