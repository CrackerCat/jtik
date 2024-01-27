package com.zxc.jtik.demo;

import android.util.Log;
import android.widget.Toast;

import com.zxc.jtik.Jtik;
import com.zxc.jtik.MethodHook;
import com.zxc.jtik.UnHooker;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public abstract class TestItem {
    public TestItem(String name, Member targetMethod, MethodHook hook) {
     this.name = name;
     this.targetMethod = targetMethod;
     this.hookCb = hook;
    }
    private String name;

    protected Member targetMethod;
    protected MethodHook hookCb;

    public String getName() {
        return name;
    }
    public void run() {
        UnHooker unHooker = Jtik.hook(targetMethod, hookCb);
        if (unHooker == null) {
            Toast.makeText(App.sAppContext, "hook fail, init = " + Jtik.isInitialized(), Toast.LENGTH_LONG).show();
            return;
        }
        orgCall();
        Jtik.unHook(unHooker);
        Log.d(TestCase.TEST_TAG, "after unhook");
        orgCall();
    }
    public abstract void orgCall();
}
