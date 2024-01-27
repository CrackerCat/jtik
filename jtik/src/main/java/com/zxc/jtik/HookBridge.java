package com.zxc.jtik;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by zxc
 */
public class HookBridge {
    public enum HookedType{
        NOT_HOOKED,
        HOOKED_HERE,
        HOOKED_OTHER_LOADER
    }
    private static final String TAG = "Jtik_bridge";

    private static ClassLoader hookClassLoader = null;

    private static HookedType checkHooked(long jmethodId) {
        if (Jtik.hasHooked(jmethodId)) {
            return HookedType.HOOKED_HERE;
        }
        // 1. maybe from BootClassloader, when hook system class
        // 2. maybe use separate classloader between hooker and host, such as plugin
        if (hookClassLoader == null) {
            Application application = Utils.getCurrentApplication();
            if (application == null) {
                return HookedType.NOT_HOOKED;
            }
            try {
                System.load(new File(application.getDataDir(),JtikConfig.nativeLib + "pub").getAbsolutePath());
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                return HookedType.NOT_HOOKED;
            }
            hookClassLoader = (ClassLoader)Jtik.getHookerClassLoader(JtikConfig.agentPath);
        }
        if (hookClassLoader == null) {
            return HookedType.NOT_HOOKED;
        }
        try {
            Class jtikClz = hookClassLoader.loadClass("com.zxc.jtik.Jtik");
            Method method = jtikClz.getMethod("hasHooked", long.class);
            Boolean hookedObj = (Boolean) method.invoke(null, jmethodId);
            boolean hooked = hookedObj != null && hookedObj;
            if (hooked) {
                return HookedType.HOOKED_OTHER_LOADER;
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            return HookedType.NOT_HOOKED;
        }
        return HookedType.NOT_HOOKED;
    }

    private static Object reflectHandleMethodExit(long jMethodId,Object thisObj,Object orgReturn) {
        try {
            Class jtikClz = hookClassLoader.loadClass("com.zxc.jtik.Jtik");
            Method method = jtikClz.getMethod("handleMethodExit", long.class,Object.class, Object.class);
            return method.invoke(null, jMethodId, thisObj, orgReturn);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    private static Object reflectHandleMethodParamModify(long jMethodId, int paraIndex, Object thisObj,Object orgReturn) {
        try {
            Class jtikClz = hookClassLoader.loadClass("com.zxc.jtik.Jtik");
            Method method = jtikClz.getMethod("handleMethodParamModify", long.class,int.class, Object.class, Object.class);
            return method.invoke(null, jMethodId, paraIndex, thisObj, orgReturn);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static void onMethodEnter(long jmethodId, Object... args) {
        Log.i(TAG, "onMethodEnter arg len:" + args.length + " for jmethodId:" + jmethodId);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            Jtik.handleMethodEnter(jmethodId, args);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            try {
                Class jtikClz = hookClassLoader.loadClass("com.zxc.jtik.Jtik");
                Method method = jtikClz.getMethod("handleMethodEnter", long.class,Object[].class);
                method.invoke(null, jmethodId, args);
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "onMethodEnter not found hook method " + jmethodId);
        }

    }


    public static void onStaticMethodExit(long jmethodId) {
        Log.i(TAG, "onStaticMethodExit V");
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            Jtik.handleMethodExit(jmethodId, null, null);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            reflectHandleMethodExit(jmethodId, null, null);
        } else {
            Log.e(TAG, "onStaticMethodExit not found hook method " + jmethodId);
        }
    }
    public static boolean onStaticMethodExit(long jmethodId,boolean orgReturn) {
        Log.i(TAG, "onStaticMethodExit Z" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (boolean)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (boolean) reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit Z not found hook method " + jmethodId);
        }
        return false;
    }
    public static byte onStaticMethodExit(long jmethodId, byte orgReturn) {
        Log.i(TAG, "onStaticMethodExit B" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (byte)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (byte)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit B not found hook method " + jmethodId);
        }
        return 0;
    }
    public static char onStaticMethodExit(long jmethodId, char orgReturn) {
        Log.i(TAG, "onStaticMethodExit C:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (char)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (char)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit C not found hook method " + jmethodId);
        }
        return 0;
    }
    public static short onStaticMethodExit(long jmethodId, short orgReturn) {
        Log.i(TAG, "onStaticMethodExit S:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (short)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (short)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit S not found hook method " + jmethodId);
        }
        return 0;
    }
    public static int onStaticMethodExit(long jmethodId, int orgReturn) {
        Log.i(TAG, "onStaticMethodExit I:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (int)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (int)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit I not found hook method " + jmethodId);
        }
        return 0;
    }
    public static long onStaticMethodExit(long jmethodId, long orgReturn) {
        Log.i(TAG, "onStaticMethodExit J:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (long)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (long)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit J not found hook method " + jmethodId);
        }
        return 0L;
    }
    public static float onStaticMethodExit(long jmethodId, float orgReturn) {
        Log.i(TAG, "onStaticMethodExit F:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (float)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (float)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit F not found hook method " + jmethodId);
        }
        return 0.0f;
    }
    public static double onStaticMethodExit(long jmethodId, double orgReturn) {
        Log.i(TAG, "onStaticMethodExit D:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (double)Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (double)reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit D not found hook method " + jmethodId);
        }
        return 0;
    }
    public static Object onStaticMethodExit(long jmethodId, Object orgReturn) {
        Log.i(TAG, "onStaticMethodExit Object:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  Jtik.handleMethodExit(jmethodId, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return reflectHandleMethodExit(jmethodId, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticMethodExit Obj not found hook method " + jmethodId);
        }
        return null;
    }

    public static void onMethodExit(long jmethodId, Object thisObject) {
        Log.i(TAG, "onMethodExit V");
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            Jtik.handleMethodExit(jmethodId, thisObject, null);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            reflectHandleMethodExit(jmethodId, thisObject, null);
        } else {
            Log.e(TAG, "onMethodExit V not found hook method " + jmethodId);
        }
    }
    public static boolean onMethodExit(long jmethodId, Object thisObject, boolean orgReturn) {
        Log.i(TAG, "onMethodExit Z" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (boolean)Jtik.handleMethodExit(jmethodId, thisObject, null);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (boolean)reflectHandleMethodExit(jmethodId, thisObject, null);
        } else {
            Log.e(TAG, "onMethodExit Z not found hook method " + jmethodId);
        }
        return false;
    }
    public static byte onMethodExit(long jmethodId, Object thisObject, byte orgReturn) {
        Log.i(TAG, "onMethodExit B" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (byte)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (byte)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit B not found hook method " + jmethodId);
        }
        return 0;
    }
    public static char onMethodExit(long jmethodId, Object thisObject, char orgReturn) {
        Log.i(TAG, "onMethodExit C:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (char)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (char)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit C not found hook method " + jmethodId);
        }
        return 0;
    }
    public static short onMethodExit(long jmethodId, Object thisObject, short orgReturn) {
        Log.i(TAG, "onMethodExit S:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (short)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (short)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit S not found hook method " + jmethodId);
        }
        return 0;
    }
    public static int onMethodExit(long jmethodId, Object thisObject, int orgReturn) {
        Log.i(TAG, "onMethodExit I:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (int)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (int)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit I not found hook method " + jmethodId);
        }
        return 0;
    }
    public static long onMethodExit(long jmethodId, Object thisObject, long orgReturn) {
        Log.i(TAG, "onMethodExit J:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (long)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (long)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit J not found hook method " + jmethodId);
        }
        return 0L;
    }
    public static float onMethodExit(long jmethodId, Object thisObject, float orgReturn) {
        Log.i(TAG, "onMethodExit F:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (float)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (float)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit F not found hook method " + jmethodId);
        }
        return 0.0f;
    }
    public static double onMethodExit(long jmethodId, Object thisObject, double orgReturn) {
        Log.i(TAG, "onMethodExit D:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return (double)Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (double)reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit D not found hook method " + jmethodId);
        }
        return 0;
    }
    public static Object onMethodExit(long jmethodId, Object thisObject, Object orgReturn) {
        Log.i(TAG, "onMethodExit Object:" + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return Jtik.handleMethodExit(jmethodId, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return reflectHandleMethodExit(jmethodId, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onMethodExit B not found hook method " + jmethodId);
        }
        return null;
    }


    public static boolean onStaticModifyParameter(long jmethodId,int paraIndex, boolean orgReturn) {
        Log.i(TAG, "onStaticModifyParameter Z " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (boolean)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (boolean)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter Z not found hook method " + jmethodId);
        }
        return false;
    }
    public static byte onStaticModifyParameter(long jmethodId, int paraIndex, byte orgReturn) {
        Log.i(TAG, "onStaticModifyParameter B " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (byte)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (byte)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter B not found hook method " + jmethodId);
        }
        return 0;
    }
    public static char onStaticModifyParameter(long jmethodId, int paraIndex, char orgReturn) {
        Log.i(TAG, "onStaticModifyParameter C " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (char)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (char)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter C not found hook method " + jmethodId);
        }
        return 0;
    }
    public static short onStaticModifyParameter(long jmethodId, int paraIndex, short orgReturn) {
        Log.i(TAG, "onStaticModifyParameter S " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (short)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (short)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter S not found hook method " + jmethodId);
        }
        return 0;
    }
    public static int onStaticModifyParameter(long jmethodId, int paraIndex, int orgReturn) {
        Log.i(TAG, "onStaticModifyParameter I " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (int)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (int)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter I not found hook method " + jmethodId);
        }
        return 0;
    }
    public static long onStaticModifyParameter(long jmethodId, int paraIndex, long orgReturn) {
        Log.i(TAG, "onStaticModifyParameter J " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (long)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (long)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter J not found hook method " + jmethodId);
        }
        return 0L;
    }
    public static float onStaticModifyParameter(long jmethodId, int paraIndex, float orgReturn) {
        Log.i(TAG, "onStaticModifyParameter F " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (float)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (float)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter F not found hook method " + jmethodId);
        }
        return 0.0f;
    }
    public static double onStaticModifyParameter(long jmethodId, int paraIndex, double orgReturn) {
        Log.i(TAG, "onStaticModifyParameter D " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (double)Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (double)reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter D not found hook method " + jmethodId);
        }
        return 0;
    }
    public static Object onStaticModifyParameter(long jmethodId, int paraIndex, Object orgReturn) {
        Log.i(TAG, "onStaticModifyParameter Object " + paraIndex +  ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  Jtik.handleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return reflectHandleMethodParamModify(jmethodId, paraIndex, null, orgReturn);
        } else {
            Log.e(TAG, "onStaticModifyParameter Object not found hook method " + jmethodId);
        }
        return null;
    }

    public static boolean onModifyParameter(long jmethodId,int paraIndex, Object thisObject, boolean orgReturn) {
        Log.i(TAG, "onModifyParameter Z " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (boolean)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (boolean)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter Z not found hook method " + jmethodId);
        }
        return false;
    }
    public static byte onModifyParameter(long jmethodId, int paraIndex, Object thisObject, byte orgReturn) {
        Log.i(TAG, "onModifyParameter B " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (byte)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (byte)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter B not found hook method " + jmethodId);
        }
        return 0;
    }
    public static char onModifyParameter(long jmethodId, int paraIndex, Object thisObject, char orgReturn) {
        Log.i(TAG, "onModifyParameter C " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (char)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (char)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter C not found hook method " + jmethodId);
        }
        return 0;
    }
    public static short onModifyParameter(long jmethodId, int paraIndex, Object thisObject, short orgReturn) {
        Log.i(TAG, "onModifyParameter S " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (short)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (short)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter S not found hook method " + jmethodId);
        }
        return 0;
    }
    public static int onModifyParameter(long jmethodId, int paraIndex, Object thisObject, int orgReturn) {
        Log.i(TAG, "onModifyParameter I " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (int)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (int)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter I not found hook method " + jmethodId);
        }
        return 0;
    }
    public static long onModifyParameter(long jmethodId, int paraIndex, Object thisObject, long orgReturn) {
        Log.i(TAG, "onModifyParameter J " + paraIndex +  ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (long)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (long)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter J not found hook method " + jmethodId);
        }
        return 0L;
    }
    public static float onModifyParameter(long jmethodId, int paraIndex, Object thisObject, float orgReturn) {
        Log.i(TAG, "onModifyParameter F " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (float)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (float)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter F not found hook method " + jmethodId);
        }
        return 0.0f;
    }
    public static double onModifyParameter(long jmethodId, int paraIndex, Object thisObject, double orgReturn) {
        Log.i(TAG, "onModifyParameter D " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  (double)Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return (double)reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter D not found hook method " + jmethodId);
        }
        return 0;
    }
    public static Object onModifyParameter(long jmethodId, int paraIndex, Object thisObject, Object orgReturn) {
        Log.i(TAG, "onModifyParameter Object " + paraIndex + ",org:" + orgReturn);
        HookedType hookedType = checkHooked(jmethodId);
        if (hookedType == HookedType.HOOKED_HERE) {
            return  Jtik.handleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else if (hookedType == HookedType.HOOKED_OTHER_LOADER) {
            return reflectHandleMethodParamModify(jmethodId, paraIndex, thisObject, orgReturn);
        } else {
            Log.e(TAG, "onModifyParameter Object not found hook method " + jmethodId);
        }
        return null;
    }
}
