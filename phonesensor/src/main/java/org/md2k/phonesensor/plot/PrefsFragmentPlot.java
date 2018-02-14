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

package org.md2k.phonesensor.plot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.phonesensor.R;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSource;
import org.md2k.phonesensor.phone.sensors.PhoneSensorDataSources;

/**
 * Preferences Fragment for Plot
 */
public class PrefsFragmentPlot extends PreferenceFragment {
    PhoneSensorDataSources phoneSensorDataSources;

    /**
     * Reads configuration, inflates <code>R.xml.pref_plot_choice</code> and calls
     * <code>createPreferencesScreen</code>.
     *
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readConfiguration();
        addPreferencesFromResource(R.xml.pref_plot_choice);
        createPreferenceScreen();
    }

    /**
     * Creates the settings view
     *
     * @param inflater Android LayoutInflater
     * @param container Android ViewGroup
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     * @return The view this method created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v != null;
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }

    /**
     * Calls <code>addPreferenceScreenSensors()</code>.
     */
    void createPreferenceScreen() {
        addPreferenceScreenSensors();
    }

    /**
     * Creates a new <code>phoneSensorDataSources</code> object which reads it's configuration.
     */
    void readConfiguration() {
        phoneSensorDataSources = new PhoneSensorDataSources(getActivity());
    }

    /**
     * Sets mSensor equal to the appropriate sensor via sensor manager.
     *
     * <p>
     *     Possible data sources are:
     *     <ul>
     *          <li>Accelerometer</li>
     *          <li>Gryoscope</li>
     *          <li>Ambient Temperature</li>
     *          <li>Compass</li>
     *          <li>Ambient Light</li>
     *          <li>Pressure</li>
     *          <li>Proximity</li>
     *          <li>Location</li>
     *     </ul>
     * </p>
     * @param dataSourceType The data source type in question.
     * @return Whether the given data source type has an appropriate sensor.
     */
    boolean isSensorSupported(String dataSourceType) {
        SensorManager mSensorManager;
        Sensor mSensor;
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        switch (dataSourceType) {
            case DataSourceType.ACCELEROMETER:
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                break;
            case (DataSourceType.GYROSCOPE):
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                break;
            case (DataSourceType.AMBIENT_TEMPERATURE):
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                break;
            case (DataSourceType.COMPASS):
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                break;
            case (DataSourceType.AMBIENT_LIGHT):
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                break;
            case (DataSourceType.PRESSURE):
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                break;
            case (DataSourceType.PROXIMITY):
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                break;
            case DataSourceType.LOCATION:
                return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
            default:
                return true;

        }
        return mSensor != null;
    }

    /**
     * Creates a preference object for the given data source type.
     *
     * @param dataSourceType The data source type in question.
     * @return The created preference object.
     */
    private Preference createPreference(String dataSourceType) {

        Preference preference = new Preference(getActivity());
        preference.setKey(dataSourceType);
        String title = dataSourceType;
        title = title.replace("_", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
        preference.setTitle(title);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            /**
             * Starts a new plot activity for the selected data source type.
             *
             * @param preference The preference that was clicked
             * @return Always returns false
             */
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent=new Intent(getActivity(), ActivityPlot.class);
                intent.putExtra("datasourcetype", preference.getKey());
                startActivity(intent);
                return false;
            }
        });
        preference.setEnabled(isSensorSupported(dataSourceType));
        return preference;
    }

    /**
     * Adds sensors to the preference screen.
     *
     * <p>
     * Removes all data source type preferences before iterating through the
     * <code>phoneSensorDataSources</code> ArrayList, creating a preference for each data source and
     * adding it to the category.
     * </p>
     */
    protected void addPreferenceScreenSensors() {
        String dataSourceType;
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
        preferenceCategory.removeAll();
        for (int i = 0; i < phoneSensorDataSources.getPhoneSensorDataSources().size(); i++) {
            dataSourceType = phoneSensorDataSources.getPhoneSensorDataSources().get(i).getDataSourceType();
            if(!phoneSensorDataSources.getPhoneSensorDataSources().get(i).isEnabled()) continue;
            Preference preference = createPreference(dataSourceType);
            preferenceCategory.addPreference(preference);
        }
    }
}
