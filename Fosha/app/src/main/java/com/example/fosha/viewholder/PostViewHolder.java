package com.example.fosha.viewholder;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fosha.PostDetailActivity;
import com.example.fosha.R;
import com.example.fosha.models.Post;

import java.util.ArrayList;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;

    private PhotoAdapter mPhotoAdapter;
    private RecyclerView mPhotoRecyclerView;
    GridLayoutManager gridLayoutManage;

    RelativeLayout relativeLayout;

    ArrayList<String> urls;


    public PostViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.postTitle);
        authorView = itemView.findViewById(R.id.postAuthor);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.postNumStars);
        bodyView = itemView.findViewById(R.id.postBody);
        bodyView.setMaxLines(4);
        mPhotoRecyclerView = itemView.findViewById(R.id.photos_rv);
        relativeLayout = itemView.findViewById(R.id.photo_item_rl);

        gridLayoutManage = new GridLayoutManager(itemView.getContext(),2);
        mPhotoRecyclerView.setLayoutManager(gridLayoutManage);


    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        titleView.setText(post.placeName);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.description);

        int size = post.download_urls.size();
        //clear the list except the first 4 element
        if (size > 4) {
            post.download_urls.subList(4, size).clear();
        }
        mPhotoAdapter=new PhotoAdapter(itemView.getContext(),post.download_urls,false);
        mPhotoRecyclerView.addItemDecoration(new SpacesItemDecoration(2));
        mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        starView.setOnClickListener(starClickListener);
    }


    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view)%2 == 0) {
                outRect.left = space;
                outRect.right = space;
            } else {
                outRect.right = space;
            }
        }
    }
}
