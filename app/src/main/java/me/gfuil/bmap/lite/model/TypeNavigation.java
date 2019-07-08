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

public enum TypeNavigation {
    HISTORY(0),
    WALK(1),
    BIKE(2),
    DRIVE(3),
    BUS(4);


    private int type;

    TypeNavigation(int type) {
        this.type = type;
    }

    public int getInt() {
        return this.type;
    }

    public static TypeNavigation fromInt(int type) {
        switch(type) {
            case 0:
                return HISTORY;
            case 1:
                return WALK;
            case 2:
                return BIKE;
            case 3:
                return DRIVE;
            case 4:
                return BUS;
            default:
                return null;
        }
    }
}
