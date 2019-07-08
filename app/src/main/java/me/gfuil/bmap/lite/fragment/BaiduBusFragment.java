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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PriceInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.RouteBaiduBusActivity;
import me.gfuil.bmap.lite.adapter.BaiduBusRouteAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.model.BusRouteModel;
import me.gfuil.bmap.lite.model.MyPoiModel;

/**
 * 百度地图公交路线
 *
 * @author gfuil
 */

public class BaiduBusFragment extends BaseFragment implements OnGetRoutePlanResultListener, AdapterView.OnItemClickListener {
    private TextView mTextData;
    private ListView mListBusRoute;
    private MyPoiModel mPoiStart, mPoiEnd;
    private BaiduBusRouteAdapter mBusRouteAdapter;

    public static BaiduBusFragment newInstance() {
        return new BaiduBusFragment();
    }

    public BaiduBusFragment() {

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
        reRoute(getArguments());
    }


    public void reRoute(Bundle bundle) {
        if (null != bundle) {
            mPoiStart = bundle.getParcelable("start");
            mPoiEnd = bundle.getParcelable("end");
        }
        if (null == mPoiStart) {
            mPoiStart = BApp.MY_LOCATION;
        }
        if (null != BApp.MY_LOCATION && "我的位置".equals(mPoiStart.getName())) {
            mPoiStart = BApp.MY_LOCATION;
        }

        if (null != mPoiStart && null != mPoiEnd) {
            PlanNode stNode = PlanNode.withLocation(new LatLng(mPoiStart.getLatitude(), mPoiStart.getLongitude()));
            PlanNode enNode = PlanNode.withLocation(new LatLng(mPoiEnd.getLatitude(), mPoiEnd.getLongitude()));
            RoutePlanSearch planSearch = RoutePlanSearch.newInstance();
            planSearch.setOnGetRoutePlanResultListener(this);
            planSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stNode).to(enNode));
        }


    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {

    }

    @Override
    public void onGetTransitRouteResult(final TransitRouteResult result) {
        hideProgress();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            mTextData.setVisibility(View.VISIBLE);
            mListBusRoute.setVisibility(View.GONE);
        } else {
            if (null != result.getRouteLines() && !result.getRouteLines().isEmpty()) {
                List<BusRouteModel> list = new ArrayList<>();
                for (int i = 0; i < result.getRouteLines().size(); i++) {
                    TransitRouteLine line = result.getRouteLines().get(i);
                    String msg = "";
                    for (int j = 0; j < line.getAllStep().size(); j++) {
                        TransitRouteLine.TransitStep step = line.getAllStep().get(j);
                        if (step.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE
                                || step.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.SUBWAY) {
                            msg += line.getAllStep().get(j).getVehicleInfo().getTitle() + " -> ";
                        }
                    }
                    BusRouteModel route = new BusRouteModel();
                    route.setName(msg);
                    route.setDistance(line.getDistance());
                    route.setDuration(line.getDuration());
                    list.add(route);
                }

                setBusRouteAdapter(list);

                mTextData.setVisibility(View.GONE);
                mListBusRoute.setVisibility(View.VISIBLE);


            } else {
                mTextData.setVisibility(View.VISIBLE);
                mListBusRoute.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
        hideProgress();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            mTextData.setVisibility(View.VISIBLE);
            mListBusRoute.setVisibility(View.GONE);
        } else {
            List<BusRouteModel> list = new ArrayList<>();
            if (null != result.getRouteLines() && !result.getRouteLines().isEmpty()) {
                for (MassTransitRouteLine line : result.getRouteLines()) {
                    double price = 0;
                    if (null != line.getPriceInfo() && !line.getPriceInfo().isEmpty()) {
                        for (PriceInfo priceInfo : line.getPriceInfo()) {
                            price += priceInfo.getTicketPrice();
                        }
                    }

                    BusRouteModel route = new BusRouteModel();
                    route.setLine(line);
                    route.setPrice(price);
                    route.setName("");
                    route.setTexi(false);
                    route.setDestinationCity(result.getDestination().getCityName());
                    route.setOriginCity(result.getOrigin().getCityName());
                    route.setSameCity(result.getDestination().getCityId() == result.getOrigin().getCityId());
                    route.setDistance(line.getDistance());
                    route.setDuration(line.getDuration());
                    list.add(route);
                }
                if (null != result.getTaxiInfo() && 0 < result.getTaxiInfo().getDistance() && 0 < result.getTaxiInfo().getDuration()) {
                    BusRouteModel route = new BusRouteModel();
                    route.setPrice(result.getTaxiInfo().getTotalPrice());
                    route.setName(result.getTaxiInfo().getDesc());
                    route.setTexi(true);
                    route.setSameCity(result.getDestination().getCityId() == result.getOrigin().getCityId());
                    route.setDestinationCity(result.getDestination().getCityName());
                    route.setOriginCity(result.getOrigin().getCityName());
                    route.setDistance(result.getTaxiInfo().getDistance());
                    route.setDuration(result.getTaxiInfo().getDuration());
                    list.add(route);
                }


                setBusRouteAdapter(list);

                mTextData.setVisibility(View.GONE);
                mListBusRoute.setVisibility(View.VISIBLE);

            } else {
                mTextData.setVisibility(View.VISIBLE);
                mListBusRoute.setVisibility(View.GONE);
            }
        }
    }

    private void setBusRouteAdapter(List<BusRouteModel> list) {
        if (null == mBusRouteAdapter) {
            mBusRouteAdapter = new BaiduBusRouteAdapter(getActivity(), list);
            mListBusRoute.setAdapter(mBusRouteAdapter);
        } else {
            mBusRouteAdapter.setList(list, true);
            mBusRouteAdapter.notifyDataSetChanged();
        }
        mListBusRoute.setVisibility(View.VISIBLE);
        mTextData.setVisibility(View.GONE);
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult result) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mBusRouteAdapter) {
            BusRouteModel bus = mBusRouteAdapter.getList().get(position);

            if (!bus.isTexi()) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("bus", bus);
                openActivity(RouteBaiduBusActivity.class, bundle);
            } else {
                showAlertDialog("出租车", bus.getName(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, null);
            }
        }
    }
}
