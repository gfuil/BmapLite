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

package me.gfuil.bmap.lite.base;

import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * fragment基类
 *
 * @author gfuil
 */
public abstract class BaseFragment extends Fragment implements OnBaseListener {
    private ProgressDialog mProgressDialog;

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResult(int code, String msg) {
        onMessage(msg);
    }

    @Override
    public void onNoData(String type) {
        hideProgress();
    }

    @Override
    public void onShowData(String type) {
        hideProgress();
    }

    @Override
    public void close() {
        getActivity().finish();
    }

    /**
     * 跳转到activity
     *
     * @param clazz Activity
     */
    protected void openActivity(Class<?> clazz) {
        openActivity(clazz, null);
    }

    /**
     * 跳转到activity,并附带bundle
     *
     * @param clazz  Activity
     * @param extras 附带Bundle
     */
    protected void openActivity(Class<?> clazz, Bundle extras) {
        Intent intent = new Intent(getActivity(), clazz);
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
    }

    /**
     * 显示加载时进度条
     */
    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("请稍候...");
        }
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();

    }

    /**
     * 隐藏进度条
     */
    public void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void showAlertDialog(String title, String msg, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(msg);
        if (okListener!=null){
            builder.setPositiveButton("确定", okListener);
        }
        if (cancelListener!=null){
            builder.setNegativeButton("取消", cancelListener);
        }
        builder.create().show();
    }

    /**
     * 省略findViewById
     *
     * @param view
     * @param resId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(View view, int resId) {
        return (T) view.findViewById(resId);
    }



    protected abstract void initView(View view);
}
