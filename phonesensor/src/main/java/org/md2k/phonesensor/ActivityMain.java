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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.core.access.appinfo.AppInfo;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSource;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;
import org.md2k.phonesensor.plot.ActivityPlotChoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static android.app.Activity.RESULT_OK;

/**
 * <code>ActivityMain</code> is the execution start of the application.
 */
public class ActivityMain extends AppCompatActivity {
    HashMap<String, TextView> hashMapData = new HashMap<>();
    boolean isEverythingOk = false;
    Handler mHandler;
    int operation;

    /** Used to signify that this activity should start normally. <p>Set to 0.</p> */
    public static final int OPERATION_RUN = 0;

    /** Used to signify this activity should start the settings view. <p>Set to 1.</p> */
    public static final int OPERATION_SETTINGS = 1;

    /** Used to signify this activity should start the plot view. <p>Set to 2.</p> */
    public static final int OPERATION_PLOT = 2;

    /** Used to signify this activity should start in the background. <p>Set to 3.</p> */
    public static final int OPERATION_START_BACKGROUND = 3;

    /** Used to signify this activity should stop in the background. <p>Set to 4.</p> */
    public static final int OPERATION_STOP_BACKGROUND = 4;

    /** Set to "operation" */
    public static final String OPERATION = "operation";

    /**
     * Calls <code>super</code>, <code>loadCrashlytics()</code>, <code>readIntent()</code>,
     * <code>initializeVariable()</code>, and <code>checkRequirement()</code> on this activity's creation.
     *
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCrashlytics();
        readIntent();
        initializeVariable();
        checkRequirement();
    }

    /**
     * This method starts this activity in the given mode. It defaults to <code>OPERATION_RUN</code>.
     * <p>
     *     Available modes are:
     *     <ul>
     *         <li><code>OPERATION_RUN</code> - starts the UI</li>
     *         <li><code>OPERATION_START_BACKGROUND</code> - starts the service in the background</li>
     *         <li><code>OPERATION_STOP_BACKGROUND</code> - stops the service running in the background</li>
     *         <li><code>OPERATION_PLOT</code> - starts the plot activity</li>
     *         <li><code>OPERATION_SETTINGS</code> - starts the settings activity</li>
     *     </ul>
     * </p>
     */
    void load() {
        isEverythingOk = true;
        Intent intent;
        switch (operation) {
            case OPERATION_RUN:
                initializeUI();
                break;
            case OPERATION_START_BACKGROUND:
                intent = new Intent(ActivityMain.this, ServicePhoneSensor.class);
                startService(intent);
                finish();
                break;
            case OPERATION_STOP_BACKGROUND:
                intent = new Intent(ActivityMain.this, ServicePhoneSensor.class);
                stopService(intent);
                finish();
                break;
            case OPERATION_PLOT:
                intent = new Intent(this, ActivityPlotChoice.class);
                intent.putExtra("datasourcetype", getIntent().getStringExtra("datasourcetype"));
                startActivity(intent);
                finish();
                break;
            case OPERATION_SETTINGS:
                intent = new Intent(this, ActivitySettings.class);
                startActivity(intent);
                finish();
                break;
            default:
                initializeUI();
        }
    }

