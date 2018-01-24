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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class CPU extends PhoneSensorDataSource {
    private static final int DELAY_MILLIS = 1000;
    private Handler scheduler;
    private long[] curValues = new long[2];
    private final Runnable statusCPU = new Runnable() {

        /**
         * When a statusCPU thread is created, this <code>run</code> method puts the CPU
         * data into an array and sends it to dataKitAPI.
         *
         * This method posts a delayed statusCPU action to the handler. The delay is set
         * by a constant <code>DELAY_MILLIS</code>
         */
        @Override
        public void run() {
            long values[] = new long[2];
            readUsage(values);

            double[] sample = new double[1];
            sample[0] = (float) (values[1] - curValues[1]) / (float) ((values[1] + values[0]) - (curValues[1] + curValues[0]));

            curValues = values;
            DataTypeDoubleArray dataTypeDouble = new DataTypeDoubleArray(DateTime.getDateTime(), sample);
            try {
                dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDouble);
                callBack.onReceivedData(dataTypeDouble);
                scheduler.postDelayed(statusCPU, DELAY_MILLIS);
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
    public CPU(Context context) {
        super(context, DataSourceType.CPU);
        frequency = "1.0";
    }

    /**
     * Unregisters the listener for this sensor
     */
    public void unregister() {
        if (scheduler != null) {
            scheduler.removeCallbacks(statusCPU);
            scheduler = null;
        }
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then schedules the statusCPU thread
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        scheduler=new Handler();
        scheduler.post(statusCPU);
    }

    /**
     * Reads the CPU usage data from <code>/proc/stat</code> and stores it in an array.
     *
     * @param values
     */
    private void readUsage(long[] values) {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            values[0] = Long.parseLong(toks[4]);
            values[1] = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
