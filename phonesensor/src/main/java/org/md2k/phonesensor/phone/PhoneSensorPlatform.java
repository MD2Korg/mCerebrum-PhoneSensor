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

package org.md2k.phonesensor.phone;

import android.content.Context;
import android.os.Build;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;

/**
 *
 */
public class PhoneSensorPlatform {
    private static PhoneSensorPlatform instance=null;
    Context context;

    /**
     * Constructor
     *
     * @param context
     */
    private PhoneSensorPlatform(Context context) {
        this.context = context;
    }

    /**
     * Creates a new instance if one does not exist
     *
     * @param context
     * @return The instance, whether pre-existing or just created.
     */
    public static PhoneSensorPlatform getInstance(Context context){
        if(instance==null)
            instance=new PhoneSensorPlatform(context);
        return instance;
    }

    /**
     * @return Always returns null.
     */
    public String getId() {
        return null;
    }

    /**
     * @return Build version
     */
    public String getOS(){
        return Build.VERSION.RELEASE;
    }

    /**
     * @return The <code>PlatformType</code> as indicated by <code>dataKitAPI</code>
     */
    public String getType(){
        return PlatformType.PHONE;
    }

    /**
     * @return Manufacturer of the device
     */
    public String getManufacturer(){
        return Build.MANUFACTURER;
    }

    /**
     * @return Model name of the device
     */
    public String getName(){
        return android.os.Build.MODEL;
    }
}
