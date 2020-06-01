package com.dal.mc.servicegenie;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final FirebaseAuth FIREBASE_AUTH = FirebaseAuth.getInstance();
    private TextView phoneView, emailView, passwordView;
    private EditText nameView,addressView;
    private CircleImageView profilePic;
    private MaterialButton signOut, deleteAccount, changePassword,editButton;
    private FirebaseUser user;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FIREBASE_AUTH.getCurrentUser();
        if (user == null) {
            finish();
        }

        loadUserElements();
        setUserElements();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);


    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;

                        case R.id.nav_bookings:
                            selectedFragment = new BookingsFragment();
                            break;

                        case R.id.nav_help:
                            selectedFragment = new HelpFragment();
                            break;

                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;

                    }
//                    bottomNavigationView.setSelectedItemId(menuItem.getItemId());

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();

                    return true;
                }
            };

    private void loadUserElements() {
        nameView = findViewById(R.id.profileName);
        phoneView = findViewById(R.id.profilePhone);
        addressView = findViewById(R.id.profileAddress);
        emailView = findViewById(R.id.profileEmail);
        profilePic = findViewById(R.id.profile_profilePic);
        signOut = findViewById(R.id.profile_signOut);
        deleteAccount = findViewById(R.id.profile_deleteAccount);
        changePassword = findViewById(R.id.profile_changePassword);
        editButton = findViewById(R.id.profile_editProfile);

        setupSignoutBtn();
        setupDeleteAccountBtn();
        setupChangePasswordBtn();
        setupEditProfile();
    }

    private void setupEditProfile() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText=editButton.getText().toString();
                Log.v("button",buttonText);
                if (buttonText.equals("Edit")){
                    Log.v("Daksh",editButton.getText().toString());
                    nameView.setEnabled(true);
                    addressView.setEnabled(true);
                    editButton.setText("Save");
                    Drawable img = getResources().getDrawable( R.drawable.ic_save_black_24dp);
                    img.setBounds( 0, 0, 174, 174);  // set the image size
                    editButton.setCompoundDrawables( img, null, null, null );
                }
                else{
                    Log.v("else","true");
                    String updatedName=nameView.getText().toString();
                    String updatedAddress=addressView.getText().toString();
                    final ProgressDialog dialog = new ProgressDialog(Profile.this);
                    nameView.setEnabled(false);
                    addressView.setEnabled(false);
                    Log.v("name",updatedName);
                    Log.v("address",updatedAddress);
                    editButton.setText("Edit");
                    Drawable img = getResources().getDrawable( R.drawable.ic_mode_edit_black_24dp);
                    img.setBounds( 0, 0, 174, 174);  // set the image size
                    editButton.setCompoundDrawables( img, null, null, null );
                    Toast.makeText(getApplicationContext(),"Entered save",Toast.LENGTH_SHORT).show();
                    dialog.setMessage("Updating Profile...");
                    dialog.setCancelable(false);
                    dialog.show();
                    nameView.setText(updatedName);
                    String[] addressComp = updatedAddress.split(",");
                    final String add2=addressComp[0].trim();
                    final String streedAddress=addressComp[1].trim();
                    final String city=addressComp[2].trim();
                    final String provice=addressComp[3].trim();
                    final String postalCode=addressComp[4].trim();


                    addressView.setText(updatedAddress);
                    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                    rootRef.child("displayName").setValue(updatedName)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (rootRef.child("address2").setValue(add2)!=null && rootRef.child("streetAddress").setValue(streedAddress)!=null
                                    && rootRef.child("city").setValue(city)!=null && rootRef.child("province").setValue(provice)!=null
                                    && rootRef.child("postalCode").setValue(postalCode)!=null){
                                        dialog.dismiss();
//                                    user.setDisplayName("")
                                        Toast.makeText(getApplicationContext(),"Profile Updated",Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(getApplicationContext(),user.getDisplayName(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Save failed",Toast.LENGTH_SHORT).show();
                                }
                            });




//                    Log.v("key",rootRef.child("service-genie-dal").child("users").child(user.getUid()).child("displayName").toString());
                }

            }
        });
    }

    private void setupChangePasswordBtn() {
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, ChangePassword.class));
            }
        });
    }

    private void deleteUserDetailsInDatabase(String uid) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        users.child(uid).removeValue();
    }

    private void deleteUser() {
        final ProgressDialog dialog = new ProgressDialog(Profile.this);
        dialog.setMessage("Deleting account...");
        dialog.setCancelable(false);
        dialog.show();
        final String uid = user.getUid();
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    deleteUserDetailsInDatabase(uid);
                    startActivity(new Intent(Profile.this, Login.class));
                    finish();
                } else {
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                    new AlertDialog.Builder(Profile.this).setMessage("Failed to delete user data. Please try again later.").setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }
        });
    }

    private void setupDeleteAccountBtn() {
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Profile.this).setMessage("Are you sure you want to delete your account ? ").setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        dialogBox.dismiss();
                        deleteUser();
                    }
                }).create().show();

            }
        });
    }

    private void setupSignoutBtn() {
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog=new AlertDialog.Builder(Profile.this).setMessage("Are you sure you want to sign out" +
                        "? ").setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        dialogBox.dismiss();
                        FIREBASE_AUTH.signOut();
                        startActivity(new Intent(Profile.this, Login.class));
                        finish();
                    }
                }).create();
                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                        Context context = Profile.this;
                        Window view = ((AlertDialog)dialog).getWindow();

                        view.setBackgroundDrawableResource(R.color.white);
                        Button negButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        negButton.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                        negButton.setTextColor(context.getResources().getColor(R.color.white));

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(20,0,20,0);
                        negButton.setLayoutParams(params);
                    }
                });
                dialog.show();
            }
        });
    }

    private static Bitmap decodeImage(String base64EncodedImage) {
        byte[] decodedBytes = Base64.decode(base64EncodedImage, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private static String getPhoneNumber(String phone) {
        phone = phone.replaceAll(" ", "");
        return phone.substring(0, 2) + " " + phone.substring(2, 5) + " " + phone.substring(5, 8) + " " + phone.substring(8);
    }

    private void setUserElements() {
        final ProgressDialog dialog = new ProgressDialog(Profile.this);
        dialog.setMessage("Loading profile...");
        dialog.setCancelable(false);
        dialog.show();
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
//        nameView.setText(user.getDisplayName());
        phoneView.setText(getPhoneNumber(user.getPhoneNumber()));
        emailView.setText(user.getEmail());

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User userDetails = dataSnapshot.child(user.getUid()).getValue(User.class);
                StringBuilder address = new StringBuilder();
                if (userDetails.getAddress2() != null && !userDetails.getAddress2().isEmpty()) {
                    address.append(userDetails.getAddress2() + ", ");
                }
                nameView.setText(userDetails.getDisplayName());
                address.append(userDetails.getStreetAddress()).append(", ").append(userDetails.getCity()).append(", ").append(userDetails.getProvince()).append(", ").append(userDetails.getPostalCode());
                addressView.setText(address.toString());
                if (userDetails.getProfilePicEncoded() != null && !userDetails.getProfilePicEncoded().isEmpty()) {
                    try {
                        Bitmap profilePicImage = decodeImage(userDetails.getProfilePicEncoded());
                        profilePic.setImageBitmap(profilePicImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(Profile.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(main);
//        super.onBackPressed();
    }

}
