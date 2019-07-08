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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.mapapi.model.LatLng;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;
import me.gfuil.bmap.lite.utils.StatusBarUtils;

/**
 * 街景
 *
 * @author gfuil
 */

public class PanoramaActivity extends BaseActivity implements PanoramaViewListener {
    private PanoramaView mPanoramaView;
    private WebView mWebPanorama;
    private TextView mTextLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //用webview实现
            super.onCreate(savedInstanceState);
            initView(R.layout.activity_panorama);

        } else {
            BMapManager mBmapManager = new BMapManager(getApplicationContext());
            mBmapManager.init(new MKGeneralListener() {
                @Override
                public void onGetPermissionState(int i) {
                    LogUtils.debug(i + "");
                }
            });
            super.onCreate(savedInstanceState);
            initView(R.layout.activity_panorama2);

        }
        getData();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mPanoramaView)
            mPanoramaView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mPanoramaView)
            mPanoramaView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (null != mPanoramaView)
            mPanoramaView.destroy();
        super.onDestroy();
    }

    private void getData() {
        Bundle bundle = getExtras();

        double latitude = 0, longitude = 0;
        MyPoiModel poi = null;
        if (null != bundle) {
            poi = bundle.getParcelable("poi");
        }
        if (null == poi) {
            finish();
            return;
        }

        com.baidu.mapapi.utils.CoordinateConverter converter = new com.baidu.mapapi.utils.CoordinateConverter();
        converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.COMMON);
        // sourceLatLng待转换坐标
        converter.coord(new LatLng(poi.getLatitude(), poi.getLongitude()));

        LatLng ll = converter.convert();

        latitude = ll.latitude;
        longitude = ll.longitude;

        if (latitude == 0 || longitude == 0) {
            CacheInteracter interacter = new CacheInteracter(this);
            latitude = interacter.getLatitude();
            longitude = interacter.getLongitude();
        }

        mTextLoading.setVisibility(View.VISIBLE);

        if (AppUtils.isNetworkConnected(this) || AppUtils.isWifiConnected(this)) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                //用webview实现
                mWebPanorama.loadUrl("file:///android_asset/panorama.html?lng=" + longitude + "&lat=" + latitude + "&uid=" + poi.getUid());
                mWebPanorama.setVisibility(View.VISIBLE);
            } else {
                mPanoramaView.setPanorama(longitude, latitude);
            }
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && null != mWebPanorama) {
                mWebPanorama.setVisibility(View.GONE);
            }
            mTextLoading.setText("网络未连接");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        StatusBarUtils.setStatusBarColor(this, Color.TRANSPARENT);
        StatusBarUtils.setNavigationBarColor(this, Color.TRANSPARENT);


        getView(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTextLoading = getView(R.id.text_loading);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            mWebPanorama = getView(R.id.web_panorama);
            mWebPanorama.setVisibility(View.VISIBLE);
            WebSettings webSettings = mWebPanorama.getSettings();
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setGeolocationEnabled(true);
            webSettings.setDomStorageEnabled(true);

            mWebPanorama.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("baidumap")) {
                        return false;
                    } else if (url.startsWith("http") || url.startsWith("https") || url.startsWith("file")) {
                        view.loadUrl(url);
                    }
                    return true;
                }

            });

            mWebPanorama.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (100 == newProgress) {
                        mTextLoading.setVisibility(View.GONE);
                    }
                    super.onProgressChanged(view, newProgress);
                }

                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, true);
                    super.onGeolocationPermissionsShowPrompt(origin, callback);
                }
            });
        } else {
            mPanoramaView = getView(R.id.panorama);
            mPanoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
            mPanoramaView.setPanoramaViewListener(this);
            mPanoramaView.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onDescriptionLoadEnd(String s) {

    }

    @Override
    public void onLoadPanoramaBegin() {

    }

    @Override
    public void onLoadPanoramaEnd(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPanoramaView.setVisibility(View.VISIBLE);
                mTextLoading.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onLoadPanoramaError(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPanoramaView.setVisibility(View.GONE);
                mTextLoading.setVisibility(View.VISIBLE);
                mTextLoading.setText("加载错误，该位置没有街景图");
            }
        });
    }

    @Override
    public void onMessage(String s, int i) {

    }

    @Override
    public void onCustomMarkerClick(String s) {

    }

    @Override
    public void onMoveStart() {

    }

    @Override
    public void onMoveEnd() {

    }
}
