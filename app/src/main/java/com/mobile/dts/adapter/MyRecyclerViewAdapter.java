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

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
      Context context;
    private LayoutInflater mInflater;
    ArrayList<String> imageArrayList;
    // Data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, ArrayList<String> imageArrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.imageArrayList = imageArrayList;
        this.context=context;
    }

    // Inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

       String path= imageArrayList.get(position);
        Glide.with(context)
                .load(path)
                .into(holder.image);


    }

    // Total number of cells
    @Override
    public int getItemCount() {
        return imageArrayList.size();
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myTextView;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image =  itemView.findViewById(R.id.img_1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClick(view, getAdapterPosition());
        }
    }

    // Convenience method for getting data at click position

    // Method that executes your code for the action received
    public void onItemClick(View view, int position) {
    }
}