package com.example.mymp3tageditor;



import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import helper.DeviceInfo;


import java.util.Date;
import java.util.zip.ZipFile;


public class Mp3EditorApplication extends Application
                                implements DeviceInfo {

    private static final String TAG = Mp3EditorApplication.class.getSimpleName();

    @Override public void onCreate()
    {
        super.onCreate();
    }

    private Date _buildDate;
    @Override
    public Date buildDate() {

        synchronized (this) {
            if (_buildDate == null) {
                try {
                    PackageManager pm = getPackageManager();
                    String pkg = getClass().getPackage().getName();
                    ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                    ZipFile zf = new ZipFile(ai.sourceDir);
                    _buildDate = new Date(zf.getEntry("classes.dex").getTime());
                } catch (Exception e) {
                    Log.w(TAG, "could not compute build date.");
                    _buildDate = new Date(0);
                }
            }
        }
        return _buildDate;

    }

    private String _version;
    @Override
    public String buildVersion() {

        synchronized (this) {
            if (_version == null) {
                try {
                    PackageManager pm = getPackageManager();
                    String pkg = getClass().getPackage().getName();
                    _version = pm.getPackageInfo(pkg, 0).versionName;
                } catch (Exception e) {
                    Log.w(TAG, "could not compute version string.");
                    _version = "version-unknown";
                }
            }
        }

        return _version;

    }

    @Override
    public Context getApplicationContext() {
        return this;
    }
}
