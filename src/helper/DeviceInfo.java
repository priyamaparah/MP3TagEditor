package helper;

import android.content.Context;

import java.util.Date;


public interface DeviceInfo
{
    public Date buildDate();
    public String buildVersion();
    public Context getApplicationContext();
}