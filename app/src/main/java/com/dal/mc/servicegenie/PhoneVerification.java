package com.dal.mc.servicegenie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneVerification extends AppCompatActivity {

    private static final int SECONDS_IN_MINUTES = 60;
    private static final int MINUTES_IN_HOURS = 60;
    private static final int ONE_SECOND_IN_MILLISECONDS = 1000;
    private static final int INTERVAL_BETWEEN_RESEND = 60;
    private static final String PHONE_NUMBER_PATTERN = "[0-9]{10}";
    private static final int PHONE_VERIFICATION_TIMEOUT = 60;
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private EditText countryCode, phoneNumber, otp;
    private Button resendCode, verifyCode;
    private ImageView editPhone;
    private TextView resendTimer;
    private Handler handler;
    private Runnable runnable;

    private String phoneNumberArg;

    private static String otpSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        phoneNumberArg = getIntent().getStringExtra(Signup.PHONE_NUMBER_KEY);
        if (phoneNumberArg != null) {
            phoneNumberArg = phoneNumberArg.replaceAll(" ", "");
            if (!phoneNumberArg.matches(PHONE_NUMBER_PATTERN)) {
                phoneNumberArg = null;
            }
        }
        loadElements();

    }

    private void loadElements() {
        handler = new Handler();

        countryCode = findViewById(R.id.phoneVerify_countryCode);
        phoneNumber = findViewById(R.id.phoneVerify_phoneNumber);
        verifyCode = findViewById(R.id.phoneVerify_verifyBtn);
        resendCode = findViewById(R.id.phoneVerify_resendBtn);
        editPhone = findViewById(R.id.phoneVerify_editPhone);
        resendTimer = findViewById(R.id.phoneVerify_timer);
        otp = findViewById(R.id.phoneVerify_otp);

        countryCode.setInputType(InputType.TYPE_NULL);
        countryCode.setEnabled(false);

        resendTimer.setVisibility(View.INVISIBLE);
        otp.setVisibility(View.INVISIBLE);
        verifyCode.setVisibility(View.INVISIBLE);
        verifyCode.setEnabled(false);
        editPhone.setVisibility(View.INVISIBLE);

        resendCode.setEnabled(false);

        setupPhoneNumberField();
        setupEditPhone();
        setupSendBtn();
        setupOtpField();
        setupVerifyBtn();
    }

    static Runnable startTimer(int seconds, Runnable runnable, final Handler handler, final TextView resendTimer, final TextView resendTxtView) {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        String timerTxt = getTimerTxt(seconds);
        resendTimer.setText(timerTxt);
        runnable = new Runnable() {
            @Override
            public void run() {
                String timerTxt = resendTimer.getText().toString();
                String[] elements = timerTxt.split(":");
                int hours = 0, min = 0, seconds = 0;
                if (elements.length == 3) {
                    hours = Integer.parseInt(elements[0]);
                }
                seconds = Integer.parseInt(elements[elements.length - 1]);
                min = Integer.parseInt(elements[elements.length - 2]);
                if (seconds > 0) {
                    seconds--;
                    timerTxt = getTimerTxt(hours, min, seconds);
                    resendTimer.setText(timerTxt);
                    handler.postDelayed(this, ONE_SECOND_IN_MILLISECONDS);
                } else {
                    if (min > 0) {
                        seconds = SECONDS_IN_MINUTES - 1;
                        min--;
                        timerTxt = getTimerTxt(hours, min, seconds);
                        resendTimer.setText(timerTxt);
                        handler.postDelayed(this, ONE_SECOND_IN_MILLISECONDS);
                    } else {
                        if (hours > 0) {
                            seconds = SECONDS_IN_MINUTES - 1;
                            min = MINUTES_IN_HOURS - 1;
                            hours--;
                            timerTxt = getTimerTxt(hours, min, seconds);
                            resendTimer.setText(timerTxt);
                            handler.postDelayed(this, ONE_SECOND_IN_MILLISECONDS);
                        } else {
                            handler.removeCallbacks(this);
                            resendTimer.setVisibility(View.INVISIBLE);
                            resendTxtView.setEnabled(true);
                        }
                    }
                }
            }
        };
        handler.postDelayed(runnable, ONE_SECOND_IN_MILLISECONDS);
        return runnable;
    }

    static String getTimerTxt(int seconds) {
        int min = 0, hours = 0;
        if (seconds > SECONDS_IN_MINUTES) {
            min = seconds / SECONDS_IN_MINUTES;
            seconds = seconds % SECONDS_IN_MINUTES;
        }
        if (min > MINUTES_IN_HOURS) {
            hours = min / MINUTES_IN_HOURS;
            min = min % MINUTES_IN_HOURS;
        }
        return getTimerTxt(hours, min, seconds);
    }

    static String getTimerTxt(int hours, int min, int seconds) {
        StringBuilder timerTxt = new StringBuilder();
        if (hours > 0) {
            timerTxt.append(hours < 10 ? "0" + hours : hours).append(":");
        }
        timerTxt.append(min < 10 ? "0" + min : min).append(":");
        timerTxt.append(seconds < 10 ? "0" + seconds : seconds);
        return timerTxt.toString();
    }

    private void setupVerifyBtn() {
        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(PhoneVerification.this);
                dialog.setMessage("Verifying OTP...");
                dialog.setCancelable(false);
                dialog.show();
                String userOtp = otp.getText().toString();
                final PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpSent, userOtp);
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                user.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            user.updatePhoneNumber(credential);
                            startActivity(new Intent(PhoneVerification.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "OTP Verification failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void setupOtpField() {
        otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) {
                    verifyCode.setEnabled(true);
                } else {
                    verifyCode.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void sendSMSForVerification() {
        String phoneNumberTxt = "+1"+phoneNumber.getText().toString().replaceAll(" ", "");
        final ProgressDialog dialog = new ProgressDialog(PhoneVerification.this);
        dialog.setMessage("Sending SMS for verification...");
        dialog.setCancelable(false);
        dialog.show();
        final PhoneAuthProvider auth = PhoneAuthProvider.getInstance();
        auth.verifyPhoneNumber(phoneNumberTxt, PHONE_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, PhoneVerification.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                otpSent = s;
                dialog.dismiss();
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                dialog.dismiss();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                user.updatePhoneNumber(phoneAuthCredential);
                startActivity(new Intent(PhoneVerification.this, MainActivity.class));
                finish();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                e.printStackTrace();
                otp.setError("Invalid OTP. Please try again!");
                dialog.dismiss();
            }


        });
    }

    private void setupSendBtn() {
        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendCode.setEnabled(false);
                resendCode.setText(R.string.activity_phone_verify_resend_code);
                resendTimer.setVisibility(View.VISIBLE);
                runnable = startTimer(INTERVAL_BETWEEN_RESEND, runnable, handler, resendTimer, resendCode);

                sendSMSForVerification();


                otp.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                editPhone.setVisibility(View.VISIBLE);
                phoneNumber.setEnabled(false);

            }
        });
    }

    private void setupPhoneNumberField() {
        if (Build.VERSION.SDK_INT > 25) {
            phoneNumber.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean valid = Signup.validatePhone(phoneNumber);
                if (valid) {
                    resendCode.setEnabled(true);
                } else {
                    resendCode.setEnabled(false);
                }
                autoFormatPhoneNumberField(s, phoneNumber);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (phoneNumberArg != null) {
            phoneNumber.setText(phoneNumberArg);
        }
    }

    static void autoFormatPhoneNumberField(CharSequence s, EditText phoneNumber) {
        String value = s.toString();
        String phone = value.replaceAll(" ", "");
        if (phone.length() > 6) {
            phone = phone.substring(0, 3) + " " + phone.substring(3, 6) + " " + phone.substring(6);
            if (!value.equals(phone)) {
                phoneNumber.setText(phone);
                phoneNumber.setSelection(phoneNumber.getText().length());
            }
        } else if (phone.length() == 6) {
            phone = phone.substring(0, 3) + " " + phone.substring(3, 6);
            if (!value.equals(phone)) {
                phoneNumber.setText(phone);
                phoneNumber.setSelection(phoneNumber.getText().length());
            }
        } else if (phone.length() > 3) {
            phone = phone.substring(0, 3) + " " + phone.substring(3);
            if (!value.equals(phone)) {
                phoneNumber.setText(phone);
                phoneNumber.setSelection(phoneNumber.getText().length());
            }
        } else if (phone.length() == 3) {
            phone = phone.substring(0, 3);
            if (!value.equals(phone)) {
                phoneNumber.setText(phone);
                phoneNumber.setSelection(phoneNumber.getText().length());
            }
        }
    }

    private void setupEditPhone() {
        editPhone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resendCode.setText(R.string.activity_phone_verify_send_code);
                resendCode.setEnabled(true);
                resendTimer.setVisibility(View.INVISIBLE);
                handler.removeCallbacks(runnable);

                otp.setVisibility(View.INVISIBLE);
                otp.setText("");
                verifyCode.setVisibility(View.INVISIBLE);
                editPhone.setVisibility(View.INVISIBLE);
                phoneNumber.setEnabled(true);
                phoneNumber.requestFocus();
            }

        });

        editPhone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        editPhone.setBackgroundColor(getResources().getColor(R.color.colorGreyTxtBackground));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        editPhone.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
    }

}
