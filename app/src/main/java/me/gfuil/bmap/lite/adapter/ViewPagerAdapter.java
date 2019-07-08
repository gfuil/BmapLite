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


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.List;

/**
 * viewpager适配器
 *
 * @author gfuil
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;
    private List<String>   mTitles;

    public List<Fragment> getFragments() {
        return mFragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.mFragments = fragments;
    }

    public List<String> getTitles() {
        return mTitles;
    }

    public void setTitles(List<String> titles) {
        this.mTitles = titles;
    }

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.mFragments = fragments;
        this.mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
