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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件操作工具类
 *
 * @author gfuil
 */
public class FileUtils {

    /**
     * 判读SD卡是否存在
     *
     * @return true/false
     */
    public static boolean isSDCardExists() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡目录File对象
     *
     * @return SD卡目录File对象
     */
    public static File getSDCard() {
        File file = null;
        if (isSDCardExists()) {
            file = Environment.getExternalStorageDirectory();
        }
        return file;
    }


    /**
     * 移动文件
     * @param srcFileName    源文件完整路径
     * @param destDirName    目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public static boolean moveFile(String srcFileName, String destDirName) {

        File srcFile = new File(srcFileName);
        if(!srcFile.exists() || !srcFile.isFile())
            return false;

        File destDir = new File(destDirName);
        if (!destDir.exists())
            destDir.mkdirs();

        return srcFile.renameTo(new File(destDirName + File.separator + srcFile.getName()));
    }

    /**
     * 移动目录
     * @param srcDirName     源目录完整路径
     * @param destDirName    目的目录完整路径
     * @return 目录移动成功返回true，否则返回false
     */
    public static boolean moveDirectory(String srcDirName, String destDirName) {

        File srcDir = new File(srcDirName);
        if(!srcDir.exists() || !srcDir.isDirectory())
            return false;

        File destDir = new File(destDirName);
        if(!destDir.exists())
            destDir.mkdirs();

        /**
         * 如果是文件则移动，否则递归移动文件夹。删除最终的空源文件夹
         * 注意移动文件夹时保持文件夹的树状结构
         */
        File[] sourceFiles = srcDir.listFiles();
        for (File sourceFile : sourceFiles) {
            if (sourceFile.isFile())
                moveFile(sourceFile.getAbsolutePath(), destDir.getAbsolutePath());
            else if (sourceFile.isDirectory())
                moveDirectory(sourceFile.getAbsolutePath(),
                        destDir.getAbsolutePath() + File.separator + sourceFile.getName());
        }
        return srcDir.delete();
    }

    /**
     * 删除文件或目录
     *
     * @return 是否成功
     */
    public static boolean deleteFile(File file) {
        boolean isSuccess = false;
        if (file.exists()) {
            if (file.isFile()) {
                isSuccess = file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();

                if (files != null || files.length == 0) {
                    isSuccess = file.delete();
                }
                for (File f : files) {
                    deleteFile(f);
                }

            }
        }
        return isSuccess;
    }

    /**
     * 删除文件或目录
     *
     * @return 是否成功
     */
    public static boolean deleteFile(String filename) {
        return deleteFile(new File(filename));
    }

    /**
     * 获取文件字节数组
     *
     * @param file
     * @return byte数组
     */
    public static byte[] getFileBytes(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
            fis.close();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    /**
     * 获取文件后缀名
     *
     * @return
     */
    public static String getFilePrefix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).trim();
    }

    /**
     * 获取文件的MIMETYPE
     *
     * @return
     */
    public static String getMimeType(String fileName) {
        String result = "application/octet-stream";
        int extPos = fileName.lastIndexOf(".");
        if (extPos != -1) {
            String ext = fileName.substring(extPos + 1);
            result = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        }
        return result;
    }

    /**
     * 读取raw里的资源文件
     *
     * @param context 上下文
     * @param resID   资源ID
     * @return
     */
    public static String readFileFromRaw(Context context, int resID) {
        String string = null;
        try {
            InputStream in = context.getResources().openRawResource(resID);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            string = new String(buffer);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    /**
     * 读取asset里的文件
     *
     * @param context  上下文
     * @param filename 文件名
     * @return
     */
    public static String readFileFromAsset(Context context, String filename) {
        String string = null;
        try {
            InputStream in = context.getResources().getAssets().open(filename);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            string = new String(buffer);

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    /**
     * 读取asset里的文件
     *
     * @param context  上下文
     * @param filename 文件名
     * @return
     */
    public static byte[] readFileByteFromAsset(Context context, String filename) {
        try {
            InputStream in = context.getResources().getAssets().open(filename);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);

            in.close();

            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取SDcard里的文件
     *
     * @param file 文件名
     * @return
     */
    public static String readFileFromSDCard(File file) {
        String string = null;
        if (isSDCardExists() && file.exists()) {
            try {
                FileInputStream fin = new FileInputStream(file);
                int length = fin.available();
                byte[] buffer = new byte[length];
                fin.read(buffer);
                string = new String(buffer);
                fin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return string;
    }

    /**
     * 写文件到SDcard
     *
     * @param file
     * @param string
     */
    public static void writeFileToSDCard(File file, String string) {
        if (isSDCardExists()) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(string.getBytes());

                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 写文件到SDcard
     *
     * @param file
     * @param string
     */
    public static void writeFileToSDCard(File file, byte[] buffer) {
        if (isSDCardExists()) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(buffer);

                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 保存图片的方法 保存到sdcard
     *
     * @throws IOException
     */
    public static void saveImageToSDCard(Bitmap bitmap, String path) throws IOException {
        FileOutputStream fos;
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }
        file.createNewFile();
        fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.flush();
        fos.close();

    }

}
