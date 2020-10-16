package com.himanshu.infirment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.facebook.FacebookSdk;
import com.himanshu.infirment.model.SignUpMember;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText;
    private EditText phoneNoEditText, otpEditText;
    private CardView getOTPBtn, verifyBtn;
    private CheckBox checkBox;
    private ProgressDialog dialog;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private SignUpMember member;
    public static GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    private String verificationCodeBySystem;
    private LinearLayout otpLayout;
    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        nameEditText = findViewById(R.id.name_editText);
        emailEditText = findViewById(R.id.email_editText);
        passwordEditText = findViewById(R.id.password_editText);
        checkBox = findViewById(R.id.checkBox);

        reference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(SignUpActivity.this);
        member = new SignUpMember();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //FOR FACEBOOK SIGN UP
//        FacebookSdk.sdkInitialize(getApplicationContext());
//
//        callbackManager = CallbackManager.Factory.create();
    }

    //FOR GOOGLE SIGN UP
    public void googleSignUp(View view) {
        dialog.show();
        dialog.setContentView(R.layout.dialog_progress);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //For Facebook
//        callbackManager.onActivityResult(requestCode, resultCode, data);

        //For Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                dialog.dismiss();
                Toast.makeText(this, "Something went wrong!! try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final FirebaseUser user = auth.getCurrentUser();

                            reference.child("SignUpMembers").child(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                member.setName(user.getDisplayName());
                                                member.setEmail(user.getEmail());
                                                member.setVia("Google");
                                                reference.child("SignUpMembers").child(user.getUid()).setValue(member);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
                            finish();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

/*
    //FOR FACEBOOK SIGN UP
    public void facebookSignUp(View view) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void handleFacebookToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            member.setName(user.getDisplayName());
                            if (user.getEmail() != null) {
                                member.setEmail(user.getEmail());
                            }
                            member.setVia("Facebook");
                            reference.child("SignUpMember").child(task.getResult().getUser().getUid()).setValue(member);
                            startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
                            finish();

                        } else {
                            Toast.makeText(SignUpActivity.this, "Authentication Failed" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

*/


    //FOR PHONE SIGN UP
    public void phoneSignUp(View view) {

        final Dialog phoneDialog = new Dialog(this);
        phoneDialog.setContentView(R.layout.dialog_phone_sign_up);
        phoneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        getOTPBtn = phoneDialog.findViewById(R.id.getOtpBtn);
        verifyBtn = phoneDialog.findViewById(R.id.verifyBtn);
        phoneNoEditText = phoneDialog.findViewById(R.id.phoneNoEditText);
        otpEditText = phoneDialog.findViewById(R.id.otpEditText);
        otpLayout = phoneDialog.findViewById(R.id.otpLayout);

        getOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNoEditText.getText().toString().trim().isEmpty()) {
                    phoneNoEditText.setError("Required");
                } else if (phoneNoEditText.getText().toString().trim().length() < 10) {
                    phoneNoEditText.setError("Wrong Number");
                } else {
                    sendVerificationCodeToUser(phoneNoEditText.getText().toString());
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                dialog.setContentView(R.layout.dialog_progress);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setCanceledOnTouchOutside(false);

                String code = otpEditText.getText().toString();
                if (code.isEmpty() || code.length() < 6) {
                    otpEditText.setError("Wrong OTP");
                    dialog.dismiss();
                    otpEditText.requestFocus();
                    return;
                }
                otpLayout.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });
        phoneDialog.show();
    }

    private void sendVerificationCodeToUser(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNo,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,  // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        otpLayout.setVisibility(View.VISIBLE);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otpLayout.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredential(credential);
    }

    private void signInTheUserByCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            member.setPhone(phoneNoEditText.getText().toString().trim());
                            member.setVia("Phone");
                            reference.child("SignUpMembers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(member);
                            startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //FOR EMAIL SIGN UP
    public void emailSignUp(View view) {

        final String name = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Required");
        } else if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Required");
        } else if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Required");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Check you email");
        } else if (password.length() < 6) {
            passwordEditText.setError("Must be greater than 6");
        } else if (!checkBox.isChecked()) {
            Toast.makeText(this, "Accept Terms and Services", Toast.LENGTH_SHORT).show();
        } else {
            dialog.show();
            dialog.setContentView(R.layout.dialog_progress);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCanceledOnTouchOutside(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                member.setName(name);
                                member.setEmail(email);
                                member.setPassword(password);
                                member.setVia("email");
                                reference.child("SignUpMembers").child(task.getResult().getUser().getUid()).setValue(member);
                                startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Failed : " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });
        }
    }

    public void logInAccount(View view) {
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
            finish();
        }
    }
}