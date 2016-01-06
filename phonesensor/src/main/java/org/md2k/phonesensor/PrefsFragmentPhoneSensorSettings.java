package org.md2k.phonesensor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.phonesensor.phone.sensors.Accelerometer;
import org.md2k.phonesensor.phone.sensors.AmbientLight;
import org.md2k.phonesensor.phone.sensors.Compass;
import org.md2k.phonesensor.phone.sensors.Gyroscope;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSource;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;
import org.md2k.utilities.Apps;
import org.md2k.utilities.Report.Log;

import java.io.IOException;
import java.util.ArrayList;

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
public class PrefsFragmentPhoneSensorSettings extends PreferenceFragment {
    private static final String TAG = PrefsFragmentPhoneSensorSettings.class.getSimpleName() ;
    PhoneSensorDataSources phoneSensorDataSources;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readConfiguration();
        addPreferencesFromResource(R.xml.pref_phonesensor_platform);
        createPreferenceScreen();
        setBackButton();
        setSaveButton();

//        setupPreferences();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container,savedInstanceState);
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }
    void handleService(boolean opType) {
        Intent intent = new Intent(getActivity(), ServicePhoneSensor.class);
        if (!opType) {
            if (Apps.isServiceRunning(getActivity(), Constants.SERVICE_NAME)) {
                getActivity().stopService(intent);
            }
        }
        else{
            getActivity().startService(intent);
        }
    }
    void updatePhoneSensorDataSources(){
        ArrayList<DefaultConfiguration.Default> defaultArrayList=DefaultConfiguration.read();
        for(int i=0;i<phoneSensorDataSources.size();i++){
            phoneSensorDataSources.getPhoneSensorDataSources().get(i).setEnabled(false);
        }
        assert defaultArrayList != null;
        for(int i=0;i<defaultArrayList.size();i++){
            String type=defaultArrayList.get(i).type;
            String freq=defaultArrayList.get(i).frequency;
            phoneSensorDataSources.find(type).setEnabled(true);
            phoneSensorDataSources.find(type).setFrequency(freq);
            Log.d(TAG,"type="+type+" freq="+freq);
        }

    }
    void setDefaultSettings(){
        final CheckBoxPreference checkBoxPreference= (CheckBoxPreference) findPreference("key_default_settings");
        if(!DefaultConfiguration.isExist()) {
            checkBoxPreference.setEnabled(false);
        }else{
            checkBoxPreference.setEnabled(true);
            checkBoxPreference.setChecked(false);
            checkBoxPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean checked = ((CheckBoxPreference) preference).isChecked();
                    Log.d(TAG, "checked=" + checked);
                    if (checked) {
                        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
                        preferenceCategory.setEnabled(false);
                        updatePhoneSensorDataSources();
                        updatePreferenceScreen();

                    } else {
                        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
                        preferenceCategory.setEnabled(true);
                    }
                    return false;
                }
            });
        }
    }

    void createPreferenceScreen() {
        setDefaultSettings();
        addPreferenceScreenSensors();
        updatePreferenceScreen();
    }
    void readConfiguration() {
        phoneSensorDataSources = new PhoneSensorDataSources(getActivity());
    }

    private SwitchPreference createSwitchPreference(String dataSourceType) {
        SwitchPreference switchPreference = new SwitchPreference(getActivity());
        switchPreference.setKey(dataSourceType);
        String title = dataSourceType;
        title = title.replace("_", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
        switchPreference.setTitle(title);
        switchPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        switch (dataSourceType) {
            case (DataSourceType.ACCELEROMETER):
                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(Accelerometer.frequencyOptions));
                break;
            case (DataSourceType.GYROSCOPE):
                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(Gyroscope.frequencyOptions));
                break;
            case (DataSourceType.AMBIENT_TEMPERATURE):
//                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(AmbientTemperature.frequencyOptions));
                break;
            case (DataSourceType.COMPASS):
                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(Compass.frequencyOptions));
                break;
            case (DataSourceType.AMBIENT_LIGHT):
                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(AmbientLight.frequencyOptions));
                break;
            case (DataSourceType.PRESSURE):
//                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(Pressure.frequencyOptions));
                break;
            case (DataSourceType.PROXIMITY):
//                switchPreference.setOnPreferenceClickListener(alertDialogFrequency(Proximity.frequencyOptions));
                break;
        }
        return switchPreference;
    }

    private Preference.OnPreferenceClickListener alertDialogFrequency(final String[] frequencies) {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SwitchPreference switchPreference = (SwitchPreference) preference;
                phoneSensorDataSources.find(preference.getKey()).setEnabled(!switchPreference.isChecked());

                UI.AlertDialogFrequency(getActivity(), preference.getKey(), frequencies, new AlertDialogResponse() {
                    @Override
                    public void onResponse(String dataSourceType, String response) {
                        if (response != null) {
                            phoneSensorDataSources.find(dataSourceType).setFrequency(response);
                            updatePreferenceScreen();
                        }
                    }
                });
                return false;
            }
        };
    }

    Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            phoneSensorDataSources.find(preference.getKey()).setEnabled((Boolean) newValue);
            updatePreferenceScreen();
            return false;
        }
    };

    protected void addPreferenceScreenSensors() {
        String dataSourceType;
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
        preferenceCategory.removeAll();
        for (int i = 0; i < phoneSensorDataSources.getPhoneSensorDataSources().size(); i++) {
            dataSourceType = phoneSensorDataSources.getPhoneSensorDataSources().get(i).getDataSourceType();
            SwitchPreference switchPreference = createSwitchPreference(dataSourceType);
            preferenceCategory.addPreference(switchPreference);
        }
    }

    void updatePreferenceScreen() {
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
        PhoneSensorDataSource phoneSensorDataSource;
        for (int i = 0; i < phoneSensorDataSources.getPhoneSensorDataSources().size(); i++) {
            phoneSensorDataSource = phoneSensorDataSources.getPhoneSensorDataSources().get(i);
            SwitchPreference switchPreference = (SwitchPreference) preferenceCategory.findPreference(phoneSensorDataSource.getDataSourceType());
            switchPreference.setChecked(phoneSensorDataSource.isEnabled());
            switchPreference.setSummary(phoneSensorDataSource.getFrequency());
        }
    }

    private void setBackButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    private void setSaveButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Apps.isServiceRunning(getActivity(), Constants.SERVICE_NAME)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Save configuration file and restart the PhoneSensor Service?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
                else{
                    saveConfigurationFile();
                    getActivity().finish();
                }
            }
        });
    }

    void saveConfigurationFile() {
        try {
            phoneSensorDataSources.writeDataSourceToFile();
            Toast.makeText(getActivity(), "Configuration file is saved.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "!!!Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Intent intent = new Intent(getActivity(), ServicePhoneSensor.class);
                    getActivity().stopService(intent);
                    saveConfigurationFile();
                    intent = new Intent(getActivity(), ServicePhoneSensor.class);
                    getActivity().startService(intent);
                    getActivity().finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(getActivity(), "Configuration file is not saved.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    break;
            }
        }
    };
}
