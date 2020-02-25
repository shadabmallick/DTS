package com.mobile.dts.adapter;

import android.content.Context;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mobile.dts.R;
import com.mobile.dts.callbacks.ImageClickListner;
import com.mobile.dts.model.KeepSafeData;

import java.util.ArrayList;

import static com.mobile.dts.activity.DtsGalleryActivity.TAG;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<String> dataArrayList;
    private ImageClickListner imageClickListner;
    private long mLastClickTime = 0;
    private boolean isLongPressed;

    public MyRecyclerViewAdapter(Context context, ArrayList<String> dataArrayList,ImageClickListner imageClickListner,boolean isLongPressed) {
        this.mInflater = LayoutInflater.from(context);
        this.dataArrayList = dataArrayList;
        this.context=context;
        this.imageClickListner=imageClickListner;
        this.isLongPressed=isLongPressed;

    }

    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        MyRecyclerViewAdapter.ViewHolder viewHolder = new MyRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyRecyclerViewAdapter.ViewHolder holder, int position) {


        String path = dataArrayList.get(position);

        //  holder.tv_folder_name.setText(folderData.getPhotoOriginalPath());
        Log.d(TAG, "onBindViewHolder: "+path);

        //  if (folderData.getPhotoByte() != null){

        // Bitmap bmp = BitmapFactory.decodeByteArray(folderData.getPhotoByte(), 0, folderData.getPhotoByte().length);
        Log.d(TAG, "onBindViewHolder: "+path);

        Glide.with(context)
                .load(path)
                .into(holder.img_1);
        if (isLongPressed && holder.checkbox.getVisibility() == View.GONE) {
            holder.checkbox.setVisibility(View.VISIBLE);
        } else if (!isLongPressed) {
            holder.checkbox.setVisibility(View.GONE);
        }
       /* if (folderData.isShowCheckbox()) {
            holder.checkbox.setChecked(true);
        } else {
            holder.checkbox.setChecked(false);
        }*/

        // }

/*
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                isLongPressed = true;


                //  mViewClickListener.onImageClicked(1, folderData.getFolderId());

                return true;
            }
        });
*/

      /*  holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewImage=new Intent(context, ViewImageFromSafe.class);
                viewImage.putExtra("entry_time",folderData.getEntryTime());
                viewImage.putExtra("path",folderData.getPhotoOriginalPath());
                viewImage.putExtra("Id",folderData.getId());
                context.startActivity(viewImage);

            }
        });*/

/*
        holder.img_1.setImageBitmap(Bitmap.createScaledBitmap(bmp,400,
                400, false));
*/

/*
        holder.rel_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mViewClickListener.onImageClicked(0, folderData.getFolderId());
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
        private AppCompatCheckBox checkbox;

        public ViewHolder(View itemView) {
            super(itemView);
            img_1 =  itemView.findViewById(R.id.img_1);
            checkbox =  itemView.findViewById(R.id.checkbox);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    isLongPressed = true;
                    int position = getLayoutPosition();
                    imageClickListner.onImageLongPressListner(position, isLongPressed);
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 50){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    int position = getLayoutPosition();
                    imageClickListner.onImageClickListner(position, isLongPressed);


                }
            });

        }


    }


    private MyRecyclerViewAdapter.ViewClickListener mViewClickListener;
    public interface ViewClickListener {
        void onImageClicked(int clickEvent, int folderId);
    }
    public void setViewClickListener (MyRecyclerViewAdapter.ViewClickListener viewClickListener) {
        mViewClickListener = viewClickListener;
    }

    public void setIsLongPressed(boolean isLongPressed) {
        this.isLongPressed = isLongPressed;
        notifyDataSetChanged();
    }


}