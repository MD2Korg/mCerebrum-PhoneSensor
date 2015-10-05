package org.md2k.phonesensor.phone.sensors;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.Constants;
import org.md2k.phonesensor.phone.CallBack;
import org.md2k.utilities.Files;
import org.md2k.utilities.Report.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
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

public class PhoneSensorDataSources {
    private static final String TAG = PhoneSensorDataSources.class.getSimpleName();
    protected Context context;

    public ArrayList<PhoneSensorDataSource> getPhoneSensorDataSources() {
        return phoneSensorDataSources;
    }

    ArrayList<PhoneSensorDataSource> phoneSensorDataSources;

    public PhoneSensorDataSources(Context context) {
        this.context = context;
        phoneSensorDataSources = new ArrayList<>();
        phoneSensorDataSources.add(new Battery(context, false));
        phoneSensorDataSources.add(new LocationFused(context, false));
        phoneSensorDataSources.add(new Accelerometer(context, false));
        phoneSensorDataSources.add(new Gyroscope(context, false));
        phoneSensorDataSources.add(new Compass(context, false));
        phoneSensorDataSources.add(new Light(context, false));
        phoneSensorDataSources.add(new Pressure(context, false));
        phoneSensorDataSources.add(new AmbientTemperature(context, false));
        phoneSensorDataSources.add(new Proximity(context, false));
        phoneSensorDataSources.add(new CPU(context, false));
        phoneSensorDataSources.add(new Memory(context, false));

        try {
            readDataSourceFromFile();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "PhoneSensor Configuration file is not available.", Toast.LENGTH_LONG).show();
        }
    }

    private void readDataSourceFromFile() throws FileNotFoundException {
        ArrayList<DataSource> dataSources = Files.readDataSourceFromFile(Constants.DIR_FILENAME);
        Log.d(TAG, "length=" + dataSources.size());
        for (int i = 0; i < dataSources.size(); i++) {
            PhoneSensorDataSource phoneSensorDataSource = find(dataSources.get(i).getType());
            if (phoneSensorDataSource == null) continue;
            phoneSensorDataSource.updateDataSource(dataSources.get(i));
        }
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < phoneSensorDataSources.size(); i++) {
            if (phoneSensorDataSources.get(i).isEnabled())
                count++;
        }

        return count;
    }

    public PhoneSensorDataSource find(String type) {
        for (int i = 0; i < phoneSensorDataSources.size(); i++) {
            if (phoneSensorDataSources.get(i).getDataSourceType().equals(type))
                return phoneSensorDataSources.get(i);
        }
        return null;
    }

    public void writeDataSourceToFile() throws IOException {
        ArrayList<DataSource> dataSources = new ArrayList<DataSource>();
        if (phoneSensorDataSources == null) throw new NullPointerException();
        if (phoneSensorDataSources.size() == 0) throw new EmptyStackException();
        Log.d(TAG, "size=" + phoneSensorDataSources.size());

        for (int i = 0; i < phoneSensorDataSources.size(); i++) {
            if (!phoneSensorDataSources.get(i).isEnabled()) continue;
            DataSource dataSource = phoneSensorDataSources.get(i).createDataSourceBuilder().build();
            if (dataSource == null) continue;
            dataSources.add(dataSource);
        }
        Files.writeDataSourceToFile(Constants.DIRECTORY, Constants.FILENAME, dataSources);
    }

    HashMap<String, Integer> hm = new HashMap<String, Integer>();
    long starttimestamp = 0;

    public void register() {
        hm.clear();
        starttimestamp = DateTime.getDateTime();
        for (int i = 0; i < phoneSensorDataSources.size(); i++) {
            if (!phoneSensorDataSources.get(i).isEnabled()) continue;
            final DataSourceBuilder dataSourceBuilder = phoneSensorDataSources.get(i).createDataSourceBuilder();
            if (dataSourceBuilder == null) continue;
            final int finalI = i;
            Intent intent = new Intent("phonesensor");
            intent.putExtra("operation", "register");
            intent.putExtra("datasource", dataSourceBuilder.build());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            phoneSensorDataSources.get(i).register(dataSourceBuilder, new CallBack() {
                @Override
                public void onReceivedData(DataType data) {
                    String dataSourceType = phoneSensorDataSources.get(finalI).getDataSourceType();
                    Intent intent = new Intent("phonesensor");
                    intent.putExtra("operation", "data");
                    if (!hm.containsKey(dataSourceType)) {
                        hm.put(dataSourceType, 0);
                    }
                    hm.put(dataSourceType, hm.get(dataSourceType) + 1);
                    intent.putExtra("count", hm.get(dataSourceType));
                    intent.putExtra("timestamp", data.getDateTime());
                    intent.putExtra("starttimestamp", starttimestamp);
                    intent.putExtra("data", data);
                    intent.putExtra("datasource", dataSourceBuilder.build());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });
        }
    }

    public void unregister() {
        hm.clear();
        for (int i = 0; i < phoneSensorDataSources.size(); i++) {
            if (!phoneSensorDataSources.get(i).isEnabled()) continue;
            phoneSensorDataSources.get(i).unregister();
        }
    }
}
