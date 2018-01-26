/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.md2k.phonesensor;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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
import org.md2k.mcerebrum.commons.permission.PermissionInfo;
import org.md2k.mcerebrum.commons.permission.ResultCallback;
import org.md2k.mcerebrum.core.access.MCerebrum;

import es.dmoral.toasty.Toasty;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observer;
import rx.Subscription;

import static org.md2k.phonesensor.PrefsFragmentSettings.REQUEST_CHECK_SETTINGS;

/**
 * This class handles the permission requests for the activity.
 */
public class ActivityPermission extends AppCompatActivity {

    /**
     * Creates this activity. If the Android version is Marshmallow (6.0) or later, the user is asked
     * for overlay permissions.
     *
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                myIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(myIntent, 101);
            }
        }
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            /**
             * @param result
             */
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    MCerebrum.setPermission(ActivityPermission.this, false);
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    finish();
                } else {
                    checkAllPermission();
                }
            }
        });
    }

    /**
     * Checks Google play services for the require permissions and enables GPS if successful.
     *
     * TODO: Consider listing required permissions
     */
    public void checkAllPermission() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getBaseContext());
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(ActivityPermission.this, result,
                        9000, new DialogInterface.OnCancelListener() {
                            /**
                             * When the user cancels the dialog, <code>Toasty</code> shows a Google
                             * play services error, the activity result is set to canceled and
                             * <code>finish()</code> is called.
                             *
                             * This is only called when the connection to Google play services is
                             * unsuccessful, the error is user resolvable, and the user decides to
                             * cancel the service.
                             *
                             * @param dialog The dialog that is displayed to the user.
                             */
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Toasty.error(getApplicationContext(), "Google play service is disabled/not updated", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_CANCELED, resultIntent);
                                finish();
                            }
                        }).show();
            } else {
                Toasty.error(getApplicationContext(), "Google play service is DISABLED/NOT UPDATED", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        } else {
            enableGPS();
        }
    }

    /**
     * If the request code is 9000 (associated with Google play services in <code>checkAllPermissions</code>),
     * <code>Toasty</code> shows a Google play services error, the activity result is set to canceled and
     * <code>finish()</code> is called.
     *
     * If the error code is <code>REQUEST_CHECK_SETTINGS</code>,
     *
     * @param requestCode The code sent with the request, used for request/result verification
     * @param resultCode  The code returned with the result, used for request/result verification
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9000) {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(this);
            if (result != ConnectionResult.SUCCESS) {
                Toasty.error(getApplicationContext(), "Google play service is disabled/not updated", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            } else {
                enableGPS();
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // All required changes were successfully made
                MCerebrum.setPermission(this, true);
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                MCerebrum.setPermission(this, false);
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        }
    }

    private Subscription updatableLocationSubscription;

    /**
     * Creates a location request with high accuracy and an interval of 5000,
     * creates a new <code>ReactiveLocationProvider</code> and subscribes it to a new observer.
     */
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
                    /**
                     * Does nothing
                     */
                    @Override
                    public void onCompleted() {

                    }

                    /**
                     * Does nothing
                     *
                     * @param e
                     */
                    @Override
                    public void onError(Throwable e) {

                    }

                    /**TODO: Not sure what this does other than check permissions again...
                     * @param locationSettingsResult
                     */
                    @Override
                    public void onNext(LocationSettingsResult locationSettingsResult) {
                        try {
                            Status status = locationSettingsResult.getStatus();
                            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                                status.startResolutionForResult(ActivityPermission.this, REQUEST_CHECK_SETTINGS);
                            } else {
                                MCerebrum.setPermission(ActivityPermission.this, true);
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        } catch (Exception e) {
                            MCerebrum.setPermission(ActivityPermission.this, false);
                            Toast.makeText(getBaseContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            finish();
                        }

                    }
                });
    }

    /**
     * When the activity gets destroyed <code>updatableLocationSubscription</code> is unsubscribed
     * and <code>super.onDestroy()</code> is called.
     */
    @Override
    public void onDestroy() {
        if (updatableLocationSubscription != null && !updatableLocationSubscription.isUnsubscribed())
            updatableLocationSubscription.unsubscribe();
        super.onDestroy();
    }

}