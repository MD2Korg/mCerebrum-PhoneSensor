package org.md2k.phonesensor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.utilities.Report.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
public class DefaultConfiguration {
    class Default{
        String type;
        String frequency;
    }
    public static boolean isExist(){
        String outDir= Constants.CONFIG_DIRECTORY ;
        File outFile = new File(outDir, Constants.DEFAULT_FILENAME_PHONESENSOR);
        return outFile.exists();
    }

    public static ArrayList<Default> read() {
        String outDir= Constants.CONFIG_DIRECTORY ;
        String filename=outDir+File.separator+Constants.DEFAULT_FILENAME_PHONESENSOR;

        ArrayList<Default> defaults=null;
        if (!isExist()) return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<Default>>() {
            }.getType();
            defaults = gson.fromJson(br, collectionType);
        } catch (FileNotFoundException e) {

        }
        return defaults;
    }
}
