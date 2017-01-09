package org.md2k.phonesensor.phone.sensors;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.phone.CallBack;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
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
public class DetectActivity1 extends PhoneSensorDataSource{
    private static final String SENSOR_DELAY_NORMAL = "6";
    private static final String SENSOR_DELAY_UI = "16";
    private static final String SENSOR_DELAY_GAME = "50";
    private static final String SENSOR_DELAY_FASTEST = "100";
    public static final String[] frequencyOptions = {SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI, SENSOR_DELAY_GAME, SENSOR_DELAY_FASTEST};
    long lastSaved=DateTime.getDateTime();
    double FILTER_DATA_MIN_TIME;
    private GoogleApiClient mApiClient;

    public DetectActivity1(Context context) {
        super(context, DataSourceType.ACCELEROMETER);
        frequency = SENSOR_DELAY_UI;
    }

    HashMap<String, String> createDataDescriptor(String name, String frequency, String description) {
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.MIN_VALUE, "-5");
        dataDescriptor.put(METADATA.MAX_VALUE, "+5");
        dataDescriptor.put(METADATA.UNIT, "g");
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DESCRIPTION, description);
        dataDescriptor.put(METADATA.DATA_TYPE, float.class.getName());
        return dataDescriptor;
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Accelerometer X", frequency, "Acceleration minus Gx on the x-axis"));
        dataDescriptors.add(createDataDescriptor("Accelerometer Y", frequency, "Acceleration minus Gy on the y-axis"));
        dataDescriptors.add(createDataDescriptor("Accelerometer Z", frequency, "Acceleration minus Gz on the z-axis"));
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Accelerometer");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "g");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the acceleration applied to the device");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeFloatArray.class.getName());
        return dataSourceBuilder;
    }

    public void updateDataSource(DataSource dataSource) {
        super.updateDataSource(dataSource);
        frequency = dataSource.getMetadata().get(METADATA.FREQUENCY);
    }
/*
    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = DateTime.getDateTime();
        if ((double)(curTime - lastSaved) > FILTER_DATA_MIN_TIME) {
            lastSaved = curTime;
            double[] samples = new double[3];
            samples[0] = event.values[0]/9.81;
            samples[1] = event.values[1]/9.81;
            samples[2] = event.values[2]/9.81;
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(curTime, samples);
            try {
                dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
                callBack.onReceivedData(dataTypeDoubleArray);
            } catch (DataKitException e) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
*/
    public void unregister() {
        if(mApiClient!=null)
            mApiClient.disconnect();
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
//                        Intent intent = new Intent( this, ActivityRecognizedService.class );
//                        PendingIntent pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
//                        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();
        mApiClient.connect();
    }
}