package org.md2k.phonesensor;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.md2k.mcerebrum.commons.permission.Permission;
import org.md2k.mcerebrum.commons.permission.PermissionCallback;

import es.dmoral.toasty.Toasty;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.md2k.phonesensor.PrefsFragmentSettings.REQUEST_CHECK_SETTINGS;

public class ActivityPermission extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Permission.requestPermission(this, new PermissionCallback() {
            @Override
            public void OnResponse(boolean isGranted) {
                if (!isGranted) {
                    Toasty.error(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    finish();
                } else {

                    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                    int result = googleAPI.isGooglePlayServicesAvailable(getBaseContext());
                    if (result != ConnectionResult.SUCCESS) {
                        if (googleAPI.isUserResolvableError(result)) {
                            googleAPI.getErrorDialog(ActivityPermission.this, result,
                                    9000, new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            Toasty.error(getApplicationContext(), "Google play service is disabled/not updated", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }).show();
                        } else {
                            Toasty.error(getApplicationContext(), "Google play service is DISABLED/NOT UPDATED", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        enableGPS();
                    }
                }
            }
        });

//        setContentView(R.layout.activity_permission);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9000) {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(this);
            if (result != ConnectionResult.SUCCESS) {
                Toasty.error(getApplicationContext(), "Google play service is disabled/not updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                enableGPS();
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // All required changes were successfully made
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private Subscription updatableLocationSubscription;

    public void enableGPS() {
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000);

        updatableLocationSubscription = new ReactiveLocationProvider(this)
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                               .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                                .build()
                ).subscribe(new Observer<LocationSettingsResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LocationSettingsResult locationSettingsResult) {
                        try {
                            Status status = locationSettingsResult.getStatus();
                            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                                status.startResolutionForResult(ActivityPermission.this, REQUEST_CHECK_SETTINGS);
                            } else {
                                setResult(RESULT_OK);
                                finish();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                });
    }

    @Override
    public void onDestroy() {
        if (updatableLocationSubscription != null && !updatableLocationSubscription.isUnsubscribed())
            updatableLocationSubscription.unsubscribe();
        super.onDestroy();
    }

}
