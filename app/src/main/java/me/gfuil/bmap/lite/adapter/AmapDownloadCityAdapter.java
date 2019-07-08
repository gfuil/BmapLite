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

package me.gfuil.bmap.lite.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;

/**
 * @author gfuil
 */

public class AmapDownloadCityAdapter extends BaseListAdapter<OfflineMapCity> {
    private OnClickAmapDownloadOptionsListener onClickAmapDownloadOptionsListener;

    public void setOnClickAmapDownloadOptionsListener(OnClickAmapDownloadOptionsListener onClickAmapDownloadOptionsListener) {
        this.onClickAmapDownloadOptionsListener = onClickAmapDownloadOptionsListener;
    }

    public AmapDownloadCityAdapter(Context context, List<OfflineMapCity> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = getInflater().inflate(R.layout.item_download_city, parent, false);
        }
        TextView textCity = ViewHolder.get(convertView, R.id.text_city);
        final Button btnOptions = ViewHolder.get(convertView, R.id.btn_options);

        final OfflineMapCity city = getList().get(position);
        textCity.setText(city.getCity() + "，大小：" + formatDataSize((int) city.getSize()) + ((city.getcompleteCode() != 100 &&  city.getState() != OfflineMapStatus.SUCCESS)? (" ("+ city.getcompleteCode() + "%)") : "") + ((city.getState() == OfflineMapStatus.NEW_VERSION) ? " [可更新]" : "") + (city.getState() == OfflineMapStatus.ERROR ? " [错误]" : ""));

        if (city.getState() == OfflineMapStatus.NEW_VERSION){
            btnOptions.setText("更新");
        }else if (city.getState() == OfflineMapStatus.PAUSE || city.getState() == OfflineMapStatus.STOP){
            btnOptions.setText("开始");
        }else if (city.getState() == OfflineMapStatus.LOADING){
            btnOptions.setText("暂停");
        }else {
            btnOptions.setText("删除");
        }
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onClickAmapDownloadOptionsListener){
                    onClickAmapDownloadOptionsListener.onClickAmapDownloadOptions(btnOptions.getText().toString().trim(), city);
                }
            }
        });


        return convertView;
    }

    private String formatDataSize(int size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dKB", size / 1024);
        } else {
            ret = String.format("%.1fMB", size / (1024 * 1024.0));
        }
        return ret;
    }

    public interface OnClickAmapDownloadOptionsListener{
        void onClickAmapDownloadOptions(String option, OfflineMapCity city);
    }

}
