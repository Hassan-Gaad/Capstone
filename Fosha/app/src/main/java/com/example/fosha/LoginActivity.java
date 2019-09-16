package com.example.fosha;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fosha.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final String KEY_IS_SIGNED = "key_is_signed";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    ProgressDialog dialog;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mUsersReference;

    // [START declare_auth]
    private FirebaseAuth mAuth;
// [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mVerificationViews;
    private ViewGroup mNameAndPictureViews;


    private EditText mVerificationField;

    private EditText mFName;
    private EditText mLName;
    ImageView ProfilePicture;
    CardView btnGetName;


    private TextView txtVerifyFrom;
    private TextView txtNumber;

    private TextView tvWrongPhoneNumber;

    private Button mStartButton;
    private LinearLayout mResendButton;

    Button btnNextInVerification;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isSigned;
    boolean signInState;
    CountryCodePicker ccp;
    EditText editTextCarrierNumber;



    String firstName;
    String lastName;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        isSigned = sharedPreferences.getBoolean(getResources().getString(R.string.sign_in_state_key), false);
        if (isSigned){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign views
        mPhoneNumberViews = findViewById(R.id.linearLayoutEnterPhoneNumber);
        mVerificationViews = findViewById(R.id.linearLayoutVerification);
        mNameAndPictureViews = findViewById(R.id.linearLayoutGetNameAndProfilePicture);

        txtVerifyFrom=findViewById(R.id.txtVerifyFromNumber);
        txtNumber=findViewById(R.id.tvNumberToBeShown);
        tvWrongPhoneNumber=findViewById(R.id.tv_lable_wrong_number);

        mVerificationField = findViewById(R.id.et_6DigitCode);

        mFName = findViewById(R.id.ed_F_name);
        mLName = findViewById(R.id.ed_l_name);

        ProfilePicture = findViewById(R.id.profileImage);
        btnGetName = findViewById(R.id.btnGetName);

        mStartButton = findViewById(R.id.btnStartSendVerificationCode);
        mResendButton = findViewById(R.id.btnResendSMS);

        btnNextInVerification=findViewById(R.id.btnNextVerification);

        ccp = findViewById(R.id.ccp);
        editTextCarrierNumber = findViewById(R.id.editText_carrierNumber);

        ccp.registerCarrierNumberEditText(editTextCarrierNumber);

         dialog= new ProgressDialog(this);
        dialog.setMessage("In Progress...");


        // Assign click listeners
        mStartButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);
        btnGetName.setOnClickListener(this);
        btnNextInVerification.setOnClickListener(this);
        tvWrongPhoneNumber.setOnClickListener(this);

        if (!isNetworkConnected(this)) {
            Snackbar.make(mPhoneNumberViews, "No Internet Connection !", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = mFirebaseDatabase.getReference("users");

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    editTextCarrierNumber.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                } else {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }


            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                dialog.dismiss();
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]

            }
        };

        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
        outState.putBoolean(KEY_IS_SIGNED, isSigned);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
        isSigned = savedInstanceState.getBoolean(KEY_IS_SIGNED);
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [STAT resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        dialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            editor.putBoolean(getResources().getString(R.string.sign_in_state_key), true);
                            editor.putString(getResources().getString(R.string.user_id_key), user.getUid());
                            editor.apply();

                            dialog.dismiss();

                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            editor.putBoolean(getResources().getString(R.string.sign_in_state_key), false);
                            editor.apply();
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            dialog.dismiss();
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]

                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private void signOut() {

        mAuth.signOut();
        editor.putBoolean(getResources().getString(R.string.sign_in_state_key), false);
        editor.apply();
        updateUI(STATE_INITIALIZED);
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button

                mPhoneNumberViews.setVisibility(View.VISIBLE);
                mVerificationViews.setVisibility(View.GONE);
                mNameAndPictureViews.setVisibility(View.GONE);

                Snackbar.make(findViewById(android.R.id.content), "Initialized",
                        Snackbar.LENGTH_SHORT).show();

                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                mVerificationViews.setVisibility(View.VISIBLE);
                mPhoneNumberViews.setVisibility(View.GONE);
                mNameAndPictureViews.setVisibility(View.GONE);
                txtNumber.setText(phoneNumber);
                txtVerifyFrom.setText(getString(R.string.verifying_from,phoneNumber));
                Snackbar.make(findViewById(android.R.id.content), R.string.status_code_sent,
                        Snackbar.LENGTH_SHORT).show();

                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options

                mPhoneNumberViews.setVisibility(View.GONE);
                mVerificationViews.setVisibility(View.VISIBLE);
                mNameAndPictureViews.setVisibility(View.GONE);
                txtNumber.setText(phoneNumber);
                txtVerifyFrom.setText(getString(R.string.verifying_from,phoneNumber));

                Snackbar.make(findViewById(android.R.id.content), R.string.status_verification_failed,
                        Snackbar.LENGTH_SHORT).show();

                break;
            case STATE_VERIFY_SUCCESS:
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());

                    } else {
                        mVerificationField.setText(R.string.instant_validation);
                    }
                }

                Snackbar.make(findViewById(android.R.id.content), R.string.status_verification_succeeded,
                        Snackbar.LENGTH_SHORT).show();
                break;
            case STATE_SIGNIN_FAILED:

                Snackbar.make(findViewById(android.R.id.content), R.string.status_sign_in_failed,
                        Snackbar.LENGTH_SHORT).show();

                break;
            case STATE_SIGNIN_SUCCESS:

                Snackbar.make(findViewById(android.R.id.content), "signed In successfully",
                        Snackbar.LENGTH_SHORT).show();

                mPhoneNumberViews.setVisibility(View.GONE);
                mVerificationViews.setVisibility(View.GONE);
                mNameAndPictureViews.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void createUser(FirebaseUser fUser, String fName, String lName, String phone) {
        dialog.show();
        User user = new User(fName, lName, phone);
        mUsersReference.child(fUser.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Done :)", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), e.getMessage(),
                        Snackbar.LENGTH_SHORT).show();

            }
        });

    }


    private boolean validatePhoneNumber() {
        String phoneNumber = editTextCarrierNumber.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            editTextCarrierNumber.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStartSendVerificationCode:
                if (!validatePhoneNumber()) {
                    return;
                }
                dialog.show();

                startPhoneNumberVerification(ccp.getFormattedFullNumber());
                phoneNumber=ccp.getFormattedFullNumber();
                break;

            case R.id.btnResendSMS:
                resendVerificationCode(ccp.getFormattedFullNumber(), mResendToken);
                break;
            case R.id.btnGetName:
                firstName = mFName.getText().toString();
                lastName = mLName.getText().toString();
                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                    mFName.setError("Enter Your full Name");
                    return;
                }
                if (mAuth.getCurrentUser() != null) {

                    createUser(mAuth.getCurrentUser(), firstName, lastName, phoneNumber);

                }

                break;
            case R.id.btnNextVerification:
                dialog.show();
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;

            case R.id.tv_lable_wrong_number:
                mPhoneNumberViews.setVisibility(View.VISIBLE);
                mVerificationViews.setVisibility(View.GONE);
                mNameAndPictureViews.setVisibility(View.GONE);
                break;

        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
