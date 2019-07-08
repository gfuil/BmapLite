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


package me.gfuil.bmap.lite.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间操作工具
 *
 * @author gfuil
 */
public class TimeUtils {

    /**
     * 获取当前时间
     *
     * @param format
     *            时间格式：例如yyyy-MM-dd HH:mm:ss
     * @return 格式化后的时间
     */
    public static String getSystemTime(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(System.currentTimeMillis());
    }

    /**
     * 时间戳转换成时间
     *
     * @param format
     *            时间格式：例如yyyy-MM-dd HH:mm:ss
     * @return 格式化后的时间
     */
    public static String convertTime(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(time);
    }

    /**
     * 时间转换成时间戳
     *
     * @param time 格式化后的时间
     * @param format 时间格式：例如yyyy-MM-dd HH:mm:ss
     * @return 时间戳
     */
    public static long timeStringToTime(String time,String format){
        long t = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date date = dateFormat.parse(time);
            t = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 时间重新格式化
     * @param time 时间
     * @param oldFormat 原来的时间格式
     * @param newFormat 新的时间格式
     * @return 格式化后的时间
     */
    public static String reformatTime(String time,String oldFormat,String newFormat){
        return convertTime(timeStringToTime(time,oldFormat),newFormat);
    }

    /**
     * 对比两个时间相差的天数
     * @param begin 开始时间戳
     * @param end 结束时间戳
     * @return 天数
     */
    public static long getDayTwoTime(long begin,long end) {
        long beginTime = new Date(begin).getTime();
        long endTime = new Date(end).getTime();

        return (long)((endTime - beginTime) / (1000 * 60 * 60 * 24));
    }

    /**
     * 对比两个时间相差的小时数
     * @param begin 开始时间戳
     * @param end 结束时间戳
     * @return 小时数
     */
    public static long getHourTwoTime(long begin,long end) {
        long beginTime = new Date(begin).getTime();
        long endTime = new Date(end).getTime();

        return (long)((endTime - beginTime) / (1000 * 60 * 60 ));
    }


}
