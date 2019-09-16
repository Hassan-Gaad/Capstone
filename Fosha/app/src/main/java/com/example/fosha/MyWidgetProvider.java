package com.example.fosha;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.fosha.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyWidgetProvider extends AppWidgetProvider {

    private static final String LOG = "MyWidgetProvider";
    Post post;
    private DatabaseReference mDatabase;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];


            readData(new MyCallback() {
                @Override
                public void onCallback(Post mPost) {
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                    remoteViews.setTextViewText(R.id.Title, mPost.placeName);
                    remoteViews.setTextViewText(R.id.postNumStars, String.valueOf(mPost.starCount));
                    remoteViews.setTextViewText(R.id.postAuthor, mPost.author);
                    Log.d(LOG, "desc " + mPost.getDescription());
                    remoteViews.setTextViewText(R.id.Body, mPost.getDescription());
                    remoteViews.setTextViewText(R.id.widgetAddress,mPost.address);

                    Intent intent = new Intent(context, MyWidgetProvider.class);
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                            0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.rlWidgetLayout, pendingIntent);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);

                }
            });

        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    public interface MyCallback {
        void onCallback(Post mPost);
    }

    public void readData(MyCallback myCallback) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
            Log.d(LOG, "cant get user id ");
        }
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(LOG, "user id " + uId);
        Query myTopPostsQuery = mDatabase.child("posts")
                .orderByChild("starCount").limitToLast(1);
        myTopPostsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                post = dataSnapshot.getValue(Post.class);
                if (dataSnapshot.exists()) {
                    Log.d(LOG, "onDataChanged data snapshot is exist");
                    if (post != null) {

                        myCallback.onCallback(post);

                        Log.d(LOG, "star count" + post.starCount);
                        Log.d(LOG, "author " + post.author);
                        Log.d(LOG, "desc " + post.description);
                    } else {
                        Log.d(LOG, "post null");

                    }
                } else {
                    Log.d(LOG, "onDataChanged datasnap not exist");
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

}