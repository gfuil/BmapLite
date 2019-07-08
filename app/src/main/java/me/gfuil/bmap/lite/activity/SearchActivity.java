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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.SuggestionCity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.SearchHotAdapter;
import me.gfuil.bmap.lite.adapter.SearchKeywordAdapter;
import me.gfuil.bmap.lite.adapter.SearchPoiResultAdapter;
import me.gfuil.bmap.lite.adapter.SearchSuggtionCityAdapter;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.interacter.CacheInteracter;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.SearchInteracter;
import me.gfuil.bmap.lite.listener.OnSearchResultListener;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.model.TypeSearch;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.StringUtils;
import me.gfuil.bmap.lite.view.LoadMoreListView;

/**
 * 搜索
 *
 * @author gfuil
 */
public class SearchActivity extends BaseActivity implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener, TextWatcher, SearchPoiResultAdapter.OnSelectPoiListener, OnSearchResultListener, View.OnClickListener, LoadMoreListView.OnLoadMoreListener, SearchKeywordAdapter.OnSearchHistoryDeleteListener, SearchSuggtionCityAdapter.OnClickCityListener {
    public final static int REQUEST_CITY_CODE = 555;
    public final static int REQUEST_POI = 666;
    private AutoCompleteTextView mEditSearch;
    private CheckBox mCheckNearby;
    private GridView mGridHot;
    private LoadMoreListView mListResult;
    private ListView mListHistory;
    private LinearLayout mLayMyCity;
    private TextView mTextCity;
    private String mCity, mFrom;
    private SearchPoiResultAdapter mResultAdapter;
    private SearchKeywordAdapter mKeywordAdapter;
    private SearchInteracter mSearchInteracter;
    private TypeSearch mType;
    private MyPoiModel mNearby;
    private boolean mShowOption = true;
    private int mPage = 0;
    private RecyclerView mRecycleCity;
    private SearchSuggtionCityAdapter mSearchSuggtionCityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_search);
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mShowOption) {
            getMenuInflater().inflate(R.menu.search, menu);
        } else {
            getMenuInflater().inflate(R.menu.search_othor, menu);
        }
        if (mType == TypeSearch.CITY) {
            menu.findItem(R.id.action_change_city).setVisible(true);
        } else {
            menu.findItem(R.id.action_change_city).setVisible(false);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        if (null != mSearchInteracter) {
            mSearchInteracter.destroy();
        }
        super.onDestroy();
    }

    private void getData() {
        String keyword = null;
        Bundle bundle = getExtras();
        if (null != bundle) {
            mType = (TypeSearch) bundle.getSerializable("type");
            mNearby = bundle.getParcelable("nearby");
            mShowOption = bundle.getBoolean("show", true);
            mFrom = bundle.getString("from");
            keyword = bundle.getString("keyword");
            if (null == mType) {
                mType = TypeSearch.CITY;
            }


        } else {
            mType = TypeSearch.CITY;
        }

        if (mType == TypeSearch.CITY) {
            mEditSearch.setHint("搜索地点");
            if (null != BApp.MY_LOCATION) {
                mCheckNearby.setChecked((new ConfigInteracter(this)).isSearchNearby());
                mLayMyCity.setVisibility(View.VISIBLE);
            }
        } else if (mType == TypeSearch.NEARBY) {
            mEditSearch.setHint("搜索附近");
            mLayMyCity.setVisibility(View.GONE);
        }

        if (null == mCity || mCity.isEmpty()) {
            mCity = (new CacheInteracter(this)).getCity2();
        }
        mTextCity.setText(mCity);

        mSearchInteracter = new SearchInteracter(this, BApp.TYPE_MAP);

        getHot();
        getHistoryKeyword();

        if (null != keyword && !keyword.isEmpty()) {
            mPage = 0;
            mEditSearch.setText(keyword);
            mEditSearch.setSelection(keyword.length());
            search();
        }


    }

    private void getHot() {
        SearchHotAdapter adapter = new SearchHotAdapter(this, Arrays.asList(getResources().getStringArray(R.array.tips)));
        mGridHot.setAdapter(adapter);
        mGridHot.setVisibility(View.VISIBLE);
    }

    private void getHistoryKeyword() {
        CacheInteracter cacheInteracter = new CacheInteracter(this);
        List<String> history = cacheInteracter.getSearchHistoryKeyword();

        if (null != history && !history.isEmpty() && null != history.get(0) && !history.get(0).isEmpty()) {
            if (mKeywordAdapter == null) {
                mKeywordAdapter = new SearchKeywordAdapter(this, history);
                mKeywordAdapter.setOnSearchHistoryDeleteListener(this);
                mListHistory.setAdapter(mKeywordAdapter);
            } else {
                mKeywordAdapter.setList(history, true);
                mKeywordAdapter.notifyDataSetChanged();
            }
            if (!mEditSearch.getText().toString().isEmpty() && null != mResultAdapter && 0 < mKeywordAdapter.getCount()) {
                mListHistory.setVisibility(View.GONE);
            } else {
                mListHistory.setVisibility(View.VISIBLE);
            }
        } else {
            if (mKeywordAdapter == null) {
                mKeywordAdapter = new SearchKeywordAdapter(this, null);
                mListHistory.setAdapter(mKeywordAdapter);
            } else {
                mKeywordAdapter.setList(null, true);
                mKeywordAdapter.notifyDataSetChanged();
            }
            mListHistory.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCheckNearby = getView(R.id.check_nearby);
        mEditSearch = getView(R.id.edit_search);
        mGridHot = getView(R.id.grid_hot);
        mListResult = getView(R.id.list_result);
        mListHistory = getView(R.id.list_history);
        mLayMyCity = getView(R.id.lay_my_city);
        mTextCity = getView(R.id.text_city);
        mRecycleCity = getView(R.id.recycler_city);

        TextView textHeader = new TextView(this);
        textHeader.setText("历史记录");
        textHeader.setPadding(AppUtils.dip2Px(this, 10), AppUtils.dip2Px(this, 10), AppUtils.dip2Px(this, 10), AppUtils.dip2Px(this, 10));
        textHeader.setTextColor(Color.parseColor("#999999"));
        textHeader.setTextSize(12);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_history_18dp);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textHeader.setCompoundDrawables(drawable, null, null, null);
        textHeader.setCompoundDrawablePadding(AppUtils.dip2Px(this, 5));
        mListHistory.addHeaderView(textHeader, null, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecycleCity.setLayoutManager(layoutManager);

        mTextCity.setOnClickListener(this);
        mEditSearch.setOnEditorActionListener(this);
        mEditSearch.addTextChangedListener(this);
        mListResult.setOnLoadMoreListener(this);
        mListResult.setOnItemClickListener(this);
        mListHistory.setOnItemClickListener(this);
        mGridHot.setOnItemClickListener(this);
        mCheckNearby.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        } else if (R.id.action_all == id) {
            Bundle bundle = new Bundle();
            if (null != mResultAdapter) {
                bundle.putParcelableArrayList("poiAll", (ArrayList<MyPoiModel>) mResultAdapter.getList());
            }

            Intent intent = new Intent();
            intent.putExtras(bundle);
            setResult(MainActivity.REQUEST_SEARCH, intent);
            finish();
            return true;
        } else if (R.id.action_favorite == id) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("show", mShowOption);
            Intent intent = new Intent(this, FavoriteActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, MainActivity.REQUEST_SEARCH);
        } else if (R.id.action_clear == id) {
            CacheInteracter cacheInteracter = new CacheInteracter(this);
            cacheInteracter.setSearchHistoryKeyword(null);
            getHistoryKeyword();
        } else if (R.id.action_search == id) {
            mPage = 0;
            search();
            AppUtils.closeKeyboard(mEditSearch, this);
        } else if (R.id.action_change_city == id) {
            startActivityForResult(new Intent(this, ChangeCityActivity.class), REQUEST_CITY_CODE);
        } else if (R.id.action_select_poi == id) {
            startActivityForResult(new Intent(this, SelectPoiActivity.class), MainActivity.REQUEST_SEARCH);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (MainActivity.REQUEST_SEARCH == resultCode) {
            if (null != data && null != data.getExtras() && null != data.getExtras().getParcelable("poi")) {
                route(0, (MyPoiModel) data.getExtras().getParcelable("poi"));
            }
        } else if (REQUEST_CITY_CODE == resultCode) {
            if (null != data && null != data.getExtras() && null != data.getExtras().getString("city")) {
                mCity = data.getExtras().getString("city");
                mTextCity.setText(mCity);
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            mPage = 0;
            search();
            AppUtils.closeKeyboard(mEditSearch, this);
            return true;
        }
        return false;
    }

    private void search() {
        String keyword = mEditSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            Snackbar.make(mEditSearch, "请输入关键字", Snackbar.LENGTH_SHORT).show();
            return;
        }

        CacheInteracter cacheInteracter = new CacheInteracter(this);

        try {
            String[] latlag = StringUtils.convertStrToArray(keyword, ",");
            if (latlag.length > 1) {
                double lat = 0, lng = 0;
                int type = 0; //0:gps 1:gcj 2:bd
                if (latlag.length == 2) {
                    lat = Double.parseDouble(latlag[0]);
                    lng = Double.parseDouble(latlag[1]);
                    type = 0;

                } else if (latlag.length == 3) {
                    lat = Double.parseDouble(latlag[0]);
                    lng = Double.parseDouble(latlag[1]);
                    type = Integer.parseInt(latlag[2]);
                }


                mSearchInteracter.searchLatLng(lat, lng, type, this);

                cacheInteracter.addSearchHistoryKeyword(keyword);
                getHistoryKeyword();
                return;
            }

        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }

        search(keyword);

        if (0 == mPage) {
            cacheInteracter.addSearchHistoryKeyword(keyword);
            getHistoryKeyword();
        }

    }

    private void search(String keyword) {
        try {
            if (TypeSearch.CITY == mType) {
                if (null != BApp.MY_LOCATION && mCheckNearby.isChecked()) {
                    mSearchInteracter.searchNearby(BApp.MY_LOCATION, keyword, mPage, this);
                } else {
                    mSearchInteracter.searchInCity(keyword, mCity, mPage, this);
                }
            } else if (TypeSearch.NEARBY == mType) {
                if (null != mNearby) {
                    mSearchInteracter.searchNearby(mNearby, keyword, mPage, this);
                } else if (null != BApp.MY_LOCATION) {
                    mSearchInteracter.searchNearby(BApp.MY_LOCATION, keyword, mPage, this);
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }


    }

    private void search(String city, String keyword) {
        try {
            mSearchInteracter.searchInCity(keyword, city, mPage, this);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (R.id.list_result == parent.getId()) {
            if (position < mResultAdapter.getCount()) {
                route(position, mResultAdapter.getList().get(position));
            }
            new CacheInteracter(this).addSearchHistoryKeyword(mEditSearch.getText().toString().trim());
            AppUtils.closeKeyboard(mEditSearch, this);
        } else if (R.id.list_history == parent.getId()) {
            String keyword = (String) mListHistory.getAdapter().getItem(position);
            if (null != keyword && !keyword.isEmpty()) {
                mPage = 0;
                mEditSearch.setText(keyword);
                mEditSearch.setSelection(keyword.length());
                search(keyword);
                AppUtils.closeKeyboard(mEditSearch, this);
            }
        } else if (R.id.grid_hot == parent.getId()) {
            String keyword = (String) mGridHot.getAdapter().getItem(position);
            if (null != keyword && !keyword.isEmpty()) {
                mPage = 0;
                mEditSearch.setText(keyword);
                mEditSearch.setSelection(keyword.length());
                search(keyword);
                AppUtils.closeKeyboard(mEditSearch, this);
            }
        }
    }

    private void route(int position, MyPoiModel poiInfo) {
        AppUtils.closeKeyboard(mEditSearch, this);
        Bundle bundle = new Bundle();
        if ("MainActivity".equals(mFrom) && !(poiInfo.getTypePoi() == TypePoi.BUS_LINE || poiInfo.getTypePoi() == TypePoi.SUBWAY_LINE)) {
            if (null != mResultAdapter) {
                bundle.putParcelableArrayList("poiAll", (ArrayList<MyPoiModel>) mResultAdapter.getList());
            }
            bundle.putInt("position", position);

        } else {
            bundle.putParcelable("poi", poiInfo);
        }

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(MainActivity.REQUEST_SEARCH, intent);
        finish();

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String str = mEditSearch.getText().toString().trim();
        if (0 == str.length()) {
            mGridHot.setVisibility(View.VISIBLE);
            mListHistory.setVisibility(View.VISIBLE);
            mListResult.setVisibility(View.GONE);
        } else if (null != mSearchInteracter) {
            search(str);
        }
    }

    @Override
    public void setSearchResult(List<MyPoiModel> list) {
        if (mEditSearch.getText().toString().isEmpty()) {
            list.clear();
        }
        if (list.size() < 20) {
            mListResult.setCanLoad(false);
        } else {
            mListResult.setCanLoad(true);
        }
        if (null == mResultAdapter) {
            mResultAdapter = new SearchPoiResultAdapter(this, list, mShowOption, true, mNearby);
            mResultAdapter.setOnSelectPoiListener(this);
            mListResult.setAdapter(mResultAdapter);
        } else {
            if (0 == mPage) {
                mResultAdapter.setList(list);
                mResultAdapter.notifyDataSetChanged();
                mListResult.setSelection(0);
            } else if (0 < mPage) {
                mResultAdapter.addList(list);
                mResultAdapter.notifyDataSetChanged();
            }
        }
        if (mResultAdapter.getCount() > 0) {
            mListResult.setVisibility(View.VISIBLE);
            mGridHot.setVisibility(View.GONE);
            mListHistory.setVisibility(View.GONE);
        } else {
            mListResult.setVisibility(View.GONE);
            mGridHot.setVisibility(View.VISIBLE);
            mListHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setSuggestCityList(List<SuggestionCity> cities) {
        mRecycleCity.setVisibility(View.VISIBLE);
        mGridHot.setVisibility(View.GONE);
        if (null == mSearchSuggtionCityAdapter) {
            mSearchSuggtionCityAdapter = new SearchSuggtionCityAdapter(this, cities);
            mSearchSuggtionCityAdapter.setOnClickCityListener(this);
            mRecycleCity.setAdapter(mSearchSuggtionCityAdapter);
        } else {
            mSearchSuggtionCityAdapter.setList(cities);
            mSearchSuggtionCityAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onNoData(String type) {
        super.onNoData(type);
        if ("search".equals(type)) {
            if (0 == mPage) {
                Snackbar.make(mEditSearch, "未搜索到相关信息，换个关键词试试", Snackbar.LENGTH_SHORT).show();
                mListResult.setCanLoad(false);
                mListResult.setVisibility(View.GONE);
                mListHistory.setVisibility(View.VISIBLE);
                mGridHot.setVisibility(View.VISIBLE);
            } else {
                Snackbar.make(mEditSearch, "没有更多内容了", Snackbar.LENGTH_SHORT).show();
                mListResult.setCanLoad(false);
            }
        } else if ("city".equals(type)) {
            mSearchSuggtionCityAdapter = null;
            mRecycleCity.setAdapter(null);
            mRecycleCity.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShowData(String type) {
        super.onShowData(type);
        if ("search".equals(type)) {
            mListResult.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setPoiStart(MyPoiModel poi) {
    }

    @Override
    public void setPoiEnd(MyPoiModel poi) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("start", BApp.MY_LOCATION);
        bundle.putParcelable("end", poi);
        openActivity(RouteActivity.class, bundle, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_city:
                startActivityForResult(new Intent(this, ChangeCityActivity.class), REQUEST_CITY_CODE);
                break;
            case R.id.check_nearby:
                (new ConfigInteracter(this)).setSearchNearby(mCheckNearby.isChecked());
                if (!mEditSearch.getText().toString().trim().isEmpty()) {
                    mPage = 0;
                    search(mEditSearch.getText().toString().trim());
                }
                break;
        }
    }

    @Override
    public void onLoadMore() {
        mPage++;
        search();
    }

    @Override
    public void onSearchHistoryDelete(String keyword) {
        CacheInteracter cacheInteracter = new CacheInteracter(this);
        cacheInteracter.deleteSearchHistoryKeyword(keyword);

        getHistoryKeyword();
    }

    @Override
    public void onClickCity(SuggestionCity city) {
        mPage = 0;
        mCity = city.getCityName();
        mTextCity.setText(mCity);
        search(city.getCityName(), mEditSearch.getText().toString().trim());
    }
}
