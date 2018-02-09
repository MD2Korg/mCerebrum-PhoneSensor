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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;

/**
 * This class handles the pedometer sensor.
 */
public class StepCount extends PhoneSensorDataSource implements SensorEventListener {
    private static final String SENSOR_DELAY_NORMAL = "6";
    private static final String SENSOR_DELAY_UI = "16";
    private static final String SENSOR_DELAY_GAME = "50";
    private static final String SENSOR_DELAY_FASTEST = "100";
    private static final String TAG = StepCount.class.getSimpleName();

    /** Array of sampling rates for the sensor
     *
     * <p>
     * <ul>
     *     <li><code>SENSOR_DELAY_NORMAL</code> is 6 hertz</li>
     *     <li><code>SENSOR_DELAY_UI</code> is 16 hertz</li>
     *     <li><code>SENSOR_DELAY_GAME</code> is 50 hertz</li>
     *     <li><code>SENSOR_DELAY_FASTEST</code> is 100 hertz</li>
     * </ul>
     * </p>
     */
    public static final String[] frequencyOptions = {SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI, SENSOR_DELAY_GAME, SENSOR_DELAY_FASTEST};
    private final static long MICROSECONDS_IN_ONE_SECOND = 1000*1000*60L;
    private SensorManager mSensorManager;
    private int prevTotalStep =0;

    /**
     * Constructor
     *
     * @param context Android context
     */
    public StepCount(Context context) {
        super(context, DataSourceType.STEP_COUNT);
        frequency = SENSOR_DELAY_NORMAL;
    }

    /**
     * Changes the frequency field to match the frequency field in the metadata of the new source
     *
     * @param dataSource dataSource that should be updated
     */
    public void updateDataSource(DataSource dataSource) {
        super.updateDataSource(dataSource);
        frequency = dataSource.getMetadata().get(METADATA.FREQUENCY);
    }

    /**
     * Called when there is a new sensor event. This can be a data change or a timestamp change.
     *
     * <p>
     * The data is put into an array and sent to dataKitAPI to be saved.
     *</p>
     *
     * @param event event that triggered the method call
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int curTotalSteps = (int) event.values[0];
        int curSteps=0;
        if(prevTotalStep ==0){
            prevTotalStep =curTotalSteps;
            return;
        }else{
            curSteps = curTotalSteps- prevTotalStep;
            prevTotalStep =curTotalSteps;
        }
        Log.d(TAG, "total steps = "+ curTotalSteps+ "  steps=" + curSteps);
        long curTime = DateTime.getDateTime();
        double[] samples = new double[]{curSteps};
        DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(curTime, samples);

        try {
            dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
            dataKitAPI.setSummary(dataSourceClient, new DataTypeIntArray(curTime, new int[]{curSteps}));
            callBack.onReceivedData(dataTypeDoubleArray);
        } catch (DataKitException e) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
        }
    }

    /**
     * Called when the accuracy of this sensor changes.
     *
     * @param sensor sensor object for this sensor
     * @param accuracy Accuracy of the sensor reading
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Unregisters the listener for this sensor
     */
    public void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then registers this sensor with Android's SensorManager
     *
     * <p>
     * This method also sets a minimum amount of time between data saves based upon the frequency
     * field of this object.
     * </p>
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);


        switch (frequency) {
            case SENSOR_DELAY_UI:
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL, (int) (MICROSECONDS_IN_ONE_SECOND));
                break;
            case SENSOR_DELAY_GAME:
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL, (int) (MICROSECONDS_IN_ONE_SECOND));

                break;
            case SENSOR_DELAY_FASTEST:
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL, (int) (MICROSECONDS_IN_ONE_SECOND));

                break;
            case SENSOR_DELAY_NORMAL:
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL, (int) (MICROSECONDS_IN_ONE_SECOND));
                break;
        }
    }
}
