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

/**
 * @author gfuil
 */

public class AboutModel implements Parcelable{
    private String name;
    private String info;

    public AboutModel(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.info);
    }

    public AboutModel() {
    }

    protected AboutModel(Parcel in) {
        this.name = in.readString();
        this.info = in.readString();
    }

    public static final Creator<AboutModel> CREATOR = new Creator<AboutModel>() {
        @Override
        public AboutModel createFromParcel(Parcel source) {
            return new AboutModel(source);
        }

        @Override
        public AboutModel[] newArray(int size) {
            return new AboutModel[size];
        }
    };
}
