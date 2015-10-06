package org.md2k.phonesensor.phone.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;

import org.md2k.datakitapi.DataKitApi;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.BCMRecord;
import org.md2k.phonesensor.Constants;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.utilities.Report.Log;

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
    private static final String TAG = Battery.class.getSimpleName();
    Handler scheduler;


    public Battery(Context context, boolean enabled) {
        super(context, DataSourceType.BATTERY, enabled);
        frequency="1.0 Hz";
    }


    public void unregister() {
        Log.d(TAG, "Battery(): unregister()");
        scheduler.removeCallbacks(batteryStatus);
        scheduler=null;

//        context.unregisterReceiver(batteryInfoReceiver);
    }

    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) {
        Log.d(TAG, "Battery(): register()");
        super.register(dataSourceBuilder, newCallBack);
        scheduler=new Handler();
        scheduler.post(batteryStatus);
    }
    private Runnable batteryStatus=new Runnable(){

        @Override
        public void run() {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = context.registerReceiver(null, iFilter);
            int  level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int  scale= intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
            float percentage;
            if (level == -1 || scale == -1) {
                percentage=0.0f;
            }
            else{
                percentage=((float) level / (float) scale) * 100.0f;
            }
            double samples[]=new double[3];
            samples[0]=percentage;
            samples[1]=intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
            samples[2]=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
            DataTypeDoubleArray dataTypeDoubleArray=new DataTypeDoubleArray(DateTime.getDateTime(),samples);
            dataKitHandler.insert(dataSourceClient, dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
            scheduler.postDelayed(batteryStatus, 1000);
            BCMRecord.getInstance().saveDataToTextFile(DataSourceType.BATTERY, dataTypeDoubleArray);
        }
    };
}
