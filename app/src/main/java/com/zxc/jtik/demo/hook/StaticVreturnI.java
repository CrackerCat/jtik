package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class StaticVreturnI extends TestItem {
    public StaticVreturnI(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "StaticVreturnI hook enter: obj=" + thisObj + ", args len =" + args.length);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        int added = 10;
                        Log.d(TestCase.TEST_TAG, "StaticVreturnI hook exit add " + added);
                        return (int)retVale + added;
                    }).build();
    }

    public static Member getMember() {
        try {
            return StaticVreturnI.class.getDeclaredMethod("func");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        int ret = func();
        Log.d(TestCase.TEST_TAG, "funStaticV_I return " + ret);
    }

    public static int func() {
        int ret = 10;
        Log.d(TestCase.TEST_TAG, "funStaticV_I called, to return " + ret);
        return ret;
    }
}
