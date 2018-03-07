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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.core.data_format.DataFormat;
import org.md2k.phonesensor.ActivityPermission;
import org.md2k.phonesensor.R;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.phonesensor.phone.sensors.geofence.GeoFenceData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import br.com.goncalves.pugnotification.notification.PugNotification;
import es.dmoral.toasty.Toasty;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.content.Context.LOCATION_SERVICE;

/**
 * This class manages geofences.
 */
class GeoFence extends PhoneSensorDataSource {

    /**
     * Geofence radius in meters
     *
     * <p>
     *     Default is 100 meters.
     * </p>
     */
    private static final float RADIUS = 100;

    /**
     * Notification id for <code>PugNotification</code>.
     *
     * <p>
     *     Arbitrarily set to 12 by default.
     * </p>
     */
    public static final int PUG_NOTI_ID = 12;
    private GeoFenceData geoFenceData;
    private ReactiveLocationProvider reactiveLocationProvider;
    private Subscription lastKnownLocationSubscription;
    private Subscription subscriptionMinute;
    private HashMap<String, DataSourceClient> currentList;
    private GeofenceBroadcastReceiver gbr;
    private static final String TAG = GeoFence.class.getSimpleName();

    /**
     * Constructor
     *
     * @param context Android context
     */
    GeoFence(final Context context) {
        super(context, DataSourceType.GEOFENCE);
        frequency = "1.0";
        currentList = new HashMap<>();
        gbr = new GeofenceBroadcastReceiver();
    }

