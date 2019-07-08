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

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiSearch;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.base.OnBaseListener;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.listener.OnSearchTipsListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;

/**
 * @author gfuil
 */

public class SearchInteracter {
    private Context mContext;
    private TypeMap mType;
    private com.baidu.mapapi.search.poi.PoiSearch mPoiSearchBaidu;
    private com.baidu.mapapi.search.geocode.GeoCoder mGeoCoderBaidu;
    private SuggestionSearch mSuggestionSearch;

    public SearchInteracter(Context context, TypeMap type) {
        this.mContext = context;
        this.mType = type;

        if (TypeMap.TYPE_BAIDU == type) {
            mPoiSearchBaidu = com.baidu.mapapi.search.poi.PoiSearch.newInstance();
            mGeoCoderBaidu = com.baidu.mapapi.search.geocode.GeoCoder.newInstance();
        }
    }

    public void destroy() {
        if (null != mPoiSearchBaidu) {
            mPoiSearchBaidu.destroy();
        }
        if (null != mGeoCoderBaidu) {
            mGeoCoderBaidu.destroy();
        }
        if (null != mSuggestionSearch) {
            mSuggestionSearch.destroy();
        }
    }

    public void searchInCity(String keyword, String city, int page, OnSearchResultListener listener) {
        try {
            if (TypeMap.TYPE_BAIDU == mType) {
                searchPoiInCityByBaidu(keyword, city, page, listener);
            } else if (TypeMap.TYPE_AMAP == mType) {
                searchPoiInCityByAmap(keyword, city, page, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onMessage("搜索异常");
        }
    }

    public void searchNearby(MyPoiModel nearby, String keyword, int page, OnSearchResultListener listener) {
        try {
            if (TypeMap.TYPE_BAIDU == mType) {
                searchPoiNearbyByBaidu(nearby, keyword, page, listener);
            } else if (TypeMap.TYPE_AMAP == mType) {
                searchPoiNearbyByAmap(nearby, keyword, page, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onMessage("搜索异常");
        }
    }


    private void searchPoiNearbyByAmap(MyPoiModel nearby, String keyword, int page, final OnSearchResultListener listener) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", nearby.getCity());
        query.setPageSize(20);
        query.setPageNum(page);

        PoiSearch.SearchBound bound = new PoiSearch.SearchBound(new LatLonPoint(nearby.getLatitude(),
                nearby.getLongitude()), 20000);

        PoiSearch poiSearchAmap = new PoiSearch(mContext, query);
        poiSearchAmap.setBound(bound);
        poiSearchAmap.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(com.amap.api.services.poisearch.PoiResult poiResult, int code) {
                if (1000 == code) {
                    if (null != poiResult && null != poiResult.getPois() && !poiResult.getPois().isEmpty()) {
                        List<MyPoiModel> list = new ArrayList<>();

                        for (PoiItem poi : poiResult.getPois()) {
                            MyPoiModel myPoi = new MyPoiModel(mType);
                            myPoi.setCity(poi.getCityName());
                            myPoi.setUid(poi.getPoiId());
                            myPoi.setName(poi.getTitle());
                            myPoi.setInfo(poi.getTel());
                            myPoi.setAddress(poi.getSnippet());
                            myPoi.setLatitude(poi.getLatLonPoint().getLatitude());
                            myPoi.setLongitude(poi.getLatLonPoint().getLongitude());
                            myPoi.setTypePoi(TypePoi.POINT);
                            list.add(myPoi);
                        }
                        listener.setSearchResult(list);
                        listener.onShowData("search");
                    } else {
                        listener.onNoData("search");
                    }
                } else {
                    listener.onNoData("search");
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearchAmap.searchPOIAsyn();
    }

    private void searchPoiNearbyByBaidu(MyPoiModel nearby, String keyword, int page, final OnSearchResultListener listener) {
        mPoiSearchBaidu.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (null != poiResult && null != poiResult.getAllPoi() && !poiResult.getAllPoi().isEmpty()) {
                    List<MyPoiModel> list = new ArrayList<>();

                    for (PoiInfo poi : poiResult.getAllPoi()) {
                        MyPoiModel myPoi = new MyPoiModel(mType);
                        myPoi.setCity(poi.city);
                        myPoi.setUid(poi.uid);
                        myPoi.setAddress(poi.address);
                        myPoi.setName(poi.name);
                        myPoi.setInfo(poi.phoneNum);
                        if (null != poi.location) {
                            myPoi.setLatitude(poi.location.latitude);
                            myPoi.setLongitude(poi.location.longitude);
                        }
                        if (poi.type == PoiInfo.POITYPE.BUS_LINE) {
                            myPoi.setTypePoi(TypePoi.BUS_LINE);
                        } else if (poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                            myPoi.setTypePoi(TypePoi.SUBWAY_LINE);
                        } else if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
                            myPoi.setTypePoi(TypePoi.BUS_STATION);
                        } else if (poi.type == PoiInfo.POITYPE.SUBWAY_STATION) {
                            myPoi.setTypePoi(TypePoi.SUBWAY_STATION);
                        } else if (poi.type == PoiInfo.POITYPE.POINT) {
                            myPoi.setTypePoi(TypePoi.POINT);
                        }

                        list.add(myPoi);
                    }
                    listener.setSearchResult(list);
                    listener.onShowData("search");
                } else {
                    listener.onNoData("search");
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        mPoiSearchBaidu.searchNearby(new PoiNearbySearchOption().location(new LatLng(nearby.getLatitude(), nearby.getLongitude())).keyword(keyword).radius(20000).sortType(PoiSortType.distance_from_near_to_far).pageNum(page).pageCapacity(20));
    }

    private void searchPoiInCityByAmap(String keyword, String city, int page, final OnSearchResultListener listener) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, null, city);
        query.setPageSize(20);
        query.setPageNum(page);

        PoiSearch poiSearchAmap = new PoiSearch(mContext, query);
        poiSearchAmap.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(com.amap.api.services.poisearch.PoiResult poiResult, int code) {
                if (null != poiResult && null != poiResult.getPois() && !poiResult.getPois().isEmpty()) {
                    List<MyPoiModel> list = new ArrayList<>();

                    for (PoiItem poi : poiResult.getPois()) {
                        MyPoiModel myPoi = new MyPoiModel(mType);
                        myPoi.setCity(poi.getCityName());
                        myPoi.setUid(poi.getPoiId());
                        myPoi.setName(poi.getTitle());
                        myPoi.setAddress(poi.getSnippet());
                        myPoi.setInfo(poi.getTel());
                        myPoi.setLatitude(poi.getLatLonPoint().getLatitude());
                        myPoi.setLongitude(poi.getLatLonPoint().getLongitude());
                        myPoi.setTypePoi(TypePoi.POINT);
                        list.add(myPoi);
                    }
                    listener.setSearchResult(list);
                    listener.onShowData("search");
                } else {
                    listener.onNoData("search");
                }
                if (null != poiResult && null != poiResult.getSearchSuggestionCitys() && !poiResult.getSearchSuggestionCitys().isEmpty()) {
                    listener.setSuggestCityList(poiResult.getSearchSuggestionCitys());
                }else {
                    listener.onNoData("city");
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearchAmap.searchPOIAsyn();
    }

    private void searchPoiInCityByBaidu(String keyword, String city, int page, final OnSearchResultListener listener) {
        mPoiSearchBaidu.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (null != poiResult && null != poiResult.getAllPoi() && !poiResult.getAllPoi().isEmpty()) {
                    List<MyPoiModel> list = new ArrayList<>();

                    for (PoiInfo poi : poiResult.getAllPoi()) {
                        MyPoiModel myPoi = new MyPoiModel(mType);
                        myPoi.setCity(poi.city);
                        myPoi.setUid(poi.uid);
                        myPoi.setAddress(poi.address);
                        myPoi.setName(poi.name);
                        myPoi.setInfo(poi.phoneNum);
                        if (null != poi.location) {
                            myPoi.setLatitude(poi.location.latitude);
                            myPoi.setLongitude(poi.location.longitude);
                        }
                        if (poi.type == PoiInfo.POITYPE.BUS_LINE) {
                            myPoi.setTypePoi(TypePoi.BUS_LINE);
                        } else if (poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                            myPoi.setTypePoi(TypePoi.SUBWAY_LINE);
                        } else if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
                            myPoi.setTypePoi(TypePoi.BUS_STATION);
                        } else if (poi.type == PoiInfo.POITYPE.SUBWAY_STATION) {
                            myPoi.setTypePoi(TypePoi.SUBWAY_STATION);
                        } else if (poi.type == PoiInfo.POITYPE.POINT) {
                            myPoi.setTypePoi(TypePoi.POINT);
                        }

                        list.add(myPoi);
                    }
                    listener.setSearchResult(list);
                    listener.onShowData("search");
                } else {
                    listener.onNoData("search");
                }
                if (null != poiResult && null != poiResult.getSuggestCityList() && !poiResult.getSuggestCityList().isEmpty()) {
                    List<SuggestionCity> suggestionCityList = new ArrayList<SuggestionCity>();
                    for (CityInfo info : poiResult.getSuggestCityList()) {
                        SuggestionCity city = new SuggestionCity(info.city, null, null, info.num);
                        suggestionCityList.add(city);
                    }
                    listener.setSuggestCityList(suggestionCityList);
                }else {
                    listener.onNoData("city");
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });
        mPoiSearchBaidu.searchInCity(new PoiCitySearchOption().city(city).keyword(keyword).pageNum(page).pageCapacity(20).isReturnAddr(true));

    }


    public void searchLatLng(double lat, double lng, int type, OnSearchResultListener listener) {
        try {
            if (TypeMap.TYPE_BAIDU == mType) {
                searchLatLngByBaidu(lat, lng, type, listener);
            } else if (TypeMap.TYPE_AMAP == mType) {
                searchLatLngByAmap(lat, lng, type, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onMessage("搜索异常");
        }
    }

    private void searchLatLngByAmap(final double lat, final double lng, final int type, final OnSearchResultListener listener) {
        com.amap.api.maps.model.LatLng latLng;
        com.amap.api.maps.CoordinateConverter converter = new com.amap.api.maps.CoordinateConverter(mContext);
        if (1 == type) {
            latLng = new com.amap.api.maps.model.LatLng(lat, lng);
        } else if (2 == type) {
            converter.from(com.amap.api.maps.CoordinateConverter.CoordType.BAIDU);
            converter.coord(new com.amap.api.maps.model.LatLng(lat, lng));
            latLng = new com.amap.api.maps.model.LatLng(converter.convert().latitude, converter.convert().longitude);
        } else {
            converter.from(com.amap.api.maps.CoordinateConverter.CoordType.GPS);
            converter.coord(new com.amap.api.maps.model.LatLng(lat, lng));
            latLng = new com.amap.api.maps.model.LatLng(converter.convert().latitude, converter.convert().longitude);
        }

        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude, latLng.longitude), 200, GeocodeSearch.AMAP);
        GeocodeSearch geocodeSearch = new GeocodeSearch(mContext);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int i) {
                if (null != result && null != result.getRegeocodeAddress()
                        && result.getRegeocodeAddress().getFormatAddress() != null) {

                    List<MyPoiModel> list = new ArrayList<>();

                    MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_AMAP);
                    poi.setName(result.getRegeocodeAddress().getFormatAddress() + "附近");
                    poi.setCity(result.getRegeocodeAddress().getCity());
                    poi.setLatitude(lat);
                    poi.setLongitude(lng);
                    poi.setTypePoi(TypePoi.POINT);
                    list.add(poi);
                    listener.setSearchResult(list);
                    listener.onShowData("search");
                } else {
                    listener.onNoData("search");
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult result, int i) {

            }
        });
        geocodeSearch.getFromLocationAsyn(query);
    }

