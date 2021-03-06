/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
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

package org.md2k.phonesensor.phone.sensors;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class creates a memory sensor which gathers memory use data.
 *
 * <p>
 *     The default sampling rate for this sensor is 1 hertz.
 * </p>
 */
public class Memory extends PhoneSensorDataSource {

    /**
     * Sample rate in milliseconds
     *
     * <p>
     *     Default is 1000 milliseconds.
     * </p>
     */
    public static final int SAMPLE_MILLIS = 1000;
    private Handler scheduler;
    private final Runnable statusMemory = new Runnable() {

        /**
         * When a statusMemory thread is created, this <code>run</code> method calls <code>readUsage</code>
         * and sends the resulting array to dataKitAPI.
         */
        @Override
        public void run() {
            double[] samples = readUsage();
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            try {
                dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
                callBack.onReceivedData(dataTypeDoubleArray);
                scheduler.postDelayed(statusMemory, SAMPLE_MILLIS);
            } catch (DataKitException e) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
            }
        }
    };

    /**
     * Constructor
     *
     * @param context Android context
     */
    public Memory(Context context) {
        super(context, DataSourceType.MEMORY);
        frequency = "1.0";
    }


    /**
     * Unregisters this sensor by removing the callbacks and setting the Handler to null.
     */
    public void unregister() {
        if (scheduler != null) {
            scheduler.removeCallbacks(statusMemory);
            scheduler = null;
        }
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then schedules the statusMemory thread.
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        scheduler = new Handler();
        scheduler.post(statusMemory);
    }

    /**
     * Reads the total and available memory of the device and stores those values in an array
     *
     * @return The array of memory data
     */
    private double[] readUsage() {
        double[] samples = new double[2];
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        samples[0] = (double) mi.totalMem / (double) (1024 * 1024);
        samples[1] = (double) mi.availMem / (double) (1024 * 1024);
        return samples;
    }
}
