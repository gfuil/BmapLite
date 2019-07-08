package com.baidu.mapapi.overlayutil;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.core.PoiInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示一条公交详情结果的Overlay
 */
public class BusLineOverlay extends OverlayManager {
    private PoiInfo.POITYPE type;

    private BusLineResult mBusLineResult = null;

    /**
     * 构造函数
     *
     * @param baiduMap 该BusLineOverlay所引用的 BaiduMap 对象
     */
    public BusLineOverlay(BaiduMap baiduMap, PoiInfo.POITYPE type) {
        super(baiduMap);
        this.type = type;
    }

    /**
     * 设置公交线数据
     *
     * @param result 公交线路结果数据
     */
    public void setData(BusLineResult result) {
        this.mBusLineResult = result;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mBusLineResult == null || mBusLineResult.getStations() == null) {
            return null;
        }
        List<OverlayOptions> overlayOptionses = new ArrayList<>();
        for (BusLineResult.BusStation station : mBusLineResult.getStations()) {
            overlayOptionses.add(new MarkerOptions().title(station.getTitle()).position(station.getLocation()).zIndex(20).anchor(0.5f, 0.5f).icon(getStationMarker()));
        }

        List<LatLng> points = new ArrayList<>();
        for (BusLineResult.BusStep step : mBusLineResult.getSteps()) {
            if (step.getWayPoints() != null) {
                points.addAll(step.getWayPoints());
            }
        }
        if (points.size() > 0) {
            overlayOptionses.add(new PolylineOptions().width(getRouteWidth()).color(getBusColor()).zIndex(0).points(points));
        }
        return overlayOptionses;
    }

    public BitmapDescriptor getStationMarker() {
        if (type == PoiInfo.POITYPE.SUBWAY_LINE){
            return BitmapDescriptorFactory.fromAssetWithDpi("icon_subway_station.png");
        }else {
            return BitmapDescriptorFactory.fromAssetWithDpi("icon_bus_station.png");
        }
    }

    /**
     * 覆写此方法以改变默认点击行为
     *
     * @param index 被点击的站点在
     *              {@link BusLineResult#getStations()}
     *              中的索引
     * @return 是否处理了该点击事件
     */
    public boolean onBusStationClick(int index) {

        return true;
    }

    public final boolean onMarkerClick(Marker marker) {
        if (mOverlayList != null && mOverlayList.contains(marker)) {
            return onBusStationClick(mOverlayList.indexOf(marker));
        } else {
            return false;
        }

    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }

}
