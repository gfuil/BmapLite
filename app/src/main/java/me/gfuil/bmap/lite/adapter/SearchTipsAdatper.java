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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;

/**
 * @author gfuil
 */

public class SearchTipsAdatper extends BaseListAdapter<String> implements Filterable {
    public SearchTipsAdatper(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = getInflater().inflate(R.layout.item_search_tips, parent, false);
        }

        TextView textTips = ViewHolder.get(convertView, R.id.text_keyword);
        textTips.setText(getList().get(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private ArrayFilter mFilter;
    private List<String> mUnfilteredData;

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = getList();
            }
            if (prefix == null || prefix.length() == 0) {
                results.values = getList();
                results.count = getList().size();
            } else {
                List<String> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                List<String> newValues = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    String pc = unfilteredValues.get(i);
                    if (pc != null) {
                        newValues.add(pc);
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            setList((List<String>) results.values, true);
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
