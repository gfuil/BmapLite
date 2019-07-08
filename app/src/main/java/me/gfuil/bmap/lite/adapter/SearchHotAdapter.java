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
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;
import me.gfuil.bmap.lite.utils.AppUtils;

/**
 * @author gfuil
 */

public class SearchHotAdapter extends BaseListAdapter<String> {
    private int[] resIds = {
            R.drawable.ic_restaurant_menu_18dp,
            R.drawable.ic_local_grocery_store_18dp,
            R.drawable.ic_local_hotel_18dp,
            R.drawable.ic_local_convenience_store_18dp,
            R.drawable.ic_local_see_18dp,
            R.drawable.ic_local_hospital_18dp,
            R.drawable.ic_local_mall_18dp,
            R.drawable.ic_local_movies_18dp,
            R.drawable.ic_local_parking_black_18dp,
            R.drawable.ic_local_gas_station_18dp,
            R.drawable.ic_directions_bus_18dp,
            R.drawable.ic_directions_subway_18dp
    };

    private int[] colorIds = {
            R.color.keyword1,
            R.color.keyword2,
            R.color.keyword3,
            R.color.keyword4,
            R.color.keyword5,
            R.color.keyword6,
            R.color.keyword7,
            R.color.keyword8,
            R.color.keyword9,
            R.color.keyword10,
            R.color.keyword11,
            R.color.keyword12,

    };

    public SearchHotAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = getInflater().inflate(R.layout.item_search_hot, parent, false);
        }
        TextView textHot = ViewHolder.get(convertView, R.id.text_keyword);
        textHot.setText(getList().get(position));
        Drawable drawable = getContext().getResources().getDrawable(resIds[position]);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textHot.setCompoundDrawables(drawable, null, null, null);
        textHot.setCompoundDrawablePadding(AppUtils.dip2Px(getContext(), 5));

        textHot.setTextColor(getContext().getResources().getColor(colorIds[position]));


        return convertView;
    }
}
