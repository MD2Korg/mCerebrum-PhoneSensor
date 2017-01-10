package org.md2k.phonesensor;

import android.os.Environment;

import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.utilities.FileManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/*
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
public class Configuration {
    private static final String CONFIG_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mCerebrum/org.md2k.phonesensor/";
    private static final String DEFAULT_CONFIG_FILENAME = "default_config.json";
    private static final String CONFIG_FILENAME = "config.json";

    public static ArrayList<DataSource> read() throws FileNotFoundException {
        return FileManager.readJSONArray(CONFIG_DIRECTORY, CONFIG_FILENAME, DataSource.class);
    }

    static ArrayList<DataSource> readDefault() throws FileNotFoundException {
        return FileManager.readJSONArray(CONFIG_DIRECTORY, DEFAULT_CONFIG_FILENAME, DataSource.class);
    }

    public static void write(ArrayList<DataSource> dataSources) throws IOException {
        FileManager.writeJSONArray(CONFIG_DIRECTORY, CONFIG_FILENAME, dataSources);
    }
}
