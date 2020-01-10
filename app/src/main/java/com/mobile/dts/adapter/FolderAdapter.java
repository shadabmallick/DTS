package com.mobile.dts.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

        final FolderData folderData = dataArrayList.get(position);

        holder.tv_folder_name.setText(folderData.getFolderName());


        holder.rel_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mViewClickListener.onImageClicked(0, folderData.getFolderId());
            }
        });

        holder.rel_main.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mViewClickListener.onImageClicked(1, folderData.getFolderId());

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tv_folder_name;
        public RelativeLayout rel_main;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_folder_name =  itemView.findViewById(R.id.tv_folder_name);
            rel_main =  itemView.findViewById(R.id.rel_main);
        }

    }


    private ViewClickListener mViewClickListener;
    public interface ViewClickListener {
        void onImageClicked(int clickEvent, int folderId);
    }
    public void setViewClickListener (ViewClickListener viewClickListener) {
        mViewClickListener = viewClickListener;
    }



}