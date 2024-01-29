package com.zxc.jtik;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zxc
 */
public class Jtik {
    public static final String TAG = "Jtik";
    private static final Map<Long, MethodHook> sHookers = new ConcurrentHashMap<>();

    private static boolean sInitialized = false;
    public static synchronized boolean init(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return false;
        }
        if (sInitialized) {
            return false;
        }
        try {
            JtikConfig.nativeLibLoader.load();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Debug.attachJvmtiAgent(JtikConfig.agentPath, null, context.getClassLoader());
            } else {
                Class vmDebugClazz = Class.forName("dalvik.system.VMDebug");
                Method attachAgentMethod = vmDebugClazz.getMethod("attachAgent", String.class);
                attachAgentMethod.setAccessible(true);
                attachAgentMethod.invoke(null, JtikConfig.agentPath);
            }
            boolean ret = initAfterAgentAttach(HookBridge.class.getName(), "onMethodEnter",
                    "onMethodExit", "onStaticMethodExit",
                    "onModifyParameter","onStaticModifyParameter",
                    JtikConfig.agentPath,
                    JtikConfig.needHookSystemClass ? context.getPackageCodePath() : null,
                    JtikConfig.setNonDebuggableState);
            if(!ret) {
                return false;
            }
            if (JtikConfig.needHookSystemClass || JtikConfig.inSeparateClassLoader) {
                boolean absolutePath  = JtikConfig.nativeLib.startsWith("/");
                String libPath = absolutePath ? JtikConfig.nativeLib
                        : new File(context.getApplicationInfo().nativeLibraryDir, System.mapLibraryName(JtikConfig.nativeLib)).getAbsolutePath();
                boolean copy = Utils.FileCopy(new File(libPath), new File(context.getDataDir(),JtikConfig.nativeLib + "pub"));
                Log.i(TAG, "copy " + copy + ":" + libPath + " to " + new File(context.getDataDir(),JtikConfig.nativeLib).getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(),e);
            return false;
        }
        sInitialized = true;
        return true;
    }

    public static boolean isInitialized() {
        return sInitialized;
    }

    public static synchronized UnHooker hook(Member targetMethod, MethodHook callback) {
        if (callback == null || !sInitialized) {
            return null;
        }

        boolean hookEnter = callback.getMethodEnter() != null;
        boolean hookExit = callback.getMethodExit() != null;
        boolean hookParameters = callback.hasParamModifiers();
        Log.i(TAG, "hook: " + targetMethod.getDeclaringClass() + "," + targetMethod.getName());
        int[] paramModifierIndex = null;
        if (hookParameters) {
            SparseArray<MethodHook.IParamModifyOperation> modifications = callback.getParamModifiers();
            paramModifierIndex = new int[modifications.size()];
            for (int i = 0; i < modifications.size(); i++) {
                paramModifierIndex[i] = modifications.keyAt(i);
            }
        }

        //TODO:native 和 proxy
        long nativePtr =  doHookTransform(targetMethod, hookEnter,hookExit, paramModifierIndex);
        if (nativePtr ==0) {
            return null;
        } else {
            Log.d(TAG, "hook success " + nativePtr);
            sHookers.put(nativePtr, callback); //TODO:暂时一个callback
            return new UnHooker(nativePtr);
        }
    }

    public static synchronized boolean unHook(UnHooker unHooker) {
        if (unHooker == null) {
            return false;
        }
        return doUnHookTransform(unHooker.getId());
    }
    public static void handleMethodEnter(long jMethodId,Object... args) {
        MethodHook result = sHookers.get(jMethodId);
        if (result == null) {
            throw new AssertionError("handleMethodEnter not found hooker !!! for " + jMethodId);
        }
        Object thisObj = args[0];
        Object[] paras = Arrays.copyOfRange(args, 1, args.length);
        result.getMethodEnter().onMethodEnter(thisObj, paras);
    }
    public static Object handleMethodExit(long jMethodId,Object thisObj,Object orgReturn) {
        MethodHook result = sHookers.get(jMethodId);
        if (result == null) {
            throw new AssertionError("handleMethodExit not found hooker !!! for " + jMethodId);
        }
        return result.getMethodExit().onMethodExit(thisObj, orgReturn);
    }

    public static Object handleMethodParamModify(long jMethodId, int paraIndex, Object thisObj,Object orgReturn) {
        MethodHook result = sHookers.get(jMethodId);
        if (result == null) {
            throw new AssertionError("handleMethodExit not found hooker !!! for " + jMethodId);
        }
        return result.getParamModifiers().get(paraIndex).change(thisObj, orgReturn);
    }

    public static boolean hasHooked(long jMethodId) {
        return  sHookers.get(jMethodId) != null;
    }

    private static native boolean initAfterAgentAttach(String bridgeClass,
                                                       String enterMethodName,
                                                       String exitMethodName,
                                                       String staticExitMethodName,
                                                       String modifyParamMethodName,
                                                       String staticModifyParamMethodName,
                                                       String agentPath,
                                                       String pathAppendToSys,
                                                       boolean setNoDebuggable);
    private static native long doHookTransform(Member targetMethod, boolean hookEnter, boolean hookExit, int... parameterIndexes);

    private static native boolean doUnHookTransform(long jmethodId);

    public static native Object getHookerClassLoader(String agent);

}
