package com.municipaltaxcollector.ui.base;

import static android.content.Context.MODE_PRIVATE;

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
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.municipaltaxcollector.R;

import java.text.DateFormat;
import java.util.Date;

public class BaseFragment extends Fragment implements BaseFragmentInterface, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final int REQUEST_LOCATION = 101;

    protected int layoutId = -1;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    protected String mLastUpdateTime;
    protected String longitude = "0";
    protected String latitude = "0";
    SharedPreferences locationpreference;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = null;
        init();
        ViewDataBinding viewBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        if (viewBinding != null) {
            view = viewBinding.getRoot();
            setUpUi(view, viewBinding);
        }

        if (!isGooglePlayServicesAvailable()) {

            showDialog(getActivity(), getString(R.string.google_play_services),false,false,0);


        }
        else if(isGPSEnabled()==false){
//            alertMessageNoGps();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        int permissionLocationCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        return view;
    }


    protected void init() {


    }

    protected void setUpUi(Bundle savedInstanceState) {

    }

    protected void setUpUi(View view, ViewDataBinding viewDataBinding) {

    }

    public void showDialog(Activity activity, String msg, boolean isCancelBtnVisible, final boolean isClickable,final int Id){
        try {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.alert_dialog);

            TextView text = dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button dialogOKButton = dialog.findViewById(R.id.btn_ok);

            dialogOKButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (isClickable)
                        BaseFragment.this.okDialogClick(Id);
                }
            });

            Button dialogCancelButton = dialog.findViewById(R.id.btn_cancel);
            if (isCancelBtnVisible) {
                dialogCancelButton.setVisibility(View.VISIBLE);
            } else {
                dialogCancelButton.setVisibility(View.GONE);

            }
            dialogCancelButton.setOnClickListener(v -> {
                dialog.dismiss();
                if (isClickable)
                    BaseFragment.this.cancelDialogClick(Id);
            });


            dialog.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    public boolean isConnectedToInternet(final Context context) {

        boolean isConnected;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        return isConnected;
    }


    @Override
    public void onBackCustom() {

    }

    @Override
    public void okDialogClick(int Id) {

    }

    @Override
    public void cancelDialogClick(int Id) {

    }
    /*@Override
    public void topBackButtonPressed() {

    }

    @Override
    public void setScreenTitle(TextView title) {

    }*/

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
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == status) {


            locationpreference=getActivity().getSharedPreferences("locationdata",MODE_PRIVATE);
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
            GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0).show();
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
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);

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

        locationpreference=getActivity().getSharedPreferences("locationdata",MODE_PRIVATE);
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

            locationpreference=getActivity().getSharedPreferences("locationdata",MODE_PRIVATE);
            SharedPreferences.Editor editr= locationpreference.edit();

            if (null != mCurrentLocation) {
                latitude = String.valueOf(mCurrentLocation.getLatitude());
                longitude = String.valueOf(mCurrentLocation.getLongitude());
                editr.putString("lat",latitude);
                editr.putString("long",longitude);
                editr.apply();
            }
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



    @Override
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
    }

    public boolean isGPSEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void alertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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


}
