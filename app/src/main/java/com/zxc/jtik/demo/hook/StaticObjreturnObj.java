package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class StaticObjreturnObj extends TestItem {
    public StaticObjreturnObj(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "StaticObjreturnObj hook enter: obj=" + thisObj + ", args len =" + args.length);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "StaticObjreturnObj hook exit");
                        return (String)retVale + " exit hooked,";
                    }).setParamModifier(0, (thisObject, inParam) -> {
                        Log.d(TestCase.TEST_TAG, "StaticObjreturnObj para " + inParam);
                        return (String)inParam + ",param hook,";
                    }).build();
    }

    public static Member getMember() {
        try {
            return StaticObjreturnObj.class.getDeclaredMethod("func", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        String ret = func("world");
        Log.d(TestCase.TEST_TAG, "StaticObjreturnObj func last return " + ret);
    }

    public static String func(String input) {
        String ret = "hello:" + input;
        Log.d(TestCase.TEST_TAG, "StaticObjreturnObj func called, to return " + ret);
        return ret;
    }
}
