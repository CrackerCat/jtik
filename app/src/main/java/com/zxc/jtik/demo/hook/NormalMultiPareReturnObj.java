package com.zxc.jtik.demo.hook;

import android.util.Log;

import com.zxc.jtik.MethodHook;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class NormalMultiPareReturnObj extends TestItem {
    public NormalMultiPareReturnObj(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj hook enter: obj=" + thisObj + ", args len =" + args.length);
                        String mem = ((NormalMultiPareReturnObj)thisObj).member;
                        Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj meber=" +mem);
                    })
                    .setMethodExitListener((thisObj, retVale) -> {
                        Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj hook exit");
                        return (String)retVale + " exit hooked,";
                    }).setParamModifier(0, (thisObject, inParam) -> {
                        Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj para " + inParam);
                        return 2;
                    }).setParamModifier(3, (thisObject, inParam) -> {
                    Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj para " + inParam);
                    return 'd';
                }).build();
    }

    public static Member getMember() {
        try {
            return NormalMultiPareReturnObj.class.getDeclaredMethod("func", int.class,long.class,boolean.class,char.class,double.class,float.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        String ret = func(1,2,false,'c', 1.5, 1.66f, "input");
        Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj func last return " + ret);
    }

    public String func(int i, long j, boolean z, char c, double d, float f, String input) {
        Log.d(TestCase.TEST_TAG, "NormalMultiPareReturnObj called: " + i + "," + j + "," + z + "," + c + "," + d + "," + f + "," + input);
        return input;
    }

    public String member = "hello";
}
