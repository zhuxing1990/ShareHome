package com.vunke.sharehome.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MypagerAdapter extends PagerAdapter {
	private List<View> list;
	public MypagerAdapter(List<View> list){
		this.list=list;
	}
	@Override
	public int getCount() {
		if (list!=null) {
			return list.size();
		}
		return 0;
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager)container).removeView(list.get(position));
	}
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		((ViewPager)container).addView(list.get(position),0);
		
		return list.get(position);
	}
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==arg1;
	}

}
