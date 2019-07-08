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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.RouteAmapBusActivity;
import me.gfuil.bmap.lite.adapter.AmapBusRouteAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;

/**
 * 高德地图公交路线
 *
 * @author gfuil
 */

public class AmapBusFragment extends BaseFragment implements AdapterView.OnItemClickListener, RouteSearch.OnRouteSearchListener, OnSearchResultListener {
    private TextView mTextData;
    private ListView mListBusRoute;
    private MyPoiModel mPoiStart, mPoiEnd;
    private RouteSearch mRouteSearch;
    private AmapBusRouteAdapter mAmapBusRouteAdapter;
    private BusRouteResult mBusRouteResult;

    public static AmapBusFragment newInstance() {
        return new AmapBusFragment();
    }

    public AmapBusFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus, container, false);
        initView(view);
        getData();
        return view;
    }

    @Override
    protected void initView(View view) {
        mTextData = getView(view, R.id.text_data);
        mListBusRoute = getView(view, R.id.list_bus_route);

        mListBusRoute.setOnItemClickListener(this);
    }

    private void getData() {
        mRouteSearch = new RouteSearch(getActivity());
        mRouteSearch.setRouteSearchListener(this);

        reRoute(getArguments());
    }

    public void reRoute(Bundle bundle) {
        if (null != bundle) {
            mPoiStart = bundle.getParcelable("start");
            mPoiEnd = bundle.getParcelable("end");
        }
        if ((mPoiStart == null || "我的位置".equals(mPoiStart.getName())) && null != BApp.MY_LOCATION) {
            mPoiStart = BApp.MY_LOCATION;
        }

        if (null != mPoiStart && null != mPoiStart.getCity()) {
            route();
        } else {
            if (null != mPoiStart) {
                SearchInteracter searchInteracter = new SearchInteracter(getActivity(), TypeMap.TYPE_AMAP);
                searchInteracter.searchLatLng(mPoiStart.getLatitude(), mPoiStart.getLongitude(), 1, this);
            }
        }


    }

    private void route() {
        if (null != mPoiStart && null != mPoiEnd) {
            LatLonPoint start = new LatLonPoint(mPoiStart.getLatitude(), mPoiStart.getLongitude());
            LatLonPoint end = new LatLonPoint(mPoiEnd.getLatitude(), mPoiEnd.getLongitude());
            RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT, mPoiStart.getCity(), 1);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BusPath bus = mAmapBusRouteAdapter.getList().get(position);

        Bundle bundle = new Bundle();
        bundle.putParcelable("bus", bus);
        bundle.putParcelable("route", mBusRouteResult);
        openActivity(RouteAmapBusActivity.class, bundle);

    }

    private void setBusRouteAdapter(List<BusPath> list, BusRouteResult result) {
        this.mBusRouteResult = result;
        if (null == mAmapBusRouteAdapter) {
            mAmapBusRouteAdapter = new AmapBusRouteAdapter(getActivity(), list);
            mListBusRoute.setAdapter(mAmapBusRouteAdapter);
        } else {
            mAmapBusRouteAdapter.setList(list, true);
            mAmapBusRouteAdapter.notifyDataSetChanged();
        }
        mListBusRoute.setVisibility(View.VISIBLE);
        mTextData.setVisibility(View.GONE);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    BusPath bus = new BusPath();
                    bus.setCost(0);
                    bus.setBusDistance(0);
                    bus.setNightBus(false);
                    bus.setWalkDistance(0);
                    bus.setSteps(null);
                    List<BusPath> list = new ArrayList<>();
                    list.addAll(result.getPaths());

                    setBusRouteAdapter(list, result);
                } else {
                    mTextData.setVisibility(View.VISIBLE);
                    mListBusRoute.setVisibility(View.GONE);
                }
            } else {
                mTextData.setVisibility(View.VISIBLE);
                mListBusRoute.setVisibility(View.GONE);
            }
        } else {
            mTextData.setVisibility(View.VISIBLE);
            mListBusRoute.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {

    }

    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (null != list && !list.isEmpty()) {
            mPoiStart.setCity(list.get(0).getCity());
            route();
        }
    }

    @Override
    public void setSuggestCityList(List<SuggestionCity> cities) {

    }
}
