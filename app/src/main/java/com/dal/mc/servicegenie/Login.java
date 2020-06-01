package com.dal.mc.servicegenie;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private TextView signupTxtView, forgotPassword;
    private Button signinBtn;
    private EditText emailId, password;

    static final String PASSWORD_KEY = "password";

    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseApp.initializeApp(Login.this);

        loadElements();

    }

    private void loadElements() {
        signupTxtView = findViewById(R.id.signin_signUpTxt);
        signinBtn = findViewById(R.id.signin_signinBtn);
        emailId = findViewById(R.id.signin_emailId);
        password = findViewById(R.id.signin_password);
        forgotPassword = findViewById(R.id.signin_forgotPassword);

        setupSigninBtn();
        setupSignupTxt();
        setupForgotPasswordTxt();
    }

    private void setupForgotPasswordTxt() {
        forgotPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Signup.validateEmail(emailId)) {
                    final ProgressDialog dialog = new ProgressDialog(Login.this);
                    dialog.setMessage("Sending password reset email...");
                    dialog.setCancelable(false);
                    dialog.show();
                    firebaseAuth.sendPasswordResetEmail(emailId.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull final Task<Void> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                new AlertDialog.Builder(Login.this).setMessage("Password reset email sent. Please don't forget to check spam folder.").setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        }).create().show();
                            } else {
                                Exception e = task.getException();
                                if (e != null) {
                                    e.printStackTrace();
                                    if (e instanceof FirebaseAuthInvalidUserException) {
                                        new AlertDialog.Builder(Login.this).setMessage("User not found in inventory. Please try valid email address.").setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error sending password resend mail", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error sending password resend mail", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }

        });

        forgotPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        forgotPassword.setBackgroundColor(getResources().getColor(R.color.colorGreyTxtBackground));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        forgotPassword.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
    }

    private void setupSigninBtn() {
        signinBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean valid = true;
                if (emailId.getText().toString().isEmpty()) {
                    emailId.setError("Enter email");
                    valid = false;
                }
                if (password.getText().toString().isEmpty()) {
                    password.setError("Enter password");
                    valid = false;
                }
                if (valid) {
                    final ProgressDialog dialog = new ProgressDialog(Login.this);
                    dialog.setMessage("Logging in...");
                    dialog.setCancelable(false);
                    dialog.show();
                    firebaseAuth.signInWithEmailAndPassword(emailId.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (!user.isEmailVerified()) {
                                    Intent emailVerifyIntent = new Intent(Login.this, EmailVerification.class);
                                    emailVerifyIntent.putExtra(PASSWORD_KEY, password.getText().toString());
                                    startActivity(emailVerifyIntent);
                                    password.setText("");
                                } else if (user.getPhoneNumber() == null) {
                                    Intent phoneIntent = new Intent(Login.this, PhoneVerification.class);
                                    startActivity(phoneIntent);
                                    password.setText("");
                                } else {
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                }
                            } else {
                                Exception e = task.getException();
                                if (e != null) {
                                    e.printStackTrace();
                                    if (e instanceof FirebaseAuthInvalidUserException) {
                                        new AlertDialog.Builder(Login.this).setMessage(e.getMessage()).setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        password.getText().clear();
                                                        password.requestFocus();
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        new AlertDialog.Builder(Login.this).setMessage("Invalid credentials. Please try again.").setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                    } else if (e instanceof FirebaseException) {
                                        new AlertDialog.Builder(Login.this).setMessage(e.getMessage()).setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void setupSignupTxt() {
        signupTxtView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Signup.class));
            }

        });

        signupTxtView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signupTxtView.setBackgroundColor(getResources().getColor(R.color.colorGreyTxtBackground));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        signupTxtView.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
    }

}
