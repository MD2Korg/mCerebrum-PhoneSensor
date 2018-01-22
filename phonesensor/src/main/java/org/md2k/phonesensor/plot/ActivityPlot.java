/**
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
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

package org.md2k.phonesensor.plot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.components.Description;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.mcerebrum.commons.plot.RealtimeLineChartActivity;
import org.md2k.phonesensor.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ActivityPlot extends RealtimeLineChartActivity {
    String dataSourceType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataSourceType = getIntent().getStringExtra("datasourcetype");
        if(dataSourceType==null) finish();

    }
    @Override
    public void onResume(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("phonesensor"));

        super.onResume();
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePlot(intent);
        }
    };
    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }
    void updatePlot(Intent intent) {
        float[] sample=new float[1];
        String[] legends;
        DataSource dataSource= intent.getParcelableExtra("datasource");
        ArrayList<HashMap<String, String>> hm = dataSource.getDataDescriptors();
        getmChart().getDescription().setText(dataSourceType);
        getmChart().getDescription().setPosition(1f,1f);
        getmChart().getDescription().setEnabled(true);
        getmChart().getDescription().setTextColor(Color.WHITE);
        legends=new String[hm.size()];
        for(int i=0;i<hm.size();i++){
            legends[i] = hm.get(i).get(METADATA.NAME);
        }
        String curDataSourceType = dataSource.getType();
        if(!curDataSourceType.equals(dataSourceType)) return;
        DataType data = intent.getParcelableExtra("data");
        if (data instanceof DataTypeFloat) {
            sample = new float[]{((DataTypeFloat) data).getSample()};
        } else if (data instanceof DataTypeFloatArray) {
            sample = ((DataTypeFloatArray) data).getSample();
        } else if (data instanceof DataTypeDoubleArray) {
            double[] samples = ((DataTypeDoubleArray) data).getSample();
            sample=new float[samples.length];
            for (int i = 0; i < samples.length; i++) {
                sample[i]= (float) samples[i];
            }
        }
        addEntry(sample, legends,600);
    }

}
