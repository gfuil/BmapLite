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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseRecyclerAdapter;

/**
 * @author gfuil
 */

public class RouteDetailsAdapter extends BaseRecyclerAdapter<String> {
    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public RouteDetailsAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_route_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView textDetails = ViewHolder.get(holder.getItemView(), R.id.text_details);
        textDetails.setText(Html.fromHtml(getList().get(position)));
        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onItemClickListener) {
                    onItemClickListener.onItemClick(position, getList().get(position));
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String route);
    }
}
