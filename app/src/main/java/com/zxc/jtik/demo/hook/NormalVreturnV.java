package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class NormalVreturnV extends TestItem {
    public NormalVreturnV(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "public class NormalVreturnV hook enter: obj=" + thisObj + ", args len=" + args.length);
                        NormalVreturnV obj = (NormalVreturnV)thisObj;
                        Log.d(TestCase.TEST_TAG, "public class NormalVreturnV hook enter: mem =" + obj.member);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "NormalVreturnV hook exit");
                        return null;
                    }).build();
    }

    public static Member getMember() {
        try {
            return NormalVreturnV.class.getDeclaredMethod("func");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        func();
    }

    public void func() {
        Log.d(TestCase.TEST_TAG, "NormalVreturnV called");
    }

    public int member = 6;
}
