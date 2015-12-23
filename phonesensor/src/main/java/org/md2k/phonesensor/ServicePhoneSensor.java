package org.md2k.phonesensor;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Toast;

import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.datakit.DataKitHandler;

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

public class ServicePhoneSensor extends Service {
    private static final String TAG = ServicePhoneSensor.class.getSimpleName();
    PhoneSensorDataSources phoneSensorDataSources = null;
    DataKitHandler dataKitHandler;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        if (Constants.TEST_BATTERY)
            BCMRecord.getInstance();
        if (!readSettings()) {
            showAlertDialogConfiguration(this);
            stopSelf();
        } else if (!connectDataKit()) {
            showAlertDialogDataKit(this);
            phoneSensorDataSources=null;
            stopSelf();
        } else
            Toast.makeText(getApplicationContext(), "PhoneSensor Service started Successfully", Toast.LENGTH_LONG).show();
    }
    void showAlertDialogConfiguration(final Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error: Configuration File")
                .setIcon(R.drawable.ic_error_red_50dp)
                .setMessage("Phone Sensor is not configured.\n\n Please go to Menu -> Settings (or, click Settings below)")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, ActivityPhoneSensorSettings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
    //            UIShow.ErrorDialog(getApplicationContext(), "DataKit Error", "DataKit is not available.\n\nPlease Install DataKit");
    void showAlertDialogDataKit(final Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error: DataKit")
                .setIcon(R.drawable.ic_error_red_50dp)
                .setMessage("DataKit is not installed.\n\n Please install DataKit")
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    private boolean connectDataKit() {
        Log.d(TAG, "connectDataKit()...");
        DataKitHandler.getInstance(getApplicationContext()).close();
        dataKitHandler = DataKitHandler.getInstance(getApplicationContext());
        return dataKitHandler.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected()...");
                phoneSensorDataSources.register();
            }
        });
    }

    private boolean readSettings() {
        phoneSensorDataSources = new PhoneSensorDataSources(getApplicationContext());
        return phoneSensorDataSources.size() != 0;
    }

    @Override
    public void onDestroy() {
        if (phoneSensorDataSources != null) {
            phoneSensorDataSources.unregister();
            phoneSensorDataSources = null;
        }
        if (Constants.TEST_BATTERY)
            BCMRecord.getInstance().close();
        if (dataKitHandler != null && dataKitHandler.isConnected()) dataKitHandler.disconnect();
        if (dataKitHandler != null)
            dataKitHandler.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
