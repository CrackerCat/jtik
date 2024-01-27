package com.zxc.jtik.demo;

import com.zxc.jtik.demo.hook.HookActivity;
import com.zxc.jtik.demo.hook.NormalIreturnI;
import com.zxc.jtik.demo.hook.NormalLongreturnLong;
import com.zxc.jtik.demo.hook.NormalMultiPareReturnObj;
import com.zxc.jtik.demo.hook.NormalObjreturnObj;
import com.zxc.jtik.demo.hook.NormalVreturnI;
import com.zxc.jtik.demo.hook.NormalVreturnV;
import com.zxc.jtik.demo.hook.StaticIreturnI;
import com.zxc.jtik.demo.hook.StaticLongreturnLong;
import com.zxc.jtik.demo.hook.StaticMultiPareReturnObj;
import com.zxc.jtik.demo.hook.StaticObjreturnObj;
import com.zxc.jtik.demo.hook.StaticVreturnI;
import com.zxc.jtik.demo.hook.StaticVreturnV;

/**
 * Created by zxc
 */
public class TestCase {
    public static final String TEST_TAG = "jtik_test";

    public static final int HOOK_ENTRY = 1;
    public static final int HOOK_EXIT = 1<<1;
    public static final int HOOK_PARA = 1<<2;


    public static final TestItem[] sTestItems = {
            new StaticVreturnV("static void f()", StaticVreturnV.getMember(), StaticVreturnV.getHook()),
            new StaticVreturnI("static int f()", StaticVreturnI.getMember(), StaticVreturnI.getHook()),
            new StaticIreturnI("static int f(int)", StaticIreturnI.getMember(), StaticIreturnI.getHook()),
            new StaticLongreturnLong("static long f(long)", StaticLongreturnLong.getMember(), StaticLongreturnLong.getHook()),
            new StaticObjreturnObj("static obj f(obj)", StaticObjreturnObj.getMember(), StaticObjreturnObj.getHook()),
            new StaticMultiPareReturnObj("static obj f(...)", StaticMultiPareReturnObj.getMember(), StaticMultiPareReturnObj.getHook()),
            new NormalVreturnV("void f()", NormalVreturnV.getMember(), NormalVreturnV.getHook()),
            new NormalVreturnI("int f()", NormalVreturnI.getMember(), NormalVreturnI.getHook()),
            new NormalIreturnI("int f(int)", NormalIreturnI.getMember(), NormalIreturnI.getHook()),
            new NormalLongreturnLong("long f(long)", NormalLongreturnLong.getMember(), NormalLongreturnLong.getHook()),
            new NormalObjreturnObj("obj f(obj)", NormalObjreturnObj.getMember(), NormalObjreturnObj.getHook()),
            new NormalMultiPareReturnObj("obj f(...)", NormalMultiPareReturnObj.getMember(), NormalMultiPareReturnObj.getHook()),
            new HookActivity("hook activity", HookActivity.getMember(), HookActivity.getHook()),
    };
}
