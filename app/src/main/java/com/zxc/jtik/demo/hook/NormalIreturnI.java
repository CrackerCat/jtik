package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class NormalIreturnI extends TestItem {
    public NormalIreturnI(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "NormalIreturnI hook enter: obj=" + thisObj + ", args len=" + args.length);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "NormalIreturnI hook exit");
                        return retVale;
                    }).setParamModifier(0, (thisObject, inParam) -> {
                        Log.d(TestCase.TEST_TAG, "NormalIreturnI hook para: org is " + inParam);
                        return 6;
                    }).build();
    }

    public static Member getMember() {
        try {
            return NormalIreturnI.class.getDeclaredMethod("func", int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        int ret = func(5);
        Log.d(TestCase.TEST_TAG, "NormalIreturnI last ret " + ret);
    }

    public int func(int a) {
        Log.d(TestCase.TEST_TAG, "NormalIreturnI called, to return " + (a+100));
        return a+100;
    }
}
