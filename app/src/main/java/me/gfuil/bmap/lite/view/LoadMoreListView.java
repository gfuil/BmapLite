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

package me.gfuil.bmap.lite.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import me.gfuil.bmap.lite.R;


/**
 * 自动加载更多listview
 *
 * @author gfuil
 */
public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {
    private boolean isLastItem = false; //是否滑动到最后一个item
    private boolean isLoading = false; //是否加载标示
    private boolean canLoad = false;
    private View footerView;
    private OnLoadMoreListener loadMoreListener;

    public LoadMoreListView(Context context) {
        super(context);
        addFootView();
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addFootView();
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addFootView();
    }

    private void addFootView() {
        footerView = LayoutInflater.from(getContext()).inflate(R.layout.view_load_more_footer, null);
        this.addFooterView(footerView, null, false);
        this.setOnScrollListener(this);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && isLastItem && loadMoreListener != null && !isLoading && canLoad) {
            isLoading = true;
            footerView.setVisibility(VISIBLE);
            loadMoreListener.onLoadMore();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        isLastItem = ((firstVisibleItem + visibleItemCount) == totalItemCount);
    }

    /**
     * 数据加载完成
     */
    public void loadComplete() {
        footerView.setVisibility(View.GONE);
        isLoading = false;
        this.invalidate();
    }

    public void setCanLoad(boolean canLoad) {
        this.canLoad = canLoad;
        if (canLoad && getFooterViewsCount() == 0) {
            addFooterView(footerView);
        } else if (!canLoad && getFooterViewsCount() == 1) {
            removeFooterView(footerView);
        }
        isLoading = false;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public interface OnLoadMoreListener {
        public void onLoadMore();
    }
}
