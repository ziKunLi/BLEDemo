package com.example.newbies.bluetoothdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 *
 * @author NewBies
 * @date 2018/3/9
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{

    public ItemClickCallBack itemClickCallBack;
    public List<String> name;
    public List<String> address;
    public Context context;

    public Adapter(List<String> name,List<String> address){
        this.name = name;
        this.address = address;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView deviceName;
        public TextView deviceAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceAddress = itemView.findViewById(R.id.deviceAddress);
        }
    }

    public interface ItemClickCallBack{
        void onItemClick(View view,int position);
        void onItemLongClick(View view, int position);
    }

    public void setItemClickCallBack(ItemClickCallBack itemClickCallBack){
        this.itemClickCallBack = itemClickCallBack;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context == null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.deviceName.setText(name.get(position));
        holder.deviceAddress.setText(address.get(position));
        holder.deviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickCallBack.onItemClick(v,position);
            }
        });
        holder.deviceAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickCallBack.onItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

}
