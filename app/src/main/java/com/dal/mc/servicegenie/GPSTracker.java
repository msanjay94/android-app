package com.dal.mc.servicegenie;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    String maininfo;

    Location location, location1, location2; // location
    double latitude; // latitude
    double longitude; // longitude

    ProgressDialog progress;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
    }

    public void getLocation() {
        try {
            canGetLocation = false;
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //Toast.makeText(mContext,isGPSEnabled+"",Toast.LENGTH_SHORT).show();

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            // Toast.makeText(mContext,isNetworkEnabled+"hi",Toast.LENGTH_SHORT).show();

            if (!isGPSEnabled || !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isGPSEnabled) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("GPS Enabled", "GPS Enabled");
                    //Toast.makeText(mContext,((locationManager==null)?true:false)+"location from gps",Toast.LENGTH_SHORT).show();
                    if (locationManager != null)
                    {
                        location1 = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        //Toast.makeText(mContext,location1+"location from gps",Toast.LENGTH_SHORT).show();
                        if (location1 != null)
                        {
                            //Toast.makeText(mContext,"location from gps",Toast.LENGTH_SHORT).show();
                            location=location1;
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }


                if (isNetworkEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        //Toast.makeText(mContext,((locationManager==null)?true:false)+"location from network",Toast.LENGTH_SHORT).show();

                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location2 = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            //Toast.makeText(mContext,location1+"location from network",Toast.LENGTH_SHORT).show();
                            if (location2 != null)
                            {
                                //Toast.makeText(mContext,"location from network",Toast.LENGTH_SHORT).show();
                                location=location2;
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
            /*{
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */

    public String getLatitude()
    {
        if (location != null)
        {
            if (location != null)
            {

//                if (location.isFromMockProvider())
//                {
//                    Toast.makeText(mContext, "Location is mock location provider not allowed", Toast.LENGTH_LONG).show();
//                    ((Activity) mContext).finish();
//                } else
//                    {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
//                Toast.makeText(mContext, "Your Location is - \nLat: "
//                        + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    maininfo = latitude + "," + longitude;

     //           }
            }
            if (location == null)
            {
                progress = new ProgressDialog(mContext);
                progress.setMessage("Getting Location........");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(true);
                progress.show();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            while (location == null) {
                                Thread.sleep(200);               //code here

                                getLocation();
                                if (location != null) {
                                    progress.dismiss();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                //progress.dismiss();
            }

        }
        return  maininfo;
    }
//        if(location==null) {
//            progress = new ProgressDialog(mContext);
//            progress.setMessage("Getting Location");
//            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progress.setIndeterminate(true);
//            progress.show();
//            while (location == null)
//            {
//                //code here
//Log.d("gps","");
//               // getLocation();
//            }
//            progress.dismiss();
//        }

  //  }

    /**
     * Function to get longitude
     * */

    /*public double getLongitude(){
        if(location != null)
        {
            if(location.isFromMockProvider())
            {
                Toast.makeText(mContext,"Location is mock location provider not allowed",Toast.LENGTH_LONG).show();
                ((Activity)mContext).finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                        + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            }

        }

        // return longitude
        return longitude;
    }*/



    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */

    public void showSettingsAlert()
    {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                Toast.makeText(mContext, "bye", Toast.LENGTH_SHORT).show();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                return;

//                Intent intent=new Intent(GPSTracker.this,Geo_tag_exam_conduct.class);
//                startActivity(intent);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        getLatitude();
        //getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


}