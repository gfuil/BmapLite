package com.baidu.mapapi.overlayutil;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.search.route.TransitRouteLine;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示换乘路线的Overlay，自3.4.0版本起可实例化多个添加在地图中显示
 */
public class TransitRouteOverlay extends OverlayManager {

    private TransitRouteLine mRouteLine = null;

    /**
     * 构造函数
     *
     * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
     */
    public TransitRouteOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {

        if (mRouteLine == null) {
            return null;
        }

        List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
        // step node
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().size() > 0) {

            for (TransitRouteLine.TransitStep step : mRouteLine.getAllStep()) {
                Bundle b = new Bundle();
                b.putInt("index", mRouteLine.getAllStep().indexOf(step));
                if (step.getEntrance() != null) {
                    overlayOptionses.add((new MarkerOptions())
                            .title(step.getInstructions())
                            .position(step.getEntrance().getLocation())
                            .anchor(0.5f, 0.5f).zIndex(20).extraInfo(b)
                            .icon(getIconForStep(step)));
                }
                // 最后路段绘制出口点
                if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                        .getAllStep().size() - 1) && step.getExit() != null) {
                    overlayOptionses.add((new MarkerOptions())
                            .title(step.getInstructions())
                            .position(step.getExit().getLocation())
                            .anchor(0.5f, 0.5f).zIndex(20)
                            .icon(getIconForStep(step)));
                }
            }
        }

        if (mRouteLine.getStarting() != null) {
            overlayOptionses.add((new MarkerOptions())
                    .title(mRouteLine.getStarting().getTitle())
                    .position(mRouteLine.getStarting().getLocation())
                    .icon(getStartMarker()).zIndex(20));
        }
        if (mRouteLine.getTerminal() != null) {
            overlayOptionses
                    .add((new MarkerOptions())
                            .title(mRouteLine.getTerminal().getTitle())
                            .position(mRouteLine.getTerminal().getLocation())
                            .icon(getTerminalMarker())
                            .zIndex(20));
        }
        // polyline
        if (mRouteLine.getAllStep() != null && mRouteLine.getAllStep().size() > 0) {

            for (TransitRouteLine.TransitStep step : mRouteLine.getAllStep()) {
                if (step.getWayPoints() == null) {
                    continue;
                }
                int color = getBusColor();
                boolean dotted = false;
                if (step.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING) {
                    color = getWalkColor();
                    dotted = true;
                }
                overlayOptionses.add(new PolylineOptions().dottedLine(dotted)
                        .points(step.getWayPoints()).width(getRouteWidth()).color(color)
                        .zIndex(0));
            }
        }
        return overlayOptionses;
    }

    private BitmapDescriptor getIconForStep(TransitRouteLine.TransitStep step) {
        switch (step.getStepType()) {
            case BUSLINE:
                return BitmapDescriptorFactory.fromAssetWithDpi("icon_bus_station.png");
            case SUBWAY:
                return BitmapDescriptorFactory.fromAssetWithDpi("icon_subway_station.png");
            case WAKLING:
                return BitmapDescriptorFactory.fromAssetWithDpi("icon_walk_route.png");
            default:
                return null;
        }
    }

    /**
     * 设置路线数据
     *
     * @param routeOverlay 路线数据
     */
    public void setData(TransitRouteLine routeOverlay) {
        this.mRouteLine = routeOverlay;
    }

    /**
     * 覆写此方法以改变起默认点击行为
     *
     * @param index 被点击的step在
     *          {@link TransitRouteLine#getAllStep()}
     *          中的索引
     * @return 是否处理了该点击事件
     */
    public boolean onRouteNodeClick(int index) {
        if (mRouteLine.getAllStep() != null && mRouteLine.getAllStep().get(index) != null) {

        }
        return false;
    }

    @Override
    public final boolean onMarkerClick(Marker marker) {
        for (Overlay mMarker : mOverlayList) {
            if (mMarker instanceof Marker && mMarker.equals(marker)) {
                if (marker.getExtraInfo() != null) {
                    onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                }
            }
        }
        return true;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }


}
