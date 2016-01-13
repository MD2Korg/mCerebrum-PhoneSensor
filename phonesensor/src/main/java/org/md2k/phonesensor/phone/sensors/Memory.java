package org.md2k.phonesensor.phone.sensors;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.datatype.DataTypeFloatArray;
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
public class Memory extends PhoneSensorDataSource {
    private Handler scheduler;
    HashMap<String,String> createDataDescriptor(String name, String frequency, String description, int minValue,int maxValue,String unit){
        HashMap<String,String> dataDescriptor=new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.MIN_VALUE, String.valueOf(minValue));
        dataDescriptor.put(METADATA.MAX_VALUE, String.valueOf(maxValue));
        dataDescriptor.put(METADATA.UNIT, unit);
        dataDescriptor.put(METADATA.FREQUENCY,frequency);
        dataDescriptor.put(METADATA.DESCRIPTION,description);
        dataDescriptor.put(METADATA.DATA_TYPE,float.class.getName());
        return dataDescriptor;
    }
    ArrayList<HashMap<String,String>> createDataDescriptors(){
        ArrayList<HashMap<String,String>> dataDescriptors= new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Size",frequency,"Size of the memory",0,2048,"megabyte"));
        dataDescriptors.add(createDataDescriptor("Available",frequency,"Available memory",0,2048, "megabyte"));
        return dataDescriptors;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = super.createDataSourceBuilder();
        if (dataSourceBuilder == null) return null;
        dataSourceBuilder=dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Memory");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures usage of memory");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeFloatArray.class.getName());
        return dataSourceBuilder;
    }


    public Memory(Context context) {
        super(context, DataSourceType.MEMORY);
        frequency = "1.0 Hz";
    }


    public void unregister() {
        scheduler.removeCallbacks(statusMemory);
        scheduler = null;
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) {
        super.register(dataSourceBuilder, newCallBack);
        scheduler = new Handler();
        scheduler.post(statusMemory);
    }

    private float[] readUsage() {
        float[] samples = new float[2];
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        samples[0] = (float) mi.totalMem/(float)(1024*1024);
        samples[1] = (float)mi.availMem/(float)(1024*1024);
        return samples;
    }

    private final Runnable statusMemory = new Runnable() {

        @Override
        public void run() {
            float[] samples = readUsage();
            DataTypeFloatArray dataTypeFloatArray = new DataTypeFloatArray(DateTime.getDateTime(), samples);
            dataKitHandler.insert(dataSourceClient, dataTypeFloatArray);
            callBack.onReceivedData(dataTypeFloatArray);
            scheduler.postDelayed(statusMemory,1000);
        }
    };
}
