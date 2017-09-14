package org.md2k.phonesensor;

import android.content.Intent;

import org.md2k.mcerebrum.commons.app_info.AppInfo;
import org.md2k.mcerebrum.core.access.AbstractServiceMCerebrum;

public class ServiceMCerebrum extends AbstractServiceMCerebrum {
    @Override
    protected boolean hasClear() {
        return false;
    }

    @Override
    public void initialize() {
        Intent intent=new Intent(this, ActivityPermission.class);

    }

    @Override
    public void launch() {

    }

    @Override
    public void startBackground() {
        Intent intent=new Intent(this, ActivityMain.class);
        intent.putExtra(ActivityMain.OPERATION,ActivityMain.OPERATION_START_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void stopBackground() {
        Intent intent=new Intent(this, ActivityMain.class);
        intent.putExtra(ActivityMain.OPERATION,ActivityMain.OPERATION_STOP_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void report() {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(ActivityMain.OPERATION,ActivityMain.OPERATION_PLOT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Override
    public void clear() {

    }

    @Override
    public boolean hasReport() {
        return true;
    }

    @Override
    public boolean isRunInBackground() {
        return true;
    }

    @Override
    public long getRunningTime() {
        return AppInfo.serviceRunningTime(this, ServicePhoneSensor.class.getName());
    }

    @Override
    public boolean isRunning() {
        return AppInfo.isServiceRunning(this, ServicePhoneSensor.class.getName());

    }

    @Override
    public boolean isConfigured() {
        return Configuration.isConfigured();
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean hasInitialize() {
        return true;
    }

    @Override
    public void configure() {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(ActivityMain.OPERATION,ActivityMain.OPERATION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public boolean isEqualDefault() {
        return Configuration.isEqualDefault(MyApplication.getContext());
    }
}
