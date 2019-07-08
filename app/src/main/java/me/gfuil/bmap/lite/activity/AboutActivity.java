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

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.BuildConfig;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.AboutAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.model.AboutModel;

/**
 * 关于
 *
 * @author gfuil
 */

public class AboutActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView mListAbout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_about);

        getData();
    }

    private void getData() {
        List<AboutModel> list = new ArrayList<>();
        list.add(new AboutModel("免责声明", getResources().getString(R.string.app_protocol)));
        list.add(new AboutModel("说明", getResources().getString(R.string.app_name) + "，" + getResources().getString(R.string.app_info) + "感谢百度地图、高德地图"));
        list.add(new AboutModel("版本号", BuildConfig.VERSION_NAME + "，开源地址：" + getResources().getString(R.string.link_github)));
        list.add(new AboutModel("开发者", getResources().getString(R.string.app_developer) + "，" + getResources().getString(R.string.mail_developer)));
        list.add(new AboutModel("特别鸣谢", getResources().getString(R.string.thanks)));
        list.add(new AboutModel("分享给好友", null));

        mListAbout.setAdapter(new AboutAdapter(this, list));
    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = getView(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mListAbout = getView(R.id.list_about);
        mListAbout.setOnItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (0 == position) {
        } else if (1 == position) {
        } else if (2 == position) {
            openUrl(getResources().getString(R.string.link_github));
        } else if (3 == position) {
            sendMail(getResources().getString(R.string.mail_developer), getResources().getString(R.string.app_name) + "-v" + BuildConfig.VERSION_NAME + " [" + Build.MODEL + "]");
        } else if (4 == position) {
            openUrl(getResources().getString(R.string.link_thanks));
        } else if (5 == position) {
            share(getResources().getString(R.string.app_name), "我正在使用" + getResources().getString(R.string.app_name) + "，简单的双地图应用，一起来试试吧！" + getResources().getString(R.string.link_download));
        }
    }

    private void openUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendMail(String mailAddress, String subject) {
        try {
            Uri uri = Uri.parse("mailto:" + mailAddress);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject); // 主题
            startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
        } catch (Exception e) {
            onMessage(mailAddress);
            e.printStackTrace();
        }
    }

    private void share(String title, String msg) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享"));
    }

}
