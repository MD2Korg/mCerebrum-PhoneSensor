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

package org.md2k.phonesensor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import org.md2k.mcerebrum.commons.permission.PermissionInfo;
import org.md2k.mcerebrum.commons.permission.ResultCallback;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;

import br.com.goncalves.pugnotification.notification.PugNotification;
import es.dmoral.toasty.Toasty;

/**
 * This class handles the phone sensor service.
 */
public class ServicePhoneSensor extends Service {
    public static final String INTENT_STOP = "intent_stop";
    private static final String TAG = ServicePhoneSensor.class.getSimpleName();
    PhoneSensorDataSources phoneSensorDataSources = null;
    DataKitAPI dataKitAPI;

    /**
     * When this service is created it checks for permission information
     */
    public void onCreate() {
        super.onCreate();
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            /**
             * Shows notification when permissions are denied.
             *
             * @param result Whether appropriate permissions are given or not
             */
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    showNotification();
                    stopSelf();
                } else {
                    removeNotification();
                    load();
                }
            }
        });
    }

    /**
     * Shows a notification to ask for permission
     */
    private void showNotification() {
        Bundle bundle = new Bundle();
        bundle.putInt(ActivityMain.OPERATION, ActivityMain.OPERATION_START_BACKGROUND);
        PugNotification.with(this).load().identifier(13).title("Permission required").smallIcon(R.mipmap.ic_launcher)
                .message("PhoneSensor app can't continue. (Please click to grant permission)")
                .autoCancel(true).click(ActivityMain.class, bundle).simple().build();
    }

    /**
     * Removes a notification
     */
    private void removeNotification() {
        PugNotification.with(this).cancel(13);
    }

    /**
     * Loads the service, reads the settings and connects to <code>dataKitAPI</code>.
     */
    void load() {
        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp="
                + DateTime.getDateTime() + ",service_start");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_STOP));
        if (!readSettings()) {
//            Toasty.error(this, "Error: not configured yet", Toast.LENGTH_SHORT).show();
            stopSelf();
        } else connectDataKit();
    }

    /**
     * Connects <code>phonesensor</code> to <code>dataKitAPI</code>.
     *
     * <p>
     * Gets the application context of <code>dataKitAPI</code> and passing it a new
     * <code>OnConnectionListener</code> object.
     *
     * If the connection fails, it is logged, Toasty produces an error message,
     * <code>disconnectDataKit</code> is called and the service is stopped.
     * </p>
     */
    private void connectDataKit() {
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        try {
            dataKitAPI.connect(new OnConnectionListener() {
                /**
                 * When <code>dataKitAPI</code> has been connected successfully,
                 * <code>phoneSensorDataSources.register</code> is called to register the data sources.
                 */
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

    /**
     * Disconnects <code>dataKitAPI</code>.
     *
     * <p>
     * Unregisters Android's message receiver from <code>LocalBroadcastManager</code>,
     * unregisters any remaining phone sensors and runs <code>dataKitAPI.disconnect</code>.
     * </p>
     */
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

    /**
     * Gets the application context of <code>PhoneSensorDataSources</code> and then counts the number
     * of data sources
     *
     * @return True when <code>phoneSensorDataSources.countEnabled</code> is not 0.
     */
    private boolean readSettings() {
        phoneSensorDataSources = new PhoneSensorDataSources(getApplicationContext());
        return phoneSensorDataSources.countEnabled() != 0;
    }

    /**
     * Logs the timestamp of <code>service_stop</code>, disconnects dataKit, and calls super.
     */
    @Override
    public void onDestroy() {
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp="
                + DateTime.getDateTime() + ",service_stop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        disconnectDataKit();
        super.onDestroy();
    }

    /**
     * This method has not been implemented yet.
     *
     * @param intent Android intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Creates a new broadcast receiver that receives the <code>stop_service</code> intent. Upon
     * receipt it disconnects dataKit and stops itself.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp="
                    + DateTime.getDateTime() + ",broadcast_receiver_stop_service");
            disconnectDataKit();
            stopSelf();
        }
    };

}
