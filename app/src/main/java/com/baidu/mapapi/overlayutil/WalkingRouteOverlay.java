package com.baidu.mapapi.overlayutil;

import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.WalkingRouteLine;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示步行路线的overlay，自3.4.0版本起可实例化多个添加在地图中显示
 */
public class WalkingRouteOverlay extends OverlayManager {

    private WalkingRouteLine mRouteLine = null;

    public WalkingRouteOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置路线数据。
     * 
     * @param line
     *            路线数据
     */
    public void setData(WalkingRouteLine line) {
        mRouteLine = line;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mRouteLine == null) {
            return null;
        }

        List<OverlayOptions> overlayList = new ArrayList<>();

        // starting
        if (mRouteLine.getStarting() != null) {
            overlayList.add((new MarkerOptions()).title(mRouteLine.getStarting().getTitle()).position(mRouteLine.getStarting().getLocation()).icon(getStartMarker()).zIndex(20));
        }
        // terminal
        if (mRouteLine.getTerminal() != null) {
            overlayList.add((new MarkerOptions()).title(mRouteLine.getTerminal().getTitle()).position(mRouteLine.getTerminal().getLocation()).icon(getTerminalMarker()).zIndex(20));
        }

        // poly line list
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().size() > 0) {
            LatLng lastStepLastPoint = null;
            for (WalkingRouteLine.WalkingStep step : mRouteLine.getAllStep()) {
                List<LatLng> watPoints = step.getWayPoints();
                if (watPoints != null) {
                    List<LatLng> points = new ArrayList<>();
                    if (lastStepLastPoint != null) {
                        points.add(lastStepLastPoint);
                    }
                    points.addAll(watPoints);
                    overlayList.add(new PolylineOptions().points(points).width(getRouteWidth()).color(getWalkColor()).zIndex(0));
                    lastStepLastPoint = watPoints.get(watPoints.size() - 1);
                }
            }
            
        }

        return overlayList;
    }


    /**
     * 处理点击事件
     * 
     * @param i
     *            被点击的step在
     *            {@link WalkingRouteLine#getAllStep()}
     *            中的索引
     * @return 是否处理了该点击事件
     */
    public boolean onRouteNodeClick(int i) {
        if (mRouteLine.getAllStep() != null && mRouteLine.getAllStep().get(i) != null) {
            Log.i("baidumapsdk", "WalkingRouteOverlay onRouteNodeClick");
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
