package org.md2k.phonesensor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSource;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;
import org.md2k.utilities.Apps;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.HashMap;

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

public class ActivityPhoneSensor extends Activity {
    private static final String TAG = ActivityPhoneSensor.class.getSimpleName();
    void updateServiceSwitch(){
        Switch service = (Switch) findViewById(R.id.switchService);
        if (Apps.isServiceRunning(ActivityPhoneSensor.this, Constants.SERVICE_NAME)) {
            service.setChecked(true);
        }
        else service.setChecked(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_sensor);
        Switch service=(Switch) findViewById(R.id.switchService);
        service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "isChecked=" + isChecked);
                if(isChecked) {
                    starttimestamp = 0;
                    Intent intent = new Intent(ActivityPhoneSensor.this, ServicePhoneSensor.class);
                    startService(intent);
                }
                else{
                    Intent intent = new Intent(ActivityPhoneSensor.this, ServicePhoneSensor.class);
                    stopService(intent);
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ActivityPhoneSensorSettings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    TableRow createDefaultRow() {
        TableRow row = new TableRow(this);
        TextView tvSensor = new TextView(this);tvSensor.setText("sensor");tvSensor.setTypeface(null, Typeface.BOLD);tvSensor.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView tvCount = new TextView(this);tvCount.setText("count");tvCount.setTypeface(null, Typeface.BOLD);tvCount.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView tvFreq = new TextView(this);tvFreq.setText("freq.");tvFreq.setTypeface(null, Typeface.BOLD);tvFreq.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView tvSample = new TextView(this);tvSample.setText("samples");tvSample.setTypeface(null, Typeface.BOLD);tvSample.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        row.addView(tvSensor);
        row.addView(tvCount);
        row.addView(tvFreq);
        row.addView(tvSample);
        return row;
    }

    void prepareTable(PhoneSensorDataSources phsDataSources) {
        ArrayList<PhoneSensorDataSource> phoneSensorDataSources = phsDataSources.getPhoneSensorDataSources();
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
                tvSensor.setText(dataSourceType.toLowerCase());
                TextView tvCount = new TextView(this);
                tvCount.setText("0");
                hm.put(dataSourceType + "_count", tvCount);
                TextView tvFreq = new TextView(this);
                tvFreq.setText("0");
                hm.put(dataSourceType + "_freq", tvFreq);
                TextView tvSample = new TextView(this);
                tvSample.setText("0");
                hm.put(dataSourceType + "_sample", tvSample);
                row.addView(tvSensor);
                row.addView(tvCount);
                row.addView(tvFreq);
                row.addView(tvSample);
                row.setBackgroundResource(R.drawable.border);
                ll.addView(row);
            }
        }

    }

    void showActiveSensors(PhoneSensorDataSources phoneSensorDataSources) {
        TextView textView = (TextView) findViewById(R.id.configuration_info);
        String str = "";
        int count = 0;
        ArrayList<PhoneSensorDataSource> phoneSensorDataSourceArrayList = phoneSensorDataSources.getPhoneSensorDataSources();
        for (int i = 0; i < phoneSensorDataSourceArrayList.size(); i++) {
            if (phoneSensorDataSourceArrayList.get(i).isEnabled()) {
                if (count % 2 == 0 && count != 0) str = str + "\n";
                else if (count != 0) str = str + "      ";
                str = str + phoneSensorDataSourceArrayList.get(i).getDataSourceType().toLowerCase() + "(" + phoneSensorDataSourceArrayList.get(i).getFrequency().toLowerCase() + ")";
                count++;
            }
        }
        textView.setText(str);
    }

    void serviceStatus() {
        TextView textView = (TextView) findViewById(R.id.service_info);
        if(Apps.isServiceRunning(ActivityPhoneSensor.this,"ServicePhoneSensor"))
            textView.setText("Running");
        else textView.setText("Not Running");
    }

/*
    private void setupButtonService() {
        final Button buttonStopService = (Button) findViewById(R.id.button_stopservice);
        final Button buttonStartService = (Button) findViewById(R.id.button_startservice);
        buttonStartService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ServicePhoneSensor.isRunning) {
                    starttimestamp = 0;
                    Intent intent = new Intent(ActivityPhoneSensor.this, ServicePhoneSensor.class);
                    startService(intent);
                    TextView textView = (TextView) findViewById(R.id.service_info);
                    textView.setText("Running");
                }
            }
        });
        buttonStopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ServicePhoneSensor.isRunning) {
                    Intent intent = new Intent(ActivityPhoneSensor.this, ServicePhoneSensor.class);
                    stopService(intent);
                    TextView textView = (TextView) findViewById(R.id.service_info);
                    textView.setText("Not Running");
                }
            }
        });
    }
*/
    long starttimestamp = 0;
    HashMap<String, TextView> hm = new HashMap<>();
    void updateServiceStatus(){
        TextView textView = (TextView) findViewById(R.id.service_info);
        if (starttimestamp == 0) starttimestamp = DateTime.getDateTime();
        long minutes=(DateTime.getDateTime()-starttimestamp)/(1000*60);
        long seconds=((DateTime.getDateTime()-starttimestamp)/(1000)%60);
        textView.setText("Running (" + minutes+" Minutes "+seconds+" Seconds)" );
    }
    void updateTable(Intent intent){
        String sampleStr = "";
        String dataSourceType=((DataSource) intent.getSerializableExtra("datasource")).getType();
        int count = intent.getIntExtra("count", 0);
        hm.get(dataSourceType+"_count").setText(String.valueOf(count));

        double time = (intent.getLongExtra("timestamp", 0) - intent.getLongExtra("starttimestamp", 0)) / 1000.0;
        double freq = (double) count / time;
        hm.get(dataSourceType+"_freq").setText(String.format("%.1f",freq));


        DataType data = (DataType) intent.getSerializableExtra("data");
        if (data instanceof DataTypeFloat) {
            sampleStr = String.format("%.1f", ((DataTypeFloat) data).getSample());
        } else if (data instanceof DataTypeFloatArray) {
            float[] sample = ((DataTypeFloatArray) data).getSample();
            for (int i = 0; i < sample.length; i++) {
                if (i != 0) sampleStr += ",";
                if(i%3==0 && i!=0) sampleStr+="\n";
                sampleStr = sampleStr + String.format("%.1f", sample[i]);
            }
        } else if (data instanceof DataTypeDoubleArray) {
            double[] sample = ((DataTypeDoubleArray) data).getSample();
            for (int i = 0; i < sample.length; i++) {
                if (i != 0) sampleStr += ",";
                if(i%3==0 && i!=0) sampleStr+="\n";
                sampleStr = sampleStr + String.format("%.1f", sample[i]);
            }
        }
        hm.get(dataSourceType+"_sample").setText(sampleStr);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateServiceStatus();
            updateTable(intent);

        }
    };

    @Override
    public void onResume() {
        updateServiceSwitch();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("phonesensor"));
        PhoneSensorDataSources phoneSensorDataSources;
        phoneSensorDataSources=new PhoneSensorDataSources(ActivityPhoneSensor.this);
        serviceStatus();
        showActiveSensors(phoneSensorDataSources);
        prepareTable(phoneSensorDataSources);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}
