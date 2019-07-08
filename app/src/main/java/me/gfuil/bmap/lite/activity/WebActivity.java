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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.FavoriteInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.tools.ADFilterTools;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;

/**
 * webview
 *
 * @author gfuil
 */

public class WebActivity extends BaseActivity {
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private boolean isClose = false;
    private MyPoiModel mPoi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            initView(R.layout.activity_web);
            getData();
        } catch (InflateException e) {
            e.printStackTrace();
            onMessage("抱歉，不支持您的手机，未找到WebView或者WebView版本过低");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web, menu);
        if (null != mPoi) {
            menu.findItem(R.id.action_collection).setVisible(true);
        }
        return true;
    }

    private void getData() {
        if (null != getExtras()) {
            String url = getExtras().getString("url");
            LogUtils.debug(url);
            mPoi = getExtras().getParcelable("poi");

            mWebView.loadUrl(url);

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mWebView = getView(R.id.web);
        mProgressBar = getView(R.id.progress_bar);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                LogUtils.debug("url=" + url);
                if (url.startsWith("tel:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        Uri data = Uri.parse(url);
                        intent.setData(data);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (url.startsWith("baidumap")) {
                    return false;
                } else if (url.startsWith("http") || url.startsWith("https") || url.startsWith("file")
                        && !url.startsWith("http://m.amap.com/index/index/")) {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handler.sendEmptyMessage(0x001);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (100 != newProgress) {
                    if (View.GONE == mProgressBar.getVisibility()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });


        isClose = false;
        //去广告
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isClose) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(0x001);
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null != mWebView) {
                String js = ADFilterTools.getClearAdDivByIdJs(getResources().getStringArray(R.array.div_ad_id));
                LogUtils.debug(js);
                mWebView.loadUrl(js);

                String js2 = ADFilterTools.getClearAdDivByClassJs(getResources().getStringArray(R.array.div_ad_class));
                LogUtils.debug(js2);
                mWebView.loadUrl(js2);
            }
        }
    };

    @Override
    protected void onDestroy() {
        isClose = true;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        } else if (R.id.action_share == id) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, mWebView.getTitle());
            intent.putExtra(Intent.EXTRA_TEXT, mWebView.getTitle() + "  " + mWebView.getUrl());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "分享"));
        } else if (R.id.action_collection == id) {
            if (null != mPoi) {
                collection(mPoi);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void collection(final MyPoiModel poi) {
        final EditText editName = new EditText(this);
        if (null != poi && null != poi.getName()) editName.setText(poi.getName());
        editName.setHint("请填写名称");
        editName.setSingleLine(true);
        editName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        final EditText editInfo = new EditText(this);
        editInfo.setHint("请填写备注信息(非必填)");
        editInfo.setSingleLine(true);
        editInfo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30, 5, 30, 5);
        lay.addView(editName, layoutParams);
        lay.addView(editInfo, layoutParams);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("备注");
        builder.setView(lay);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editName.getText().toString().trim();
                String info = editInfo.getText().toString().trim();
                if (name.isEmpty()) {
                    AppUtils.closeKeyboard(editInfo, WebActivity.this);
                    onMessage("未填写名称无法收藏");
                    return;
                }
                poi.setAddress(info);
                poi.setName(name);
                FavoriteInteracter favoriteInteracter = new FavoriteInteracter(WebActivity.this);
                int i = favoriteInteracter.addFavorite(poi);
                if (i > 0) {
                    onMessage("收藏成功");
                } else {
                    onMessage("收藏失败，请尝试修改名称");
                }
                favoriteInteracter.destroy();

                AppUtils.closeKeyboard(editInfo, WebActivity.this);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppUtils.closeKeyboard(editInfo, WebActivity.this);
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
