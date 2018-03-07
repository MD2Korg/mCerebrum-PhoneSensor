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
import android.content.IntentFilter;
import android.os.BatteryManager;
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
import org.md2k.mcerebrum.core.data_format.DataFormat;;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class creates a battery sensor which gathers battery temperature, voltage,
 * and charge percentage data.
 *
 * <p>
 *     The default sampling rate for this sensor is 1 hertz.
 * </p>
 */
public class Battery extends PhoneSensorDataSource {
    /**
     * Sample rate in milliseconds
     *
     * <p>
     *     Default is 1000 milliseconds.
     * </p>
     */
    public static final int SAMPLE_MILLIS = 1000;
    private Handler scheduler;
    private final Runnable batteryStatus = new Runnable() {

        /**
         * When a batteryStatus thread is created, this <code>run</code> method puts the battery
         * data into an array and sends it to dataKitAPI.
         */
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
            samples[DataFormat.Battery.Parcentage] = percentage;
            samples[DataFormat.Battery.Voltage] = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            samples[DataFormat.Battery.Temperature] = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            try {
                dataKitAPI.insert(dataSourceClient, dataTypeDoubleArray);
                callBack.onReceivedData(dataTypeDoubleArray);
                scheduler.postDelayed(batteryStatus, SAMPLE_MILLIS);
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
    public Battery(Context context) {
        super(context, DataSourceType.BATTERY);
        frequency = "1.0";
    }

    /**
     * Unregisters the listener for this sensor
     */
    public void unregister() {
        if (scheduler != null) {
            scheduler.removeCallbacks(batteryStatus);
            scheduler = null;
        }
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then schedules the batteryStatus thread
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        scheduler = new Handler();
        scheduler.post(batteryStatus);
    }
}
