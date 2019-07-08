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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gfuil
 */

public class MyPoiModel implements Parcelable {
    private String name;
    private String address;
    private String uid;
    private String city;
    private String info;
    private double accuracy;
    private double altitude;
    private double latitude;
    private double longitude;
    private TypeMap typeMap;
    private TypePoi typePoi;


    public void fromJSON(JSONObject object) {
        this.name = object.optString("name");
        this.address = object.optString("address");
        this.uid = object.optString("uid");
        this.city = object.optString("city");
        this.info = object.optString("info");
        this.accuracy = object.optDouble("accuracy");
        this.altitude = object.optDouble("altitude");
        this.latitude = object.optDouble("latitude");
        this.longitude = object.optDouble("longitude");
        this.typeMap = TypeMap.fromInt(object.optInt("typeMap"));
        this.typePoi = TypePoi.fromInt(object.optInt("typePoi"));

    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", this.name);
        object.put("address", this.address);
        object.put("uid", this.uid);
        object.put("city", this.city);
        object.put("info", this.info);
        object.put("accuracy", this.accuracy);
        object.put("altitude", this.altitude);
        object.put("latitude", this.latitude);
        object.put("longitude", this.longitude);
        object.put("typeMap", this.typeMap.getInt());
        if (null != this.typePoi) {
            object.put("typePoi", this.typePoi.getInt());
        }

        return object;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof MyPoiModel) {
            MyPoiModel another = (MyPoiModel) anObject;
            return ((another.getTypeMap() == this.getTypeMap())
                    && another.getLatitude() == this.getLatitude()
                    && another.getLongitude() == this.getLongitude());
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
    //
//    public com.baidu.mapapi.model.LatLng getLatLngBaidu(Context context) {
//        com.baidu.mapapi.model.LatLng latLng = null;
//        if (TypeMap.TYPE_BAIDU == getTypeMap()) {
//            latLng = new com.baidu.mapapi.model.LatLng(getLatitude(), getLongitude());
//        } else if (TypeMap.TYPE_AMAP == getTypeMap()) {
//            com.baidu.mapapi.utils.CoordinateConverter converter = new com.baidu.mapapi.utils.CoordinateConverter();
//            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.COMMON);
//            // sourceLatLng待转换坐标
//            converter.coord(new com.baidu.mapapi.model.LatLng(getLatitude(), getLongitude()));
//
//            latLng = converter.convert();
//        } else if (TypeMap.TYPE_GOOGLE == getTypeMap()) {
//            com.baidu.mapapi.utils.CoordinateConverter converter = new com.baidu.mapapi.utils.CoordinateConverter();
//            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.COMMON);
//            // sourceLatLng待转换坐标
//            converter.coord(new com.baidu.mapapi.model.LatLng(getLatitude(), getLongitude()));
//
//            latLng = converter.convert();
//        }
//        return latLng;
//    }
//
//    public com.amap.api.maps.model.LatLng getLatLngAmap(Context context) {
//        com.amap.api.maps.model.LatLng latLng = null;
//
//        if (TypeMap.TYPE_BAIDU == getTypeMap()) {
//            com.amap.api.maps.CoordinateConverter converter = new com.amap.api.maps.CoordinateConverter(context);
//            converter.from(com.amap.api.maps.CoordinateConverter.CoordType.BAIDU);
//            converter.coord(new com.amap.api.maps.model.LatLng(getLatitude(), getLongitude()));
//
//            latLng = new com.amap.api.maps.model.LatLng(converter.convert().latitude, converter.convert().longitude);
//        } else if (TypeMap.TYPE_AMAP == getTypeMap()) {
//            latLng = new com.amap.api.maps.model.LatLng(getLatitude(), getLongitude());
//        } else if (TypeMap.TYPE_GOOGLE == getTypeMap()) {
//            com.amap.api.maps.CoordinateConverter converter = new com.amap.api.maps.CoordinateConverter(context);
//            converter.from(com.amap.api.maps.CoordinateConverter.CoordType.GOOGLE);
//            converter.coord(new com.amap.api.maps.model.LatLng(getLatitude(), getLongitude()));
//
//            latLng = new com.amap.api.maps.model.LatLng(converter.convert().latitude, converter.convert().longitude);
//        }
//        return latLng;
//    }

    public MyPoiModel(TypeMap type) {
        this.typeMap = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public TypeMap getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(TypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public TypePoi getTypePoi() {
        return typePoi;
    }

    public void setTypePoi(TypePoi typePoi) {
        this.typePoi = typePoi;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.uid);
        dest.writeString(this.city);
        dest.writeString(this.info);
        dest.writeDouble(this.accuracy);
        dest.writeDouble(this.altitude);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.typeMap == null ? -1 : this.typeMap.ordinal());
        dest.writeInt(this.typePoi == null ? -1 : this.typePoi.ordinal());
    }

    protected MyPoiModel(Parcel in) {
        this.name = in.readString();
        this.address = in.readString();
        this.uid = in.readString();
        this.city = in.readString();
        this.info = in.readString();
        this.accuracy = in.readDouble();
        this.altitude = in.readDouble();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        int tmpTypeMap = in.readInt();
        this.typeMap = tmpTypeMap == -1 ? null : TypeMap.values()[tmpTypeMap];
        int tmpTypePoi = in.readInt();
        this.typePoi = tmpTypePoi == -1 ? null : TypePoi.values()[tmpTypePoi];
    }

    public static final Creator<MyPoiModel> CREATOR = new Creator<MyPoiModel>() {
        @Override
        public MyPoiModel createFromParcel(Parcel source) {
            return new MyPoiModel(source);
        }

        @Override
        public MyPoiModel[] newArray(int size) {
            return new MyPoiModel[size];
        }
    };
}
