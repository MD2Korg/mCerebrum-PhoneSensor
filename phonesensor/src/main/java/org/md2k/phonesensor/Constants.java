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

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

/**
 * This class provides a list of constants for monitored activies
 */
public class Constants {

    /**
     * Filename of the metadata file.
     *
     * <p>
     *     <code>"meta_data.json"</code>
     * </p>
     */
    public static final String FILENAME_ASSET_METADATA = "meta_data.json";
    private Constants() {
    }

    /**
     * Name of the package.
     * <p>
     *     <code>"org.md2k.phonesensor"</code>
     * </p>
     */
    public static final String PACKAGE_NAME = "org.md2k.phonesensor";


    /**
     * List of DetectedActivity types that we monitor in this sample.
     */
    protected static final int[] MONITORED_ACTIVITIES = {
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };

    /**
     * The DetectedActivities this app monitors are:
     * <ul>
     *     <li><code>IN_VEHICLE</code></li>
     *     <li><code>ON_BICYCLE</code></li>
     *     <li><code>ON_FOOT</code></li>
     *     <li><code>RUNNING</code></li>
     *     <li><code>STILL</code></li>
     *     <li><code>TILTING</code></li>
     *     <li><code>UNKNOWN</code></li>
     *     <li><code>WALKING</code></li>
     *     <li><code>UNDEFINED</code></li>
     * </ul>
     *
     * @param detectedActivityType An integer value associated with a detected activity
     * @return The detected activity type as a string
     */
    public static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNDEFINED";
        }
    }
}
