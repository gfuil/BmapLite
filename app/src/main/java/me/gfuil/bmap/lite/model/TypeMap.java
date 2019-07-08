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

/**
 * @author gfuil
 */

public enum TypeMap {
    TYPE_BAIDU(0),
    TYPE_AMAP(1),
    TYPE_GOOGLE(2),
    TYPE_GPS(3);

    private int type;

    TypeMap(int type) {
        this.type = type;
    }

    public int getInt() {
        return this.type;
    }

    public static TypeMap fromInt(int type) {
        switch(type) {
            case 0:
                return TYPE_BAIDU;
            case 1:
                return TYPE_AMAP;
            case 2:
                return TYPE_GOOGLE;
            case 3:
                return TYPE_GPS;
            default:
                return null;
        }
    }
}
