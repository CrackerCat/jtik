package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class NormalLongreturnLong extends TestItem {
    public NormalLongreturnLong(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "NormalLongreturnLong hook enter: obj=" + thisObj + ", args len=" + args.length);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "NormalLongreturnLong hook exit");
                        return retVale;
                    }).setParamModifier(0, (thisObject, inParam) -> {
                        Log.d(TestCase.TEST_TAG, "NormalLongreturnLong hook para: org is " + inParam);
                        return 6666666666666L;
                    }).build();
    }

    public static Member getMember() {
        try {
            return NormalLongreturnLong.class.getDeclaredMethod("func", long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        long ret = func(555555555L);
        Log.d(TestCase.TEST_TAG, "NormalLongreturnLong last ret " + ret);
    }

    public long func(long a) {
        //Log.d(TestCase.TEST_TAG, "NormalLongreturnLong called, to return " + (a+100));
        return a+1000000L;
    }
}
