package com.dal.mc.servicegenie;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MyBookings extends AppCompatActivity implements RViewAdapter.onClickListener {

    RecyclerView recyclerView;
    RViewAdapter rviewAdapter;
    ArrayList<Booking> bookings;
    private Runnable runnable;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_bookings);

        bookings = new ArrayList<Booking>();

        runnable = new Runnable() {
            @Override
            public void run() {
                getAllBookingsByUser();
            }
        };

        //retrieve data on separate thread
        Thread thread = new Thread(null, runnable, "background");
        thread.start();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
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

        /*rviewAdapter = new RViewAdapter(bookings);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rviewAdapter);

        //notifying RecyclerViewAdapter for change in data
        rviewAdapter.notifyDataSetChanged();*/

    public void getAllBookingsByUser() {
        //final TaskCompletionSource<String> task = new TaskCompletionSource<>();
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();

        final DatabaseReference allBookings = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("Bookings");
        allBookings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    Booking booking = data.getValue(Booking.class);
                    if(theUser.getEmail().toString().equalsIgnoreCase(booking.getRequestedByEmailId())){
                        // set front layout

                        //add to recycler view
                        bookings.add(booking);
                        Log.e("bookings","bookings Size:"+bookings.size());

                        if(rviewAdapter == null )
                        {
                            rviewAdapter = new RViewAdapter(MyBookings.this,bookings,MyBookings.this);
                            recyclerView.setAdapter(rviewAdapter);
                        }
                        if(bookings!=null && !bookings.isEmpty())
                        {
                            rviewAdapter.doRefresh(bookings);
                        }
                    }
                }
                //rviewAdapter.notifyDataSetChanged();
                rviewAdapter = new RViewAdapter(bookings);

                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(rviewAdapter);

                //notifying RecyclerViewAdapter for change in data
                rviewAdapter.notifyDataSetChanged();
                //task.setResult(bookings);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*Calendar tmp = Calendar.getInstance();
        Booking booking = new Booking();
        booking.setServiceName("Car Cleaner");
        booking.setCost(new Float(200));
        booking.setProfInfo("Kaycereous");
        booking.setStatus("Completed");
        booking.setTimeNDate(tmp);

        for (int i=0;i<10;i++)
            bookings.add(booking);*/

        //notifying RecyclerViewAdapter for change in data


        //adding request in queue
        //RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    @Override
    public void onItemClickListener(int position, Booking booking)
    {
        Intent intent = new Intent(this, activity_help.class);
        startActivity(intent);
    }
}
