package com.zxc.jtik;

import android.util.SparseArray;

/**
 * Created by zxc
 */
public class MethodHook {

    private IMethodEnter mMethodEnter;
    private IMethodExit mMethodExit;
    private SparseArray<IParamModifyOperation> mParamModifiers;

    private MethodHook(){
        throw new RuntimeException("Use Builder");
    }
    MethodHook(MethodHook.Builder builder) {
        mMethodEnter = builder.mMethodEnter;
        mMethodExit = builder.mMethodExit;
        mParamModifiers = builder.mParamModifiers;
    }

    public IMethodEnter getMethodEnter() {
        return mMethodEnter;
    }

    public IMethodExit getMethodExit() {
        return mMethodExit;
    }

    public SparseArray<IParamModifyOperation> getParamModifiers() {
        return mParamModifiers;
    }
    public boolean hasParamModifiers() {
        return mParamModifiers != null && mParamModifiers.size() > 0;
    }

    public static class Builder {
        private IMethodEnter mMethodEnter;
        private IMethodExit mMethodExit;
        private SparseArray<IParamModifyOperation> mParamModifiers;

        public Builder() {
        }
        public Builder setMethodEnterListener(IMethodEnter methodEnter) {
            mMethodEnter = methodEnter;
            return this;
        }
        public Builder setMethodExitListener(IMethodExit methodEnter) {
            mMethodExit = methodEnter;
            return this;
        }
        public Builder setParamModifier(int index,  IParamModifyOperation operation) {
            if (mParamModifiers == null) {
                mParamModifiers = new SparseArray<IParamModifyOperation>();
            }
            mParamModifiers.put(index, operation);
            return this;
        }

        public MethodHook build() {
            return new MethodHook(this);
        }
    }

    public interface IMethodEnter {
        void onMethodEnter(Object thisObj, Object... args);
    }
    public interface IMethodExit {
        Object onMethodExit(Object thisObj, Object retVale);
    }
    public interface IParamModifyOperation {
        Object change(Object thisObject,  Object inParam);
    }
}
