package org.md2k.phonesensor.phone.sensors;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.Constants;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.utilities.data_format.DataFormat;
import org.md2k.utilities.data_format.ResultType;

import java.util.ArrayList;
import java.util.HashMap;

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
class ActivityType extends PhoneSensorDataSource implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final String TAG = ActivityType.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private static final int INTERVAL_MILLIS = 1000;

    ActivityType(Context context) {
        super(context, DataSourceType.ACTIVITY_TYPE);
        frequency = "1.0";
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
    }

    private HashMap<String, String> createDataDescriptor(String name, String frequency, String description) {
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DESCRIPTION, description);
        dataDescriptor.put(METADATA.DATA_TYPE, double.class.getName());
        return dataDescriptor;
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Activity Type", frequency, "Represents types of activity (0: STILL,1: ON_FOOT, 2:TILTING, 3: WALKING, 4: RUNNING, 5: ON_BICYCLE, 6: IN_VEHICLE, 7: UNKNOWN"));
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Activity Type");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents types of activity (0: STILL,1: ON_FOOT, 2:TILTING, 3: WALKING, 4: RUNNING, 5: ON_BICYCLE, 6: IN_VEHICLE, 7: UNKNOWN");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeDoubleArray.class.getName());
        return dataSourceBuilder;
    }

    @Override
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        try {
            super.register(dataSourceBuilder, newCallBack);
            LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(Constants.BROADCAST_ACTION));
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(ActivityRecognition.API)
                    .build();
            mGoogleApiClient.connect();
        }catch (Exception e){

        }
    }

    @Override
    public void unregister() {
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
            if (mGoogleApiClient != null) {
                com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                        mGoogleApiClient,
                        getActivityDetectionPendingIntent()
                ).setResultCallback(this);
                mGoogleApiClient.disconnect();
            }
        }catch (Exception e){

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                INTERVAL_MILLIS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(ActivityType.this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Success: " + status.getStatusMessage());
        } else {
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }

    }

    private int getActivityType(int type) {
        switch (type) {
            case DetectedActivity.STILL:
                return ResultType.ActivityType.STILL;
            case DetectedActivity.ON_FOOT:
                return ResultType.ActivityType.ON_FOOT;
            case DetectedActivity.TILTING:
                return ResultType.ActivityType.TILTING;
            case DetectedActivity.WALKING:
                return ResultType.ActivityType.WALKING;
            case DetectedActivity.RUNNING:
                return ResultType.ActivityType.RUNNING;
            case DetectedActivity.ON_BICYCLE:
                return ResultType.ActivityType.ON_BICYCLE;
            case DetectedActivity.IN_VEHICLE:
                return ResultType.ActivityType.IN_VEHICLE;
            default:
                return ResultType.ActivityType.UNKNOWN;
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double samples[] = new double[2];
            DetectedActivity mostProbableActivity = intent.getParcelableExtra(Constants.ACTIVITY_EXTRA);
            samples[DataFormat.ActivityType.Confidence] = mostProbableActivity.getConfidence();
            samples[DataFormat.ActivityType.Type] = getActivityType(mostProbableActivity.getType());
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            try {
                dataKitAPI.insert(dataSourceClient, dataTypeDoubleArray);
                callBack.onReceivedData(dataTypeDoubleArray);
            } catch (DataKitException e) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
            }
        }
    }
}
