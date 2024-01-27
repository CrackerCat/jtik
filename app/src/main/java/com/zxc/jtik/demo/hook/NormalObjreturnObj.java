package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class NormalObjreturnObj extends TestItem {
    public NormalObjreturnObj(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.i(TestCase.TEST_TAG, "NormalObjreturnObj hook enter: obj=" + thisObj + ", args len =" + args.length);
                        Log.i(TestCase.TEST_TAG, "NormalObjreturnObj hook enter: member=" + ((NormalObjreturnObj)thisObj).member);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.i(TestCase.TEST_TAG, "NormalObjreturnObj hook exit");
                        return (String)retVale + " exit hooked,";
                    }).setParamModifier(0, (thisObject, inParam) -> {
                        Log.i(TestCase.TEST_TAG, "NormalObjreturnObj para " + inParam);
                        return (String)inParam + ",param hook,";
                    }).build();
    }

    public static Member getMember() {
        try {
            return NormalObjreturnObj.class.getDeclaredMethod("func", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        String ret = func("world");
        Log.i(TestCase.TEST_TAG, "NormalObjreturnObj func last return " + ret);
    }

    public String func(String input) {
        String ret = "hello:" + input;
        Log.i(TestCase.TEST_TAG, "NormalObjreturnObj func called, to return " + ret);
        return ret;
    }
    public String member = "i am member";
}
