package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class StaticIreturnI extends TestItem {
    public StaticIreturnI(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "StaticIreturnI hook enter: obj=" + thisObj + ", args len=" + args.length);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "StaticIreturnI hook exit");
                        return retVale;
                    }).setParamModifier(0, (thisObject, inParam) -> {
                        Log.d(TestCase.TEST_TAG, "StaticIreturnI hook para: org is " + inParam);
                        return 6;
                    }).build();
    }

    public static Member getMember() {
        try {
            return StaticIreturnI.class.getDeclaredMethod("func", int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        int ret = func(5);
        Log.d(TestCase.TEST_TAG, "StaticIreturnI last ret " + ret);
    }

    public static int func(int a) {
        Log.d(TestCase.TEST_TAG, "StaticIreturnI called, to return " + (a+100));
        return a+100;
    }
}
