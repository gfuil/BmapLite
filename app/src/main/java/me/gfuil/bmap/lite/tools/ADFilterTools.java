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


package me.gfuil.bmap.lite.tools;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by gfuil on 2017/4/23.
 */

public class ADFilterTools {
    public static String getClearAdDivByIdJs(String[] adDivs) {
        String js = "javascript:";
        for (int i = 0; i < adDivs.length; i++) {
            js += "var adDiv" + i + "= document.getElementById('" + adDivs[i] + "');if(adDiv" + i + " != null)adDiv" + i + ".parentNode.removeChild(adDiv" + i + ");";
        }
        return js;
    }

    public static String getClearAdDivByClassJs(String[] adDivs) {
        String js = "javascript:";
        for (int i = 0; i < adDivs.length; i++) {
            js += "var adDiv" + i + "= document.getElementsByClassName('" + adDivs[i] + "');if(adDiv" + i + " != null) { for(var j =0; j< adDiv" + i+".length; j++){adDiv" + i + "[j].parentNode.removeChild(adDiv" + i + "[j]);}}";
        }
        return js;
    }
}
