package com.zxc.jtik;

/**
 * Created by zxc
 */
public class UnHooker {
    public  UnHooker(long jmethodId) {
        this.jmethodId = jmethodId;
    }
    long getId() {
        return jmethodId;
    }
    private long jmethodId;
}
