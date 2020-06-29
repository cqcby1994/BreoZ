package com.breo.breoz;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breo.baseble.BreoBle;
import com.breo.baseble.callback.scan.IScanCallback;
import com.breo.baseble.callback.scan.ScanCallback;
import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.baseble.model.BluetoothLeDeviceGroup;
import com.breo.breoz.adapter.DeviceRecycleAdapter;
import com.breo.breoz.base.BaseActivity;
import com.breo.breoz.event.ble.BreoBleConEvent;
import com.breo.breoz.service.BreoBleService;
import com.breo.breoz.ui.ISeeKActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView mDeviceRecyclerView;
    private Button mButton;
    private List<BluetoothLeDevice> data = new ArrayList<>();
    private DeviceRecycleAdapter adapter;
    private ServiceConnection serviceBreoConnection;
    private BreoBleService mBreoBleService;
    private ProgressDialog progressDialog;

    @Override
    protected int contentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                askPermissions(findUnAskedPermissions(
                        Arrays.asList(getPackageManager().getPackageInfo(
                                getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions.clone())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mDeviceRecyclerView = findViewById(R.id.rv_main_device);
        mButton = findViewById(R.id.bt_main_start);
        adapter = new DeviceRecycleAdapter(data, this);
        mDeviceRecyclerView.setAdapter(adapter);
        mDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initView() {
        adapter.setOnDeviceRecycleItemListener(new DeviceRecycleAdapter.OnDeviceRecycleItemListener() {
            @Override
            public void onItemClick(int position) {
                startDevice(data.get(position));
                showWaiting();
            }
        });
        BreoBle.getInstance().startScan(new ScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
                Log.d("cqc", "onDeviceFound");
                if (bluetoothLeDevice.getName() != null) {
                    data.add(bluetoothLeDevice);
                    adapter.setData(data);
                }
            }

            @Override
            public void onScanFinish(BluetoothLeDeviceGroup bluetoothLeDeviceGroup) {
                Log.d("cqc", "onScanFinish");

            }

            @Override
            public void onScanTimeout() {
                Log.d("cqc", "onScanTimeout");

            }
        }));
    }

    private void startDevice(BluetoothLeDevice bluetoothLeDevice) {
        startBreoBleService(bluetoothLeDevice.getAddress());
    }

    private void startBreoBleService(final String address) {
        if (mBreoBleService == null) {
            startService(new Intent(this, BreoBleService.class));
            serviceBreoConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mBreoBleService = ((BreoBleService.LocalBinder) service).getService();
                    mBreoBleService.connect(address);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mBreoBleService = null;
                    serviceBreoConnection = null;
                }
            };
            bindService(new Intent(this, BreoBleService.class), serviceBreoConnection, Service.BIND_AUTO_CREATE);
        } else {
            mBreoBleService.connect(address);
        }

    }

    @Override
    protected void initEvent() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ISeeKActivity.class));
            }
        });
    }

    @Subscribe
    public void getEvent(BreoBleConEvent bleConEvent) {
        Log.d("Cqc", "getEvent=" + bleConEvent);
        if (bleConEvent != null) {
//            private static final int STATE_DISCONNECTED = 0;
//            private static final int STATE_CONNECTING = 1;
//            private static final int STATE_CONNECTED = 2;
//            private static final int STATE_FAILED = 3;
            switch (bleConEvent.getConnectStatus()) {
                case BreoBleService.STATE_DISCONNECTED:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    break;
                case BreoBleService.STATE_CONNECTING:
                    break;
                case BreoBleService.STATE_CONNECTED:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    startActivity(new Intent(MainActivity.this, ISeeKActivity.class));

                    break;
                case BreoBleService.STATE_FAILED:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();

                    break;
            }
        }

    }

    private void showWaiting() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIcon(R.mipmap.ic_launcher);
            progressDialog.setTitle("Breo");
            progressDialog.setMessage("连接中...");
            progressDialog.setIndeterminate(true);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
            progressDialog.setCancelable(false);//点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
        }
        progressDialog.show();

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
