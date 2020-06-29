package com.breo.breoz.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.breo.baseble.BreoBle;
import com.breo.breoz.R;
import com.breo.breoz.base.BaseActivity;

public class ISeeKActivity extends BaseActivity {
    private AlertDialog.Builder builder;

    @Override
    protected int contentView() {
        return R.layout.activity_iseek;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void onBackPressed() {
        showDis();
    }


    private void showDis() {

        builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("当前已连接设备，要断开吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        BreoBle.getInstance().disconnect();
                        ISeeKActivity.super.onBackPressed();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }
}
