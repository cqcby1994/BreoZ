package com.breo.breoz.base;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.breo.breoz.BreoZApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by Administrator on 2016/7/13.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected BreoZApplication breoApplication;
    private String mActivityTag;
    private static final String PERMISSION_PERFERENCES = "permission_perferences_file";
    private static final int requestPremissionCode = 208;
    private TextView tvTitle;
    private View ivBack;
    private RelativeLayout rlTitleBarParent;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        this.mActivityTag = UUID.randomUUID().toString();
        super.onCreate(savedInstanceState);
        setContentView(contentView());
        initBaseView();
        initData();
        initView();
        initEvent();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onNewIntent(Intent intent) {
        initBaseView();
        initData();
        initView();
        initEvent();
    }

    @Override
    public final void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(contentView());
    }

    protected abstract int contentView();

    protected abstract void initData();

    protected abstract void initView();

    protected abstract void initEvent();

    private void initBaseView() {
        //设置状态栏与APP界面一致！！！
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

    }


    public void callMe(Class<? extends BaseActivity> targetActivity) {
        this.callMe(targetActivity, null);
    }

    public void callMe(Class<? extends BaseActivity> targetActivity, Bundle bundle) {
        if (targetActivity != null) {
            Intent intent = new Intent(this, targetActivity);
            if (bundle != null) {
                intent.putExtras(bundle);
            }

            this.startActivity(intent);
        }
    }

    public void callMeForBack(Class<? extends BaseActivity> targetActivity, int requestCode) {
        this.callMeForBack(targetActivity, null, requestCode);
    }

    public void callMeForBack(Class<? extends BaseActivity> targetActivity, Bundle bundle, int requestCode) {
        if (targetActivity != null) {
            Intent intent = new Intent(this, targetActivity);
            if (bundle != null) {
                intent.putExtras(bundle);
            }

            this.startActivityForResult(intent, requestCode);
        }
    }

    public BaseFragment replaceFragment(int containerId, BaseFragment fragment,
                                        String flag, boolean addToBackStack) {
        /* 在指定容器中切换fragment */
        if (fragment != null) {
            FragmentManager manager = getSupportFragmentManager();
            BaseFragment fragmentTag = null;
            if (!TextUtils.isEmpty(flag)) {
                fragmentTag = (BaseFragment) manager.findFragmentByTag(flag);
            }
            fragment = fragmentTag == null ? fragment : fragmentTag;
            FragmentTransaction transaction = manager.beginTransaction();
            if (addToBackStack) {
                transaction.addToBackStack(flag);
            }
            transaction.replace(containerId, fragment, flag);
            transaction.commit();
            return fragment;
        } else {
        }
        return null;
    }

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
            FragmentManager manager = getSupportFragmentManager();
            if (!TextUtils.isEmpty(flag)) {
                fragment = (BaseFragment) manager.findFragmentByTag(flag);
            }
            fragment = fragment == null ? fragmentClass.newInstance() : fragment;
            return replaceFragment(containerId, fragment, flag, addToBackStack);
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public BaseFragment addFragment(int containerId, BaseFragment fragment, String flag, boolean addToBackStack) {
        /* 在指定容器中添加一个fragment */
        if (fragment != null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(containerId, fragment, flag);
            transaction.commit();
            return fragment;
        } else {
        }
        return null;
    }

    public BaseFragment addFragment(int containerId,
                                    Class<? extends BaseFragment> fragmentClass, boolean addToBackStack) {
        /* 在指定容器中添加一个fragment */
        try {
            BaseFragment ABCFragment = fragmentClass.newInstance();
            return addFragment(containerId, ABCFragment, fragmentClass.getName(), addToBackStack);
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }


    public String getActivityTag() {
        return mActivityTag;
    }


    public BreoZApplication getBreoApplication() {
        return breoApplication;
    }

    public void setTvTitle(int stringId) {
        if (tvTitle != null) {
            tvTitle.setText(stringId);
        }
    }

    public void setTvTitle(String s) {
        if (tvTitle != null) {
            tvTitle.setText(s);
        }
    }

    public void setBackVisibility(int visibility) {
        if (ivBack != null) {
            ivBack.setVisibility(visibility);
        }
    }

    public View getTitlePrentView() {
        return rlTitleBarParent;
    }

    protected boolean shouldWeAsk(String permission) {
        SharedPreferences permissionPreferences = this.getSharedPreferences(PERMISSION_PERFERENCES, 0);
        return permissionPreferences.getBoolean(permission, true);
    }

    protected void markAsAsked(String permission) {
        SharedPreferences permissionPreferences = this.getSharedPreferences(PERMISSION_PERFERENCES, 0);
        permissionPreferences.edit().putBoolean(permission, false).apply();
    }

    protected List<String> findUnAskedPermissions(List<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!this.hasPermission(perm) && this.shouldWeAsk(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    protected boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 || hasChackPermission(permission);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasChackPermission(String permission) {
        return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean isNeedAskPermission() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    protected void askPermissions(List<String> permissions) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String[] askPermission = new String[permissions.size()];
            this.requestPermissions((String[]) permissions.toArray(askPermission), 208);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 208:
                if (permissions != null) {
                    for (int i = 0; i < permissions.length; ++i) {
                        String permission = permissions[i];
                        if (grantResults[i] == 0) {
                            this.markAsAsked(permission);
                        }
                    }
                }
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //KeyboardUtils.closeInput(this, getWindow().getDecorView());
    }
}
