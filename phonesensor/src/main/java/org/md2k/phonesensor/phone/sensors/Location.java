package org.md2k.phonesensor.phone.sensors;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.md2k.datakitapi.DataKitApi;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.phone.CallBack;

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
public class Location extends PhoneSensorDataSource implements
        LocationListener {

    private static final String TAG = Location.class.getSimpleName();
    private LocationManager locationManager;
    private static final long INTERVAL = 1000L;
    private static final long FASTEST_INTERVAL = 1000L;
//    public static String[] frequencyOptions={"5 Second","30 Second","60 Second","5 Minutes"};

    public Location(Context context, boolean enabled) {
        super(context, DataSourceType.LOCATION, enabled);

    }
    protected void createLocationRequest() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000,1, this);
        }
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        double samples[]=new double[6];
        samples[0]=location.getLatitude();
        samples[1]=location.getLongitude();
        samples[2]=location.getAltitude();
        samples[3]=location.getSpeed();
        samples[4]=location.getBearing();
        samples[5]=location.getAccuracy();
        DataTypeDoubleArray dataTypeDoubleArray=new DataTypeDoubleArray(DateTime.getDateTime(),samples);
        mDataKitApi.insert(dataSourceClient, dataTypeDoubleArray);
        callBack.onReceivedData(dataTypeDoubleArray);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void register(DataKitApi dataKitApi, DataSource dataSource, CallBack newCallBack) {
        mDataKitApi = dataKitApi;
        dataSourceClient = dataKitApi.register(dataSource).await();
        this.callBack = newCallBack;
        createLocationRequest();
    }

    @Override
    public void unregister() {
        locationManager.removeUpdates(this);
    }
}
