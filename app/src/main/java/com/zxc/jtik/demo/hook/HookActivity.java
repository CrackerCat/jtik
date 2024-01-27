package com.zxc.jtik.demo.hook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.zxc.jtik.Jtik;
import com.zxc.jtik.MethodHook;
import com.zxc.jtik.UnHooker;
import com.zxc.jtik.demo.App;
import com.zxc.jtik.demo.SecondActivity;
import com.zxc.jtik.demo.TestCase;
import com.zxc.jtik.demo.TestItem;

import java.lang.reflect.Member;

/**
 * Created by zxc
 */
public class HookActivity extends TestItem {
    public HookActivity(String name, Member targetMethod, MethodHook hook) {
        super(name, targetMethod, hook);
    }
    public static MethodHook getHook()  {
        return new MethodHook.Builder()
                    .setMethodEnterListener((thisObj, args) -> {
                        Log.d(TestCase.TEST_TAG, "Activity onCreate hook enter: obj=" + thisObj + ", args len =" + args.length);
                    }).build();
    }

    public static Member getMember() {
        try {
            return Activity.class.getDeclaredMethod("onCreate", Bundle.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void orgCall() {
        Intent intent = new Intent(App.sAppContext, SecondActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.sAppContext.startActivity(intent);
    }

    @Override
    public void run() {
        UnHooker unHooker = Jtik.hook(targetMethod, hookCb);
        if (unHooker == null) {
            Toast.makeText(App.sAppContext, "hook fail, init = " + Jtik.isInitialized(), Toast.LENGTH_LONG).show();
            return;
        }
        orgCall();
    }
}
