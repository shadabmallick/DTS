package com.mobile.dts.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mobile.dts.R;
import com.mobile.dts.helper.GlideApp;
import com.mobile.dts.model.FolderData;
import com.mobile.dts.model.KeepSafeData;

import java.util.ArrayList;

public class FolderAdapterImage extends RecyclerView.Adapter<FolderAdapterImage.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<KeepSafeData> dataArrayList;


    public FolderAdapterImage(Context context, ArrayList<KeepSafeData> dataArrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.dataArrayList = dataArrayList;
        this.context=context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final KeepSafeData folderData = dataArrayList.get(position);

      //  holder.tv_folder_name.setText(folderData.getPhotoOriginalPath());

        Bitmap bmp = BitmapFactory.decodeByteArray(folderData.getPhotoByte(), 0, folderData.getPhotoByte().length);

/*
        holder.img_1.setImageBitmap(Bitmap.createScaledBitmap(bmp,400,
                400, false));
*/
        GlideApp.with(context).load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(((FolderAdapterImage.ViewHolder) holder).img_1);

/*
        holder.rel_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mViewClickListener.onImageClicked(0, folderData.getFolderId());
            }
        });
*/

/*
        holder.rel_main.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mViewClickListener.onImageClicked(1, folderData.getFolderId());

                return true;
            }
        });
*/

    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView img_1;

        public ViewHolder(View itemView) {
            super(itemView);
            img_1 =  itemView.findViewById(R.id.img_1);
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