package org.md2k.phonesensor.phone.sensors;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.DataFormat;
import org.md2k.utilities.data_format.ResultType;

import java.util.ArrayList;
import java.util.HashMap;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observer;
import rx.Subscription;

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
class ActivityType extends PhoneSensorDataSource {
    private static final String TAG = ActivityType.class.getSimpleName();
    private ReactiveLocationProvider locationProvider;
    private Subscription subscription;

    ActivityType(Context context) {
        super(context, DataSourceType.ACTIVITY_TYPE);
        locationProvider = new ReactiveLocationProvider(context);
        frequency = "1.0";
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
            subscription = locationProvider.getDetectedActivity(0)
                    .subscribe(new Observer<ActivityRecognitionResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(context,"!!! Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onNext(ActivityRecognitionResult activityRecognitionResult) {
                            saveData(activityRecognitionResult.getMostProbableActivity());
                        }
                    });
        }catch (Exception e){
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
        }
    }

    @Override
    public void unregister() {
        try {
            if(subscription!=null && !subscription.isUnsubscribed())
                subscription.unsubscribe();
        }catch (Exception ignored){

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
    private void saveData(DetectedActivity mostProbableActivity){
        double samples[] = new double[2];
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
