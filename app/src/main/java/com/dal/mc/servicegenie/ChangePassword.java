package com.dal.mc.servicegenie;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private static final FirebaseAuth FIREBASE_AUTH = FirebaseAuth.getInstance();

    private TextInputLayout oldPwdLayout, newPwdLayout, confirmPwdLayout;
    private TextInputEditText oldPwd, newPwd, confirmPwd;
    private MaterialButton updatePwdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadElements();

    }

    private void loadElements() {
        oldPwdLayout = findViewById(R.id.changePassword_oldPwdLayout);
        newPwdLayout = findViewById(R.id.changePassword_newPwdLayout);
        confirmPwdLayout = findViewById(R.id.changePassword_confirmPwdLayout);

        oldPwd = findViewById(R.id.changePassword_oldPwd);
        newPwd = findViewById(R.id.changePassword_newPwd);
        confirmPwd = findViewById(R.id.changePassword_confirmPwd);

        updatePwdBtn = findViewById(R.id.changePassword_changePassword);

        setupOldPwd();
        setupNewPwd();
        setupConfirmPwd();
        setupUpdatePwdBtn();
    }

    private void setupOldPwd() {
        oldPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(oldPwdLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupUpdatePwdBtn() {
        updatePwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = validateForm();
                if (valid) {
                    final ProgressDialog dialog = new ProgressDialog(ChangePassword.this);
                    dialog.setMessage("Updating password...");
                    dialog.setCancelable(false);
                    dialog.show();
                    final FirebaseUser user = FIREBASE_AUTH.getCurrentUser();
                    if (user != null) {
                        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPwd.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    dialog.dismiss();
                                    Exception e = task.getException();
                                    if (e != null) {
                                        new AlertDialog.Builder(ChangePassword.this).setMessage("Failed to change password. " + e.getMessage()).setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                    } else {
                                        new AlertDialog.Builder(ChangePassword.this).setMessage("Failed to change password. Please try again after some time").setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                    }
                                } else {
                                    user.updatePassword(newPwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialog.dismiss();
                                            if (task.isSuccessful()) {
                                                new AlertDialog.Builder(ChangePassword.this).setMessage("Password updated successfully.").setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.dismiss();
                                                                finish();
                                                            }
                                                        }).create().show();

                                            } else {
                                                Exception e = task.getException();
                                                if (e != null) {
                                                    new AlertDialog.Builder(ChangePassword.this).setMessage("Failed to change password. " + e.getMessage()).setCancelable(false)
                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.dismiss();
                                                                }
                                                            }).create().show();
                                                } else {
                                                    new AlertDialog.Builder(ChangePassword.this).setMessage("Failed to change password. Please try again after some time").setCancelable(false)
                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.dismiss();
                                                                }
                                                            }).create().show();
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        startActivity(new Intent(ChangePassword.this, Login.class));
                        finish();
                    }
                }
            }
        });
    }

    private void setupNewPwd() {
        newPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(newPwdLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupConfirmPwd() {
        confirmPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!confirmPwd.getText().toString().equals(newPwd.getText().toString())) {
                    confirmPwdLayout.getEditText().setError("Passwords don't match");
                } else {
                    confirmPwdLayout.getEditText().setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean validatePassword(TextInputLayout layout) {
        String text = layout.getEditText().getText().toString();
        String error = Signup.validatePassword(text);
        if (!error.isEmpty()) {
            layout.getEditText().setError(error);
            return false;
        }
        layout.getEditText().setError(null);
        return true;
    }

    private boolean validateForm() {
        boolean valid = true;
        valid = validatePassword(oldPwdLayout) && valid;
        valid = validatePassword(newPwdLayout) && valid;
        valid = validatePassword(confirmPwdLayout) && valid;
        return valid;
    }

}
