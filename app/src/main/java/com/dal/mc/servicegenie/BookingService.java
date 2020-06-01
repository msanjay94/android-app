package com.dal.mc.servicegenie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vivekkaushik.datepicker.OnDateSelectedListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;

public class BookingService extends AppCompatActivity {

    private static final String TAG = "TAG";
    EditText datetimeedt;
    EditText addrLine1, city1, province, postelCode;
    Calendar serviceDateTimeCalendar;
    //RangeTimePickerDialog tpd;
    TimePickerDialog tpd;
    Button location,book;
    GPSTracker2 gps;
    BookingService activity;
    private int LOCATION_PERMISSION_CODE = 1;

    List<Address> addresses;
    Geocoder geocoder;
    String address, city, state, country, postalCode, knownName, numberStr, longitudeee;
    //Button location;
    //GPSTracker gps;
    String serviceNm;
    TextView bookingServiceName;
    TextView bookingDesc;

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_service);
        activity = BookingService.this;

        //firebase object
        databaseReference = FirebaseDatabase.getInstance().getReference();


        gps = new GPSTracker2(BookingService.this);
        bookingServiceName = findViewById(R.id.booking_service_name);
        bookingDesc = findViewById(R.id.description);
        book = (Button) findViewById(R.id.book);

        Intent intent = getIntent();
        // get desc and name from firebase
        serviceNm= intent.getStringExtra("SERVICE_NAME");

        final Query serviceQry = FirebaseDatabase.getInstance().getReference("services").orderByChild("serviceName").equalTo(serviceNm).limitToFirst(1);
        serviceQry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Service service = dataSnapshot.getChildren().iterator().next().getValue(Service.class);
                // set front layout
                bookingServiceName.setText(serviceNm);
                bookingDesc.setText(service.getServiceDesc());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addrLine1 = (EditText) findViewById(R.id.addrLine1);
        city1 = (EditText) findViewById(R.id.city);
        province = (EditText) findViewById(R.id.province);
        postelCode = (EditText) findViewById(R.id.postelCode);


        datetimeedt = (EditText) findViewById(R.id.datetimeedt);

        location = (Button) findViewById(R.id.location);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkForCameraAndLocationPermission(1)) {
                    new AddressProgress().execute();
                } else {
                    Log.e("Permission", "We Don't Have Permission");
                    Toast.makeText(activity, "We don't have permission please enable it", Toast.LENGTH_SHORT).show();
                }
            }
        });

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Booking booking = new Booking();
                booking.setRequestedServiceName(serviceNm);

                //get emailId
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final FirebaseUser theUser = mAuth.getCurrentUser();
                booking.setRequestedByEmailId(theUser.getEmail().toString());

                booking.setRequestTimeandDate(datetimeedt.getText().toString());
                booking.setRequestCost("$70");
                booking.setRequestProfName("Mathew");
                booking.setRequestStatus("Pending");
                databaseReference.child("ServiceRequest").push().setValue(booking);
            }
        });


        datetimeedt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //custom dialog
                final Dialog dialog = new Dialog(BookingService.this);
                dialog.setContentView(R.layout.datetimedialog);

                //set custom dialog component
                final DatePickerTimeline1 dateTimeLinePicker = (DatePickerTimeline1) dialog.findViewById(R.id.dateTimeLinePicker);
                final TextView timeSelected = (TextView) dialog.findViewById(R.id.timeSelected);

                Button selectDateTimeBtn = (Button) dialog.findViewById(R.id.selectDateTime);

                //set a start date
                Calendar currentDate = Calendar.getInstance();
                dateTimeLinePicker.setInitialDate(currentDate.get(Calendar.YEAR), (currentDate.get(Calendar.MONTH)), currentDate.get(Calendar.DAY_OF_MONTH));

                //Today's date
                //Toast.makeText(BookingService.this, "Y: "+calendar.get(Calendar.YEAR)+"\nM: "+(calendar.get(Calendar.MONTH)+1)+"\nD: "+calendar.get(Calendar.DAY_OF_MONTH), Toast.LENGTH_SHORT).show();

                //set date selected listener
                dateTimeLinePicker.setOnDateSelectedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(int year, int month, int day, int dayOfWeek) {

                        //Selected Date
                        //Toast.makeText(BookingService.this, "Y+: " + year + "\nM: " + month + "\nD: " + day, Toast.LENGTH_SHORT).show();


                        serviceDateTimeCalendar = Calendar.getInstance();
                        serviceDateTimeCalendar.set(year, month, day);

                        //Toast.makeText(BookingService.this, , Toast.LENGTH_SHORT).show();
                        //return calendar.getTime();
                        if ((year == serviceDateTimeCalendar.get(Calendar.YEAR)) && (month + 1 == serviceDateTimeCalendar.get(Calendar.MONTH)) && (day == serviceDateTimeCalendar.get(Calendar.DAY_OF_MONTH))) {
                            //client can book service after an hour of current time
                            int afterMintuesBooking = 60;

                            //Current hour and minute
                            //Toast.makeText(BookingService.this,"H: "+(calendar.HOUR)+"\nM: "+(calendar.MINUTE),Toast.LENGTH_SHORT).show();
                            final int hour = (serviceDateTimeCalendar.get(Calendar.HOUR) + (afterMintuesBooking / 60));
                            int min = (serviceDateTimeCalendar.get(Calendar.MINUTE) + (afterMintuesBooking % 60));

                            tpd = new TimePickerDialog(BookingService.this, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


                                    serviceDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    serviceDateTimeCalendar.set(Calendar.MINUTE, minute);
                                    serviceDateTimeCalendar.set(Calendar.SECOND, 0);
                                }
                            }, hour, min, true);
                            tpd.show();


                        } else {

                            //Toast.makeText(BookingService.this, "Hello", Toast.LENGTH_SHORT).show();
                            tpd = new TimePickerDialog(BookingService.this, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    serviceDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    serviceDateTimeCalendar.set(Calendar.MINUTE, minute);
                                    serviceDateTimeCalendar.set(Calendar.SECOND, 0);

                                }
                            }, 0, 0, true);
                            tpd.show();
                        }


                        tpd.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    Toast.makeText(BookingService.this, "" + serviceDateTimeCalendar.getTime().toString(), Toast.LENGTH_LONG).show();
                                    timeSelected.setText(serviceDateTimeCalendar.getTime().toString());
                                }
                            }
                        });

                    }

                    @Override
                    public void onDisabledDateSelected(int year, int month, int day, int dayOfWeek, boolean isDisabled) {

                    }
                });

                dialog.show();

                selectDateTimeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //tpd.dismiss();
                        dialog.dismiss();


                        //Toast.makeText(getApplicationContext(), "Time: " + serviceDateTimeCalendar.getTime(), Toast.LENGTH_LONG).show();
                        datetimeedt.setText(serviceDateTimeCalendar.getTime().toString());

                    }
                });

            }
        });


    }


    private boolean checkForCameraAndLocationPermission(int code) {
        if (ContextCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                    code);
            return false;
        } else {
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (permissions.length == 0) {
            return;
        }
        boolean allPermissionsGranted = true;
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
        }
        if (!allPermissionsGranted) {
            boolean somePermissionsForeverDenied = false;
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //denied

                    Log.e(TAG, "onRequestPermissionsResult:===> permition denied " + permission);
                    switch (requestCode) {

                        case 1:

                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
                            break;
                        default:
                            break;
                    }

                } else {
                    if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        //allowed
                        Log.e("allowed", permission);
                        Log.e(TAG, "onRequestPermissionsResult:permition allowed " + permission);
                    } else {
                        //set to never ask again
                        Log.e("set to never ask again", permission);
                        Log.e(TAG, "onRequestPermissionsResult:set to never ask again " + permission);
                        somePermissionsForeverDenied = true;
                    }
                }
            }
            if (somePermissionsForeverDenied) {

                Log.e(TAG, "onRequestPermissionsResult: somePermissionsForeverDenied " + somePermissionsForeverDenied);


                String title = "";
                switch (requestCode) {

                    case 1:
                        title = "location";
                        break;
                    default:
                        break;
                }

                if (title.toString().trim().endsWith(",") || title.toString().trim().endsWith("&")) {
                    title = title.toString().trim().substring(0, title.toString().trim().length() - 1);
                }

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Permissions Required")
                        .setMessage("Please allow permission for " + title + ".")
                        .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        } else {
            switch (requestCode) {
                case 1:


                    new AddressProgress().execute();
                default:
                    break;
            }
        }
    }

    public class AddressProgress extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;
        double latitude;
        double longitude;


        @Override
        protected Void doInBackground(Void... voids) {

            try {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                try {
                    geocoder = new Geocoder(activity, Locale.getDefault());
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                address = addresses.get(0).getAddressLine(0).split(",")[0];
                Log.e("Live Adress", "--->" + address);


                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName();

                String code = addresses.get(0).getCountryCode();
                Log.e("TAG", "doInBackground: " + code);

                numberStr = Double.toString(latitude);
                longitudeee = Double.toString(longitude);


            } catch (Exception e) {
                Log.e("Exception", "Exception in doInBackGround" + e.getMessage());
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                        // set title
                        alertDialogBuilder.setTitle("GPS Route Finder");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Your Internet Connection might be Slow.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, close
                                        // current activity
                                        dialog.dismiss();
                                        finish();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                    }
                });
            }


            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);

            try {

                progressDialog.dismiss();
                //txt_lat.setText(numberStr);
                //txt_lng.setText(longitudeee);
                addrLine1.setText(address);
                city1.setText(city);
                province.setText(state);
                postelCode.setText(postalCode);

            } catch (Exception e) {
                Log.e("Exception", "Exception in postExecution" + e.getMessage());
                e.printStackTrace();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                // set title
                alertDialogBuilder.setTitle("GPS Route Finder");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Your Internet Connection might be Slow.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                dialog.dismiss();
                                finish();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }

        }
    }


}