    /**
     * Saves location data to dataKitAPI on an observable interval
     */
    public void saveData() {
        subscriptionMinute = Observable.interval(0, 1, TimeUnit.MINUTES).map(new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long aLong) {
                for (Map.Entry<String, DataSourceClient> entry : currentList.entrySet()) {
                    if (entry.getValue() != null) {
                        DataSourceClient dataSourceClient = entry.getValue();
                        try {
                            Log.d(TAG, "location=" + entry.getKey() + " stays 1 minute");
                            dataKitAPI.setSummary(dataSourceClient, new DataTypeIntArray(DateTime.getDateTime(), new int[]{60000}));
                        } catch (DataKitException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
        }).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean aBoolean) {

            }
        });
    }

    /**
     * Queries dataKitAPI for the last set of data it was sent
     *
     * @return The sample string from the first node of the ArrayList
     * @throws DataKitException
     */
    private String getLastListFromDataKit() throws DataKitException {
        ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClient, 1);
        if (dataTypes.size() == 0) return null;
        return ((DataTypeString) dataTypes.get(0)).getSample();
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then registers this sensor with Android's SensorManager
     *
     * <p>
     * Creates a new <code>geoFenceData</code> object and fetches the last list from dataKitAPI.
     *</p><p>
     * If the last list from dataKit and the recent list of locations are null, then the registering
     * is complete.
     * If only the recent list is null, then a new data point is inserted into dataKitAPI with the
     * current timestamp and an empty string.
     * If the last list from dataKit is null, then a new data point is inserted into dataKitAPI with
     * current timestamp and the recent list of locations.
     * </p>
     * <p>
     * A broadcast receiver is registered with Android when <code>android.location.PROVIDERS_CHANGED</code>
     * is triggered.
     *
     * This <code>geoBroadcastReciever</code> is registered with Android when this sensor is registered
     * with dataKitAPI.
     *
     * Calls <code>addGeofence</code> and <code>saveData</code>
     *</p>
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    @Override
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        geoFenceData = new GeoFenceData(context);
        String lastListFromDataKit = getLastListFromDataKit();
        String recentListFromMemory = geoFenceData.getGeoFenceString();
        if (lastListFromDataKit == null && recentListFromMemory == null) return;
        if (recentListFromMemory == null) {
            dataKitAPI.insert(dataSourceClient, new DataTypeString(DateTime.getDateTime(), ""));
            return;
        } else if (lastListFromDataKit == null)
            dataKitAPI.insert(dataSourceClient, new DataTypeString(DateTime.getDateTime(), recentListFromMemory));
        else if (!lastListFromDataKit.equalsIgnoreCase(recentListFromMemory))
            dataKitAPI.insert(dataSourceClient, new DataTypeString(DateTime.getDateTime(), recentListFromMemory));
        context.registerReceiver(br, new IntentFilter("android.location.PROVIDERS_CHANGED"));
        context.registerReceiver(gbr, new IntentFilter("org.md2k.phonesensor.gbr"));

        reactiveLocationProvider = new ReactiveLocationProvider(context);

        // This conditional currently checks for fine and course location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastKnownLocationSubscription = reactiveLocationProvider
                .getLastKnownLocation()
                .subscribe();
        addGeofence();
        saveData();
    }

    /**
     * Creates a new intent to send a broadcast
     *
     * @return The pending intent
     */
    private PendingIntent createNotificationBroadcastPendingIntent() {
        Intent intent = new Intent("org.md2k.phonesensor.gbr"); // intent to send a broadcast

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /**
     * Creates a geofencing request and a pending intent.
     *
     * <p>
     * <code>reactiveLocationProvider</code> is called
     * to remove current geofences and create a flatMap. If fine location permission is granted,
     * then <code>reactiveLocationProvider</code> is called to create a new geofence with the pending
     * intent and the geofencing request created at the beginning of this method call.
     * </p>
     */
    private void addGeofence() {
        final GeofencingRequest geofencingRequest = createGeofencingRequest();

        final PendingIntent pendingIntent = createNotificationBroadcastPendingIntent();
        reactiveLocationProvider
                .removeGeofences(pendingIntent)
                .flatMap(new Func1<Status, Observable<Status>>() {
                    @Override
                    public Observable<Status> call(Status pendingIntentRemoveGeofenceResult) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
//                            return TODO;
                        }
                        return reactiveLocationProvider.addGeofences(pendingIntent, geofencingRequest);
                    }
                })
                .subscribe(new Action1<Status>() {
                    @Override
                    public void call(Status addGeofenceResult) {
                        Log.d(TAG, "Geofence added, success: " + addGeofenceResult.isSuccess());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error adding geofence.", throwable);
                    }
                });
    }

    /**
     * Unregisters the listener for this sensor
     */
    @Override
    public void unregister() {
        try {
            context.unregisterReceiver(br);
        } catch (Exception ignored) {

        }
        try {
            context.unregisterReceiver(gbr);
        } catch (Exception ignored) {

        }
        clearGeofence();
        if (lastKnownLocationSubscription != null && !lastKnownLocationSubscription.isUnsubscribed())
            lastKnownLocationSubscription.unsubscribe();
        if (subscriptionMinute != null && !subscriptionMinute.isUnsubscribed())
            subscriptionMinute.unsubscribe();
    }

    /**
     * Removes all geofences from <code>reactiveLocationProvider</code>
     */
    private void clearGeofence() {
        if(reactiveLocationProvider==null) return;
        reactiveLocationProvider.removeGeofences(createNotificationBroadcastPendingIntent()).subscribe(new Action1<Status>() {
            @Override
            public void call(Status status) {
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
            }
        });
    }

    /**
     * Uses <code>PugNotification</code> to ask the user to turn on GPS.
     */
    private void showNotification() {
        PugNotification.with(context).load().identifier(PUG_NOTI_ID).title("Turn on GPS").smallIcon(R.mipmap.ic_launcher)
                .message("Location data can't be recorded. (Please click to turn on GPS)")
                .autoCancel(true).click(ActivityPermission.class).simple().build();
    }

    /**
     * Removes a PugNotification
     */
    private void removeNotification() {
        PugNotification.with(context).cancel(PUG_NOTI_ID);
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    removeNotification();
                else {
                    Toasty.error(context, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                    showNotification();
                }
            }
        }
    };

    /**
     * Creates a new ArrayList of geofences from the current list of geofence data.
     *
     * @return A geofencing request
     */
    private GeofencingRequest createGeofencingRequest() {
        ArrayList<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < geoFenceData.getGeoFenceLocationInfos().size(); i++) {
            currentList.put(geoFenceData.getGeoFenceLocationInfos().get(i).getLocation(), null);
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(geoFenceData.getGeoFenceLocationInfos().get(i).getLocation())
                    .setCircularRegion(geoFenceData.getGeoFenceLocationInfos().get(i).getLatitude(),
                            geoFenceData.getGeoFenceLocationInfos().get(i).getLongitude(), RADIUS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            geofences.add(geofence);
        }
        return new GeofencingRequest.Builder().addGeofences(geofences).setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                GeofencingRequest.INITIAL_TRIGGER_EXIT).build();
    }

    // TODO: Does this need to be deleted??
    void getLastLocation() {
/*
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        locationProvider.getLastKnownLocation()
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        doSthImportantWithObtainedLocation(location);
                    }
                });
*/
    }

    /**
     * This class allows for better geofence optimization by using broadcasts to receive geofence
     * transitions instead of a service.
     */
    public class GeofenceBroadcastReceiver extends BroadcastReceiver {
        /**
         * Triggered by a geofencing event.
         *
         * <p>
         * When <code>GeofenceBroadcastReceiver</code> receives a new broadcast, it determines if the
         * generating event was an enter or exit transition and generates a list of triggered geofences.
         * This resulting list is iterated through, data sources are built out of the locations and
         * sent to dataKitAPI.
         *</p>
         *
         * @param context Android context
         * @param intent Android intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            GeofencingEvent event = GeofencingEvent.fromIntent(intent);
            boolean result;
            if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER)
                result = true;
            else if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT)
                result = false;
            else return;
            List<Geofence> list = event.getTriggeringGeofences();
            for (int i = 0; i < list.size(); i++) {
                try {
                    String location = list.get(i).getRequestId();
                    DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSourceClient.getDataSource()).setId(location);
                    DataSourceClient dataSourceClient = dataKitAPI.register(dataSourceBuilder);
                    if (!result) {
                        dataKitAPI.insert(dataSourceClient, new DataTypeString(DateTime.getDateTime(), "Exit"));
                        Log.d(TAG, "location=" + location + " status=EXIT");
                        currentList.put(location, null);
                    } else {
                        dataKitAPI.insert(dataSourceClient, new DataTypeString(DateTime.getDateTime(), "Enter"));
                        Log.d(TAG, "location=" + location + " status=ENTER");
                        currentList.put(location, dataSourceClient);

                    }
                } catch (DataKitException ignored) {
                    Log.w(TAG, "error 1");
                }
            }
        }
    }

}