    /**
     * Creates the options menu.
     *
     * <p>
     *     Inflate the menu; this adds items to the action bar if it is present.
     * </p>
     *
     * @param menu Android Menu object
     * @return Always returns true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Handles the selection of items on the action bar.
     *
     * <p>
     *     Handle action bar item clicks here. The action bar will automatically handle clicks on
     *     the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
     * </p>
     *
     * @param item Android MenuItem object
     * @return TODO: onOptionsItemSelected is a callback method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
                intent = new Intent(this, ActivitySettings.class);
                startActivity(intent);
                break;
            case R.id.action_plot:
                intent = new Intent(this, ActivityPlotChoice.class);
                startActivity(intent);
                break;
            case R.id.action_location:
                intent = new Intent(this, ActivitySettingsGeofence.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a <code>TableRow</code> widget using default settings.
     *
     * @return the <code>TableRow</code> widget
     */
    TableRow createDefaultRow() {
        TableRow row = new TableRow(this);
        TextView tvSensor = new TextView(this);
        tvSensor.setText(R.string.table_sensor_id);
        tvSensor.setTypeface(null, Typeface.BOLD);
        tvSensor.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvCount = new TextView(this);
        tvCount.setText(R.string.table_header_count);
        tvCount.setTypeface(null, Typeface.BOLD);
        tvCount.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvFreq = new TextView(this);
        tvFreq.setText(R.string.table_header_freq);
        tvFreq.setTypeface(null, Typeface.BOLD);
        tvFreq.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvSample = new TextView(this);
        tvSample.setText(R.string.table_header_samples);
        tvSample.setTypeface(null, Typeface.BOLD);
        tvSample.setTextColor(getResources().getColor(R.color.teal_A700));
        row.addView(tvSensor);
        row.addView(tvCount);
        row.addView(tvFreq);
        row.addView(tvSample);
        return row;
    }

    /**
     *  Creates a table widget that displays the phone sensor data sources.
     */
    void prepareTable() {
        try {
            ArrayList<PhoneSensorDataSource> phoneSensorDataSources = new PhoneSensorDataSources(getApplicationContext()).getPhoneSensorDataSources();
            TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
            ll.removeAllViews();
            ll.addView(createDefaultRow());
            for (int i = 0; i < phoneSensorDataSources.size(); i++) {
                if (phoneSensorDataSources.get(i).isEnabled()) {
                    String dataSourceType = phoneSensorDataSources.get(i).getDataSourceType();
                    TableRow row = new TableRow(this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                    row.setLayoutParams(lp);
                    TextView tvSensor = new TextView(this);
                    tvSensor.setPadding(10, 0, 0, 0);
                    try {
                        tvSensor.setText(dataSourceType.toLowerCase() + " (" + phoneSensorDataSources.get(i).getFrequency() + " Hz)");
                    } catch (NumberFormatException nfe) {
                        tvSensor.setText(dataSourceType.toLowerCase() + " (" + phoneSensorDataSources.get(i).getFrequency() + ")");
                    }

                    TextView tvCount = new TextView(this);
                    tvCount.setText("0");
                    hashMapData.put(dataSourceType + "_count", tvCount);
                    TextView tvFreq = new TextView(this);
                    tvFreq.setText("0");
                    hashMapData.put(dataSourceType + "_freq", tvFreq);
                    TextView tvSample = new TextView(this);
                    tvSample.setText("0");
                    hashMapData.put(dataSourceType + "_sample", tvSample);
                    row.addView(tvSensor);
                    row.addView(tvCount);
                    row.addView(tvFreq);
                    row.addView(tvSample);
                    row.setBackgroundResource(R.drawable.border);
                    ll.addView(row);
                }
            }
        }catch (Exception e){

        }
    }

    /**
     * Updates the table widget with refreshed data from the sensors.
     *
     * @param intent Android intent
     */
    void updateTable(Intent intent) {
        String sampleStr = "";
        String dataSourceType = ((DataSource) intent.getParcelableExtra("datasource")).getType();
        int count = intent.getIntExtra("count", 0);
        hashMapData.get(dataSourceType + "_count").setText(String.valueOf(count));

        double time = (intent.getLongExtra("timestamp", 0) - intent.getLongExtra("starttimestamp", 0)) / 1000.0;
        double freq = (double) count / time;
        hashMapData.get(dataSourceType + "_freq").setText(String.format(Locale.getDefault(), "%.1f", freq));


        DataType data = intent.getParcelableExtra("data");

        if (data instanceof DataTypeFloat) {
            sampleStr = String.format(Locale.getDefault(), "%.1f", ((DataTypeFloat) data).getSample());
        } else if (data instanceof DataTypeFloatArray) {
            float[] sample = ((DataTypeFloatArray) data).getSample();
            for (int i = 0; i < sample.length; i++) {
                if (i != 0) sampleStr += ",";
                if (i % 3 == 0 && i != 0) sampleStr += "\n";
                sampleStr = sampleStr + String.format(Locale.getDefault(), "%.1f", sample[i]);
            }
        } else if (data instanceof DataTypeDoubleArray) {
            double[] sample = ((DataTypeDoubleArray) data).getSample();
            for (int i = 0; i < sample.length; i++) {
                if (i != 0) sampleStr += ",";
                if (i % 3 == 0 && i != 0) sampleStr += "\n";
                sampleStr = sampleStr + String.format(Locale.getDefault(), "%.1f", sample[i]);
            }
        }
        hashMapData.get(dataSourceType + "_sample").setText(sampleStr);
    }

    /**
     * Registers receivers, prepares a new table widget and adds the relevant <code>runnable</code>
     * methods to the message queue upon resuming the activity.
     */
    @Override
    public void onResume() {
        try {
            if (isEverythingOk) {
                LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                        new IntentFilter("phonesensor"));
                prepareTable();
                mHandler.post(runnable);
            }
        }catch (Exception e){

        }
        super.onResume();
    }

