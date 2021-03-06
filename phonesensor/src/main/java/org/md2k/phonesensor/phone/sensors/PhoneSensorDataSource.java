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
import android.util.Log;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.phonesensor.Configuration;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.phonesensor.phone.PhoneSensorPlatform;

import java.util.ArrayList;

/**
 * This class handles the creation of <code>PhoneSensorDataSource</code> objects.
 *
 * <p>
 *     Each sensor makes one of these objects so it can interact with <code>dataKitAPI</code>.
 *
 *     The data sources that inherit from this superclass are:
 *     <ul>
 *         <li><code>Accelerometer</code></li>
 *         <li><code>ActivityType</code></li>
 *         <li><code>AmbientLight</code></li>
 *         <li><code>AmbientTemperature</code></li>
 *         <li><code>Battery</code></li>
 *         <li><code>Compass</code></li>
 *         <li><code>CPU</code></li>
 *         <li><code>GeoFence</code></li>
 *         <li><code>Gyroscope</code></li>
 *         <li><code>LocationFused</code></li>
 *         <li><code>Memory</code></li>
 *         <li><code>Pressure</code></li>
 *         <li><code>Proximity</code></li>
 *         <li><code>StepCount</code></li>
 *         <li><code>TouchScreen</code></li>
 *     </ul>
 * </p>
 */
public abstract class PhoneSensorDataSource {

    /**
     * <code>EPSILON_NORMAL</code> is 2.0 by default
     */
    public static final double EPSILON_NORMAL = 2.0;

    /**
     * <code>EPSILON_UI</code> is 5.0 by default
     */
    public static final double EPSILON_UI = 5.0;

    /**
     * <code>EPSILON_GAME</code> is 10.0 by default
     */
    public static final double EPSILON_GAME = 10.0;

    /**
     * <code>EPSILON_FASTEST</code> is 50.0 by default
     */
    public static final double EPSILON_FASTEST = 50.0;
    
    private static final String TAG = PhoneSensorDataSource.class.getSimpleName();
    final Context context;
    private final String dataSourceType;
    DataSourceClient dataSourceClient;
    CallBack callBack;
    String frequency="SENSOR_DELAY_UI";
    DataKitAPI dataKitAPI;
    private boolean enabled;

    /**
     * Constructor
     *
     * @param context Android context
     * @param dataSourceType Type of the data source.
     */
    PhoneSensorDataSource(Context context, String dataSourceType) {
        this.context = context;
        this.dataSourceType = dataSourceType;
        this.enabled = false;
    }

    /**
     * @return the dataSourceType field
     */
    public String getDataSourceType() {
        return dataSourceType;
    }

    /**
     * @return whether or not this data source is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled field to the desired boolean state
     *
     * @param enabled whether the data source should be enabled or not
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets enabled field to true when passed a DataSource object
     *
     * @param dataSource dataSource that should be updated
     */
    public void updateDataSource(DataSource dataSource){
        enabled=true;
    }

    /**
     * @return the frequency field value
     */
    public String getFrequency(){
        return frequency;
    }

    /**
     * @param frequency the frequency this object should be set to
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /**
     * @return null if not enabled and a new DataSourceBuilder object otherwise
     */
    DataSourceBuilder createDataSourceBuilder() {
        if (!enabled) return null;
        DataSource dataSource  = Configuration.getMetaData(dataSourceType);
        return new DataSourceBuilder(dataSource).setType(dataSourceType).setMetadata(METADATA.FREQUENCY, frequency);
    }


    /**
     * Registers the given dataSourceBuilder object with dataKitAPI
     *
     * @param dataSourceBuilder data source to be registered with dataKitAPI
     * @param newCallBack CallBack object
     * @throws DataKitException
     */
    public void register(DataSourceBuilder dataSourceBuilder, CallBack newCallBack) throws DataKitException {
        dataKitAPI = DataKitAPI.getInstance(context);
        dataSourceClient = dataKitAPI.register(dataSourceBuilder);
        callBack = newCallBack;
    }

    /**
     * Unregisters the listener for this sensor
     */
    public abstract void unregister();
}
