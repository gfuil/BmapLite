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

package me.gfuil.bmap.lite.activity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import org.json.JSONException;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.fragment.AmapBusFragment;
import me.gfuil.bmap.lite.fragment.AmapRouteFragment;
import me.gfuil.bmap.lite.fragment.BaiduBusFragment;
import me.gfuil.bmap.lite.fragment.BaiduMapRouteFragment;
import me.gfuil.bmap.lite.fragment.RouteHistoryFragment;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.RouteHistoryModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypeNavigation;
import me.gfuil.bmap.lite.model.TypeSearch;

/**
 * 路线
 *
 * @author gfuil
 */

public class RouteActivity extends BaseActivity implements TabLayout.OnTabSelectedListener, View.OnClickListener {
    private TabLayout mTab;
    private AppBarLayout mLayAppBar;
    private TextView mTextStart, mTextEnd;
    private MyPoiModel mPoiStart, mPoiEnd;
    private int mSelect = 0;
    private BaiduMapRouteFragment mBaiduMapRouteFragment;
    private BaiduBusFragment mBaiduBusFragment;
    private AmapRouteFragment mAmapRouteFragment;
    private AmapBusFragment mAmapBusFragment;
    private RouteHistoryFragment mHistoryFragment;
    private TypeMap mTypeMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView(R.layout.activity_route);
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.route, menu);

        String map = "查";
        if (mTypeMap == TypeMap.TYPE_AMAP) {
            map += "百度路线";
        } else if (mTypeMap == TypeMap.TYPE_BAIDU) {
            map += "高德路线";
        }
        menu.findItem(R.id.action_change_map).setTitle(map);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //常亮
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        if (configInteracter.isScreenLightAlways()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void getData() {
        TypeNavigation typeNavigation = TypeNavigation.WALK;
        if (null != getExtras()) {
            mPoiStart = getExtras().getParcelable("start");
            mPoiEnd = getExtras().getParcelable("end");
            mTypeMap = TypeMap.fromInt(getExtras().getInt("type", BApp.TYPE_MAP.getInt()));
            typeNavigation = (TypeNavigation) getExtras().getSerializable("typeNavi");
        }
        if (null == mTypeMap) {
            mTypeMap = BApp.TYPE_MAP;
        }
        if (null == typeNavigation){
            typeNavigation = TypeNavigation.DRIVE;
        }

        if (null == mPoiStart) {
            mPoiStart = BApp.MY_LOCATION;
        }
        if (null == mPoiStart) {
            CacheInteracter cacheInteracter = new CacheInteracter(this);
            mPoiStart = new MyPoiModel(BApp.TYPE_MAP);
            mPoiStart.setName("我的位置");
            mPoiStart.setLatitude(cacheInteracter.getLatitude());
            mPoiStart.setLongitude(cacheInteracter.getLongitude());
        }
        mTextStart.setHint(mPoiStart.getName());

        if (null != mPoiEnd) {
            mTextEnd.setHint(mPoiEnd.getName());

//            if (getDistance() > 0 && getDistance() < 1000) {
//                updateView(TypeNavigation.WALK);
//            } else {
                if (typeNavigation == TypeNavigation.DRIVE) {
                    updateView(TypeNavigation.DRIVE);
                }else if (typeNavigation == TypeNavigation.BIKE){
                    updateView(TypeNavigation.BIKE);
                }else if (typeNavigation == TypeNavigation.WALK){
                    updateView(TypeNavigation.WALK);
                }else if (typeNavigation == TypeNavigation.BUS){
                    updateView(TypeNavigation.BUS);
                }
//            }
        } else {
            updateView(TypeNavigation.HISTORY);
        }
    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTab = getView(R.id.tab);

        mTab.addTab(mTab.newTab().setText("历史"));
        mTab.addTab(mTab.newTab().setText("公交"));
        mTab.addTab(mTab.newTab().setText("步行"));
        mTab.addTab(mTab.newTab().setText("骑行"));
        mTab.addTab(mTab.newTab().setText("驾驶"));
        mTab.addOnTabSelectedListener(this);

        mLayAppBar = getView(R.id.lay_app_bar);
        mTextStart = getView(R.id.text_start);
        mTextEnd = getView(R.id.text_end);

        mTextStart.setOnClickListener(this);
        mTextEnd.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        } else if (R.id.action_back == id) {
            MyPoiModel tmp = mPoiStart;
            mPoiStart = mPoiEnd;
            mPoiEnd = tmp;
            tmp = null;

            if (null != mPoiStart && null != mPoiEnd) {
                mTextStart.setHint(mPoiStart.getName());
                mTextEnd.setHint(mPoiEnd.getName());

                routeLine();
            }
        } else if (R.id.action_change_map == id) {
            Bundle bundle = getExtras();
            if (mTypeMap == TypeMap.TYPE_AMAP) {
                bundle.putInt("type", TypeMap.TYPE_BAIDU.getInt());
            } else if (mTypeMap == TypeMap.TYPE_BAIDU) {
                bundle.putInt("type", TypeMap.TYPE_AMAP.getInt());
            }
            bundle.putParcelable("start", mPoiStart);
            bundle.putParcelable("end", mPoiEnd);
            openActivity(RouteActivity.class, bundle, true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        routeLine();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        routeLine();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("type", TypeSearch.CITY);
        bundle.putBoolean("show", false);
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtras(bundle);

        switch (v.getId()) {
            case R.id.text_start:
                mSelect = 0;
                startActivityForResult(intent, MainActivity.REQUEST_SEARCH);
                break;
            case R.id.text_end:
                mSelect = 1;
                startActivityForResult(intent, MainActivity.REQUEST_SEARCH);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (MainActivity.REQUEST_SEARCH == resultCode) {
            if (null != data && null != data.getExtras()) {
                MyPoiModel poiInfo = data.getExtras().getParcelable("poi");
                if (null != poiInfo) {
                    if (mSelect == 0) {
                        mPoiStart = poiInfo;
                        mTextStart.setHint(mPoiStart.getName());
                    } else {
                        mPoiEnd = poiInfo;
                        mTextEnd.setHint(mPoiEnd.getName());
                    }

                    if (null != mPoiStart && null != mPoiEnd) {
                        routeLine();
                    }
                }

            }
        }
    }

    public int getDistance() {
        if (null == mPoiStart || null == mPoiEnd) {
            return 0;
        }
        return (int) DistanceUtil.getDistance(new LatLng(mPoiStart.getLatitude(), mPoiStart.getLongitude()),
                new LatLng(mPoiEnd.getLatitude(), mPoiEnd.getLongitude()));
    }

    public void updateView(TypeNavigation type) {
        if (type == TypeNavigation.BUS) {
            mTab.getTabAt(1).select();
            //mTab.setScrollPosition(0, 0, true);
        } else if (type == TypeNavigation.WALK) {
            mTab.getTabAt(2).select();
            //mTab.setScrollPosition(1, 0, true);
        } else if (type == TypeNavigation.BIKE) {
            mTab.getTabAt(3).select();
            // mTab.setScrollPosition(2, 0, true);
        } else if (type == TypeNavigation.DRIVE) {
            mTab.getTabAt(4).select();
            //mTab.setScrollPosition(3, 0, true);
        } else {
            mTab.getTabAt(0).select();
        }
    }


    private void routeLine(TypeNavigation type) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("start", mPoiStart);
        bundle.putParcelable("end", mPoiEnd);
        bundle.putSerializable("type", type);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (type == TypeNavigation.HISTORY) {
            if (null == mHistoryFragment) {
                mHistoryFragment = RouteHistoryFragment.newInstance();
            }

            if (null != mBaiduMapRouteFragment && mBaiduMapRouteFragment.isAdded()) {
                transaction.hide(mBaiduMapRouteFragment);
            }
            if (null != mBaiduBusFragment && mBaiduBusFragment.isAdded()) {
                transaction.hide(mBaiduBusFragment);
            }
            if (null != mAmapBusFragment && mAmapBusFragment.isAdded()) {
                transaction.hide(mAmapBusFragment);
            }
            if (null != mAmapRouteFragment && mAmapRouteFragment.isAdded()) {
                transaction.hide(mAmapRouteFragment);
            }
            if (!mHistoryFragment.isAdded()) {
                transaction.add(R.id.lay_content, mHistoryFragment);
            } else {
                transaction.show(mHistoryFragment);
            }

        } else {
            if (null != mHistoryFragment && mHistoryFragment.isAdded()) {
                transaction.hide(mHistoryFragment);
            }

            if (mTypeMap == TypeMap.TYPE_BAIDU) {
                if (type == TypeNavigation.BUS) {
                    if (null == mBaiduBusFragment) {
                        mBaiduBusFragment = BaiduBusFragment.newInstance();
                        mBaiduBusFragment.setArguments(bundle);
                    } else {
                        mBaiduBusFragment.reRoute(bundle);
                    }

                    if (null != mBaiduMapRouteFragment && mBaiduMapRouteFragment.isAdded()) {
                        transaction.hide(mBaiduMapRouteFragment);
                    }
                    if (!mBaiduBusFragment.isAdded()) {
                        transaction.add(R.id.lay_content, mBaiduBusFragment);
                    } else {
                        transaction.show(mBaiduBusFragment);
                    }


                } else {
                    if (null == mBaiduMapRouteFragment) {
                        mBaiduMapRouteFragment = BaiduMapRouteFragment.newInstance();
                        mBaiduMapRouteFragment.setArguments(bundle);
                    } else {
                        mBaiduMapRouteFragment.reRoute(bundle);
                    }

                    if (null != mBaiduBusFragment && mBaiduBusFragment.isAdded()) {
                        transaction.hide(mBaiduBusFragment);
                    }
                    if (!mBaiduMapRouteFragment.isAdded()) {
                        transaction.add(R.id.lay_content, mBaiduMapRouteFragment);
                    } else {
                        transaction.show(mBaiduMapRouteFragment);
                    }


                }
            } else if (mTypeMap == TypeMap.TYPE_AMAP) {
                if (type == TypeNavigation.BUS) {
                    if (null == mAmapBusFragment) {
                        mAmapBusFragment = AmapBusFragment.newInstance();
                        mAmapBusFragment.setArguments(bundle);
                    } else {
                        mAmapBusFragment.reRoute(bundle);
                    }

                    if (null != mAmapRouteFragment && mAmapRouteFragment.isAdded()) {
                        transaction.hide(mAmapRouteFragment);
                    }
                    if (!mAmapBusFragment.isAdded()) {
                        transaction.add(R.id.lay_content, mAmapBusFragment);
                    } else {
                        transaction.show(mAmapBusFragment);
                    }


                } else {
                    if (null == mAmapRouteFragment) {
                        mAmapRouteFragment = AmapRouteFragment.newInstance();
                        mAmapRouteFragment.setArguments(bundle);
                    } else {
                        mAmapRouteFragment.reRoute(bundle);
                    }

                    if (null != mAmapBusFragment && mAmapBusFragment.isAdded()) {
                        transaction.hide(mAmapBusFragment);
                    }
                    if (!mAmapRouteFragment.isAdded()) {
                        transaction.add(R.id.lay_content, mAmapRouteFragment);
                    } else {
                        transaction.show(mAmapRouteFragment);
                    }

                }
            }
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

    }

    public void showToolbar() {
        showToolbar(mLayAppBar.getVisibility() == View.GONE);
    }

    private void showToolbar(boolean isShow) {
        if (isShow) {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            animation.setDuration(200);
            mLayAppBar.startAnimation(animation);
            mLayAppBar.setVisibility(View.VISIBLE);
        } else {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
            animation.setDuration(200);
            mLayAppBar.startAnimation(animation);
            mLayAppBar.setVisibility(View.GONE);
        }
    }

    public void resetEnd(MyPoiModel poiClickNow) {
        mPoiEnd = poiClickNow;
        mTextEnd.setHint(mPoiEnd.getName());

        if (null != mPoiStart) {
            routeLine();
        }
    }

    public void resetStart(MyPoiModel poiClickNow) {
        mPoiStart = poiClickNow;
        mTextStart.setHint(mPoiStart.getName());

        if (null != mPoiEnd) {
            routeLine();
        }
    }

    public void reset(MyPoiModel start, MyPoiModel end) {
        if (null == start || "我的位置".equals(start.getName())) {
            mPoiStart = BApp.MY_LOCATION;
        } else {
            mPoiStart = start;
        }
        if (null == end || "我的位置".equals(end.getName())) {
            mPoiEnd = BApp.MY_LOCATION;
        } else {
            mPoiEnd = end;
        }

        if (null != mPoiStart && null != mPoiEnd) {
            mTextStart.setHint(mPoiStart.getName());
            mTextEnd.setHint(mPoiEnd.getName());
            routeLine();
        }
    }

    private void routeLine() {
        if (mTab.getSelectedTabPosition() == 1) {
            routeLine(TypeNavigation.BUS);
        } else if (mTab.getSelectedTabPosition() == 2) {
            routeLine(TypeNavigation.WALK);
        } else if (mTab.getSelectedTabPosition() == 3) {
            routeLine(TypeNavigation.BIKE);
        } else if (mTab.getSelectedTabPosition() == 4) {
            routeLine(TypeNavigation.DRIVE);
        } else {
            routeLine(TypeNavigation.HISTORY);
        }

        if (null != mPoiStart && null != mPoiEnd) {
            RouteHistoryModel history = new RouteHistoryModel();
            history.setNameStart(mPoiStart.getName());
            history.setLatStart(mPoiStart.getLatitude());
            history.setLngStart(mPoiStart.getLongitude());
            history.setNameEnd(mPoiEnd.getName());
            history.setLatEnd(mPoiEnd.getLatitude());
            history.setLngEnd(mPoiEnd.getLongitude());
            history.setTime(System.currentTimeMillis());
            CacheInteracter cacheInteracter = new CacheInteracter(this);
            try {
                cacheInteracter.addRouteHistory(history);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
