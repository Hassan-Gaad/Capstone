package com.example.fosha;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class fullSrcreenActivity extends AppCompatActivity {
    ImageView myImage;
    ProgressBar progressBar;
    private ArrayList<String> photoItemsUrls;
    int position;
    private Interpolator interpolator;
    ImageView next;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_full_srcreen);

        Toolbar toolbar=findViewById(R.id.fullScreenToolBar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();
        next=findViewById(R.id.imageView_next);
        back=findViewById(R.id.imageView_back);

        photoItemsUrls = getIntent().getStringArrayListExtra("image_urls");
        position=getIntent().getIntExtra("position",0);

        myImage = findViewById(R.id.image_full_screen);

        myImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (actionBar.isShowing()) {
                        actionBar.hide();
                    } else {
                        actionBar.show();
                    }
                    return true;
                } else return false;
            }
        });

        progressBar=findViewById(R.id.full_screenProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this).load(photoItemsUrls.get(position))
                .placeholder(R.color.black_overlay)
                .error(R.drawable.ic_launcher_background)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .fitCenter()
                .into(myImage);



    }

    public void btnNext(View view){


        if (photoItemsUrls.listIterator(position).hasNext()){

                loadImage(photoItemsUrls.listIterator(position).next());
                position++;

        }else {
            Toast.makeText(this, "No more", Toast.LENGTH_SHORT).show();
            position--;

        }

    }

    public void btnBack(View view){
        if (photoItemsUrls.listIterator(position).hasPrevious()){

            loadImage(photoItemsUrls.listIterator(position).previous());
            position--;
        }else
            Toast.makeText(this, "No more", Toast.LENGTH_SHORT).show();
        position++;

    }

    private void loadImage(String url){
        Glide.with(this).load(url)
                .placeholder(R.drawable.loading_place_holder)
                .error(R.drawable.ic_broken_image)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(myImage);
    }
}

