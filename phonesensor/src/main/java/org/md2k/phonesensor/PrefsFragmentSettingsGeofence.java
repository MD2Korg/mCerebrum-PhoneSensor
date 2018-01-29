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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.md2k.mcerebrum.commons.dialog.Dialog;
import org.md2k.mcerebrum.commons.dialog.DialogCallback;
import org.md2k.phonesensor.phone.sensors.geofence.GeoFenceData;
import org.md2k.phonesensor.phone.sensors.geofence.GeoFenceLocationInfo;

import es.dmoral.toasty.Toasty;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Preferences Fragment for geofence settings
 */
public class PrefsFragmentSettingsGeofence extends PreferenceFragment {
    public static final int REQUEST_CHECK_SETTINGS = 1000;
    GeoFenceData geoFenceData;
    Subscription subscription;
    MaterialDialog materialDialog;

    /**
     * Reads configuration, enables GPS, inflates <code>R.xml.pref_geofence</code> and calls
     * <code>createPreferencesScreen</code>.
     *
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoFenceData = new GeoFenceData(getActivity());
        enableGPS();
        addPreferencesFromResource(R.xml.pref_geofence);
        createPreferenceScreen();
    }

    private static final long INTERVAL = 5000L;
    private Subscription updatableLocationSubscription;


    /**
     * Unsubscribes <code>updatableLocationSubscripton</code>.
     */
    public void unregister() {
        if (updatableLocationSubscription != null && !updatableLocationSubscription.isUnsubscribed())
            updatableLocationSubscription.unsubscribe();
    }

    /**
     * Handles user permissions results.
     *
     * @param requestCode The code sent with the request, used for request/result verification
     * @param resultCode  The code returned with the result, used for request/result verification
     * @param data Android intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        unregister();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(getActivity(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                        unregister();
                        getActivity().finish();
                        break;
                    default:
                        break;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Creates the settings view
     *
     * @param inflater Android LayoutInflater
     * @param container Android ViewGroup
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     * @return The view this method created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v != null;
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }

    /**
     * Creates a preference screen.
     *
     * <p>
     *     Calls <code>setConfiguredLocation()</code> and <code>addLocation</code>.
     * </p>
     */
    void createPreferenceScreen() {
        setConfiguredLocation();
        addLocation();
    }

    /**
     * Adds a configured location.
     *
     * <p>
     *     This is used to set a user's current local as home, work, or other. The name of "other"
     *     can be customized by the user.
     * </p>
     */
    void addLocation() {
        Preference p = findPreference("key_add");
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Dialog.singleChoice(getActivity(), "Set your current location", new String[]{"Home", "Work", "Other"}, 0, new DialogCallback() {
                    @Override
                    public void onSelected(String value) {
                        if (value.equals("Other")) {
                            Dialog.editbox(getActivity(), "Set Other Location", "Type your other location name.", new DialogCallback() {
                                @Override
                                public void onSelected(String value) {
                                    if (geoFenceData.isExist(value))
                                        Toasty.error(getActivity(), value + " already configured", Toast.LENGTH_SHORT).show();
                                    else addToList(value);
                                }
                            }).show();

                        }else {
                            if (geoFenceData.isExist(value))
                                Toasty.error(getActivity(), value + " already configured", Toast.LENGTH_SHORT).show();
                            else addToList(value);
                        }
                    }
                }).show();
                return true;
            }
        });

    }

    /**
     * Sets a configured location
     */
    void setConfiguredLocation() {
        PreferenceCategory pc = (PreferenceCategory) findPreference("key_configured_location");
        pc.removeAll();
        for (int i = 0; i < geoFenceData.getGeoFenceLocationInfos().size(); i++) {
            Preference p = new Preference(getActivity());
            String l = geoFenceData.getGeoFenceLocationInfos().get(i).getLocation();
            String lo = String.valueOf(geoFenceData.getGeoFenceLocationInfos().get(i).getLongitude());
            String la = String.valueOf(geoFenceData.getGeoFenceLocationInfos().get(i).getLatitude());
            p.setKey(l);
            p.setTitle(l);
            p.setSummary("(Latitude: " + la + ", Longitude: " + lo+")");
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                /**
                 * Allows a user to delete a configured location when they select it.
                 *
                 * @param preference Preference in question.
                 * @return Always returns true.
                 */
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    Dialog.simple(getActivity(), "Delete Location", "Delete location = " + preference.getKey() + "?", "Yes", "Cancel", new DialogCallback() {
                        @Override
                        public void onSelected(String value) {
                            if ("Yes".equals(value)) {
                                geoFenceData.delete(preference.getKey());
                                createPreferenceScreen();
                            }
                        }
                    }).show();
                    return true;
                }
            });
            pc.addPreference(p);
        }
    }

    /**
     * Enables the GPS via location requests.
     *
     * <p>
     * Creates a location request with high accuracy and an interval of 5000,
     * creates a new <code>ReactiveLocationProvider</code> and subscribes it to a new observer.
     * </p>
     * <p>
     * REFERENCE: <a href="http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never" >StackOverflow</a>
     * </p>
     */
    void enableGPS() {
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getActivity());
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL);
        Observable<LocationSettingsResult> locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //See REFERENCE in the method description.
                                .build()
                );
        updatableLocationSubscription = locationUpdatesObservable.subscribe(new Observer<LocationSettingsResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            /**
             * Checks for location permissions
             *
             * @param locationSettingsResult
             */
            @Override
            public void onNext(LocationSettingsResult locationSettingsResult) {
                try {
                    Status status = locationSettingsResult.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            break;
                        case LocationSettingsStatusCodes.SUCCESS:
                            unregister();
                            break;
                        default:
                            unregister();
                            break;
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    unregister();
                    getActivity().finish();
                }

            }
        });

    }

    /**
     * Adds a geofence centered on the current location to the preferences list.
     *
     * @param l Name of location to be added.
     */
    void addToList(final String l) {
        materialDialog = Dialog.progressIndeterminate(getActivity(), "Searching current location...").show();

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getActivity());

        LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(0);
        subscription = locationProvider.getUpdatedLocation(request).subscribe(new Action1<Location>() {
            /**
             * Creates a new geofence.
             *
             * @param location Location to geofence around.
             */
            @Override
            public void call(Location location) {
                GeoFenceLocationInfo g=new GeoFenceLocationInfo(l, location.getLatitude(), location.getLongitude());
                geoFenceData.add(g);
                createPreferenceScreen();
                materialDialog.dismiss();
            }
        });
    }

    /**
     * Dismisses any remaining dialogs, unsubscribe from any remaining subscriptions.
     */
    @Override
    public void onDestroy(){
        if(materialDialog!=null && materialDialog.isShowing()) materialDialog.dismiss();
        if(subscription!=null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        super.onDestroy();
    }

}
