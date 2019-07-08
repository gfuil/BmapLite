/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapapi.overlayutil;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.search.route.MassTransitRouteLine;

import android.os.Bundle;

public class MassTransitRouteOverlay extends OverlayManager {

    private MassTransitRouteLine mRouteLine;
    private boolean isSameCity;

    /**
     * 构造函数
     *
     * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
     */
    public MassTransitRouteOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }


    /**
     * 设置路线数据
     *
     * @param routeOverlay 路线数据
     */
    public void setData(MassTransitRouteLine routeOverlay) {
        this.mRouteLine = routeOverlay;
    }

    public void setSameCity(boolean sameCity) {
        isSameCity = sameCity;
    }

    @Override
    public List<OverlayOptions> getOverlayOptions() {
        if (mRouteLine == null) {
            return null;
        }

        List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
        List<List<MassTransitRouteLine.TransitStep>> steps = mRouteLine.getNewSteps();
        if (isSameCity) {
            // 同城 (同城时，每个steps的get(i)对应的List是一条step的不同方案，此处都选第一条进行绘制，即get（0））

            // step node
            for (int i = 0; i < steps.size(); i++) {

                MassTransitRouteLine.TransitStep step = steps.get(i).get(0);
                Bundle b = new Bundle();
                b.putInt("index", i + 1);

                if (step.getStartLocation() != null) {
                    overlayOptionses.add((new MarkerOptions()).position(step.getStartLocation())
                            .title(step.getInstructions())
                            .anchor(0.5f, 0.5f).zIndex(20).extraInfo(b).icon(getIconForStep(step)));
                }

                // 最后一个终点
                if ((i == steps.size() - 1) && (step.getEndLocation() != null)) {
                    overlayOptionses.add((new MarkerOptions()).position(step.getEndLocation())
                            .title(step.getInstructions())
                            .anchor(0.5f, 0.5f).zIndex(20)
                            .icon(getIconForStep(step))
                    );
                }

            }

            // polyline
            for (int i = 0; i < steps.size(); i++) {

                MassTransitRouteLine.TransitStep step = steps.get(i).get(0);
                if (step.getWayPoints() != null && step.getWayPoints().size() > 1) {
                    int color = getBusColor();
                    boolean dotted = false;
                    if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                        color = getWalkColor();
                        dotted = true;
                    }

                    overlayOptionses.add(new PolylineOptions().points(step.getWayPoints()).dottedLine(dotted).width(getRouteWidth()).color(color).zIndex(0));
                }
            }

        } else {
            // 跨城 （跨城时，每个steps的get(i)对应的List是一条step的子路线sub_step，需要将它们全部拼接才是一条完整路线）
            int stepSum = 0;
            for (int i = 0; i < steps.size(); i++) {
                stepSum += steps.get(i).size();
            }

            // step node
            int k = 1;
            for (int i = 0; i < steps.size(); i++) {

                for (int j = 0; j < steps.get(i).size(); j++) {
                    MassTransitRouteLine.TransitStep step = steps.get(i).get(j);
                    Bundle b = new Bundle();
                    b.putInt("index", k);

                    if (step.getStartLocation() != null) {
                        overlayOptionses.add((new MarkerOptions()).position(step.getStartLocation())
                                .title(step.getInstructions())
                                .anchor(0.5f, 0.5f).zIndex(20).extraInfo(b).icon(getIconForStep(step)));
                    }

                    // 最后一个终点
                    if ((k == stepSum) && (step.getEndLocation() != null)) {
                        overlayOptionses.add((new MarkerOptions()).position(step.getEndLocation())
                                .title(step.getInstructions())
                                .anchor(0.5f, 0.5f).zIndex(20).icon(getIconForStep(step)));
                    }

                    k++;
                }
            }


            // polyline
            for (int i = 0; i < steps.size(); i++) {

                for (int j = 0; j < steps.get(i).size(); j++) {
                    MassTransitRouteLine.TransitStep step = steps.get(i).get(j);
                    if (step.getWayPoints() != null && step.getWayPoints().size() > 1) {
                        int color = getBusColor();
                        boolean dotted = false;
                        if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                            color = getWalkColor();
                            dotted = true;
                        }
                        overlayOptionses.add(new PolylineOptions().points(step.getWayPoints()).dottedLine(dotted).width(getRouteWidth()).color(color).zIndex(0));
                    }
                }
            }

        }

        // 起点
        if (mRouteLine.getStarting() != null && mRouteLine.getStarting().getLocation() != null) {
            overlayOptionses.add((new MarkerOptions()).title(mRouteLine.getStarting().getTitle()).position(mRouteLine.getStarting().getLocation()).icon(getStartMarker()).zIndex(20));
        }
        // 终点
        if (mRouteLine.getTerminal() != null && mRouteLine.getTerminal().getLocation() != null) {
            overlayOptionses.add((new MarkerOptions()).title(mRouteLine.getTerminal().getTitle()).position(mRouteLine.getTerminal().getLocation()).icon(getTerminalMarker()).zIndex(20));
        }

        return overlayOptionses;

    }

    private BitmapDescriptor getIconForStep(MassTransitRouteLine.TransitStep step) {
        switch (step.getVehileType()) {
            case ESTEP_WALK:
                return BitmapDescriptorFactory.fromAssetWithDpi("icon_walk_route.png");
            case ESTEP_TRAIN:
                return BitmapDescriptorFactory.fromAssetWithDpi("icon_subway_station.png");
            case ESTEP_DRIVING:
            case ESTEP_COACH:
            case ESTEP_PLANE:
            case ESTEP_BUS:
                return BitmapDescriptorFactory.fromAssetWithDpi("icon_bus_station.png");
            default:
                return null;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
}
