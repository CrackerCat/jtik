package com.zxc.jtik;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

public class Utils {

    public static Application getCurrentApplication() {
        try {
            Class ActivityThreadClz = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = ActivityThreadClz.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            Object ApplicationObj = currentApplicationMethod.invoke(null);
            if (ApplicationObj != null) {
                return (Application)ApplicationObj;
            }
        } catch (Throwable e) {
            Log.e(Jtik.TAG, e.getMessage(),e);
        }
        return null;
    }

    public static boolean FileCopy(File source, File dest){
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            long size = sourceChannel.size();
            long transferred = 0;
            while(transferred < size){
                transferred += destChannel.transferFrom(sourceChannel, transferred, size-transferred);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }
}