    /**
     * Removes <code>runnable</code> callbacks and unregisters <code>mMessageReciver</code> when the
     * activity is paused.
     */
    @Override
    public void onPause() {
        try {
            if (isEverythingOk) {
                mHandler.removeCallbacks(runnable);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            }
        }catch (Exception ignored){

        }
        super.onPause();
    }

    /**
     * Creates a new <code>Runnable()</code> object. This object creates a start button when run.
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            {
                long time = AppInfo.serviceRunningTime(ActivityMain.this, ServicePhoneSensor.class.getName());
                if (time < 0) {
                    ((Button) findViewById(R.id.button_app_status)).setText("START");
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_off));

                } else {
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_on));
                    ((Button) findViewById(R.id.button_app_status)).setText(DateTime.convertTimestampToTimeStr(time));

                }
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    /**
     * TODO: What is this looking for a response from??
     */
    ResponseCallback status = new ResponseCallback() {
        @Override
        public void onResponse(boolean result) {
            if (!result)
                finish();
            else {
                load();
            }
        }
    };
    /**
     * Creates a new broadcast receiver that updates the table widget when it receives new data.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTable(intent);
        }
    };


    /**
     * Creates a new <code>Intent</code> to check <code>ActivityPermission()</code> for the proper
     * permissions.
     */
    void checkRequirement() {
        Intent intent = new Intent(this, ActivityPermission.class);
        startActivityForResult(intent, 1111);
    }


    /**
     * Handles callback results for <code>checkRequirement()</code>.
     *
     * @param requestCode The code sent with the request, used for request/result verification
     * @param resultCode The code returned with the result, used for request/result verification
     * @param data Android intent
     */
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1111) {
        if(resultCode!=RESULT_OK){
            finish();
        }else{
            load();
        }
    }
}

    /**
     * Creates a new <code>Crashlytics</code> object.
     */
    void loadCrashlytics() {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlyticsKit, new Crashlytics());
    }

    /**
     * Sets the operation mode based on whether the intent has extras.
     */
    void readIntent() {
        if(getIntent().getExtras()!= null)
        operation = getIntent().getExtras().getInt(OPERATION, 0);
        else operation = 0;
    }

    /**
     * Sets <code>isEverythingOk</code> to false and registers a new <code>Handler</code>.
     */
    void initializeVariable() {
        isEverythingOk = false;
        mHandler = new Handler();
    }

    /**
     * Starts the UI
     */
    void initializeUI() {
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Button buttonService = (Button) findViewById(R.id.button_app_status);
        buttonService.setOnClickListener(new View.OnClickListener() {
            /**
             * Toggles the phone sensor service.
             *
             * @param v Active view
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServicePhoneSensor.class);
                if (AppInfo.isServiceRunning(getBaseContext(), ServicePhoneSensor.class.getName())) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });

    }
}

