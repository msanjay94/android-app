package com.dal.mc.servicegenie;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private Button signout;
    private Button profile;
    GridView gridView;
    Button addserviceBtn;
    User userDetails;
    //private Button contactUsBtn;
    private BottomNavigationView bottomNavigationView;

    int[] images = {
            R.drawable.cleaning, R.drawable.plumbing, R.drawable.electrician,
            R.drawable.painting, R.drawable.salon, R.drawable.movers,
            R.drawable.pest_control, R.drawable.carpenter, R.drawable.party
    };

    String[] values = {"Home Cleaning", "Plumber", "Electrician", "Painting", "Beauty & Salon", "Movers and Packers",
            "Pest Control", "Carpenter", "Party Planning"
    };

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final FirebaseAuth auth = FirebaseAuth.getInstance();

        final FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            final TextView view = findViewById(R.id.testTxt);
//            Log.v("view",view.toString());
            DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
            users.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userDetails = dataSnapshot.child(user.getUid()).getValue(User.class);

                    view.setText("Hello "+userDetails.getDisplayName());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
        profile = findViewById(R.id.profileBtn);
        signout = findViewById(R.id.testBtn);
        addserviceBtn = findViewById(R.id.addMoreServices);
        gridView = (GridView) findViewById(R.id.grid);
        //contactUsBtn = findViewById(R.id.activity_contact_us);

        profile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });

        // display grid view of services
        gridView.setAdapter(new GridAdaptor(this, images, values));

        // Go to Booking section
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                /*switch(position) {
                    default:
                        break;
                }*/
                intent = new Intent(getApplicationContext(), BookingService.class);
                intent.putExtra("SERVICE_NAME", values[position]);
                startActivity(intent);
            }
        });

//         Go to addMoreServices page on "addMoreServices' button click
        addserviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddNewService.class));
            }
        });
        // go to contact us page
//        contactUsBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getApplicationContext(), activity_help.class));
//            }
//        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();

                    return true;
                }
            };
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit? You will be signed out!")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        signout.callOnClick();
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();

    }
    }

