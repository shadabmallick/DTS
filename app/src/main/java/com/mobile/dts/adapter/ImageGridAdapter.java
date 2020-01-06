package com.mobile.dts.adapter;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mobile.dts.R;
import com.mobile.dts.callbacks.ImageClickListner;
import com.mobile.dts.helper.GlideApp;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.utills.Utils;

import java.util.ArrayList;

import static com.mobile.dts.activity.TemproryFile.TAG;
import static com.mobile.dts.utills.Constants.CHILD;
import static com.mobile.dts.utills.Constants.HEADER;
import static com.mobile.dts.utills.Constants.defaultDeleteTimeInterval;

/*Use to show Dts and restore gallery*/
public class ImageGridAdapter extends RecyclerView.Adapter {

    final String deleteTimeInterval;
    private Context context;
    private ArrayList<ImageBean> imagePathList;
    private ImageClickListner imageClickListner;
    private LayoutInflater layoutInflater;
    private int imageDimen = 100;
    private boolean isLongPressed;
    private long mLastClickTime = 0;

    public ImageGridAdapter(Context context, ImageClickListner imageClickListner, ArrayList<ImageBean> imagePathList, int imageDimen, boolean isLongPressed) {
        this.context = context;
        this.imagePathList = imagePathList;
        layoutInflater = LayoutInflater.from(context);
        this.imageDimen = imageDimen;
        this.imageClickListner = imageClickListner;
        this.isLongPressed = isLongPressed;
        deleteTimeInterval = defaultDeleteTimeInterval;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatingView;
        if (viewType == HEADER) {
            inflatingView = layoutInflater.inflate(R.layout.adapter_photofolder_header, parent, false);
            return new HeaderViewHolder(inflatingView);
        } else {
            inflatingView = layoutInflater.inflate(R.layout.adapter_photosfolder, parent, false);
            return new ViewHolder(inflatingView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageBean imageBean = imagePathList.get(position);
        if (imageBean.getViewType() == CHILD) {
            ((ViewHolder) holder).iv_image_name.setVisibility(View.GONE);
              ((ViewHolder) holder).imgSize.setVisibility(View.GONE);
            ((ViewHolder) holder).tv_time.setVisibility(View.GONE);
            ((ViewHolder) holder).tv_date.setVisibility(View.GONE);
            GlideApp.with(context).load("file://" + imageBean.getImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(((ViewHolder) holder).iv_folder_thumbnail);
            if (isLongPressed && ((ViewHolder) holder).checkbox.getVisibility() == View.GONE) {
                ((ViewHolder) holder).checkbox.setVisibility(View.VISIBLE);
            } else if (!isLongPressed) {
                ((ViewHolder) holder).checkbox.setVisibility(View.GONE);
            }
            if (imageBean.isChecked()) {
                ((ViewHolder) holder).checkbox.setChecked(true);
            } else {
                ((ViewHolder) holder).checkbox.setChecked(false);
            }
            if (!Utils.isImageFile(imageBean.getImagePath())) {
                ((ViewHolder) holder).iv_play.setVisibility(View.VISIBLE);
            } else {
                ((ViewHolder) holder).iv_play.setVisibility(View.GONE);
            }
            if (imageBean.isSaved24() || imageBean.isDeleted()) {
                ((ViewHolder) holder).iv_image_name.setVisibility(View.VISIBLE);
               ((ViewHolder) holder).imgSize.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).tv_time.setVisibility(View.GONE);
                ((ViewHolder) holder).tv_date.setVisibility(View.GONE);
                ((ViewHolder) holder).iv_image_name.setText(context.getResources().getString(R.string.remaining));
                int seconds = (int) (imageBean.getRemainingTime() / 1000);
                int hours = seconds / (60 * 60);
                int tempMint = (seconds - (hours * 60 * 60));
                int minutes = tempMint / 60;
                seconds = tempMint - (minutes * 60);
                int days = hours / 24;
                if (days < 1) {
                    ((ViewHolder) holder).imgSize.setText(String.format("%02d", hours)
                            + ":" + String.format("%02d", minutes)
                            + ":" + String.format("%02d", seconds));

                    Log.d(TAG, "onBindViewHolder: "+hours + ""+ minutes + ""+seconds);
                } else {
                    int temphours = (hours - (days * 24));
                    String dayStr;
                    String hoursStr;
                    if (days > 1) {
                        dayStr = context.getResources().getString(R.string.days);
                    } else {
                        dayStr = context.getResources().getString(R.string.day);
                    }
                    if (hours > 1) {
                        hoursStr = context.getResources().getString(R.string.hours);
                    } else {
                        hoursStr = context.getResources().getString(R.string.hour);
                    }
                    ((ViewHolder) holder).imgSize.setText(String.format("%02d", days) + " " + dayStr
                            + ":" + String.format("%02d", temphours) + " " + hoursStr);
                }
                // Indicate size for now
                ((ViewHolder) holder).tv_time.setText(imageBean.getImageSize() + " KB");

            } else {
                ((ViewHolder) holder).iv_image_name.setVisibility(View.GONE);
                ((ViewHolder) holder).imgSize.setText(imageBean.getImageSize() + " KB");
                ((ViewHolder) holder).tv_time.setText(imageBean.getCreatedTime());
            }
            if (imageBean.isNew()) {
                ((ViewHolder) holder).imgRecent.setVisibility(View.GONE);
            } else {
                ((ViewHolder) holder).imgRecent.setVisibility(View.GONE);
            }
            ((ViewHolder) holder).tv_date.setText(imageBean.getCreatedDate());

        }
        if (imageBean.getViewType() == HEADER) {
            ((HeaderViewHolder) holder).headertext.setText(imageBean.getCreatedDate());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return imagePathList.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return imagePathList.size();
    }

    public void setIsLongPressed(boolean isLongPressed) {
        this.isLongPressed = isLongPressed;
        notifyDataSetChanged();
    }

    public void setImageDimen(int imageDimen) {
        this.imageDimen = imageDimen;
    }

    public void updateReceiptsList(ArrayList<ImageBean> updatedPhotos) {
        this.imagePathList = updatedPhotos;
        this.notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_folder_thumbnail;
        private AppCompatCheckBox checkbox;
        private TextView imgSize, iv_image_name, tv_date, tv_time;
        private ImageView imgRecent, imgsave24, iv_play;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_folder_thumbnail = itemView.findViewById(R.id.iv_folder_thumbnail);
            checkbox = itemView.findViewById(R.id.checkbox);
            imgSize = itemView.findViewById(R.id.tv_size);
            imgsave24 = itemView.findViewById(R.id.imgsave24);
            imgRecent = itemView.findViewById(R.id.iv_new);
            iv_image_name = itemView.findViewById(R.id.iv_image_name);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_time = itemView.findViewById(R.id.tv_time);
            iv_play = itemView.findViewById(R.id.iv_play);
            ViewGroup.LayoutParams layoutParams = iv_folder_thumbnail.getLayoutParams();
            layoutParams.width = imageDimen;
            layoutParams.height = imageDimen;
            iv_folder_thumbnail.setLayoutParams(layoutParams);
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

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headertext;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headertext = itemView.findViewById(R.id.headertext);
        }
    }
}
