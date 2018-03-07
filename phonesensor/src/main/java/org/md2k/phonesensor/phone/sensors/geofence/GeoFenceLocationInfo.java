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

package org.md2k.phonesensor.phone.sensors.geofence;

/**
 * This class creates a location info object for use in location arrays.
 *
 * <p>
 *     This object has a location name, a lattitude, and a longitude.
 * </p>
 */
public class GeoFenceLocationInfo {
    private String location;
    private double latitude;
    private double longitude;

    /**
     * Constructor
     *
     * @param location Name of the location
     * @param latitude Latitude, or North-South position, of the location
     * @param longitude Longitude, or East-West position, of the location
     */
    public GeoFenceLocationInfo(String location, double latitude, double longitude) {
        this.location=location;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    /**
     * @return Name of the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return Latitude, or North-South position, of the location
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return Longitude, or East-West position, of the location
     */
    public double getLongitude() {
        return longitude;
    }
}
