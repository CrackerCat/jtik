package com.zxc.jtik;

/**
 * Created by zxc
 */
public class JtikConfig {

    public static final String agentPath = "libjtik_agent.so";

    public static final String nativeLib = "jtik";

    public interface NativeLibLoader  {
        void load();
    }
    public static boolean needHookSystemClass = false;

    /**
     * the hook logic not use target apk classloader
     */
    public static boolean inSeparateClassLoader = false;

    public static NativeLibLoader nativeLibLoader = () -> System.loadLibrary(nativeLib);

    public static boolean setNonDebuggableState = true;
}