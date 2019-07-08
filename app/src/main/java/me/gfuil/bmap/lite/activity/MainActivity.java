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

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.SearchResultRecyclerAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.fragment.AmapFragment;
import me.gfuil.bmap.lite.fragment.BaiduMapFragment;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.FavoriteInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypeNavigation;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.model.TypeSearch;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;
import me.gfuil.bmap.lite.utils.PermissionUtils;
import me.gfuil.bmap.lite.utils.StatusBarUtils;
import me.gfuil.bmap.lite.utils.StringUtils;

/**
 * 首页
 *
 * @author gfuil
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SearchResultRecyclerAdapter.OnSelectSearchResultListener {
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_LOCATION = 100;
    public static final int REQUEST_SEARCH = 1000;
    private final static int TIME_UP_OTHER = 2;
    public static boolean IS_FOREGROUND = true;

    private TextView textSearch, mTextPoiName, mTextPoiDistance, mTextStreet, mTextCollection, mTextNearby, mTextDetails, mTextShare;
    private DrawerLayout mDrawer;
    private MenuItem menuItemClose, menuItemClear, menuItemDelete, mMenuRanging;
    private FloatingActionButton btnLine;
    private FrameLayout mLayPoi, mLaySearchResult;
    private BottomSheetBehavior mBehaviorPoi, mBehaviorSearchRseult;
    private BaiduMapFragment baiduMapFragment;
    private AmapFragment mAmapFragment;
    private String mTypeShortcuts;
    private RecyclerView mRecycleResult;
    private SearchResultRecyclerAdapter mSearchPoiResultAdapter;

    public static boolean isExit = false;

    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_UP_OTHER:
                    getSchemeData(getIntent());
                    getOneStepData(getIntent());
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_main);

        getData();

        checkTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getOneStepData(intent);
        getSchemeData(intent);
    }

    private void checkTask() {
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(TIME_UP_OTHER);
            }
        };

        timer = new Timer(true);
        timer.schedule(task1, 1000);
    }

    private void getData() {
        if (null != getIntent() && null != getIntent().getDataString()) {
            mTypeShortcuts = getIntent().getDataString();
        }
    }

    private void getSchemeData(Intent intent) {
        if (null != intent) {
            if (null != intent.getDataString()) {
                mTypeShortcuts = intent.getDataString();
            }
            try {
                Uri uri = intent.getData();
                if (uri != null) {
                    String host = uri.getHost();
                    String dataString = intent.getDataString();
                    LogUtils.debug(dataString);
                    if ("maps.google.com".equals(host)) {
                        Map<String, String> requestMap = StringUtils.getUrlRequest(dataString);
                        String saddr = requestMap.get("saddr");
                        String daddr = requestMap.get("daddr");
                        String q = requestMap.get("q");

                        if (null != saddr && !saddr.isEmpty() && null != daddr && !daddr.isEmpty()) {

                            String[] startString = StringUtils.convertStrToArray(saddr, ",");
                            String[] endString = StringUtils.convertStrToArray(daddr, ",");

                            MyPoiModel startPoi = new MyPoiModel(TypeMap.TYPE_GOOGLE);
                            startPoi.setName("我的位置");
                            startPoi.setLatitude(Double.parseDouble(startString[0]));
                            startPoi.setLongitude(Double.parseDouble(startString[1]));

                            MyPoiModel endPoi = new MyPoiModel(TypeMap.TYPE_GOOGLE);
                            endPoi.setName("目的地");
                            endPoi.setLatitude(Double.parseDouble(endString[0]));
                            endPoi.setLongitude(Double.parseDouble(endString[1]));

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("start", startPoi);
                            bundle.putParcelable("end", endPoi);
                            openActivity(RouteActivity.class, bundle, false);
                        } else if (null != q && !q.isEmpty()) {
                            double lat = Double.parseDouble(StringUtils.convertStrToArray(q, ",")[0]);
                            double lng = Double.parseDouble(StringUtils.convertStrToArray(q, ",")[1]);
                            String name = dataString.substring(dataString.indexOf("(") > 0 ? dataString.indexOf("(") : 0);
                            MyPoiModel poi = new MyPoiModel(BApp.TYPE_MAP);
                            poi.setName(!dataString.equals(name) ? name : "外部传来的地点");
                            poi.setLatitude(lat);
                            poi.setLongitude(lng);
                            if (TypeMap.TYPE_AMAP == BApp.TYPE_MAP && null != mAmapFragment) {
                                mAmapFragment.showOtherPoi(poi);
                            } else if (TypeMap.TYPE_BAIDU == BApp.TYPE_MAP && null != baiduMapFragment) {
                                baiduMapFragment.showOtherPoi(poi);
                            }
                        }

                    } else if (null != dataString && dataString.contains("detail")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("url", dataString);
                        openActivity(WebActivity.class, bundle, false);
                    } else if (null != dataString && dataString.contains("geo:")) {

                        String keyword = dataString.substring(dataString.indexOf("geo:") + 4);
                        if (keyword.contains("?q=")) {
                            keyword = keyword.substring(0, keyword.indexOf("?q="));
                        } else if (keyword.contains("?z=")) {
                            keyword = keyword.substring(0, keyword.indexOf("?z="));
                        }
                        if (!"0,0".equals(keyword)) {
                            double lat = Double.parseDouble(StringUtils.convertStrToArray(keyword, ",")[0]);
                            double lng = Double.parseDouble(StringUtils.convertStrToArray(keyword, ",")[1]);

                            String name = dataString.substring(dataString.indexOf("(") > 0 ? dataString.indexOf("(") : 0);

                            MyPoiModel poi = new MyPoiModel(BApp.TYPE_MAP);
                            poi.setName(!dataString.equals(name) ? name : "外部传来的地点");
                            poi.setLatitude(lat);
                            poi.setLongitude(lng);
                            if (TypeMap.TYPE_AMAP == BApp.TYPE_MAP && null != mAmapFragment) {
                                mAmapFragment.showOtherPoi(poi);
                            } else if (TypeMap.TYPE_BAIDU == BApp.TYPE_MAP && null != baiduMapFragment) {
                                baiduMapFragment.showOtherPoi(poi);
                            }
                        } else {
                            keyword = dataString.substring(dataString.indexOf("?q=") + 3);
                            Bundle bundle = new Bundle();
                            bundle.putString("keyword", URLDecoder.decode(keyword, "UTF-8"));
                            bundle.putString("from", "MainActivity");
                            openActivity(SearchActivity.class, bundle, false);
                        }


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getOneStepData(Intent intent) {
        if (null != intent) {
            if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    if (sharedText.contains("maps.google.com/maps?q=")) {
                        sharedText = sharedText.substring(sharedText.indexOf("q=") + 2);
                        if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
                            sharedText += ",1";
                        } else if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
                            sharedText += ",2";
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("keyword", sharedText);
                    bundle.putString("from", "MainActivity");
                    openActivity(SearchActivity.class, bundle, false);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IS_FOREGROUND = true;
        //常亮
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        if (configInteracter.isScreenLightAlways()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        IS_FOREGROUND = false;
    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textSearch = getView(R.id.text_search);
        mTextPoiName = getView(R.id.text_poi_name);
        mTextPoiDistance = getView(R.id.text_poi_distance);
        mTextCollection = getView(R.id.text_collection);
        mTextStreet = getView(R.id.text_street);
        mTextNearby = getView(R.id.text_nearby);
        mTextDetails = getView(R.id.text_details);
        mTextShare = getView(R.id.text_share);
        mRecycleResult = getView(R.id.recycler_result);

        mLayPoi = getView(R.id.lay_poi);
        mLaySearchResult = getView(R.id.lay_search_result);

        btnLine = getView(R.id.fab_line);
        //mBtnRoute = getView(R.id.fab_route);
        //btnLine.setOnClickListener(this);
        textSearch.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleResult.setLayoutManager(layoutManager);
        mRecycleResult.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = getView(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenuRanging = navigationView.getMenu().findItem(R.id.nav_ranging);
        if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            navigationView.getMenu().findItem(R.id.nav_change_map).setTitle("切换百度地图");
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            navigationView.getMenu().findItem(R.id.nav_change_map).setTitle("切换高德地图");
        }


        int statusHeight = StatusBarUtils.getStatusBarHeight(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtils.setStatusBarColor(this, Color.TRANSPARENT);

            CardView cardView = getView(R.id.card_view);
            CoordinatorLayout.LayoutParams layoutParams2 = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppUtils.dip2Px(this, 45));
            layoutParams2.topMargin = statusHeight + AppUtils.dip2Px(this, 10);
            layoutParams2.rightMargin = AppUtils.dip2Px(this, 10);
            layoutParams2.leftMargin = AppUtils.dip2Px(this, 10);
            layoutParams2.bottomMargin = AppUtils.dip2Px(this, 10);
            cardView.setLayoutParams(layoutParams2);

            FrameLayout layStatus = getView(R.id.lay_status);
            layStatus.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusHeight));
            layStatus.setVisibility(View.VISIBLE);

        }

        initMap();

        showPoiLay(null, -1);

    }

    private void initMap() {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            baiduMapFragment = BaiduMapFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.lay_content, baiduMapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            mAmapFragment = AmapFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.lay_content, mAmapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        mBehaviorPoi = BottomSheetBehavior.from(mLayPoi);
        mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBehaviorPoi.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    showPoiLay(null, -1);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (-1 <= slideOffset && 0 >= slideOffset) {
                    float behaviorHeight = getResources().getDimension(R.dimen.bottom_poi_option_height);
                    btnLine.setTranslationY((-1 - slideOffset) * (behaviorHeight - AppUtils.dip2Px(MainActivity.this, 66)));
                    if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                        baiduMapFragment.setBtnLocationTranslationY((-1 - slideOffset) * (behaviorHeight - AppUtils.dip2Px(MainActivity.this, 40)));
                    } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                        mAmapFragment.setBtnLocationTranslationY((-1 - slideOffset) * (behaviorHeight - AppUtils.dip2Px(MainActivity.this, 40)));
                    }
                }
            }
        });

        mBehaviorSearchRseult = BottomSheetBehavior.from(mLaySearchResult);
        mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBehaviorSearchRseult.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    btnLine.setVisibility(View.VISIBLE);

                } else {
                    btnLine.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    public void showPoiLay(final MyPoiModel poi, final int distance) {
        if (null != poi) {
            mBehaviorPoi.setState(BottomSheetBehavior.STATE_EXPANDED);
            mTextPoiName.setText(poi.getName());
            mTextPoiDistance.setVisibility(View.VISIBLE);
            if (1000 > distance && 0 < distance) {
                mTextPoiDistance.setText(distance + "米");
            } else if (1000 <= distance) {
                mTextPoiDistance.setText(distance / 1000 + "公里");
            } else {
                mTextPoiDistance.setVisibility(View.GONE);
            }

            if (!"我的位置".equals(poi.getName())) {
                textSearch.setHint(poi.getName());
                mTextShare.setVisibility(View.GONE);
                mTextDetails.setVisibility(View.VISIBLE);
            } else {
                String info = "";

                if (poi.getAccuracy() > 0) {
                    info += "精度" + (int) poi.getAccuracy() + "米以内";
                }
                if (poi.getAltitude() != 0) {
                    info += "  海拔" + (int) poi.getAltitude() + "米";
                }

                mTextPoiDistance.setText(info);
                mTextPoiDistance.setVisibility(View.VISIBLE);
                mTextShare.setVisibility(View.VISIBLE);
                mTextDetails.setVisibility(View.GONE);
                mTextShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareMyLoc();
                    }
                });
            }
        } else {
            textSearch.setHint("搜索地点");
            mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);
            mTextDetails.setOnClickListener(null);
            mTextStreet.setOnClickListener(null);
            mTextCollection.setOnClickListener(null);
            mTextNearby.setOnClickListener(null);
            mTextShare.setOnClickListener(null);

            btnLine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    routeLine();
                }
            });

            return;
        }
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                int typeRoute = new ConfigInteracter(MainActivity.this).getTypeRoute();
                if (typeRoute == 0) {
                    if (distance <= 1000) {
                        bundle.putSerializable("typeNavi", TypeNavigation.WALK);
                    } else {
                        bundle.putSerializable("typeNavi", TypeNavigation.DRIVE);
                    }
                } else if (typeRoute == 1) {
                    bundle.putSerializable("typeNavi", TypeNavigation.WALK);
                } else if (typeRoute == 3) {
                    bundle.putSerializable("typeNavi", TypeNavigation.BIKE);
                } else if (typeRoute == 4) {
                    bundle.putSerializable("typeNavi", TypeNavigation.DRIVE);
                } else if (typeRoute == 2) {
                    bundle.putSerializable("typeNavi", TypeNavigation.BUS);
                }
                if (null != BApp.MY_LOCATION) {
                    bundle.putParcelable("start", BApp.MY_LOCATION);
                }
                bundle.putParcelable("start", BApp.MY_LOCATION);
                bundle.putParcelable("end", poi);
                openActivity(RouteActivity.class, bundle, false);
            }
        });
        mTextDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == poi.getUid() || poi.getUid().isEmpty()) {
                    onMessage("没有详情信息");
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
                openActivity(WebActivity.class, bundle, false);
            }
        });
        mTextStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("poi", poi);
                openActivity(PanoramaActivity.class, bundle, false);
            }
        });
        mTextCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collection(poi);


            }
        });
        mTextNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("type", TypeSearch.NEARBY);
                bundle.putParcelable("nearby", poi);
                bundle.putString("from", "MainActivity");
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_SEARCH);
            }
        });

    }

    private void collection(final MyPoiModel poi) {
        final EditText editName = new EditText(MainActivity.this);
        if (null != poi && null != poi.getName()) editName.setText(poi.getName());
        editName.setHint("请填写名称");
        editName.setSingleLine(true);
        editName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        final EditText editInfo = new EditText(MainActivity.this);
        editInfo.setHint("请填写备注信息(非必填)");
        editInfo.setSingleLine(true);
        editInfo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});

        LinearLayout lay = new LinearLayout(MainActivity.this);
        lay.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30, 5, 30, 5);
        lay.addView(editName, layoutParams);
        lay.addView(editInfo, layoutParams);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("备注");
        builder.setView(lay);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editName.getText().toString().trim();
                String info = editInfo.getText().toString().trim();
                if (name.isEmpty()) {
                    AppUtils.closeKeyboard(editInfo, MainActivity.this);
                    onMessage("未填写名称无法收藏");
                    return;
                }
                poi.setAddress(info);
                poi.setName(name);
                FavoriteInteracter favoriteInteracter = new FavoriteInteracter(MainActivity.this);
                int i = favoriteInteracter.addFavorite(poi);
                if (i > 0) {
                    if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && baiduMapFragment != null) {
                        baiduMapFragment.getFavoriteList();
                    } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && mAmapFragment != null) {
                        mAmapFragment.getFavoriteList();
                    }
                    onMessage("收藏成功");
                } else {
                    onMessage("收藏失败，请尝试修改名称");
                }
                favoriteInteracter.destroy();

                AppUtils.closeKeyboard(editInfo, MainActivity.this);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppUtils.closeKeyboard(editInfo, MainActivity.this);
            }
        });
        builder.create().show();
    }

    private void shareMyLoc() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "我在这里");
        intent.putExtra(Intent.EXTRA_TEXT, "我在这里 http://maps.google.com/maps?q=" + BApp.MY_LOCATION.getLatitude() + "," + BApp.MY_LOCATION.getLongitude());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuItemClose = menu.findItem(R.id.action_close);
        menuItemClear = menu.findItem(R.id.action_clear);
        menuItemDelete = menu.findItem(R.id.action_delete);
        menu.findItem(R.id.action_real_time_traffic).setChecked((new ConfigInteracter(this).isTrafficEnable()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (R.id.action_close == id) {

            if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment && baiduMapFragment.isModeRanging()) {
                changeModeRanging(false);
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment && mAmapFragment.isModeRanging()) {
                changeModeRanging(false);
            }
            if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                baiduMapFragment.clearMarker();
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                mAmapFragment.clearMarker();
            }
            showPoiLay(null, -1);
            mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_HIDDEN);
            menuItemClose.setVisible(false);
            textSearch.setHint("搜索地点");
            mSearchPoiResultAdapter = null;
            mRecycleResult.setAdapter(null);
        } else if (R.id.action_clear == id) {
            clearRangingPoi();
        } else if (R.id.action_delete == id) {
            deleteRangingPoi();
        } else if (R.id.action_real_time_traffic == id) {
            changeTraffic(item);
        } else if (R.id.action_look_angle == id) {
            changeAngle(item);
        } else if (R.id.action_satellite_map == id) {
            changeMapType(item);
        }

        return super.onOptionsItemSelected(item);
    }


    private void clearRangingPoi() {
        setRangingDistance(0);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            baiduMapFragment.clearRangingPoi();
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            mAmapFragment.clearRangingPoi();
        }
    }

    private void deleteRangingPoi() {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            baiduMapFragment.deleteRangingPoi();
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            mAmapFragment.deleteRangingPoi();
        }

    }

    public void setRangingDistance(double distance) {
        if (null != textSearch) {
            String dis = "";
            if (2000 > distance) {
                dis += (int) distance + "m";
            } else {
                dis += String.format("%.1f", distance / 1000) + "km";
            }
            textSearch.setHint(dis);
        }
    }

    public void verifyPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                baiduMapFragment.initLocationSdk();
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                mAmapFragment.initAmapSdk();
            }
        }
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            showAlertDialog("权限申请", "感谢使用，为了您更好的使用体验，请授予应用获取位置的权限，否则您可能无法正常使用，谢谢您的支持。", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
                if (null != baiduMapFragment) {
                    baiduMapFragment.initLocationSdk();
                }
            } else {
                onMessage("您没有授予所需权限");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != timer) {
            timer.cancel();
        }
        if (isExit) {
            System.exit(0);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_SEARCH == resultCode) {
            mDrawer.closeDrawer(Gravity.START);
            if (null != data && null != data.getExtras()) {

                MyPoiModel poiInfo = data.getExtras().getParcelable("poi");
                List<MyPoiModel> poiAll = data.getExtras().getParcelableArrayList("poiAll");
                int position = data.getExtras().getInt("position");
                if (null != poiInfo) {
                    textSearch.setHint(poiInfo.getName());
                } else if (null != poiAll && !poiAll.isEmpty()) {
                    textSearch.setHint(poiAll.get(position).getName());
                    setSearchResultAdapter(position, poiAll);
                }

                if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                    baiduMapFragment.onActivityResult(requestCode, resultCode, data);
                } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                    mAmapFragment.onActivityResult(requestCode, resultCode, data);
                }

            }
        }
    }

    private void setSearchResultAdapter(int position, List<MyPoiModel> poiAll) {
        if (null == mSearchPoiResultAdapter) {
            mSearchPoiResultAdapter = new SearchResultRecyclerAdapter(this, poiAll, BApp.MY_LOCATION);
            mSearchPoiResultAdapter.setOnSelectSearchResultListener(this);
            mRecycleResult.setAdapter(mSearchPoiResultAdapter);
        } else {
            mSearchPoiResultAdapter.setList(poiAll);
            mSearchPoiResultAdapter.notifyDataSetChanged();
        }
        mRecycleResult.scrollToPosition(position);
        mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_EXPANDED);
        menuItemClose.setVisible(true);
    }

    public void showSearchResultLay(boolean show) {
        if (show) {
            mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }


    private void gotoSearch() {
        String keyword = textSearch.getHint().toString().trim();

        Bundle bundle = new Bundle();
        bundle.putSerializable("type", TypeSearch.CITY);
        if (!"智能巡航".equals(keyword) && !"搜索地点".equals(keyword)) {
            bundle.putString("keyword", keyword);
        }
        bundle.putString("from", "MainActivity");
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_SEARCH);

        textSearch.setHint("搜索地点");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawer.isDrawerOpen(Gravity.START)) {
                mDrawer.closeDrawer(Gravity.START);
            } else if (mBehaviorPoi.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);

            } else {
                exitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出
     */
    private long exitTime = 0;// 记录按返回键时间

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            onMessage("再按一次退出应用程序");
            exitTime = System.currentTimeMillis();
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            isExit = true;
            BApp.exitApp();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.text_search == v.getId()) {
            if (mMenuRanging.isChecked()) {
                onMessage("测距模式！可点击右上角关闭按钮x退出该模式");
            } else {
                gotoSearch();
            }
        }
    }

    private void routeLine() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("start", BApp.MY_LOCATION);
        openActivity(RouteActivity.class, bundle, false);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_change_map) {
            changeMap(item);
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_ranging) {
            changeModeRanging(!item.isChecked());
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_offline_map) {
            openActivity(OfflineMapActivity.class);
        } else if (id == R.id.nav_about) {
            openActivity(AboutActivity.class);
        } else if (id == R.id.nav_favorite) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH);
        } else if (id == R.id.nav_setting) {
            openActivity(SettingActivity.class);
        }

        return true;
    }

    private void changeMap(MenuItem item) {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            if (null == mAmapFragment) {
                mAmapFragment = AmapFragment.newInstance();
            }
            getFragmentManager().beginTransaction().replace(R.id.lay_content, mAmapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

            BApp.TYPE_MAP = TypeMap.TYPE_AMAP;
            item.setTitle("切换百度地图");
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            if (new ConfigInteracter(this).getNightMode() == 2) {
                Toast.makeText(this, "夜间模式下百度地图可能需要重启应用后生效", Toast.LENGTH_LONG).show();
            }

            if (null == baiduMapFragment) {
                baiduMapFragment = BaiduMapFragment.newInstance();
            }
            getFragmentManager().beginTransaction().replace(R.id.lay_content, baiduMapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

            BApp.TYPE_MAP = TypeMap.TYPE_BAIDU;
            item.setTitle("切换高德地图");
        }

        new ConfigInteracter(this).setTypeMap(BApp.TYPE_MAP);
    }

    private void changeModeRanging(boolean isChecked) {
        showPoiLay(null, -1);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            baiduMapFragment.setModeRanging(isChecked);
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            mAmapFragment.setModeRanging(isChecked);
        }
        mMenuRanging.setChecked(isChecked);
        menuItemDelete.setVisible(isChecked);
        menuItemClear.setVisible(isChecked);
        menuItemClose.setVisible(isChecked);
        if (isChecked) {
            textSearch.setHint("0m");
        } else {
            textSearch.setHint("搜索地点");
        }
    }

    public void changeTraffic(MenuItem item) {
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            boolean isTraffic = !baiduMapFragment.isTrafficEnabled();
            baiduMapFragment.setTrafficEnabled(isTraffic);
            item.setChecked(isTraffic);

            configInteracter.setTrafficEnable(isTraffic);
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            boolean isTraffic = !mAmapFragment.isTrafficEnabled();
            mAmapFragment.setTrafficEnabled(isTraffic);
            item.setChecked(isTraffic);

            configInteracter.setTrafficEnable(isTraffic);
        }
    }

    private void changeAngle(MenuItem item) {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            boolean isLookAngle = !(baiduMapFragment.getMapStatus().overlook == -45);
            if (isLookAngle) {
                item.setTitle("平视角度");

                MapStatus ms = new MapStatus.Builder(baiduMapFragment.getMapStatus()).overlook(-45).build();
                baiduMapFragment.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));

            } else {
                item.setTitle("俯视(3D)角度");
                MapStatus ms = new MapStatus.Builder(baiduMapFragment.getMapStatus()).overlook(90).build();
                baiduMapFragment.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
            }
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            if ("平视角度".equals(item.getTitle())) {
                mAmapFragment.changeTilt(0);
                item.setTitle("俯视(3D)角度");
            } else {
                mAmapFragment.changeTilt(45);
                item.setTitle("平视角度");
            }
        }
    }

    private void changeMapType(MenuItem item) {
        ConfigInteracter interacter = new ConfigInteracter(this);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            boolean isSatellite = !(baiduMapFragment.getMapType() == BaiduMap.MAP_TYPE_NORMAL);
            if (isSatellite) {
                item.setTitle("卫星图像");
                baiduMapFragment.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            } else {
                item.setTitle("平面地图");
                baiduMapFragment.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            }
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            boolean isSatellite = !(mAmapFragment.getMapType() == AMap.MAP_TYPE_NORMAL || mAmapFragment.getMapType() == AMap.MAP_TYPE_NIGHT);
            if (isSatellite) {
                item.setTitle("卫星图像");
                if (interacter.getNightMode() == 2) {
                    mAmapFragment.setMapType(AMap.MAP_TYPE_NIGHT);
                } else {
                    mAmapFragment.setMapType(AMap.MAP_TYPE_NORMAL);
                }

            } else {
                item.setTitle("平面地图");
                mAmapFragment.setMapType(AMap.MAP_TYPE_SATELLITE);
            }
        }
    }


    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(btnLine, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void firstLocationComplete() {
        if ("search".equals(mTypeShortcuts)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("type", TypeSearch.CITY);
            bundle.putString("from", "MainActivity");
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_SEARCH);
        }
    }

    @Override
    public void onClick(MyPoiModel poi) {
        if (TypeMap.TYPE_BAIDU == BApp.TYPE_MAP && null != baiduMapFragment) {
            if (poi.getTypePoi() == TypePoi.BUS_LINE || poi.getTypePoi() == TypePoi.SUBWAY_LINE) {
                baiduMapFragment.mTypePoi = poi.getTypePoi();
                baiduMapFragment.searchBusLine(poi.getCity(), poi.getUid());
                showPoiLay(null, -1);
            } else {
                baiduMapFragment.showOtherPoi(poi);
            }
        } else if (TypeMap.TYPE_AMAP == BApp.TYPE_MAP && null != mAmapFragment) {
            mAmapFragment.showOtherPoi(poi);
        }
    }

}
