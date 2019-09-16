package com.example.fosha;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.fosha.models.Post;
import com.example.fosha.models.User;
import com.example.fosha.services.MyUploadService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AddNewPlaceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Storage#AddNewPlace";

    private static final String REQUIRED = "Required";

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private static final String KEY_IS_SIGNED = "key_is_signed";

    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    SharedPreferences sharedPreferences;
    boolean isSigned;


    TextInputLayout editTextDescription;
    TextInputLayout etPlaceAddress;
    TextInputLayout etPlaceName;
    TextInputLayout etPlacePrice;
    private FloatingActionButton mSubmitButton;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mUsersReference;

    DatabaseReference mPostReference;

    Post post;
    int price;
    // post Key
    String key;
    int increment = 0;

    String userId ;
    List<Image> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_place);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isSigned = sharedPreferences.getBoolean(getResources().getString(R.string.sign_in_state_key), false);

        Log.d(TAG, "is ueser signed " + isSigned);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = mFirebaseDatabase.getReference("users");
        mPostReference = mFirebaseDatabase.getReference("posts");


        post = new Post();

        editTextDescription = findViewById(R.id.editText_description);
        etPlaceAddress = findViewById(R.id.edit_text_place_address);
        etPlaceName = findViewById(R.id.edit_text_place_name);

        etPlacePrice = findViewById(R.id.edit_text_place_price);
        mSubmitButton = findViewById(R.id.fab_add_new_places);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        editTextDescription.setVerticalScrollBarEnabled(true);
        //editTextPost.setMovementMethod(new ScrollingMovementMethod());


        // Click listeners
        findViewById(R.id.buttonCamera).setOnClickListener(this);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
        out.putBoolean(KEY_IS_SIGNED, isSigned);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            images = ImagePicker.getImages(data);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .putExtra(MyUploadService.POST_KEY,key)
                .putExtra(MyUploadService.USER_ID,userId)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
    }




    private void submitPost() {
        final String description = editTextDescription.getEditText().getText().toString();
        final String title = etPlaceName.getEditText().getText().toString();
        final String address = etPlaceAddress.getEditText().getText().toString();
        final String price = etPlacePrice.getEditText().getText().toString();

        if (TextUtils.isEmpty(title)) {
            etPlaceName.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(price)) {
            etPlacePrice.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError(REQUIRED);
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etPlaceAddress.setError(REQUIRED);
            return;
        }
        if (images == null) {
            Snackbar.make(findViewById(android.R.id.content), "Please choose the place's photos !",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        userId = mAuth.getCurrentUser().getUid();
        key = mPostReference.push().getKey();

        mUsersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user == null) {
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(AddNewPlaceActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();

                } else {
                    // Write new post
                    writeNewPost(userId, user.fName, title, description, Integer.parseInt(price),address);
                }
                // Finish this Activity, back to the stream
                setEditingEnabled(false);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                setEditingEnabled(true);
                // [END_EXCLUDE]
            }
        });

        if (images.size() >= 1) {
            for (int i = 0; i < images.size(); i++) {
                mFileUri = Uri.fromFile(new File(images.get(i).getPath()));
                uploadFromUri(mFileUri);
            }
        }

    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body, int price, String address) {
        showProgressDialog("Loading");
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        post.address = address;
        post.author = username;
        post.description = body;
        post.placeName = title;
        post.price = price;
        post.uid = userId;
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mFirebaseDatabase.getReference().updateChildren(childUpdates);
        hideProgressDialog();

    }

    private void setEditingEnabled(boolean enabled) {
        etPlaceName.setEnabled(enabled);
        editTextDescription.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.show();
        } else {
            mSubmitButton.hide();
        }
    }


    private void pickPhoto() {

        ImagePicker.create(this).start();
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonCamera) {
            pickPhoto();
        }
    }
}

