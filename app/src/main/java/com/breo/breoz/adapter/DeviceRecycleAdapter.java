package com.breo.breoz.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.breoz.R;

import java.util.List;

public class DeviceRecycleAdapter extends RecyclerView.Adapter<DeviceRecycleAdapter.DeviceViewHolder> {
    private List<BluetoothLeDevice> data;
    private Context mContext;
    private OnDeviceRecycleItemListener onDeviceRecycleItemListener;

    public DeviceRecycleAdapter(List<BluetoothLeDevice> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.recycle_item_device, null);

        return new DeviceViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.name.setText("name:"+data.get(position).getName());
        holder.address.setText(data.get(position).getAddress());
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeviceRecycleItemListener!=null){
                    onDeviceRecycleItemListener.onItemClick(position);
                }
            }
        });
    }

    public void setData(List<BluetoothLeDevice> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setOnDeviceRecycleItemListener(OnDeviceRecycleItemListener onDeviceRecycleItemListener) {
        this.onDeviceRecycleItemListener = onDeviceRecycleItemListener;
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView address;
        LinearLayout root;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_item_name);
            address = itemView.findViewById(R.id.tv_item_address);
            root = itemView.findViewById(R.id.ll_item_root);

        }
    }
    public interface OnDeviceRecycleItemListener{
        void onItemClick(int position);
    }
}
