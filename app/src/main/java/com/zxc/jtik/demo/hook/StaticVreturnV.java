package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class StaticVreturnV extends TestItem {
    public StaticVreturnV(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "StaticVreturnV hook enter: obj=" + thisObj + ", args len=" + args.length);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "StaticVreturnV hook exit");
                        return null;
                    }).build();
    }

    public static Member getMember() {
        try {
            return StaticVreturnV.class.getDeclaredMethod("func");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        func();
    }

    public static void func() {
        Log.d(TestCase.TEST_TAG, "StaticVreturnV called");
    }
}
