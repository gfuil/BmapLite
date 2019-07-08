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

import com.baidu.mapapi.map.offline.MKOLUpdateElement;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;

/**
 * @author gfuil
 */

public class BaiduDownloadCityAdapter extends BaseListAdapter<MKOLUpdateElement> {
    private OnClickBadiduDownloadOptionsListener onClickBadiduDownloadOptionsListener;

    public void setOnClickBadiduDownloadOptionsListener(OnClickBadiduDownloadOptionsListener onClickBadiduDownloadOptionsListener) {
        this.onClickBadiduDownloadOptionsListener = onClickBadiduDownloadOptionsListener;
    }

    public BaiduDownloadCityAdapter(Context context, List<MKOLUpdateElement> list) {
        super(context, list);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = getInflater().inflate(R.layout.item_download_city, parent, false);
        }
        TextView textCity = ViewHolder.get(convertView, R.id.text_city);
        final Button btnOptions = ViewHolder.get(convertView, R.id.btn_options);

        final MKOLUpdateElement city = getList().get(position);
        textCity.setText(city.cityName + "，大小：" + formatDataSize(city.size) + (city.ratio != 100 ? (" (" + city.ratio + "%)") : "") + (city.update ? " [可更新]" : ""));

        if (city.status == MKOLUpdateElement.DOWNLOADING){
            btnOptions.setText("暂停");
        }else if (city.status == MKOLUpdateElement.SUSPENDED){
            btnOptions.setText("开始");
        }else if (city.update){
            btnOptions.setText("更新");
        }else {
            btnOptions.setText("删除");
        }
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onClickBadiduDownloadOptionsListener){
                    onClickBadiduDownloadOptionsListener.onClickBaiduDownloadOptions(btnOptions.getText().toString().trim(), city);
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

    public interface OnClickBadiduDownloadOptionsListener{
        void onClickBaiduDownloadOptions(String option, MKOLUpdateElement city);
    }

}