    private void searchLatLngByBaidu(double lat, double lng, int type, final OnSearchResultListener listener) {
        LatLng latLng;
        com.baidu.mapapi.utils.CoordinateConverter converter = new com.baidu.mapapi.utils.CoordinateConverter();

        if (1 == type) {
            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.COMMON);
            // sourceLatLng待转换坐标
            converter.coord(new LatLng(lat, lng));

            latLng = converter.convert();
        } else if (2 == type) {
            latLng = new LatLng(lat, lng);
        } else {
            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.GPS);
            // sourceLatLng待转换坐标
            converter.coord(new LatLng(lat, lng));

            latLng = converter.convert();
        }
        mGeoCoderBaidu.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR || null == result.getAddress() || result.getAddress().isEmpty()) {
                    listener.onNoData("search");
                    return;
                }

                List<MyPoiModel> list = new ArrayList<>();

                MyPoiModel poi = new MyPoiModel(TypeMap.TYPE_BAIDU);
                poi.setLatitude(result.getLocation().latitude);
                poi.setLongitude(result.getLocation().longitude);
                poi.setCity(result.getAddressDetail().city);
                poi.setName(result.getSematicDescription());
                poi.setAddress(result.getAddress());
                poi.setTypePoi(TypePoi.POINT);
                list.add(poi);
                listener.setSearchResult(list);
                listener.onShowData("search");
            }
        });
        mGeoCoderBaidu.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }

    public void getSearchTips(String str, String city, OnSearchTipsListener listener) {
        try {
            if (TypeMap.TYPE_BAIDU == mType) {
                getSearchTipsByBaidu(str, city, listener);
            } else if (TypeMap.TYPE_AMAP == mType) {
                getSearchTipsByAmap(str, city, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onMessage("搜索异常");
        }
    }

    private void getSearchTipsByAmap(String str, String city, final OnSearchTipsListener listener) {
        InputtipsQuery inputquery = new InputtipsQuery(str, city);
        inputquery.setCityLimit(true);//限制在当前城市
        Inputtips inputTips = new Inputtips(mContext, inputquery);
        inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
            @Override
            public void onGetInputtips(List<Tip> list, int i) {
                if (null != list && !list.isEmpty()) {
                    List<String> tips = new ArrayList<>();
                    for (Tip t : list) {
                        tips.add(t.getName());
                    }
                    listener.setSearchTipsAdatper(tips);
                }
            }
        });
        inputTips.requestInputtipsAsyn();
    }

    private void getSearchTipsByBaidu(String str, String city, final OnSearchTipsListener listener) {
        SuggestionSearch mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city(city).keyword(str).citylimit(true));
        mSuggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult result) {

                if (null != result && null != result.getAllSuggestions() && !result.getAllSuggestions().isEmpty()) {
                    List<String> tips = new ArrayList<>();
                    for (SuggestionResult.SuggestionInfo info : result.getAllSuggestions()) {
                        tips.add(info.key);
                    }
                    listener.setSearchTipsAdatper(tips);
                }
            }
        });
    }

    public void getPoiDetails(String uid, OnBaseListener listener) {
        try {
            if (TypeMap.TYPE_BAIDU == mType) {
                getPoiDetailsByBaidu(uid, listener);
            } else if (TypeMap.TYPE_AMAP == mType) {
                getPoiDetailsByAmap(uid, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onMessage("搜索异常");
        }
    }

    private void getPoiDetailsByAmap(String uid, OnBaseListener listener) {
        PoiSearch poiSearch = new PoiSearch(mContext, null);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(com.amap.api.services.poisearch.PoiResult poiResult, int i) {

            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearch.searchPOIIdAsyn(uid);
    }

    private void getPoiDetailsByBaidu(String uid, OnBaseListener listener) {
        mPoiSearchBaidu.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });
        mPoiSearchBaidu.searchPoiDetail(new PoiDetailSearchOption().poiUid(uid));
    }

}
