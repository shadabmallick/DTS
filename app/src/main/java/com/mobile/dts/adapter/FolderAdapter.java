package com.mobile.dts.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mobile.dts.R;
import com.mobile.dts.model.FolderData;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<FolderData> dataArrayList;

    public FolderAdapter(Context context, ArrayList<FolderData> dataArrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.dataArrayList = dataArrayList;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.folder_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FolderData folderData = dataArrayList.get(position);

        holder.tv_folder_name.setText(folderData.getFolderName());


    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        public TextView tv_folder_name;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_folder_name =  itemView.findViewById(R.id.tv_folder_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClick(view, getAdapterPosition());
        }
    }

    public void onItemClick(View view, int position) {

    }
}