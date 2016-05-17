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
public class Compass extends PhoneSensorDataSource implements SensorEventListener {
    private static final String SENSOR_DELAY_NORMAL = "SENSOR_DELAY_NORMAL";
    private static final String SENSOR_DELAY_UI = "SENSOR_DELAY_UI";
    private static final String SENSOR_DELAY_GAME = "SENSOR_DELAY_GAME";
    private static final String SENSOR_DELAY_FASTEST = "SENSOR_DELAY_FASTEST";
    public static final String[] frequencyOptions = {SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI, SENSOR_DELAY_GAME, SENSOR_DELAY_FASTEST};
    private SensorManager mSensorManager;

    public Compass(Context context) {
        super(context, DataSourceType.COMPASS);
        frequency = SENSOR_DELAY_UI;
    }

    HashMap<String,String> createDataDescriptor(String name, String frequency, String description,int minValue,int maxValue){
        HashMap<String,String> dataDescriptor=new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.MIN_VALUE, String.valueOf(minValue));
        dataDescriptor.put(METADATA.MAX_VALUE,String.valueOf(maxValue));
        dataDescriptor.put(METADATA.UNIT, "degree");
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DESCRIPTION, description);
        dataDescriptor.put(METADATA.DATA_TYPE, float.class.getName());
        return dataDescriptor;
    }

    ArrayList<HashMap<String,String>> createDataDescriptors(){
        ArrayList<HashMap<String,String>> dataDescriptors= new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Compass X",frequency,"Azimuth, angle between the magnetic north direction and the y-axis, around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West",0,359));
        dataDescriptors.add(createDataDescriptor("Compass Y",frequency,"Pitch, rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis",-180,180));
        dataDescriptors.add(createDataDescriptor("Compass Z",frequency,"Roll, rotation around the y-axis (-90 to 90) increasing as the device moves clockwise",-90,90));
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder=dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Compass");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "degree");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the angles between x, y, and z axis");
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        switch (frequency) {
            case SENSOR_DELAY_UI:
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
                break;
            case SENSOR_DELAY_GAME:
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
                break;
            case SENSOR_DELAY_FASTEST:
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                break;
            case SENSOR_DELAY_NORMAL:
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                break;
        }
    }
}
