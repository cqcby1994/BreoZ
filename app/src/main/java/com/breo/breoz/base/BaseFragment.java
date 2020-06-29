package com.breo.breoz.base;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


/**
 * Created by hch on 2016/3/9.
 * Fragment的Base类
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    /**
     * 是否已经初始化Fragment
     */
    protected boolean isInitFragment = false;
    protected View rootView;
    private BaseActivity baseActivity;
    /**
     * 是否可以进行初始化操作
     */
    private boolean isCanInit = false;
    /**
     * 是否是从本类调用
     */
    private boolean fromThis = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;
        isInitFragment = false;
        isCanInit = true;
    }

    @Override
    public final void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callFragment(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * 可能会调用两次，如介意，不要重写
     */
    public synchronized void callFragment(boolean fromThis) {
        if (!fromThis) {
            this.fromThis = fromThis;
        }
        if (isCanInit && !isInitFragment && !this.fromThis) {
            initData();
            initView();
            initEvent();
            isInitFragment = true;
        }
    }

    /**
     * 初始化默认数据
     */
    public abstract void initData();

    /**
     * 初始化View
     */
    public abstract void initView();

    /**
     * 初始化事件（监听）
     */
    public abstract void initEvent();

    public BaseFragment replaceFragment(int containerId, BaseFragment fragment, boolean addToBackStack) {
        /* 在指定容器中切换fragment */
        return replaceFragment(containerId, fragment, fragment.getClass().getName(), addToBackStack);
    }

    public BaseFragment replaceFragment(int containerId,
                                        Class<? extends BaseFragment> fragmentClass, boolean addToBackStack) {
        return replaceFragment(containerId, fragmentClass, fragmentClass.getName(), addToBackStack);
    }

    public BaseFragment replaceFragment(int containerId,
                                        Class<? extends BaseFragment> fragmentClass, String flag, boolean addToBackStack) {
        /* 在指定容器中切换fragment */
        try {
            BaseFragment fragment = null;
            FragmentManager manager = getFragmentManager();
            if (!TextUtils.isEmpty(flag)) {
                fragment = (BaseFragment) manager.findFragmentByTag(flag);
                if (fragment != null) {
                    manager.beginTransaction().remove(fragment).commitAllowingStateLoss();
                }
                fragment = null;
            }
            fragment = fragmentClass.newInstance();
            return replaceFragment(containerId, fragment, flag, addToBackStack);
        } catch (java.lang.InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public BaseFragment replaceFragment(int containerId, BaseFragment fragment,
                                        String flag, boolean addToBackStack) {
        /* 在指定容器中切换fragment */
        if (fragment != null) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            if (addToBackStack) {
                transaction.addToBackStack(flag);
            }
            transaction.replace(containerId, fragment, flag);
            transaction.commitAllowingStateLoss();
            fragment.callFragment(false);
            return fragment;
        } else {
        }
        return null;
    }

    public BaseFragment addFragment(int containerId,
                                    Class<? extends BaseFragment> fragmentClass, String flag, boolean addToBackStack) {
        /* 在指定容器中添加一个fragment */
        try {
            BaseFragment fragment = null;
            FragmentManager manager = getFragmentManager();
            if (!TextUtils.isEmpty(flag)) {
                fragment = (BaseFragment) manager.findFragmentByTag(flag);
                if (fragment != null) {
                    manager.beginTransaction().remove(fragment).commit();
                }
                fragment = null;
            }
            fragment = fragmentClass.newInstance();
            return addFragment(containerId, fragment, flag, addToBackStack);
        } catch (java.lang.InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public BaseFragment addFragment(int containerId, BaseFragment fragment, String flag, boolean addToBackStack) {
        /* 在指定容器中添加一个fragment */
        if (fragment != null) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(containerId, fragment, flag);
            if (addToBackStack) {
                transaction.addToBackStack(flag);
            }
            transaction.commit();
            fragment.callFragment(false);
            return fragment;
        } else {
        }
        return null;
    }

    public BaseFragment addFragment(int containerId,
                                    Class<? extends BaseFragment> fragmentClass, boolean addToBackStack) {
        return addFragment(containerId, fragmentClass, fragmentClass.getName(), addToBackStack);
    }


    /**
     * 获得ZZBActivity
     *
     * @return ZZBActivity
     */
    public BaseActivity getBaseActivity() {
        return baseActivity;
    }


}
