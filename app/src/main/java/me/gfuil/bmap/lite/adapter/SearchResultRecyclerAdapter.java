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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.activity.RouteActivity;
import me.gfuil.bmap.lite.activity.WebActivity;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.utils.StringUtils;

/**
 * @author gfuil
 */

public class SearchResultRecyclerAdapter extends RecyclerView.Adapter<SearchResultRecyclerAdapter.SearchResultViewHolder> {
    private Context context;
    private List<MyPoiModel> list;
    private MyPoiModel mNearby;
    private OnSelectSearchResultListener onSelectSearchResultListener;

    public OnSelectSearchResultListener getOnSelectSearchResultListener() {
        return onSelectSearchResultListener;
    }

    public void setOnSelectSearchResultListener(OnSelectSearchResultListener onSelectSearchResultListener) {
        this.onSelectSearchResultListener = onSelectSearchResultListener;
    }

    public List<MyPoiModel> getList() {
        return list;
    }

    public void setList(List<MyPoiModel> list) {
        if (this.list == null) {
            this.list = list;
        } else {
            this.list.clear();
            if (null != list) {
                this.list.addAll(list);
            }
        }
    }

    public SearchResultRecyclerAdapter(Context context, List<MyPoiModel> list, MyPoiModel nearby) {
        this.context = context;
        this.list = list;
        this.mNearby = nearby;
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
        final MyPoiModel poi = getList().get(position);
        holder.textName.setText(poi.getName());
        if (null != poi.getAddress() && !poi.getAddress().isEmpty()) {
            holder.textAddress.setVisibility(View.VISIBLE);
            holder.textAddress.setText(poi.getAddress());
        } else {
            holder.textAddress.setVisibility(View.GONE);
        }

        if (null != mNearby && (poi.getTypePoi() != TypePoi.BUS_LINE || poi.getTypePoi() != TypePoi.SUBWAY_LINE)) {
            int distance = (int) DistanceUtil.getDistance(new LatLng(mNearby.getLatitude(), mNearby.getLongitude()), new LatLng(poi.getLatitude(), poi.getLongitude()));
            if (1000 > distance && 0 < distance) {
                holder.textInfo.setText("[" + distance + "米]");
            } else if (1000 <= distance) {
                holder.textInfo.setText("[" + distance / 1000 + "公里]");
            } else {
                holder.textInfo.setVisibility(View.GONE);
            }

            holder.textInfo.setVisibility(View.VISIBLE);
        } else if (null != BApp.MY_LOCATION && (poi.getTypePoi() != TypePoi.BUS_LINE || poi.getTypePoi() != TypePoi.SUBWAY_LINE)) {

            int distance = (int) DistanceUtil.getDistance(new LatLng(BApp.MY_LOCATION.getLatitude(), BApp.MY_LOCATION.getLongitude()), new LatLng(poi.getLatitude(), poi.getLongitude()));
            if (1000 > distance && 0 < distance) {
                holder.textInfo.setText("[" + distance + "米]");
            } else if (1000 <= distance) {
                holder.textInfo.setText("[" + distance / 1000 + "公里]");
            } else {
                holder.textInfo.setVisibility(View.GONE);
            }

            holder.textInfo.setVisibility(View.VISIBLE);


        } else {
            holder.textInfo.setVisibility(View.GONE);
        }


        if (null != poi.getUid() && !poi.getUid().isEmpty()) {
            holder.btnCall.setVisibility(View.VISIBLE);
            holder.btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    callPhone(poi.getInfo());
                    gotoDetails(poi);
                }
            });
        } else {
            holder.btnCall.setVisibility(View.GONE);
        }


        if (poi.getTypePoi() == TypePoi.BUS_LINE || poi.getTypePoi() == TypePoi.SUBWAY_LINE) {
            holder.btnGoHere.setVisibility(View.GONE);
        } else {
            holder.btnGoHere.setVisibility(View.VISIBLE);
            holder.btnGoHere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    route(poi);
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onSelectSearchResultListener) {
                    onSelectSearchResultListener.onClick(poi);
                }
            }
        });
    }

    private void gotoDetails(MyPoiModel poi) {
        if (null == poi.getUid() || poi.getUid().isEmpty()) {
            Toast.makeText(context, "没有详情信息", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            bundle.putString("uid", poi.getUid());
            bundle.putString("url", "https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid=" + poi.getUid());
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            bundle.putString("url", "http://m.amap.com/detail/index/poiid=" + poi.getUid());
        }
        bundle.putParcelable("poi", poi);
        Intent intent= new Intent(context, WebActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private void route(MyPoiModel poi) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("start", BApp.MY_LOCATION);
        bundle.putParcelable("end", poi);
        Intent intent = new Intent(context, RouteActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }


    private void callPhone(final String info) {
        try {
            List<String> pStringList = new ArrayList<>();
            if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
                pStringList = Arrays.asList(StringUtils.convertStrToArray(info, ";"));
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
                pStringList = Arrays.asList(StringUtils.convertStrToArray(info, ","));
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("拨打电话");
            final String[] finalPhone = (String[]) pStringList.toArray();
            builder.setItems(finalPhone, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri data = Uri.parse("tel:" + finalPhone[which]);
                    intent.setData(data);
                    context.startActivity(intent);
                }
            });
            builder.create().show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textAddress;
        TextView textInfo;
        ImageView btnGoHere;
        ImageView btnCall;

        public SearchResultViewHolder(View itemView) {
            super(itemView);
            textName = (TextView) itemView.findViewById(R.id.text_name);
            textAddress = (TextView) itemView.findViewById(R.id.text_address);
            textInfo = (TextView) itemView.findViewById(R.id.text_info);
            btnGoHere = (ImageView) itemView.findViewById(R.id.btn_go_here);
            btnCall = (ImageView) itemView.findViewById(R.id.btn_call);
        }
    }

    public interface OnSelectSearchResultListener {
        void onClick(MyPoiModel poi);
    }
}
