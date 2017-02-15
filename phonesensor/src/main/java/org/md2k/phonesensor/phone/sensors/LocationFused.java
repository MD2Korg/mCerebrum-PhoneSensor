package org.md2k.phonesensor.phone.sensors;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.utilities.UI.AlertDialogs;
import org.md2k.utilities.data_format.DataFormat;

import java.util.ArrayList;
import java.util.HashMap;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
class LocationFused extends PhoneSensorDataSource {

    private static final String TAG = LocationFused.class.getSimpleName();
    private static final long INTERVAL = 5000L;
    private ReactiveLocationProvider locationProvider;
    private Subscription updatableLocationSubscription;
    private Observable<Location> locationUpdatesObservable;


    LocationFused(final Context context) {
        super(context, DataSourceType.LOCATION);
        frequency = "1.0";
        locationProvider = new ReactiveLocationProvider(context);
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL);
        locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                                .build()
                )
                .doOnNext(new Action1<LocationSettingsResult>() {
                    @Override
                    public void call(LocationSettingsResult locationSettingsResult) {
                        Status status = locationSettingsResult.getStatus();
                        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
                        }
                    }
                })
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

    private HashMap<String, String> createDataDescriptor(String name, String frequency, String description, int minValue, int maxValue, String unit) {
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.MIN_VALUE, String.valueOf(minValue));
        dataDescriptor.put(METADATA.MAX_VALUE, String.valueOf(maxValue));
        dataDescriptor.put(METADATA.UNIT, unit);
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DESCRIPTION, description);
        dataDescriptor.put(METADATA.DATA_TYPE, double.class.getName());
        return dataDescriptor;
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Latitude", frequency, "latitude, in degrees", -90, 90, "degree"));
        dataDescriptors.add(createDataDescriptor("Longitude", frequency, "Longitude, in degrees", -180, 180, "degree"));
        dataDescriptors.add(createDataDescriptor("Altitude", frequency, "Get the altitude if available, in meters above the WGS 84 reference ellipsoid", 0, 1000, "meters"));
        dataDescriptors.add(createDataDescriptor("Speed", frequency, "speed over ground", 0, 500, "meter/second"));
        dataDescriptors.add(createDataDescriptor("Bearing", frequency, "Bearing is the horizontal direction of travel of this device, and is not related to the device orientation", 0, 360, "degree"));
        dataDescriptors.add(createDataDescriptor("Accuracy", frequency, "Get the estimated accuracy of this location, in meters", 0, 100, "radius"));
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Location");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents a geographic location");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeDoubleArray.class.getName());
        return dataSourceBuilder;
    }


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

    private void statusCheck() {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialogs.AlertDialog(context, "Error: GPS is off", "Please turn on GPS\n\n (* select Mode = High Accuracy)", org.md2k.utilities.R.drawable.ic_error_red_50dp, "Turn On", "Cancel", null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        dialog.cancel();
                    }
                }
            });
        }
    }

    @Override
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        statusCheck();
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

    @Override
    public void unregister() {
        if(updatableLocationSubscription!=null && !updatableLocationSubscription.isUnsubscribed())
            updatableLocationSubscription.unsubscribe();
    }
}
