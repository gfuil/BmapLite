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
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;

/**
 * @author gfuil
 */

public class BaiduIndoorInfoAdapter extends BaseListAdapter<String> {
    private String floorId;
    private String curFloor;

    public BaiduIndoorInfoAdapter(Context context, List<String> list, String curFloor, String floorId) {
        super(context, list);
        setCurFloor(curFloor);
        setFloorId(floorId);
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getCurFloor() {
        return curFloor;
    }

    public void setCurFloor(String curFloor) {
        this.curFloor = curFloor;
        if (null == this.curFloor || this.curFloor.trim().isEmpty()){
            this.curFloor = "F1";
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = getInflater().inflate(R.layout.item_indoor_floor, parent, false);
        }
        TextView text = ViewHolder.get(convertView, R.id.text_floor);
        text.setText(getList().get(position));
        if (getList().get(position).equals(getCurFloor())){
            text.setBackgroundColor(getContext().getResources().getColor(R.color.colorPressed));
        }else {
            text.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }
}
