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
import android.os.Environment;
import android.util.Log;

import org.md2k.datakitapi.source.AbstractObject;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.mcerebrum.commons.storage.Storage;
import org.md2k.mcerebrum.commons.storage.StorageType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class handles the configuration file management for this application.
 */
public class Configuration {
    private static final String CONFIG_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mCerebrum/org.md2k.phonesensor/";
    private static final String DEFAULT_CONFIG_FILENAME = "default_config.json";
    private static final String CONFIG_FILENAME = "config.json";

    /**
     * Reads the configuration file
     *
     * <p>
     *     The configuration file should be stored at <code>root/mCerebrum/org.md2k.phonesensor/config.json</code>
     *     by default.
     * </p>
     *
     * @param context Android context
     * @return an ArrayList of available data sources
     * @throws FileNotFoundException when a configuration file cannot be found.
     */
    public static ArrayList<DataSource> read(Context context) throws FileNotFoundException {
        String root=Storage.getRootDirectory(context, StorageType.SDCARD_INTERNAL);
        if(root==null) throw new FileNotFoundException();
        String filePath=root+"/mCerebrum/"+context.getPackageName()+"/"+CONFIG_FILENAME;
        return Storage.readJsonArrayList(filePath, DataSource.class);
    }

    /**
     * Reads the meta data json file for the application.
     *
     * @return an ArrayList of data sources
     */
    private static ArrayList<DataSource> readMetaData(){
        try {
            return Storage.readJsonArrayFromAsset(MyApplication.getContext(), Constants.FILENAME_ASSET_METADATA, DataSource.class);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets the meta data for the given data source type
     *
     * @param dataSourceType Data source type in question
     * @return The data source that corresponds to the given type
     */
    public static DataSource getMetaData(String dataSourceType){
        ArrayList<DataSource> dataSources = readMetaData();
        if(dataSources==null || dataSources.size()==0) return null;
        for(int i=0;i<dataSources.size();i++)
            if(dataSources.get(i).getType().equals(dataSourceType.toUpperCase()))
                return dataSources.get(i);
        return null;
    }

    /**
     * Reads the default configuration file
     *
     * @param context Android context
     * @return An ArrayList of data sources
     * @throws FileNotFoundException
     */
    static ArrayList<DataSource> readDefault(Context context) throws FileNotFoundException {
        String root=Storage.getRootDirectory(context, StorageType.SDCARD_INTERNAL);
        if(root==null) throw new FileNotFoundException();
        String filePath=root+"/mCerebrum/"+context.getPackageName()+"/"+DEFAULT_CONFIG_FILENAME;
        return Storage.readJsonArrayList(filePath, DataSource.class);
    }

    /**
     * Writes the list of available data sources into the configuration file.
     *
     * @param context Android context
     * @param dataSources ArrayList of available data sources
     * @throws IOException
     */
    public static void write(Context context, ArrayList<DataSource> dataSources) throws IOException {
        String root=Storage.getRootDirectory(context, StorageType.SDCARD_INTERNAL);
        if(root==null) throw new FileNotFoundException();
        String filePath=root+"/mCerebrum/"+context.getPackageName()+"/"+CONFIG_FILENAME;
        Storage.writeJsonArray(filePath, dataSources);
    }

    /**
     * Reads the list of data sources and default data sources and compares them.
     *
     * @param context Android context
     * @return True as long as both files are readable and are the same
     */
    public static boolean isEqualDefault(Context context) {
        ArrayList<DataSource> dataSources;
        ArrayList<DataSource> dataSourcesDefault;
        try {
            dataSources = read(context);
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            dataSourcesDefault = readDefault(context);
        } catch (FileNotFoundException e) {
            return true;
        }
        if (dataSources.size() != dataSourcesDefault.size()) return false;
        for (int i = 0; i < dataSources.size(); i++) {
            if (!isDataSourceMatch(dataSources.get(i), dataSourcesDefault)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks for a data source in the default ArrayList that is equal to the singular data source
     * passed to this method.
     *
     * @param dataSource data source to compare
     * @param dataSourcesDefault ArrayList of default data sources
     * @return Whether the data source is equal to one of the defaults.
     */
    private static boolean isDataSourceMatch(DataSource dataSource, ArrayList<DataSource> dataSourcesDefault){
        for(int i=0;i<dataSourcesDefault.size();i++){
            DataSource dataSourceDefault=dataSourcesDefault.get(i);
            if(isEqualDataSource(dataSource, dataSourceDefault)) return true;
        }
        return false;
    }

    /**
     * Checks the data source against the default for comparison.
     *
     * @param dataSource non-default data source
     * @param dataSourceDefault default data source
     * @return Whether the data sources are a match.
     */
    private static boolean isEqualDataSource(DataSource dataSource, DataSource dataSourceDefault){
        if(!isFieldMatch(dataSource.getId(), dataSourceDefault.getId())) return false;
        if(!isFieldMatch(dataSource.getType(), dataSourceDefault.getType())) return false;
        if(!isMetaDataMatch(dataSource.getMetadata(), dataSourceDefault.getMetadata())) return false;
        if(!isObjectMatch(dataSource.getPlatform(), dataSourceDefault.getPlatform())) return false;
        if(!isObjectMatch(dataSource.getPlatformApp(), dataSourceDefault.getPlatformApp())) return false;
        if(!isObjectMatch(dataSource.getApplication(), dataSourceDefault.getApplication())) return false;
        return true;
    }

    /**
     * Checks the object against the default.
     *
     * @param object Object to check
     * @param objectDefault Object to check against
     * @return Whether the objects are equivalent.
     */
    private static boolean isObjectMatch(AbstractObject object, AbstractObject objectDefault){
        if(objectDefault==null) return true;
        if(object==null) return false;
        if(!isFieldMatch(object.getId(), objectDefault.getId())) return false;
        if(!isFieldMatch(object.getType(), objectDefault.getType())) return false;
        return true;
    }

    /**
     * Checks the value against the default.
     *
     * @param value value to check
     * @param valueDefault value to check against
     * @return Whether the values are equivalent.
     */
    private static boolean isFieldMatch(String value, String valueDefault){
        if(valueDefault==null) return true;
        if(value==null) return false;
        if(value.equals(valueDefault)) return true;
        return false;
    }

    /**
     * Checks the metadata against the default.
     *
     * @param metadata metadata to check
     * @param metadataDefault metadata to check against
     * @return Whether the metadata is equivalent.
     */
    private static boolean isMetaDataMatch(HashMap<String, String> metadata, HashMap<String, String> metadataDefault){
        String valueDefault, value;
        if(metadataDefault==null) return true;
        if(metadata==null) return false;
        for(String key:metadataDefault.keySet()){
            if(!metadata.containsKey(key)) return false;
            valueDefault=metadataDefault.get(key);
            value=metadata.get(key);
            if(!value.equals(valueDefault))return false;
        }
        return true;
    }

    /**
     * Checks for configuration
     *
     * <p>
     *     Configuration is checked by reading a list of data sources. If the list is null or has
     *     no nodes then nothing was configured.
     * </p>
     *
     * @return Whether the ArrayList has nodes
     */
    public static boolean isConfigured(){
        ArrayList<DataSource> dataSources;
        try {
            dataSources = read(MyApplication.getContext());
        } catch (FileNotFoundException e) {
            return false;
        }
        if(dataSources==null || dataSources.size()==0) return false;
        return true;
    }

}
