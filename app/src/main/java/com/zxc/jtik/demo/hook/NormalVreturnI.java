package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class NormalVreturnI extends TestItem {
    public NormalVreturnI(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "NormalVreturnI hook enter: obj=" + thisObj + ", args len =" + args.length);
                        Log.d(TestCase.TEST_TAG, "NormalVreturnI hook enter: member=" + ((NormalVreturnI)thisObj).member);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        int added = 20;
                        Log.d(TestCase.TEST_TAG, "NormalVreturnI hook exit add " + added);
                        return (int)retVale + added;
                    }).build();
    }

    public static Member getMember() {
        try {
            return NormalVreturnI.class.getDeclaredMethod("func");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        int ret = func();
        Log.d(TestCase.TEST_TAG, "NormalVreturnI func last return " + ret);
    }

    public int func() {
        int ret = 10;
        Log.d(TestCase.TEST_TAG, "NormalVreturnI func called, to return " + ret);
        return ret;
    }
    public int member = 11;
}
