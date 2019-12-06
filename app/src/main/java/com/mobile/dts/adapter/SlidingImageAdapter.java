package com.mobile.dts.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mobile.dts.R;
import com.mobile.dts.callbacks.ZoomImageClickListener;
import com.mobile.dts.helper.GlideApp;
import com.mobile.dts.helper.photoview.OnViewTapListener;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.utills.Utils;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;

/*use to show Image viewer image*/
public class SlidingImageAdapter extends PagerAdapter {
    private ArrayList<ImageBean> imageArrayList;
    private Context context;
    private LayoutInflater layoutInflater;
    private ZoomImageClickListener zoomImageClickListener;
    private boolean isNavigationExists;

    public SlidingImageAdapter(Context context, ArrayList<ImageBean> imageArrayList) {
        this.context = context;
        this.imageArrayList = imageArrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        zoomImageClickListener = (ZoomImageClickListener) context;
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        if (hasBackKey && hasHomeKey) {
            isNavigationExists = false;
        } else {
            isNavigationExists = true;
        }
    }

    @Override
    public int getCount() {
        return imageArrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = layoutInflater.inflate(R.layout.layout_adapter_sliding_image, container, false);
        final com.mobile.dts.helper.photoview.PhotoView imageView = itemView.findViewById(R.id.imageView);
        CropImageView cropImageView=itemView.findViewById(R.id.cropImageView);
        imageView.setOnViewTapListener(new ImageViewTapListener());
        ImageView iv_play = itemView.findViewById(R.id.iv_play);
        if (isNavigationExists) {
            ViewTreeObserver vto = imageView.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    Drawable drawable = imageView.getDrawable();
                    if (drawable == null) {
                    } else {
                        /*use to fit image on screen or to remove blank space*/
                        final float viewWidth = getImageViewWidth(imageView);
                        final int drawableWidth = drawable.getIntrinsicWidth();
                        if (viewWidth > drawableWidth && (drawableWidth / viewWidth) > 0.93) {
                            final float widthScale = viewWidth / drawableWidth;
                            imageView.setScaleX(widthScale);
                        }
                    }
                    return true;
                }
            });
        }
        File mediaFile = new File(imageArrayList.get(position).getImagePath());
        if (mediaFile != null && mediaFile.exists()) {
            GlideApp.with(context).
                    load(imageArrayList.get(position).getImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView);

        /*    GlideApp.with(context).
                    load(imageArrayList.get(position).getImagePath())
                    .into(imageView);*/

            itemView.findViewById(R.id.deletedmediatxt).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.deletedmediatxt).setVisibility(View.VISIBLE);
        }
        if (!Utils.isImageFile(imageArrayList.get(position).getImagePath())) {
            iv_play.setVisibility(View.VISIBLE);

        } else {
            iv_play.setVisibility(View.GONE);

        }
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.isImageFile(imageArrayList.get(position).getImagePath())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageArrayList.get(position).getImagePath()));
                    intent.setDataAndType(Uri.parse(imageArrayList.get(position).getImagePath()), "video/*");
                    context.startActivity(intent);
                }
            }
        });
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    private int getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private class ImageViewTapListener implements OnViewTapListener {
        @Override
        public void onViewTap(View view, float x, float y) {
            zoomImageClickListener.zoomImageClick();
        }
    }
}
