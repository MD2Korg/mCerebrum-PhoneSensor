package org.md2k.phonesensor;

import android.os.Environment;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.source.datasource.DataSourceType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
public class BCMRecord {
    private static BCMRecord instance = null;
    OutputStreamWriter outputStreamWriterBattery;
    FileOutputStream fosBattery;
    OutputStreamWriter outputStreamWriterCPU;
    FileOutputStream fosCPU;
    OutputStreamWriter outputStreamWriterMemory;
    FileOutputStream fosMemory;

    public static BCMRecord getInstance() {
        if (instance == null) instance = new BCMRecord();
        return instance;
    }

    void createFileBattery() {
        String filename = "battery.csv";
        try {
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            file.createNewFile();
            fosBattery = new FileOutputStream(file);
            outputStreamWriterBattery = new OutputStreamWriter(fosBattery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void createFileCPU() {
        String filename = "cpu.csv";
        try {
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            file.createNewFile();
            fosCPU = new FileOutputStream(file);
            outputStreamWriterCPU = new OutputStreamWriter(fosCPU);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void createFileMemory() {
        String filename = "memory.csv";
        try {
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            file.createNewFile();
            fosMemory = new FileOutputStream(file);
            outputStreamWriterMemory = new OutputStreamWriter(fosMemory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BCMRecord() {
        createFileBattery();
        createFileCPU();
        createFileMemory();
    }

    public void saveDataToTextFile(String dataSourceType, DataType data) {
        String dataStr = String.valueOf(data.getDateTime());
        if (data instanceof DataTypeFloatArray) {
            DataTypeFloatArray d = (DataTypeFloatArray) data;
            for (int i = 0; i < d.getSample().length; i++)
                dataStr += "," + d.getSample()[i];
        } else if (data instanceof DataTypeDoubleArray) {
            DataTypeDoubleArray d = (DataTypeDoubleArray) data;
            for (int i = 0; i < d.getSample().length; i++)
                dataStr += "," + d.getSample()[i];
        } else if (data instanceof DataTypeFloat) {
            DataTypeFloat d = (DataTypeFloat) data;
            dataStr += "," + d.getSample();
        } else if (data instanceof DataTypeInt) {
            DataTypeInt d = (DataTypeInt) data;
            dataStr += "," + d.getSample();
        } else if (data instanceof DataTypeIntArray) {
            DataTypeIntArray d = (DataTypeIntArray) data;
            for (int i = 0; i < d.getSample().length; i++)
                dataStr += "," + d.getSample()[i];
        }
        dataStr += "\n";
        try {
            if (dataSourceType.equals(DataSourceType.BATTERY))
                outputStreamWriterBattery.append(dataStr);
            if (dataSourceType.equals(DataSourceType.CPU))
                outputStreamWriterCPU.append(dataStr);
            if (dataSourceType.equals(DataSourceType.MEMORY))
                outputStreamWriterMemory.append(dataStr);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void closeFileBattery() {
        try {
            outputStreamWriterBattery.close();
            fosBattery.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void closeFileCPU() {
        try {
            outputStreamWriterCPU.close();
            fosCPU.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void closeFileMemory() {
        try {
            outputStreamWriterMemory.close();
            fosMemory.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close() {
        closeFileBattery();
        closeFileCPU();
        closeFileMemory();
        instance = null;
    }

}
