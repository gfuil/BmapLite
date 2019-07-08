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

import android.support.annotation.NonNull;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author gfuil
 */
public class StringUtils {
    /**
     * string分割取值
     *
     * @param string    原字符串
     * @param partition 分隔符，例如 , / - &
     * @return String数组
     */
    public static String[] convertStrToArray(String string, String partition) {
        return string.split(partition);
    }

    /**
     * 判断是否是正确的手机号码格式
     *
     * @param mobile 输入的内容
     * @return 返回匹配结果
     */
    public static boolean isMobile(String mobile) {
        String regex = "(\\+\\d+)?1[34578]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    /**
     * 判断是否是正确的邮箱格式
     *
     * @param email 输入的内容
     * @return 返回匹配结果
     */
    public static boolean isEmail(String email) {
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email);
    }


    /**
     * 判断是否是正确的身份证号码
     *
     * @param idCard 输入的内容
     * @return 返回匹配结果
     */
    public static boolean isIdCard(String idCard) {
        String regex = null;
        if (idCard.length() == 15) {
            regex = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        } else if (idCard.length() == 18) {
            regex = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
        } else {
            return false;
        }
        return Pattern.matches(regex, idCard);
    }

    /**
     * 验证URL地址
     *
     * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isURL(String url) {
        String regex = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?";
        return Pattern.matches(regex, url);
    }

    /**
     * 匹配IP地址(简单匹配，格式，如：192.168.1.1，127.0.0.1，没有匹配IP段的大小)
     *
     * @param ipAddress IPv4标准地址
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isIpAddress(String ipAddress) {
        String regex = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))";
        return Pattern.matches(regex, ipAddress);
    }

    /**
     * 过滤html标签
     *
     * @param html
     * @return
     */
    public static String filterHtml(String html) {
        return html != null ? html.replaceAll("<[.[^<]]*>", "") : "";
    }

    /**
     * 过滤html标签
     *
     * @param html
     * @param replace 替换的字符串
     * @return
     */
    public static String filterHtml(String html, String replace) {
        return html != null ? html.replaceAll("<[.[^<]]*>", replace) : "";
    }

    public static String stringToMD5(String string) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            MessageDigest md5Temp = MessageDigest.getInstance("MD5");
            md5Temp.update(string.getBytes("UTF8"));
            byte[] md = md5Temp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析出url的路径，包括页面
     *
     * @param strURL url地址
     * @return url路径
     */
    public static String getUrlPage(String strURL) {
        String url = null;
        String[] strArray = convertStrToArray(strURL, "\\?");
        if (strArray.length > 1 && strArray[0] != null) {
            url = strArray[0];
        }
        return url;
    }

    /**
     * 解析出url的参数部分
     *
     * @param strURL url地址
     * @return url路径
     */
    public static String getUrlParam(String strURL) {
        String param = null;
        String[] strArray = convertStrToArray(strURL, "\\?");
        if (strArray.length > 1 && strArray[1] != null) {
            param = strArray[1];
        }
        return param;
    }

    /**
     * 解析出url参数中的键值对 如 "index.php?action=del&id=123" or
     * "action=del&id=123"，解析出action:del,id:123存入map中
     *
     * @param urlOrParam url地址或url参数部分
     * @return url请求参数部分
     */
    public static Map<String, String> getUrlRequest(String urlOrParam) {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit = null;
        if (getUrlParam(urlOrParam) == null) {
            arrSplit = convertStrToArray(urlOrParam, "&");
        } else {
            arrSplit = convertStrToArray(getUrlParam(urlOrParam), "&");
        }

        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = convertStrToArray(strSplit, "=");

            if (arrSplitEqual.length > 1) {
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }


    public static String getFileNameFromURL(String url) {
        return url.substring(url.lastIndexOf("/") + 1).trim();
    }

    public static String getSubStringLast(String string, String last) {
        return string.substring(string.lastIndexOf(last) + last.length()).trim();
    }

    /**
     * 把数组转换为一个分隔的字符串
     */
    public static String converToString(String[] ig, String partition) {
        String str = "";
        if (ig != null && ig.length > 0) {
            for (int i = 0; i < ig.length; i++) {
                str += ig[i] + partition;
            }
        }
        str = str.substring(0, str.length() - partition.length());
        return str;
    }

    public static String converToString(List<String> ig, String partition) {
        String str = "";
        if (ig != null && ig.size() > 0) {
            for (int i = 0; i < ig.size(); i++) {
                str += ig.get(i) + partition;
            }
        }
        str = str.substring(0, str.length() - partition.length());
        return str;
    }

    /**
     * 把list转换为一个分隔的字符串
     */
    public static String listToString(List list, String partition) {
        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (i < list.size() - 1) {
                    sb.append(list.get(i) + partition);
                } else {
                    sb.append(list.get(i));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 去掉字符串中的空格、换行、制表符
     *
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 检测是否有emoji表情
     *
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }


    /**
     * 去除html标签
     * @param htmlStr
     * @return
     */
    @NonNull
    private static String delHTMLTag(String htmlStr) {
        String regExScript = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regExStyle = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regExHtml = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern pScript = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
        Matcher mScript = pScript.matcher(htmlStr);
        htmlStr = mScript.replaceAll(""); //过滤script标签

        Pattern pStyle = Pattern.compile(regExStyle, Pattern.CASE_INSENSITIVE);
        Matcher mStyle = pStyle.matcher(htmlStr);
        htmlStr = mStyle.replaceAll(""); //过滤style标签

        Pattern pHtml = Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
        Matcher mHtml = pHtml.matcher(htmlStr);
        htmlStr = mHtml.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }

    public static boolean isEmpty(String str) {
        return null == str || str.isEmpty();
    }
}
