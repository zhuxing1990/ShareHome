package com.vunke.sharehome.adapter;


import android.view.View;
import android.view.ViewGroup;

public abstract class BaseHolder<T> {

	protected View contentView;

	protected BaseHolder() {
		contentView = initView();
		contentView.setTag(this);
	}
	

	protected void setData(T data, int position, ViewGroup parent) {
		refreshView(data,position,parent);
		
	}

	protected View getContentView() {
		return contentView;
	}
	protected abstract View initView();
	protected abstract void refreshView(T data, int position, ViewGroup parent);
}
