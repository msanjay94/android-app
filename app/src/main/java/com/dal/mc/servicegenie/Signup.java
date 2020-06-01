package com.dal.mc.servicegenie;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Signup extends AppCompatActivity {

    private TextView signinTxtView;
    private EditText firstName, lastName, phone, emailId, password, confirmPassword;
    private EditText address1, address2, city, country, postalCode;
    private CircleImageView profilePic;
    private ImageView deleteProfilePic;
    private Button signUpBtn;
    private Spinner provinces;
    private static final Pattern UPPERCASE_REGEX = Pattern.compile("[A-Z]+");
    private static final Pattern LOWERCASE_REGEX = Pattern.compile("[a-z]+");
    private static final Pattern NUMBER_REGEX = Pattern.compile("[0-9]+");
    private static final Pattern SPECIAL_CHAR_REGEX = Pattern.compile("[!@#$%*&^-_=+]+");
    private static final Pattern CANADA_POSTAL_CODE_REGEX = Pattern.compile("^[ABCEGHJKLMNPRSTVXY][0-9][ABCEGHJKLMNPRSTVWXYZ] ?[0-9][ABCEGHJKLMNPRSTVWXYZ][0-9]$");
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private Bitmap defaultProfilePic;
    private Bitmap profilePicBtmp;
    private boolean isDefaultProfilePic;

    static final String PHONE_NUMBER_KEY = "phoneNumber";

    private enum REQUEST_CODES {CAMERA_ACCESS_PERMISSION, WRITE_EXTERNAL_STORAGE_PERMISSION}

    private Uri profilePicUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadLayoutElements();

        defaultProfilePic = BitmapFactory.decodeResource(getResources(), R.drawable.userphoto);
        isDefaultProfilePic = true;
    }

    private void loadLayoutElements() {
        profilePic = findViewById(R.id.signup_profilePic);
        signinTxtView = findViewById(R.id.signup_signInTxt);
        firstName = findViewById(R.id.signup_firstName);
        lastName = findViewById(R.id.signup_lastName);
        phone = findViewById(R.id.signup_phone);
        emailId = findViewById(R.id.signup_emailId);
        password = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.signup_confirmPassword);
        signUpBtn = findViewById(R.id.signup_signUpBtn);
        provinces = findViewById(R.id.signup_province);
        address1 = findViewById(R.id.signup_address1);
        address2 = findViewById(R.id.signup_address2);
        city = findViewById(R.id.signup_city);
        country = findViewById(R.id.signup_country);
        postalCode = findViewById(R.id.signup_postalCode);
        deleteProfilePic = findViewById(R.id.signup_deleteProfilePic);

        country.setEnabled(false);

        setupPhoneEditText();
        setupEmailEditText();
        setupPasswordEditText();
        setupConfirmPasswordEditText();
        setupProvinceSpinner();

        setupDeleteProfilePic();
        setupProfilePicElement();

        setupPostalCodeField();

        setupSigninTextView();
        setupSignupBtn();
    }

    private void setupPostalCodeField() {
        postalCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePostalCode();
                autoFormatPostalCodeField();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void autoFormatPostalCodeField() {
        String value = postalCode.getText().toString();
        String postalCodeTxt = value.replaceAll(" ", "").toUpperCase();
        if (postalCodeTxt.length() == 3) {
            postalCodeTxt = postalCodeTxt.substring(0, 3);
            if (!value.equals(postalCodeTxt)) {
                postalCode.setText(postalCodeTxt);
                postalCode.setSelection(postalCode.getText().length());
            }
        } else if (postalCodeTxt.length() > 3) {
            postalCodeTxt = postalCodeTxt.substring(0, 3) + " " + postalCodeTxt.substring(3);
            if (!value.equals(postalCodeTxt)) {
                postalCode.setText(postalCodeTxt);
                postalCode.setSelection(postalCode.getText().length());
            }
        } else if (!value.equals(postalCodeTxt)) {
            postalCode.setText(postalCodeTxt);
            postalCode.setSelection(postalCode.getText().length());
        }
    }

    private boolean validatePostalCode() {
        String postalCodeTxt = postalCode.getText().toString();
        if (!CANADA_POSTAL_CODE_REGEX.matcher(postalCodeTxt).find()) {
            postalCode.setError("Enter valid postal code");
            return false;
        }
        return true;
    }

    private boolean validateForm() {
        boolean valid = true;
        if (firstName.getText().toString().isEmpty()) {
            firstName.setError("Enter first name");
            valid = false;
        }
        if (lastName.getText().toString().isEmpty()) {
            lastName.setError("Enter last name");
            valid = false;
        }
        valid = validatePhone(phone) && valid;
        if (address1.getText().toString().isEmpty()) {
            address1.setError("Enter street address");
            valid = false;
        }
        if (city.getText().toString().isEmpty()) {
            city.setError("Enter city");
            valid = false;
        }
        if (provinces.getSelectedItemPosition() == 0) {
            TextView errorText = (TextView) provinces.getSelectedView();
            errorText.setTextColor(Color.RED);
            errorText.setText("Select a province");
        }
        valid = validatePostalCode() && valid;
        valid = validateEmail(emailId) && valid;
        valid = validatePassword(password) && valid;
        valid = validateConfirmPassword() && valid;
        return valid;
    }

    private boolean validateConfirmPassword() {
        boolean valid = true;
        if (!confirmPassword.getText().toString().equals(password.getText().toString())) {
            confirmPassword.setError("Passwords don't match");
            valid = false;
        }
        return valid;
    }

    static String validatePassword(String passwordTxt) {
        boolean valid = true;
        if (passwordTxt.isEmpty()) {
            return "Enter password";
        } else {
            StringBuilder error = new StringBuilder("Password must contain:");
            if (!UPPERCASE_REGEX.matcher(passwordTxt).find()) {
                error.append("\n- 1 Uppercase letter");
                valid = false;
            }
            if (!LOWERCASE_REGEX.matcher(passwordTxt).find()) {
                error.append("\n- 1 Lowercase letter");
                valid = false;
            }
            if (!NUMBER_REGEX.matcher(passwordTxt).find()) {
                error.append("\n- 1 Number");
                valid = false;
            }
            if (!SPECIAL_CHAR_REGEX.matcher(passwordTxt).find()) {
                error.append("\n- 1 Special character");
                valid = false;
            }
            if (passwordTxt.length() < 8) {
                error.append("\n- 8 Characters");
                valid = false;
            }
            if (!valid) {
                return error.toString();
            }
        }
        return "";
    }

    private boolean validatePassword(EditText password) {
        String passwordTxt = password.getText().toString();
        String error = validatePassword(passwordTxt);
        if (!error.isEmpty()) {
            password.setError(error);
            return false;
        }
        return true;
    }

    static boolean validateEmail(EditText emailId) {
        boolean valid = true;
        String emailAddress = emailId.getText().toString();
        if (emailAddress.isEmpty()) {
            emailId.setError("Enter email ID");
            valid = false;
        } else if (!emailAddress.contains("@")) {
            emailId.setError("Enter valid email ID");
            valid = false;
        } else if (emailAddress.indexOf(".", emailAddress.indexOf("@")) == -1) {
            emailId.setError("Enter valid email ID");
            valid = false;
        } else {
            String[] split = emailAddress.split("@");
            if (split.length > 1) {
                String domainName = split[1];
                if (!"dal.ca".equalsIgnoreCase(domainName)) {
                    emailId.setError("Please use dal.ca email");
                    valid = false;
                }
            }
        }

        return valid;
    }

    static boolean validatePhone(EditText phone) {
        boolean valid = true;
        String phoneNumber = phone.getText().toString().replaceAll(" ", "");
        if (phoneNumber.isEmpty()) {
            phone.setError("Enter phone number");
            valid = false;
        }
        if (phoneNumber.length() != 10) {
            phone.setError("Enter 10 digit phone number");
            valid = false;
        }
        if (phoneNumber.contains("+") || phoneNumber.contains("-") || phoneNumber.contains("(") || phoneNumber.contains(")")) {
            phone.setError("Enter phone number without +/-/()");
            valid = false;
        }
        return valid;
    }

    private void setupConfirmPasswordEditText() {
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupPasswordEditText() {
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(password);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupEmailEditText() {
        emailId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(emailId);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupPhoneEditText() {
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone(phone);
                PhoneVerification.autoFormatPhoneNumberField(s, phone);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private static String encodeImage(Bitmap image) {
        final int compressionQuality = 100;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, compressionQuality, bos);
        return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
    }

    private void updateUserDetailsInDatabase(FirebaseUser user) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        String profilePicEncoded = null;
        if (!isDefaultProfilePic) {
            profilePicEncoded = encodeImage(profilePicBtmp);
        }
        User client = new User(user.getUid(), phone.getText().toString(), user.getDisplayName(), address1.getText().toString(), address2.getText().toString(), city.getText().toString(), provinces.getSelectedItem().toString(), country.getText().toString(), postalCode.getText().toString(), profilePicEncoded);
        users.child(user.getUid()).setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setupSignupBtn() {
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    if (isDefaultProfilePic) {
                        new AlertDialog.Builder(Signup.this).setMessage("Are you sure you want to skip uploading a profile pic ?").setCancelable(false)
                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                signupUser();
                            }
                        }).create().show();
                    } else {
                        signupUser();
                    }
                }
            }
        });
    }

    private void signupUser() {
        final ProgressDialog dialog = new ProgressDialog(Signup.this);
        dialog.setMessage("Signing up...");
        dialog.setCancelable(false);
        dialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailId.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest
                            .Builder().setDisplayName(firstName.getText() + " " + lastName.getText());
                    if (!isDefaultProfilePic) {
                        builder.setPhotoUri(profilePicUri);
                    }
                    UserProfileChangeRequest profileUpdates = builder.build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                updateUserDetailsInDatabase(user);
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            new AlertDialog.Builder(Signup.this).setMessage("User created. Please check email for verification. Please don't forget to check spam folder.").setCancelable(false)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.dismiss();
                                                            Intent emailVerifyIntent = new Intent(new Intent(Signup.this, EmailVerification.class));
                                                            emailVerifyIntent.putExtra(Login.PASSWORD_KEY, password.getText().toString());
                                                            emailVerifyIntent.putExtra(PHONE_NUMBER_KEY, phone.getText().toString());
                                                            startActivity(emailVerifyIntent);
                                                            finish();
                                                        }
                                                    }).create().show();

                                        } else {
                                            new AlertDialog.Builder(Signup.this).setMessage("User created. Please sign in to continue registration").setCancelable(false)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.dismiss();
                                                        }
                                                    }).create().show();
                                        }
                                    }
                                });
                            } else {
                                Exception e = task.getException();
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error signing up", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    dialog.dismiss();
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        new AlertDialog.Builder(Signup.this).setMessage("User with email already exists").setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    } else {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Error signing up", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupProfilePicElement() {
        profilePic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        profilePic.setBackgroundColor(getResources().getColor(R.color.colorGreyTxtBackground));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        profilePic.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        REQUEST_CODES.WRITE_EXTERNAL_STORAGE_PERMISSION.ordinal()) &&
                        requestPermission(Manifest.permission.CAMERA,
                                REQUEST_CODES.CAMERA_ACCESS_PERMISSION.ordinal())) {
                    CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).setFixAspectRatio(true).setAspectRatio(1, 1).start(Signup.this);
                }
            }
        });
    }

    private boolean requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Signup.this,
                    permission)) {
                Toast.makeText(getApplicationContext(),
                        "Allow access to choose profile picture", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(Signup.this, new String[]{permission},
                        requestCode);
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    isDefaultProfilePic = false;
                    deleteProfilePic.setVisibility(View.VISIBLE);
                    profilePic.setImageURI(result.getUri());
                    profilePicUri = result.getUri();
                    try {
                        profilePicBtmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void setupSigninTextView() {

        signinTxtView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

        signinTxtView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signinTxtView.setBackgroundColor(getResources().getColor(R.color.colorGreyTxtBackground));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        signinTxtView.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
    }

    private void setupProvinceSpinner() {
        provinces.setPadding(0, provinces.getPaddingTop(), provinces.getPaddingRight(), provinces.getPaddingBottom());
        ArrayAdapter<CharSequence> dataAdaptor = ArrayAdapter.createFromResource(this, R.array.activity_signup_provinces, android.R.layout.simple_spinner_item);
        dataAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinces.setAdapter(dataAdaptor);

        provinces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    TextView txtView = (TextView) provinces.getChildAt(position);
                    if (txtView != null) {
                        txtView.setTextColor(getResources().getColor(R.color.hintColor));
                        txtView.setTextSize(18);
                    }
                } else {
                    TextView txtView = (TextView) provinces.getChildAt(position);
                    if (txtView != null) {
                        txtView.setTextColor(getResources().getColor(R.color.black));
                        txtView.setTextSize(18);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupDeleteProfilePic() {
        deleteProfilePic.setVisibility(View.INVISIBLE);
        deleteProfilePic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deleteProfilePic.setBackgroundColor(getResources().getColor(R.color.colorGreyTxtBackground));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        deleteProfilePic.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
        deleteProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDefaultProfilePic = true;
                deleteProfilePic.setVisibility(View.INVISIBLE);
                profilePic.setImageBitmap(defaultProfilePic);
            }
        });
    }

}
