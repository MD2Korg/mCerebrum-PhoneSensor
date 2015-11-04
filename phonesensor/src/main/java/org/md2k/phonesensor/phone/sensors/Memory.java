package org.md2k.phonesensor.phone.sensors;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.BCMRecord;
import org.md2k.phonesensor.phone.CallBack;

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


    public Memory(Context context) {
        super(context, DataSourceType.MEMORY);
        frequency = "1.0 Hz";
    }


    public void unregister() {
        scheduler.removeCallbacks(statusMemory);
        scheduler = null;

//        context.unregisterReceiver(batteryInfoReceiver);
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
            BCMRecord.getInstance().saveDataToTextFile(DataSourceType.MEMORY, dataTypeFloatArray);
            callBack.onReceivedData(dataTypeFloatArray);
            scheduler.postDelayed(statusMemory,1000);
        }
    };
}
