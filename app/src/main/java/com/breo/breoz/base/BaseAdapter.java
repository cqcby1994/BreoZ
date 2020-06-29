package com.breo.breoz.base;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
    protected Activity activity;
    protected List<T> tList;
    protected int layoutResId;

    /**
     * @param activity    应用上下文
     * @param tList       列表参数集合
     * @param layoutResId 列表ITEM布局文件
     */
    public BaseAdapter(Activity activity, List<T> tList, int layoutResId) {
        this.activity = activity;
        this.tList = tList;
        this.layoutResId = layoutResId;
    }

    @Override
    public int getCount() {
        return tList.size();
    }

    @Override
    public Object getItem(int position) {
        return tList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity)
                    .inflate(layoutResId, parent, false);
        }
        T t = tList.get(position);
        viewHandler(position, t, convertView);
        return convertView;
    }

    public abstract void viewHandler(int position, T t, View convertView);
}