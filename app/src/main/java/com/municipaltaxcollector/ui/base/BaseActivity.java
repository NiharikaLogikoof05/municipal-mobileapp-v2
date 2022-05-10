package com.municipaltaxcollector.ui.base;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.municipaltaxcollector.R;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity implements  BaseActivityInterface,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    protected int layoutId = -1;
    protected ViewDataBinding activityBaseBinding;
    SharedPreferences locationpreference;
    private static final int REQUEST_LOCATION = 101;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    protected String mLastUpdateTime;
    protected String longitude = "0";
    protected String latitude = "0";
    // public static String district="";
    // public static String state="";
/*
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
           /* if (this instanceof LoginActivity || this instanceof FarmerLogin ||this instanceof DealerLogin ||this instanceof UserLogin) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                window.setStatusBarColor(getResources().getColor(R.color.colorwindowfeature));
            }*/
        }
        init();
        activityBaseBinding = DataBindingUtil.setContentView(this, layoutId);
        if (activityBaseBinding != null) {
            setUpUi(savedInstanceState, activityBaseBinding);
        }

        if (!isGooglePlayServicesAvailable()) {

            showDialog(this, getString(R.string.google_play_services),false,false,0);


        } else if(isGPSEnabled()==false){
//            alertMessageNoGps();
        }
   //     createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        int permissionLocationCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

    }
    public  void currentAddress(double latitude,double longitude)
    {
        Geocoder geocoder;
        List<Address> addresses=null;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            // district = addresses.get(0).getLocality();
            // state = addresses.get(0).getAdminArea();
            // Log.v("District",district);
            // Log.v("State",state);
            //String country = addresses.get(0).getCountryName();
            //String postalCode = addresses.get(0).getPostalCode();
            //String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        } catch (IOException e) {
            e.getMessage();
        }


    }



    protected void init() {

    }

    protected void setUpUi(Bundle savedInstanceState) {

    }
    protected void setUpUi(Bundle savedInstanceState, ViewDataBinding viewDataBinding) {


    }
    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    public void showDialog(Activity
                                   activity, String msg, boolean isCancelBtnVisible, final boolean isClickable,final int Id){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.alert_dialog);

        TextView text = dialog.findViewById(R.id.text_dialog);
        text.setText(msg);

        Button dialogOKButton =dialog.findViewById(R.id.btn_ok);

        dialogOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(isClickable)
                    BaseActivity.this.okDialogClick(Id);
            }
        });

        Button dialogCancelButton =dialog.findViewById(R.id.btn_cancel);
        if(isCancelBtnVisible)
        {
            dialogCancelButton.setVisibility(View.VISIBLE);
        }
        else
        {
            dialogCancelButton.setVisibility(View.GONE);

        }
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(isClickable)
                    BaseActivity.this.cancelDialogClick(Id);
            }
        });


        dialog.show();

    }

    public  boolean isConnectedToInternet(final Context context) {

        boolean isConnected;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        return isConnected;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show, final ProgressBar progressBar) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);



            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart fired ..............");
        try {
            mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //Log.d(TAG, "onStop fired");
        try {
            mGoogleApiClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {

            locationpreference=BaseActivity.this.getSharedPreferences("locationdata",MODE_PRIVATE);
            SharedPreferences.Editor editr= locationpreference.edit();

            if (null != mCurrentLocation) {
                latitude = String.valueOf(mCurrentLocation.getLatitude());
                longitude = String.valueOf(mCurrentLocation.getLongitude());
                editr.putString("lat",latitude);
                editr.putString("long",longitude);
                editr.apply();
            }
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        try {
            startLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        else
        {
            //PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
            //        mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;

        locationpreference=BaseActivity.this.getSharedPreferences("locationdata",MODE_PRIVATE);
        SharedPreferences.Editor editr= locationpreference.edit();

        if (null != mCurrentLocation) {
            latitude = String.valueOf(mCurrentLocation.getLatitude());
            longitude = String.valueOf(mCurrentLocation.getLongitude());
            editr.putString("lat",latitude);
            editr.putString("long",longitude);
            editr.apply();
        }
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }



    private void updateUI() {
        // Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            latitude = String.valueOf(mCurrentLocation.getLatitude());
            longitude = String.valueOf(mCurrentLocation.getLongitude());
            locationpreference=BaseActivity.this.getSharedPreferences("locationdata",MODE_PRIVATE);
            SharedPreferences.Editor editr= locationpreference.edit();

            if (null != mCurrentLocation) {
                latitude = String.valueOf(mCurrentLocation.getLatitude());
                longitude = String.valueOf(mCurrentLocation.getLongitude());
                editr.putString("lat",latitude);
                editr.putString("long",longitude);
                editr.apply();}
            //  currentAddress(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        } else {
            //  Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        try
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
            // Log.d(TAG, "Location update stopped .......................");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }



   /* @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case REQUEST_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
                else
                {

                }

                break;

            default:
                break;
        }
    }*/

    public boolean isGPSEnabled() {
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void alertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        dialog.cancel();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }





}
