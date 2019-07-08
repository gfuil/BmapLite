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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import me.gfuil.bmap.lite.model.RouteHistoryModel;
import me.gfuil.bmap.lite.utils.PreferenceUtils;
import me.gfuil.bmap.lite.utils.StringUtils;

/**
 * @author gfuil
 */

public class CacheInteracter {
    PreferenceUtils preferenceUtils;

    public CacheInteracter(Context context) {
        preferenceUtils = new PreferenceUtils(context, "cache");
    }

    public void clear() {
        preferenceUtils.clear();
    }

    public void setLauncherFirst(boolean isFirst) {
        preferenceUtils.setBooleanPreference("first", isFirst);
    }

    public boolean isLauncherFirst() {
        return preferenceUtils.getBooleanPreference("first", true);
    }

    public void setCity(String city) {
        preferenceUtils.setStringPreference("city", city);
    }

    public String getCity() {
        return preferenceUtils.getStringPreference("city", "北京");
    }

    public void setCity2(String city) {
        preferenceUtils.setStringPreference("city2", city);
    }

    public String getCity2() {
        return preferenceUtils.getStringPreference("city2", getCity());
    }

    public void setLongitude(double longitude) {
        preferenceUtils.setStringPreference("longitude", longitude + "");
    }

    public double getLongitude() {
        String s = preferenceUtils.getStringPreference("longitude", null);
        if (null != s && !s.isEmpty()) {
            return Double.parseDouble(s);
        } else {
            return 116.403963;
        }
    }

    public void setLatitude(double latitude) {
        preferenceUtils.setStringPreference("latitude", latitude + "");
    }

    public double getLatitude() {
        String s = preferenceUtils.getStringPreference("latitude", null);
        if (null != s && !s.isEmpty()) {
            return Double.parseDouble(s);
        } else {
            return 39.915119;
        }
    }

    public void addSearchHistoryKeyword(String keyword) {
        LinkedList<String> historyList = getSearchHistoryKeyword();
        if (null == historyList) {
            historyList = new LinkedList<>();
        }
        if (historyList.contains(keyword)) {
            historyList.remove(keyword);
        }
        historyList.addFirst(keyword);

        setSearchHistoryKeyword(historyList);
    }

    public void deleteSearchHistoryKeyword(String keyword) {
        if (null == keyword || keyword.isEmpty()) {
            return;
        }
        LinkedList<String> historyList = getSearchHistoryKeyword();
        if (null == historyList) {
            return;
        }
        historyList.remove(keyword);

        setSearchHistoryKeyword(historyList);
    }

    public void setSearchHistoryKeyword(LinkedList<String> list) {
        if (null == list || list.isEmpty()) {
            preferenceUtils.setStringPreference("search_history", "");
        } else {
            while (list.size() > 20) {
                list.removeLast();
            }
            String str = StringUtils.converToString(list, "/");
            preferenceUtils.setStringPreference("search_history", str);
        }
    }

    public LinkedList<String> getSearchHistoryKeyword() {
        LinkedList<String> historyList = null;
        String history = preferenceUtils.getStringPreference("search_history", null);
        if (null != history) {
            String[] array = StringUtils.convertStrToArray(history, "/");
            if (array.length > 0) {
                historyList = new LinkedList<>();
                Collections.addAll(historyList, array);
            }
        }
        return historyList;
    }


    public void addRouteHistory(RouteHistoryModel history) throws JSONException {
        LinkedList<RouteHistoryModel> historyList = getRouteHistory();
        if (null == historyList) {
            historyList = new LinkedList<>();
        }
        if (historyList.contains(history)) {
            historyList.remove(history);
        }
        historyList.addFirst(history);

        setRouteHistory(historyList);
    }

    public void deleteRouteHistory(RouteHistoryModel history) throws JSONException {
        if (null == history) {
            return;
        }
        LinkedList<RouteHistoryModel> historyList = getRouteHistory();
        if (null == historyList) {
            return;
        }
        historyList.remove(history);

        setRouteHistory(historyList);
    }

    public void setRouteHistory(LinkedList<RouteHistoryModel> list) throws JSONException {
        if (null == list || list.isEmpty()) {
            preferenceUtils.setStringPreference("route_history", "");
        } else {
            while (list.size() > 20) {
                list.removeLast();
            }
            List<String> strList = new ArrayList<>();
            for (RouteHistoryModel model : list) {
                strList.add(model.toJSON().toString());
            }
            String str = StringUtils.converToString(strList, "<#>");
            preferenceUtils.setStringPreference("route_history", str);
        }
    }

    public LinkedList<RouteHistoryModel> getRouteHistory() throws JSONException {
        LinkedList<RouteHistoryModel> historyList = null;
        String history = preferenceUtils.getStringPreference("route_history", null);
        if (null != history) {
            String[] array = StringUtils.convertStrToArray(history, "<#>");
            if (array.length > 0) {
                historyList = new LinkedList<>();
                for (String s : array) {
                    RouteHistoryModel model = new RouteHistoryModel();
                    model.fromJSON(new JSONObject(s));
                    historyList.add(model);
                }
            }
        }
        return historyList;
    }
}
