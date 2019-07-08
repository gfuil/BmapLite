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

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;
import me.gfuil.bmap.lite.model.AboutModel;

/**
 * @author gfuil
 */

public class AboutAdapter extends BaseListAdapter<AboutModel> {
    public AboutAdapter(Context context, List<AboutModel> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = getInflater().inflate(R.layout.item_about, parent, false);
        }
        TextView textName = ViewHolder.get(convertView, R.id.text_name);
        TextView textInfo = ViewHolder.get(convertView, R.id.text_info);

        AboutModel about = getList().get(position);
        textName.setText(about.getName());
        if (null != about.getInfo() && !about.getInfo().isEmpty()) {
            textInfo.setVisibility(View.VISIBLE);
            textInfo.setText(getList().get(position).getInfo());
        }else {
            textInfo.setVisibility(View.GONE);
        }

        return convertView;
    }
}
