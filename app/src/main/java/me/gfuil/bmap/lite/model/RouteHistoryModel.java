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

package me.gfuil.bmap.lite.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gfuil on 2017/6/28.
 */

public class RouteHistoryModel implements Parcelable {
    private String nameStart;
    private String nameEnd;
    private double latStart;
    private double lngStart;
    private double latEnd;
    private double lngEnd;
    private long time;

    public void fromJSON(JSONObject object) {
        this.nameStart = object.optString("nameStart");
        this.nameEnd = object.optString("nameEnd");
        this.latStart = object.optDouble("latStart", 0);
        this.lngStart = object.optDouble("lngStart", 0);
        this.latEnd = object.optDouble("latEnd", 0);
        this.lngEnd = object.optDouble("lngEnd", 0);
        this.time = object.optLong("time", 0);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("nameStart", nameStart);
        object.put("nameEnd", nameEnd);
        object.put("latStart", latStart);
        object.put("lngStart", lngStart);
        object.put("latEnd", latEnd);
        object.put("lngEnd", lngEnd);
        object.put("time", time);

        return object;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (null != anObject && anObject instanceof RouteHistoryModel) {
            RouteHistoryModel history = (RouteHistoryModel) anObject;
            if ("我的位置".equals(history.getNameStart()) && "我的位置".equals(this.getNameStart())){
                return history.getLatEnd() == this.getLatEnd() && history.getLngEnd() == this.getLngEnd();
            }else if ("我的位置".equals(history.getNameEnd()) && "我的位置".equals(this.getNameEnd())){
                return history.getLatStart() == this.getLngStart() && history.getLngStart() == this.getLngStart();
            }else if ("我的位置".equals(history.getNameStart()) && "我的位置".equals(this.getNameEnd())){
                return history.getLatEnd() == this.getLatStart() && history.getLngEnd() == this.getLngStart();
            } else if ("我的位置".equals(history.getNameEnd()) && "我的位置".equals(this.getNameStart())) {
                return history.getLatStart() == this.getLatEnd() && history.getLngStart() == this.getLngEnd();
            }else {
                return history.getLatStart() == this.getLatStart() && history.getLngStart() == this.getLngStart()
                        && history.getLatEnd() == this.getLatEnd() && history.getLngEnd() == this.getLngEnd();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        try {
            return toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }

    public String getNameStart() {
        return nameStart;
    }

    public void setNameStart(String nameStart) {
        this.nameStart = nameStart;
    }

    public String getNameEnd() {
        return nameEnd;
    }

    public void setNameEnd(String nameEnd) {
        this.nameEnd = nameEnd;
    }

    public double getLatStart() {
        return latStart;
    }

    public void setLatStart(double latStart) {
        this.latStart = latStart;
    }

    public double getLngStart() {
        return lngStart;
    }

    public void setLngStart(double lngStart) {
        this.lngStart = lngStart;
    }

    public double getLatEnd() {
        return latEnd;
    }

    public void setLatEnd(double latEnd) {
        this.latEnd = latEnd;
    }

    public double getLngEnd() {
        return lngEnd;
    }

    public void setLngEnd(double lngEnd) {
        this.lngEnd = lngEnd;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nameStart);
        dest.writeString(this.nameEnd);
        dest.writeDouble(this.latStart);
        dest.writeDouble(this.lngStart);
        dest.writeDouble(this.latEnd);
        dest.writeDouble(this.lngEnd);
        dest.writeLong(this.time);
    }

    public RouteHistoryModel() {
    }

    protected RouteHistoryModel(Parcel in) {
        this.nameStart = in.readString();
        this.nameEnd = in.readString();
        this.latStart = in.readDouble();
        this.lngStart = in.readDouble();
        this.latEnd = in.readDouble();
        this.lngEnd = in.readDouble();
        this.time = in.readLong();
    }

    public static final Creator<RouteHistoryModel> CREATOR = new Creator<RouteHistoryModel>() {
        @Override
        public RouteHistoryModel createFromParcel(Parcel source) {
            return new RouteHistoryModel(source);
        }

        @Override
        public RouteHistoryModel[] newArray(int size) {
            return new RouteHistoryModel[size];
        }
    };
}
