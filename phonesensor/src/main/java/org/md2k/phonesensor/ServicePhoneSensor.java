package org.md2k.phonesensor;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.UI.AlertDialogs;

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

public class ServicePhoneSensor extends Service {
    public static final String INTENT_STOP = "intent_stop";
    private static final String TAG = ServicePhoneSensor.class.getSimpleName();
    PhoneSensorDataSources phoneSensorDataSources = null;
    DataKitAPI dataKitAPI;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            disconnectDataKit();
            stopSelf();
        }
    };

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_STOP));
        if (!readSettings()) {
            showAlertDialogConfiguration(this);
            stopSelf();
        } else connectDataKit();
    }

    void showAlertDialogConfiguration(final Context context){
        try {
            AlertDialogs.AlertDialog(this, "Error: Phone Sensor Settings", "Please configure the phone sensor", R.drawable.ic_error_red_50dp, "Settings", "Cancel", null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        Intent intent = new Intent(context, ActivityPhoneSensorSettings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void connectDataKit() {
        Log.d(TAG, "connectDataKit()...");
        DataKitAPI.getInstance(getApplicationContext()).disconnect();

        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        try {
            dataKitAPI.connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected()...");
//                    Toast.makeText(ServicePhoneSensor.this, "PhoneSensor Started successfully", Toast.LENGTH_LONG).show();
                    phoneSensorDataSources.register();
                }
            });
        } catch (DataKitException e) {
            android.util.Log.d(TAG, "onException...");
            Toast.makeText(ServicePhoneSensor.this, "DataKit unavailable", Toast.LENGTH_LONG).show();
            disconnectDataKit();
            stopSelf();
        }
    }

    private void disconnectDataKit(){
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        disconnectDataKit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
