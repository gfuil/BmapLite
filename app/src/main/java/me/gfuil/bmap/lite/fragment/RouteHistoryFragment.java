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

package me.gfuil.bmap.lite.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.RouteActivity;
import me.gfuil.bmap.lite.adapter.RouteHistoryAdapter;
import me.gfuil.bmap.lite.base.BaseFragment;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.RouteHistoryModel;

/**
 * Created by gfuil on 2017/6/28.
 */

public class RouteHistoryFragment extends BaseFragment implements AdapterView.OnItemClickListener, RouteHistoryAdapter.OnRouteHistoryDeleteListener {
    private ListView mListHistory;
    private TextView mTextData;
    private RouteHistoryAdapter mHistoryAdapter;

    public static RouteHistoryFragment newInstance() {
        return new RouteHistoryFragment();
    }

    public RouteHistoryFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_history, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            getData();
        }
    }

    private void getData() {
        CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
        try {
            List<RouteHistoryModel> historyList = cacheInteracter.getRouteHistory();

            if (null != historyList && !historyList.isEmpty() && null != historyList.get(0)) {
                if (mHistoryAdapter == null) {
                    mHistoryAdapter = new RouteHistoryAdapter(getActivity(), historyList);
                    mHistoryAdapter.setOnRouteHistoryDeleteListener(this);
                    mListHistory.setAdapter(mHistoryAdapter);
                } else {
                    mHistoryAdapter.setList(historyList, true);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            } else {
                if (mHistoryAdapter == null) {
                    mHistoryAdapter = new RouteHistoryAdapter(getActivity(), null);
                    mListHistory.setAdapter(mHistoryAdapter);
                } else {
                    mHistoryAdapter.setList(null, true);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initView(View view) {
        mTextData = getView(view, R.id.text_history);
        mListHistory = getView(view, R.id.list_history);
        mListHistory.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RouteHistoryModel history = mHistoryAdapter.getList().get(position);
        MyPoiModel start = new MyPoiModel(BApp.TYPE_MAP);
        start.setName(history.getNameStart());
        start.setLatitude(history.getLatStart());
        start.setLongitude(history.getLngStart());

        MyPoiModel end = new MyPoiModel(BApp.TYPE_MAP);
        end.setName(history.getNameEnd());
        end.setLatitude(history.getLatEnd());
        end.setLongitude(history.getLngEnd());

        ((RouteActivity)getActivity()).reset(start, end);
        getData();
        
    }

    @Override
    public void onRouteHistoryDelete(RouteHistoryModel history) {
        CacheInteracter cacheInteracter = new CacheInteracter(getActivity());
        try {
            cacheInteracter.deleteRouteHistory(history);
            getData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
