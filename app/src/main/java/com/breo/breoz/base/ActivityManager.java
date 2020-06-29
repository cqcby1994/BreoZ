package com.breo.breoz.base;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hch on 2016/3/9.
 * Activity的管理类
 */
public class ActivityManager {
    private static ActivityManager activityManager = null;
    private static Map<String, BaseActivity> mActivityMap = new HashMap<>();//Activity的Map
    private static List<String> mWaitForFinishTags = new LinkedList<>(); //等待被关闭的Activity的Tag
//    private boolean isStartWaitFor = false;

    private ActivityManager() {
    }

    public static ActivityManager getInstance() {
        if(activityManager == null) {
            activityManager = new ActivityManager();
        }

        return activityManager;
    }

    /**
     * 添加Activity入栈
     * @param baseActivity Activity
     */
    public void addActivity(BaseActivity baseActivity,boolean isStartWaitFor) {
        if(baseActivity != null) {
            String tag = baseActivity.getActivityTag();
            mActivityMap.put(tag, baseActivity);
            if(isStartWaitFor) {
                this.waitForFinish(tag);
            }
        }

    }

    /**
     * 获得对应Tag的Activity的引用
     * @param activityTag Tag
     * @return 对应Activity的引用
     */
    private BaseActivity getActivity(String activityTag) {
        return mActivityMap.get(activityTag);
    }

    /**
     * 关闭掉对应Tag的Activity
     * @param activityTag Tag
     */
    public void finish(String activityTag) {
        if (!TextUtils.isEmpty(activityTag) && mActivityMap.containsKey(activityTag)) {
            BaseActivity baseActivity = mActivityMap.remove(activityTag);
            baseActivity.finish();
        }

    }

    /**
     * 结束掉所有的Activity
     */
    public void finishAll() {
        ArrayList<String> keyList = new ArrayList<>();
        keyList.addAll(mActivityMap.keySet());

        for (String key : keyList) {
            this.finish(key);
        }

    }

    /**
     * 结束掉给定Tag的对应的Activity
     * @param finishTags Tag
     */
    private void finish(List<String> finishTags) {
        if(finishTags != null) {
            for (String activityTag : finishTags) {
                this.finish(activityTag);
            }
        }

    }

    /**
     * 清空Activity管理栈
     */
    public void clear() {
        mWaitForFinishTags.clear();
        mActivityMap.clear();
    }

    /**
     *
     */
    public void startWaitFor() {
        mWaitForFinishTags.clear();
    }

    private void waitForFinish(String activityTag) {
        mWaitForFinishTags.add(activityTag);
    }

    public void endWaitFor() {
        this.finish(mWaitForFinishTags);
        mWaitForFinishTags.clear();
    }

    /**
     * 从Activity管理栈中移除掉对应Tag的Activity
     * @param activityTag Tag
     */
    public void remove(String activityTag) {
        mActivityMap.remove(activityTag);
    }
}