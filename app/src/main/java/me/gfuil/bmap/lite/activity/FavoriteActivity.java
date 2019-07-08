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
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.BuildConfig;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.SearchPoiResultAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.FavoriteInteracter;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.FileUtils;
import me.gfuil.bmap.lite.utils.LogUtils;
import me.gfuil.bmap.lite.utils.PermissionUtils;
import me.gfuil.bmap.lite.utils.TimeUtils;


/**
 * 收藏夹
 *
 * @author gfuil
 */

public class FavoriteActivity extends BaseActivity implements SearchPoiResultAdapter.OnSelectPoiListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String[] PERMISSIONS_STOEAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_STOEAGE = 200;
    private ListView mListFavorite;
    private FavoriteInteracter mFavoriteInteracter;
    private SearchPoiResultAdapter mSearchPoiResultAdapter;
    private boolean mShowOption = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_favorite);

        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mFavoriteInteracter) {
            mFavoriteInteracter.destroy();
        }
    }

    private void getData() {
        Bundle bundle = getExtras();
        if (null != bundle) {
            mShowOption = bundle.getBoolean("show", true);
        }
        if (null == mFavoriteInteracter) {
            mFavoriteInteracter = new FavoriteInteracter(this);
        }

        if (null == mSearchPoiResultAdapter) {
            mSearchPoiResultAdapter = new SearchPoiResultAdapter(this, mFavoriteInteracter.getFavoriteList(), mShowOption, false, null);
            mSearchPoiResultAdapter.setOnSelectPoiListener(this);

            mListFavorite.setAdapter(mSearchPoiResultAdapter);
        } else {
            mSearchPoiResultAdapter.setList(mFavoriteInteracter.getFavoriteList(), true);
            mSearchPoiResultAdapter.notifyDataSetChanged();
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

        mListFavorite = getView(R.id.list_favorite);

        mListFavorite.setOnItemClickListener(this);
        mListFavorite.setOnItemLongClickListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        } else if (R.id.action_import == id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                verifyPermissions();
            } else {
                importFav();
            }
        } else if (R.id.action_export == id) {
            exportFav();
        } else if (R.id.action_clear == id) {
            showAlertDialog("提示", "您确定要清空收藏夹吗？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (null != mFavoriteInteracter) {
                        boolean b = mFavoriteInteracter.clearFavorite();
                        if (b) {
                            onMessage("收藏夹已清空");
                            getData();
                        }
                    }
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    public void verifyPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            importFav();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_STOEAGE, REQUEST_STOEAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STOEAGE) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
                importFav();
            } else {
                onMessage("您没有授予所需权限");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void exportFav() {
        if (null != mFavoriteInteracter) {
            List<MyPoiModel> list = mFavoriteInteracter.getFavoriteList();

            try {
                JSONArray array = new JSONArray();
                if (null != list && !list.isEmpty()) {
                    for (MyPoiModel poi : list) {
                        array.put(poi.toJSON());
                    }
                }

                JSONObject object = new JSONObject();
                object.put("app_version", BuildConfig.VERSION_CODE);
                if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
                    if (SDKInitializer.getCoordType() == CoordType.BD09LL) {
                        object.put("type_coord", "bd09ll");
                    } else {
                        object.put("type_coord", "gcj02");
                    }
                } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
                    object.put("type_coord", "gcj02");
                }
                object.put("fav", array);

                ConfigInteracter interacter = new ConfigInteracter(this);
                File dir = new File(interacter.getDirectory());
                File file = new File(dir, "Bmap-favorite " + TimeUtils.getSystemTime("yyyy-MM-dd") + ".json");
                if (file.exists()) {
                    file.delete();
                }
                if (file.createNewFile()) {
                    FileUtils.writeFileToSDCard(file, object.toString());
                }
                showAlertDialog("导出目录", file.getPath(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, null);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                onMessage("导出失败");
            }
        }
    }

    private void importFav() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/json");//设置类型
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, 222);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            onMessage("抱歉，未找到文件管理器程序");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 222 && resultCode == RESULT_OK) {
            if (null != data) {
                try {
                    String path;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        path = AppUtils.getAbsolutePathFromUri(this, data.getData());
                        importFav(new File(path));
                    } else {
                        path = AppUtils.getPathFromUri(this, data.getData());
                        importFav(new File(path));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    onMessage("导入失败");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importFav(File file) throws Exception {
        String str = FileUtils.readFileFromSDCard(file);
        JSONObject object = new JSONObject(str);
        boolean isBD09LL = ("bd09ll".equals(object.optString("type_coord")) || "bd09".equals(object.optString("type_coord")));
        if (null != object.optJSONArray("fav") && object.optJSONArray("fav").length() != 0) {
            for (int i = 0; i < object.optJSONArray("fav").length(); i++) {
                MyPoiModel poi = new MyPoiModel(BApp.TYPE_MAP);
                poi.fromJSON(object.optJSONArray("fav").optJSONObject(i));
                if (null != mFavoriteInteracter) {
                    LogUtils.debug("isBD09=" + isBD09LL);
                    if (isBD09LL) {
                        LogUtils.debug("before=" + poi.getLatitude() + "    " + poi.getLongitude());
                        com.amap.api.maps.CoordinateConverter converter = new com.amap.api.maps.CoordinateConverter(this);
                        converter.from(com.amap.api.maps.CoordinateConverter.CoordType.BAIDU);
                        converter.coord(new com.amap.api.maps.model.LatLng(poi.getLatitude(), poi.getLongitude()));

                        com.amap.api.maps.model.LatLng latLng = new com.amap.api.maps.model.LatLng(converter.convert().latitude, converter.convert().longitude);
                        poi.setLatitude(latLng.latitude);
                        poi.setLongitude(latLng.longitude);
                        LogUtils.debug("after=" + poi.getLatitude() + "    " + poi.getLongitude());
                        mFavoriteInteracter.addFavorite(poi);
                    } else {
                        mFavoriteInteracter.addFavorite(poi);
                    }
                }
            }
        }

        onMessage("导入成功");
        getData();
    }

    @Override
    public void setPoiStart(MyPoiModel poi) {

    }

    @Override
    public void setPoiEnd(MyPoiModel poi) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("start", BApp.MY_LOCATION);
        bundle.putParcelable("end", poi);
        openActivity(RouteActivity.class, bundle, true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        route(mSearchPoiResultAdapter.getList().get(position));
    }

    private void route(MyPoiModel poiInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("poi", poiInfo);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(MainActivity.REQUEST_SEARCH, intent);
        finish();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        showAlertDialog("提示", "您要删除该收藏吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyPoiModel poi = mSearchPoiResultAdapter.getList().get(position);
                boolean b = mFavoriteInteracter.deleteFavorite(poi.getUid());
                if (b) {
                    onMessage("删除成功");
                    getData();
                } else {
                    onMessage("删除失败");
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return true;
    }

    @Override
    public void onResult(int code, String msg) {
        if (666 == code) {
            getData();
        } else if (1 == code) {

        } else {
            super.onResult(code, msg);
        }
    }

    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(mListFavorite, msg, Snackbar.LENGTH_SHORT).show();
    }

}
