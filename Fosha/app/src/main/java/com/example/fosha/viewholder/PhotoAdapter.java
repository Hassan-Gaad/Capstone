package com.example.fosha.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.fosha.R;
import com.example.fosha.fullSrcreenActivity;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private static final String TAG = "PhotoAdapter";
    private Context mContext;
    private ArrayList<String> PhotoItemsUrls;
    boolean isImageDetailScreen;



    public PhotoAdapter(Context mContext, ArrayList<String> photoItemsUrls ,boolean isImageDetailScreen) {
        this.mContext = mContext;
        PhotoItemsUrls = photoItemsUrls;
        this.isImageDetailScreen=isImageDetailScreen;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.item_photo,parent,false);
        PhotoViewHolder photoViewHolder=new PhotoViewHolder(view);
        return photoViewHolder;


    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {


        holder.progressBar.setVisibility(View.VISIBLE);

        String url=PhotoItemsUrls.get(position);
        if (url.isEmpty()){
            Log.d(TAG,"url is "+url);
            return;
        }

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(mContext, fullSrcreenActivity.class);
                intent.putStringArrayListExtra("image_urls",PhotoItemsUrls);
                intent.putExtra("position",position);
                mContext.startActivity(intent);
            }
        });
        Glide.with(mContext)
                .asBitmap()
                .load(url)
                .error(R.drawable.ic_broken_image)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);

                        return false;
                    }
                })
                .override(200)
                .centerCrop()
                .into(holder.photo);

    }

    @Override
    public int getItemCount() {
        return PhotoItemsUrls.size();
    }



    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        ProgressBar progressBar;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            progressBar=itemView.findViewById(R.id.progressBar_cyclic);
            photo=itemView.findViewById(R.id.photo);


        }
    }

}
