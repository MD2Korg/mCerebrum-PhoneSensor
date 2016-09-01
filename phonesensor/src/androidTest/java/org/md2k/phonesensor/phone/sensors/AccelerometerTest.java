package org.md2k.phonesensor.phone.sensors;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
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

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.phonesensor.ActivityMain;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class AccelerometerTest {

    @Rule
    public ActivityTestRule<ActivityMain> mActivityRule = new ActivityTestRule<>(ActivityMain.class);


    Context context;

    @Before
    public void setUp() throws Exception {
        context = mActivityRule.getActivity().getBaseContext();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void createDataDescriptorShouldContainKeys() throws Exception {
        Accelerometer accel = new Accelerometer(context);
        HashMap<String, String> dataDescriptor = accel.createDataDescriptor("Test Accel", "120", "Test Description");

        //Check for mandatory fields
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.NAME));
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.MIN_VALUE));
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.MAX_VALUE));
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.UNIT));
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.FREQUENCY));
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.DESCRIPTION));
        Assert.assertTrue(dataDescriptor.containsKey(METADATA.DATA_TYPE));

    }

    @Test
    public void createDataDescriptorShouldMatchValues() throws Exception {
        Accelerometer accel = new Accelerometer(context);
        HashMap<String, String> dataDescriptor = accel.createDataDescriptor("Test Accel", "120", "Test Description");

        Assert.assertSame("Names are equal", "Test Accel", dataDescriptor.get(METADATA.NAME));
        Assert.assertSame("Frequency matches", "120", dataDescriptor.get(METADATA.FREQUENCY));
        Assert.assertSame("Description matches", "Test Description", dataDescriptor.get(METADATA.DESCRIPTION));
    }

    @Test
    public void createDataDescriptorShouldMatchAccelerometerUnit() throws Exception {
        Accelerometer accel = new Accelerometer(context);
        HashMap<String, String> dataDescriptor = accel.createDataDescriptor("Test Accel", "120", "Test Description");

        Assert.assertSame("Names are equal", "meter/second^2", dataDescriptor.get(METADATA.UNIT));
        Assert.assertNotSame("Names are different", "m/s^2", dataDescriptor.get(METADATA.UNIT));

    }

    @Test
    public void createDataDescriptorsShouldContainThreeKeys() throws Exception {
        Accelerometer accel = new Accelerometer(context);
        ArrayList<HashMap<String, String>> dataDescriptors = accel.createDataDescriptors();

        Assert.assertEquals("Array size is 3", 3, dataDescriptors.size());
    }

    @Ignore("Need to figure out how to test DataKit connections")
    @Test
    public void createDataSourceBuilderShouldExist() throws Exception {
        Accelerometer accel = new Accelerometer(context);
        DataSourceBuilder dataSourceBuilder = accel.createDataSourceBuilder();

        Assert.assertNotNull(dataSourceBuilder);

    }


}