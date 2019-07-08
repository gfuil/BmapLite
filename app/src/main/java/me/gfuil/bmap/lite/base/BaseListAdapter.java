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

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;


/**
 * adapter基类
 *
 * @author gfuil
 */
public abstract class BaseListAdapter<T> extends BaseAdapter {
    private Context context;
    private List<T> list;

    public BaseListAdapter(Context context, List<T> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public LayoutInflater getInflater() {
        return LayoutInflater.from(context);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        if (this.list == null) {
            this.list = list;
        }else {
            this.list.clear();
            if (null != list) {
                this.list.addAll(list);
            }
        }
    }

    public void setList(List<T> list, boolean force) {
        if (force){
            this.list = list;
        }else {
            setList(list);
        }
    }

    public void addList(List<T> list) {
        if (this.list == null) {
            this.list = list;
        } else {
            this.list.addAll(list);
        }
    }

    public void delList(List<T> list) {
        if (this.list != null) {
            this.list.removeAll(list);
        }
    }

    public void deleteItem(int position) {
        if (this.list!= null) {
            this.list.remove(position);
            notifyDataSetChanged();
        }
    }

    protected void showAlertDialog(String title, String msg, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        if (okListener != null) {
            builder.setPositiveButton("确定", okListener);
        }
        if (cancelListener != null) {
            builder.setNegativeButton("取消", cancelListener);
        }

        builder.create().show();
    }

    /**
     * 跳转到activity, 不结束本Activity
     *
     * @param clazz
     */
    protected void openActivity(Class<?> clazz) {
        openActivity(clazz, null);
    }


    /**
     * 跳转到activity，附带bundle，是否结束本activity
     *
     * @param clazz
     * @param extras 附带Bundle
     */
    protected void openActivity(Class<?> clazz, Bundle extras) {
        Intent intent = new Intent(getContext(), clazz);
        if (extras != null) {
            intent.putExtras(extras);
        }
        getContext().startActivity(intent);
    }

    protected static class ViewHolder {

        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}
