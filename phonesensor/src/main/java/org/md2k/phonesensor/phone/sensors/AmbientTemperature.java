package org.md2k.phonesensor.phone.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
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
public class AmbientTemperature extends PhoneSensorDataSource implements SensorEventListener {
    private SensorManager mSensorManager;

    public AmbientTemperature(Context context) {
        super(context, DataSourceType.AMBIENT_TEMPERATURE);
        frequency = "1.0 Hz";
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Ambient Temperature");
        dataDescriptor.put(METADATA.MIN_VALUE, "-50");
        dataDescriptor.put(METADATA.MAX_VALUE, "100");
        dataDescriptor.put(METADATA.UNIT, "degree celsius");
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DESCRIPTION, "ambient (room) temperature in degree Celsius");
        dataDescriptor.put(METADATA.DATA_TYPE, float.class.getSimpleName());

        dataDescriptors.add(dataDescriptor);
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Ambient Temperature");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "degree celsius");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "ambient (room) temperature in degree Celsius");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeFloat.class.getName());
        return dataSourceBuilder;
    }

    public void updateDataSource(DataSource dataSource) {
        super.updateDataSource(dataSource);

        frequency = dataSource.getMetadata().get(METADATA.FREQUENCY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double[] sample = new double[1];
        sample[0] = event.values[0];
        DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), sample);
        try {
            dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
        } catch (DataKitException e) {
            try {
                unregister();
                reconnect();
                dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
            } catch (DataKitException e1) {
                Toast.makeText(context, "Reconnection Error", Toast.LENGTH_LONG).show();
                e1.printStackTrace();
            }
        }
        callBack.onReceivedData(dataTypeDoubleArray);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
