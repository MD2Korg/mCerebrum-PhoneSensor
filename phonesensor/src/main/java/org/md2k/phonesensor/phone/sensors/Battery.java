package org.md2k.phonesensor.phone.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.widget.Toast;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
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
public class Battery extends PhoneSensorDataSource {
    private Handler scheduler;
    private final Runnable batteryStatus = new Runnable() {

        @Override
        public void run() {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = context.registerReceiver(null, iFilter);
            assert intent != null;
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
            float percentage;
            if (level == -1 || scale == -1) {
                percentage = 0.0f;
            } else {
                percentage = ((float) level / (float) scale) * 100.0f;
            }
            double samples[] = new double[3];
            samples[0] = percentage;
            samples[1] = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            samples[2] = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
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
            if (scheduler != null) {
                scheduler.postDelayed(batteryStatus, 1000);
            }
        }
    };

    public Battery(Context context) {
        super(context, DataSourceType.BATTERY);
        frequency = "1.0 Hz";
    }

    HashMap<String,String> createDataDescriptor(String name, String frequency, String description, int minValue,int maxValue,String unit){
        HashMap<String,String> dataDescriptor=new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.MIN_VALUE, String.valueOf(minValue));
        dataDescriptor.put(METADATA.MAX_VALUE, String.valueOf(maxValue));
        dataDescriptor.put(METADATA.UNIT, unit);
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DESCRIPTION, description);
        dataDescriptor.put(METADATA.DATA_TYPE, float.class.getName());
        return dataDescriptor;
    }

    ArrayList<HashMap<String,String>> createDataDescriptors(){
        ArrayList<HashMap<String,String>> dataDescriptors= new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Level",frequency,"current battery charge",0,100,"percentage"));
        dataDescriptors.add(createDataDescriptor("Voltage",frequency,"current battery voltage level",0,5000, "voltage"));
        dataDescriptors.add(createDataDescriptor("Temperature",frequency,"current battery temperature",-50,100, "celsius"));
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder=dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Battery");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the current status of the battery");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeFloatArray.class.getName());
        return dataSourceBuilder;
    }

    public void unregister() {
        if (scheduler != null) {
            scheduler.removeCallbacks(batteryStatus);
            scheduler = null;
        }
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        scheduler=new Handler();
        scheduler.post(batteryStatus);
    }
}
