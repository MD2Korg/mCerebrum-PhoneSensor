package org.md2k.phonesensor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.commons.permission.Permission;
import org.md2k.mcerebrum.commons.debug.LogStorage;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;

import es.dmoral.toasty.Toasty;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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

public class ServicePhoneSensor extends Service {
    public static final String INTENT_STOP = "intent_stop";
    private static final String TAG = ServicePhoneSensor.class.getSimpleName();
    PhoneSensorDataSources phoneSensorDataSources = null;
    DataKitAPI dataKitAPI;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_stop_service");
            disconnectDataKit();
            stopSelf();
        }
    };

    public void onCreate() {
        super.onCreate();
        if (!Permission.hasPermission(this)) {
            Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
            stopSelf();
        } else {
            load();
        }
    }

    void load() {
        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_start");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_STOP));
        if (!readSettings()) {
            Toasty.error(this, "Error: not configured yet", Toast.LENGTH_SHORT).show();
            stopSelf();
        } else connectDataKit();
    }

    private void connectDataKit() {
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        try {
            dataKitAPI.connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    try {
                        phoneSensorDataSources.register();
                    } catch (Exception e) {
                        LocalBroadcastManager.getInstance(ServicePhoneSensor.this).sendBroadcast(new Intent(INTENT_STOP));
                    }
                }
            });
        } catch (DataKitException e) {
            android.util.Log.d(TAG, "onException...");
            Toasty.error(ServicePhoneSensor.this, "Error: DataKit is unavailable", Toast.LENGTH_LONG).show();
            disconnectDataKit();
            stopSelf();
        }
    }

    private synchronized void disconnectDataKit() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        if (phoneSensorDataSources != null) {
            phoneSensorDataSources.unregister();
            phoneSensorDataSources = null;
        }
        if (dataKitAPI != null && dataKitAPI.isConnected()) {
            dataKitAPI.disconnect();
        }
    }

    private boolean readSettings() {
        phoneSensorDataSources = new PhoneSensorDataSources(getApplicationContext());
        return phoneSensorDataSources.countEnabled() != 0;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_stop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        disconnectDataKit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
