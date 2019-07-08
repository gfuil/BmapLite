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

package me.gfuil.bmap.lite.interacter;

import android.content.Context;

import com.baidu.mapapi.favorite.FavoriteManager;
import com.baidu.mapapi.favorite.FavoritePoiInfo;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;

/**
 * @author gfuil
 */

public class FavoriteInteracter {
    private Context mContext;
    private FavoriteManager mFavoriteManagerBaidu;

    public FavoriteInteracter(Context context) {
        this.mContext = context;

        mFavoriteManagerBaidu = FavoriteManager.getInstance();
        mFavoriteManagerBaidu.init();
    }

    public void destroy() {
        if (null != mFavoriteManagerBaidu) {
            mFavoriteManagerBaidu.destroy();
        }
    }

    public int addFavorite(MyPoiModel poi) {
        //构造一个点信息，pt和poiName是必填项
        FavoritePoiInfo info = new FavoritePoiInfo()
                .poiName(poi.getName())
                .pt(new LatLng(poi.getLatitude(), poi.getLongitude()))
                .addr(poi.getAddress())
                .cityName(poi.getCity())
                .uid(poi.getUid());

        return mFavoriteManagerBaidu.add(info);

    }

    public boolean deleteFavorite(String id) {
        return mFavoriteManagerBaidu.deleteFavPoi(id);
    }

    public boolean clearFavorite() {
        return mFavoriteManagerBaidu.clearAllFavPois();
    }

    public List<MyPoiModel> getFavoriteList() {
        List<MyPoiModel> list = null;

        List<FavoritePoiInfo> favoritePoiInfos = mFavoriteManagerBaidu.getAllFavPois();
        if (null != favoritePoiInfos && !favoritePoiInfos.isEmpty()) {
            list = new ArrayList<>();
            for (FavoritePoiInfo favoritePoiInfo : favoritePoiInfos) {
                MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_BAIDU);
                poi.setTypePoi(TypePoi.POINT);
                poi.setName(favoritePoiInfo.getPoiName());
                poi.setLatitude(favoritePoiInfo.getPt().latitude);
                poi.setLongitude(favoritePoiInfo.getPt().longitude);
                poi.setAddress(favoritePoiInfo.getAddr());
                poi.setCity(favoritePoiInfo.getCityName());
                poi.setUid(favoritePoiInfo.getID());
                list.add(poi);
            }
        }
        return list;

    }
}
