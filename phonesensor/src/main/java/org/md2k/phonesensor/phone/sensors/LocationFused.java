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

package org.md2k.phonesensor.phone.sensors;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.core.data_format.DataFormat;
import org.md2k.phonesensor.ActivityPermission;
import org.md2k.phonesensor.R;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import br.com.goncalves.pugnotification.notification.PugNotification;
import es.dmoral.toasty.Toasty;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;

import static android.content.Context.LOCATION_SERVICE;

/**
 * This class handles the fused location (WIFI and GPS based) sensor.
 */
class LocationFused extends PhoneSensorDataSource {

    private static final String TAG = LocationFused.class.getSimpleName();
    /**
     * Sampling interval in milliseconds.
     *
     * <p>
     *     Default is 60,000 milliseconds (the number of milliseconds in a minute).
     * </p>
     */
    private static final long INTERVAL = 1000L * 60;

    /**
     * Notification id for <code>PugNotification</code>.
     *
     * <p>
     *     Arbitrarily set to 12 by default.
     * </p>
     */
    public static final int PUG_NOTI_ID = 12;

    private ReactiveLocationProvider locationProvider;
    private Subscription updatableLocationSubscription;
    private Observable<Location> locationUpdatesObservable;

    /**
     * Constructor
     *
     * @param context Android context
     */
    LocationFused(final Context context) {
        super(context, DataSourceType.LOCATION);
        frequency = String.format(Locale.getDefault(), "%.2f",(1.0/(INTERVAL/1000.0)));
    }

    /**
     * Sends data samples to dataKitAPI as an array.
     *
     * @param location Location to be saved
     */
    public void saveData(Location location) {
        double samples[] = new double[6];
        samples[DataFormat.Location.Latitude] = location.getLatitude();
        samples[DataFormat.Location.Longitude] = location.getLongitude();
        samples[DataFormat.Location.Altitude] = location.getAltitude();
        samples[DataFormat.Location.Speed] = location.getSpeed();
        samples[DataFormat.Location.Bearing] = location.getBearing();
        samples[DataFormat.Location.Accuracy] = location.getAccuracy();
        DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
        try {
            dataKitAPI.insert(dataSourceClient, dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
        } catch (DataKitException e) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
        }
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then subscribes this sensor to an observer.
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    @Override
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        context.registerReceiver(br, new IntentFilter("android.location.PROVIDERS_CHANGED"));
        prepareObservable();
        updatableLocationSubscription = locationUpdatesObservable.subscribe(new Observer<Location>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Location location) {
                saveData(location);
            }
        });
    }

    /**
     * Unregisters the listener for this sensor and unsubscribes this sensor from its observer
     */
    @Override
    public void unregister() {
        try {context.unregisterReceiver(br);}catch (Exception ignored){}
        if (updatableLocationSubscription != null && !updatableLocationSubscription.isUnsubscribed())
            updatableLocationSubscription.unsubscribe();
    }

    /**
     * Shows a notification if the GPS is currently disabled and asks the user to enable it.
     */
    private void showNotification() {
        PugNotification.with(context).load().identifier(PUG_NOTI_ID).title("Turn on GPS").smallIcon(R.mipmap.ic_launcher)
                .message("Location data can't be recorded. (Please click to turn on GPS)").autoCancel(true).click(ActivityPermission.class).simple().build();
    }

    /**
     * Removes a notification
     */
    private void removeNotification() {
        PugNotification.with(context).cancel(PUG_NOTI_ID);
    }

    /**
     * Prepares an observable, updatable location using the Android-ReactiveLocation Library
     *
     * <p>
     *     <a href="https://github.com/mcharmas/Android-ReactiveLocation">Android-ReactiveLocation Library</a>
     *     <a href="http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never">REFERENCE</a>
     * </p>
     */
    private void prepareObservable(){
        locationProvider = new ReactiveLocationProvider(context);
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(10)
                .setInterval(INTERVAL);
        locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //See REFERENCE above
                                .build()
                )
                .flatMap(new Func1<LocationSettingsResult, Observable<Location>>() {
                    @Override
                    public Observable<Location> call(LocationSettingsResult locationSettingsResult) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return null;
                        }
                        return locationProvider.getUpdatedLocation(locationRequest);
                    }
                });

    }
    private BroadcastReceiver br = new BroadcastReceiver() {
        /**
         * Updates <code>locationManager</code> if the location provider changes and asks for GPS to
         * be turned on if a provider is not enabled.
         *
         * @param context Android context
         * @param intent 
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    removeNotification();
                else {
                    Toasty.error(context, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                    showNotification();
                }
            }
        }
    };
}
