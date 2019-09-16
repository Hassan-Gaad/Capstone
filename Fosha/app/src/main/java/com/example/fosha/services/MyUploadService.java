package com.example.fosha.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fosha.AddNewPlaceActivity;
import com.example.fosha.R;
import com.example.fosha.models.Post;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to handle uploading files to Firebase Storage.
 */
public class MyUploadService extends MyBaseTaskService {

    private static final String TAG = "MyUploadService";

    /** Intent Actions **/
    public static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";

    public static final String POST_KEY="post_key";
    public static final String USER_ID="user_id";

    /** Intent Extras **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

    // [START declare_ref]
    private StorageReference mStorageRef;
    FirebaseDatabase mFirebaseDatabase;
    // [END declare_ref]

    Post post;

    private String key;
    private String userId;
    int increment=0;

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // [END get_storage_ref]
        post=new Post();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD.equals(intent.getAction())) {
            Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
            key=intent.getStringExtra(POST_KEY);
            userId=intent.getStringExtra(USER_ID);


            // Make sure we have permission to read the data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            uploadFromUri(fileUri);
        }

        return START_REDELIVER_INTENT;
    }

    // [START upload_from_uri]
    private void uploadFromUri(final Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START_EXCLUDE]
        taskStarted();
        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri).
                addOnProgressListener(taskSnapshot -> showProgressNotification(getString(R.string.progress_uploading),
                        taskSnapshot.getBytesTransferred(),
                        taskSnapshot.getTotalByteCount()))
                .continueWithTask(task -> {
                    // Forward any exceptions
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Log.d(TAG, "uploadFromUri: upload success --->"+photoRef.getDownloadUrl());

                    // Request the public download URL
                    return photoRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    // Upload succeeded
                    Log.d(TAG, "uploadFromUri: getDownloadUri success ++++> "+downloadUri);


                    post.download_urls.add(downloadUri.toString());
                    Map<String, Object> childUpdate = new HashMap<>();

                    childUpdate.put("/posts/" + key + "/download_urls", post.download_urls );
                    childUpdate.put("/user-posts/" + userId + "/" + key + "/download_urls", post.download_urls);
                    mFirebaseDatabase.getReference().updateChildren(childUpdate);


                    // [START_EXCLUDE]
                    showUploadFinishedNotification(downloadUri, fileUri);
                    taskCompleted();
                    // [END_EXCLUDE]
                })
                .addOnFailureListener(exception -> {
                    // Upload failed
                    Log.w(TAG, "uploadFromUri:onFailure", exception);

                    // [START_EXCLUDE]
                    showUploadFinishedNotification(null, fileUri);
                    taskCompleted();
                    // [END_EXCLUDE]
                });
    }
    // [END upload_from_uri]

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to AddNewPlaceActivity
        Intent intent = new Intent(this, AddNewPlaceActivity.class)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = downloadUrl != null;
        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
        showFinishedNotification(caption, intent, success);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;
    }

}
