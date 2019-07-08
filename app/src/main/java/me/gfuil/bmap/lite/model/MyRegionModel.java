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

import java.util.Arrays;
import java.util.List;

import me.gfuil.bmap.lite.utils.StringUtils;

/**
 * @author gfuil
 */

public class MyRegionModel implements Parcelable {
    private String name;
    private List<String> child;

    public void fromJSON(JSONObject object) {
        this.name = object.optString("a");
        this.child = Arrays.asList(StringUtils.convertStrToArray(object.optString("b"), ","));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("a", name);
        object.put("b", StringUtils.converToString(child, ","));
        return object;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getChild() {
        return child;
    }

    public void setChild(List<String> child) {
        this.child = child;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeStringList(this.child);
    }

    public MyRegionModel() {
    }

    protected MyRegionModel(Parcel in) {
        this.name = in.readString();
        this.child = in.createStringArrayList();
    }

    public static final Creator<MyRegionModel> CREATOR = new Creator<MyRegionModel>() {
        @Override
        public MyRegionModel createFromParcel(Parcel source) {
            return new MyRegionModel(source);
        }

        @Override
        public MyRegionModel[] newArray(int size) {
            return new MyRegionModel[size];
        }
    };
}
