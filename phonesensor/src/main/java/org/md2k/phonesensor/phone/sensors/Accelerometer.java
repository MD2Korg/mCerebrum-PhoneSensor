package org.md2k.phonesensor.phone.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
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
public class Accelerometer extends PhoneSensorDataSource implements SensorEventListener {
    private static final String SENSOR_DELAY_NORMAL = "SENSOR_DELAY_NORMAL";
    private static final String SENSOR_DELAY_UI = "SENSOR_DELAY_UI";
    private static final String SENSOR_DELAY_GAME = "SENSOR_DELAY_GAME";
    private static final String SENSOR_DELAY_FASTEST = "SENSOR_DELAY_FASTEST";
    public static final String[] frequencyOptions = {SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI, SENSOR_DELAY_GAME, SENSOR_DELAY_FASTEST};
    long lastSaved=DateTime.getDateTime();
    double FILTER_DATA_MIN_TIME;
    private SensorManager mSensorManager;

    public Accelerometer(Context context) {
        super(context, DataSourceType.ACCELEROMETER);
        frequency = SENSOR_DELAY_UI;
    }

    HashMap<String, String> createDataDescriptor(String name, String frequency, String description) {
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.MIN_VALUE, "-20");
        dataDescriptor.put(METADATA.MAX_VALUE, "20");
        dataDescriptor.put(METADATA.UNIT, "meter/second^2");
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
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "meter/second^2");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the acceleration applied to the device");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeFloatArray.class.getName());
        return dataSourceBuilder;
    }

    public void updateDataSource(DataSource dataSource) {
        super.updateDataSource(dataSource);
        frequency = dataSource.getMetadata().get(METADATA.FREQUENCY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = DateTime.getDateTime();
        if ((double)(curTime - lastSaved) > FILTER_DATA_MIN_TIME) {
            lastSaved = curTime;
            double[] samples = new double[3];
            samples[0] = event.values[0];
            samples[1] = event.values[1];
            samples[2] = event.values[2];
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(curTime, samples);
            try {
                dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
            } catch (DataKitException e) {
                try {
                    reconnect();
                } catch (DataKitException e1) {
                    e1.printStackTrace();
                }
            }
            callBack.onReceivedData(dataTypeDoubleArray);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        switch (frequency) {
            case SENSOR_DELAY_UI:
                FILTER_DATA_MIN_TIME = 1000.0 / (16.0 + EPSILON_UI);
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
                break;
            case SENSOR_DELAY_GAME:
                FILTER_DATA_MIN_TIME = 1000.0 / (50.0 + EPSILON_GAME);
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

                break;
            case SENSOR_DELAY_FASTEST:
                FILTER_DATA_MIN_TIME = 1000.0 / (100.0 + EPSILON_FASTEST);
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

                break;
            case SENSOR_DELAY_NORMAL:
                FILTER_DATA_MIN_TIME = 1000.0 / (6.0 + EPSILON_NORMAL);
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                break;
        }
    }
}
