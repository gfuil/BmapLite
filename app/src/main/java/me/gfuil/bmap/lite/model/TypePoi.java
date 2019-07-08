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

public enum TypePoi {
    POINT(0),
    BUS_STATION(1),
    BUS_LINE(2),
    SUBWAY_STATION(3),
    SUBWAY_LINE(4);

    private int type;

    TypePoi(int type) {
        this.type = type;
    }

    public int getInt() {
        return this.type;
    }

    public static TypePoi fromInt(int type) {
        switch(type) {
            case 0:
                return POINT;
            case 1:
                return BUS_STATION;
            case 2:
                return BUS_LINE;
            case 3:
                return SUBWAY_STATION;
            case 4:
                return SUBWAY_LINE;
            default:
                return null;
        }
    }
}
