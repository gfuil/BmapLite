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

import com.baidu.mapapi.search.route.MassTransitRouteLine;

import java.util.List;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseListAdapter;
import me.gfuil.bmap.lite.model.BusRouteModel;

/**
 * @author gfuil
 */

public class BaiduBusRouteAdapter extends BaseListAdapter<BusRouteModel> {
    private boolean isSame;

    public BaiduBusRouteAdapter(Context context, List<BusRouteModel> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = getInflater().inflate(R.layout.item_bus_route, parent, false);
        }
        TextView textInfo = ViewHolder.get(convertView, R.id.text_info);
        TextView textRoute = ViewHolder.get(convertView, R.id.text_route);

        BusRouteModel bus = getList().get(position);

        String info = "";
        int duration = bus.getDuration() / 60;
        int distance = bus.getDistance();

        if (duration > 60) {
            info += duration / 60 + "小时" + duration % 60 + "分钟 - ";
        } else {
            info += duration + "分钟 - ";
        }

        if (1000 > distance) {
            info += distance + "米";
        } else {
            info += String.format("%.1f", (double) distance / 1000) + "公里";
        }

        if (bus.isTexi()) {
            textInfo.setText(info + " - 出租车");
            textRoute.setVisibility(View.GONE);
        } else {
            textRoute.setText(info);
            textRoute.setVisibility(View.VISIBLE);

            MassTransitRouteLine line = bus.getLine();
            if ("实时公交".equals(bus.getName())) {
                textInfo.setText(bus.getName());
                textRoute.setVisibility(View.GONE);
                return convertView;
            }
            String msg = "";
            int walk = 0;
            if (bus.isSameCity()) {
                if (line.getNewSteps() != null) {
                    for (List<MassTransitRouteLine.TransitStep> transitStepList : line.getNewSteps()) {
                        if (transitStepList.size() == 1) {
                            MassTransitRouteLine.TransitStep step = transitStepList.get(0);
                            if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS) {
                                msg += step.getBusInfo().getName() + " > ";
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_TRAIN) {
                                msg += step.getTrainInfo().getName() + " > ";
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                                walk += step.getDistance();
                            }
                        } else {
                            for (MassTransitRouteLine.TransitStep step : transitStepList) {
                                if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS) {
                                    msg += step.getBusInfo().getName() + " / ";
                                } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_TRAIN) {
                                    msg += step.getTrainInfo().getName() + " / ";
                                } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                                    walk += step.getDistance();
                                }
                            }
                            if (msg.endsWith(" / ")) {
                                msg = msg.substring(0, msg.length() - 3) + " > ";
                            }
                        }
                    }
                }

            } else {
                if (line.getNewSteps() != null) {
                    for (List<MassTransitRouteLine.TransitStep> transitStepList : line.getNewSteps()) {
                        for (MassTransitRouteLine.TransitStep step : transitStepList) {
                            if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS) {
                                msg += step.getBusInfo().getName() + " > ";
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_TRAIN) {
                                msg += step.getTrainInfo().getName() + " > ";
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_PLANE) {
                                msg += step.getPlaneInfo().getName() + " > ";
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_COACH) {
                                msg += step.getCoachInfo().getName() + " > ";
                            } else if (step.getVehileType() == MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_WALK) {
                                walk += step.getDistance();
                            }
                        }

                    }

                }
            }

            if (msg.endsWith(" / ")) {
                textInfo.setText(msg.substring(0, msg.length() - 3));
            } else if (msg.endsWith(" > ")) {
                textInfo.setText(msg.substring(0, msg.length() - 3));
            } else {
                textInfo.setText(msg);
            }
            if (1000 > walk) {
                info += " - 步行" + walk + "米";
            } else {
                info += " - 步行" + String.format("%.1f", (double) walk / 1000) + "公里";
            }

            double price = bus.getPrice();
            if (price > 0) {
                info += " - " + String.format("%.2f", price) + "元";
            }

            textRoute.setText(info);
        }

        return convertView;
    }
}
