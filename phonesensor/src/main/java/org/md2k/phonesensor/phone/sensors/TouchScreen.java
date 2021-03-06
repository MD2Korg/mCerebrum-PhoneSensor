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
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.core.data_format.DataFormat;
import org.md2k.phonesensor.ServicePhoneSensor;
import org.md2k.phonesensor.phone.CallBack;

/**
 * This class handles the touchscreen interaction sensor.
 */
public class TouchScreen extends PhoneSensorDataSource implements View.OnTouchListener{
    // window manager
    private WindowManager mWindowManager;
    // linear layout will use to detect touch event
    private LinearLayout touchLayout;

    /**
     * Constructor
     *
     * @param context Android context
     */
    public TouchScreen(Context context) {
        super(context, DataSourceType.TOUCH_SCREEN);
        frequency = "ON_CHANGE";
    }

    /**
     * Changes the frequency field to match the frequency field in the metadata of the new source
     *
     * @param dataSource dataSource that should be updated
     */
    public void updateDataSource(DataSource dataSource){
        super.updateDataSource(dataSource);
        frequency=dataSource.getMetadata().get(METADATA.FREQUENCY);
    }

    /**
     * Puts time data into an array when the device has detected user interaction and sends that
     * data to dataKitAPI
     *
     * @param v Android view, actionable part of the UI
     * @param event Android MotionEvent, movement of the input device
     * @return Returns false if data is not successfully sent back to dataKitAPI
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        double[] sample = new double[1];
        double curTime=DateTime.getDateTime();
        sample[0]=curTime;
        DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), sample);
        try {
            dataKitAPI.insertHighFrequency(dataSourceClient, dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
        } catch (DataKitException e) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServicePhoneSensor.INTENT_STOP));
        }
        return false;
    }

    /**
     * Unregisters the listener for this sensor
     */
    public void unregister() {
        if (mWindowManager != null) {
            if (touchLayout != null) mWindowManager.removeView(touchLayout);
        }
    }

    /**
     * Calls <code>PhoneSensorDataSource.register</code> to register this sensor with dataKitAPI
     * and then registers this sensor with Android's SensorManager
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack       CallBack object
     * @throws DataKitException
     */
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        super.register(dataSourceBuilder, newCallBack);
        touchLayout = new LinearLayout(context);
        // set layout width 30 px and height is equal to full screen
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(30, LinearLayout.LayoutParams.MATCH_PARENT);
        touchLayout.setLayoutParams(lp);
        // set on touch listener
        touchLayout.setOnTouchListener(this);

        // fetch window manager object
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // set layout parameter of window manager
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                1, // width of layout 30 px
                1, // height is equal to full screen
                WindowManager.LayoutParams.TYPE_PHONE, // Type Phone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| // this window won't ever get key input focus
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,

                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.START | Gravity.TOP;

        mWindowManager.addView(touchLayout, mParams);
    }
}
