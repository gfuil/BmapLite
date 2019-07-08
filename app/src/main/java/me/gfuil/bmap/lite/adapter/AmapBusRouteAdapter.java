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
import android.widget.TextView;

import com.amap.api.services.route.BusPath;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;
import com.amap.mapapi.overlay.AMapUtil;

/**
 * @author gfuil
 */

public class AmapBusRouteAdapter extends BaseListAdapter<BusPath> {
    public AmapBusRouteAdapter(Context context, List<BusPath> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = getInflater().inflate(R.layout.item_bus_route, parent, false);
        }
        TextView textInfo = ViewHolder.get(convertView, R.id.text_info);
        TextView textRoute = ViewHolder.get(convertView, R.id.text_route);

        if (getList().get(position).getSteps() != null ) {
            textInfo.setText(AMapUtil.getBusPathTitle(getList().get(position)));
            textRoute.setText(AMapUtil.getBusPathDes(getList().get(position)));
        }

        return convertView;
    }
}
