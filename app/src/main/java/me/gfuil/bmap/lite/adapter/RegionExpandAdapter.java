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

import me.gfuil.bmap.lite.base.BaseExpandableAdapter;
import me.gfuil.bmap.lite.model.MyRegionModel;

/**
 * @author gfuil
 */

public class RegionExpandAdapter extends BaseExpandableAdapter<MyRegionModel> {
    public RegionExpandAdapter(Context context, List<MyRegionModel> list) {
        super(context, list);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (null != getList() && null != getList().get(groupPosition).getChild()) ? getList().get(groupPosition).getChild().size() : 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (null != getList() && null != getList().get(groupPosition).getChild()) ? getList().get(groupPosition).getChild().get(childPosition) : null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = getInflater().inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        }
        TextView textName = ViewHolder.get(convertView, android.R.id.text1);
        textName.setText(getList().get(groupPosition).getName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = getInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textName = ViewHolder.get(convertView, android.R.id.text1);
        textName.setText(getList().get(groupPosition).getChild().get(childPosition));
        return convertView;
    }

}
