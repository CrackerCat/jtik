#pragma once

#include <jni.h>
#include <string>
#include "jvmti.h"

#define CHECK_NULL_RETURN_NULL(PTR,MESSAGE) if (PTR == nullptr) { \
                                        ALOGE(MESSAGE);     \
                                        return nullptr; }
#define CHECK_NULL_RETURN(PTR,MESSAGE) if (PTR == nullptr) { \
                                        ALOGE(MESSAGE);     \
                                        return; }
#define CHECK_NULL_RETURN_BOOL(PTR,MESSAGE) if (PTR == nullptr) { \
                                        ALOGE(MESSAGE);     \
                                        return false; }
namespace jni {

    // copy from art
    static constexpr uint32_t kAccPublic =       0x0001;  // class, field, method, ic
    static constexpr uint32_t kAccPrivate =      0x0002;  // field, method, ic
    static constexpr uint32_t kAccProtected =    0x0004;  // field, method, ic
    static constexpr uint32_t kAccStatic =       0x0008;  // field, method, ic
    static constexpr uint32_t kAccFinal =        0x0010;  // class, field, method, ic
    static constexpr uint32_t kAccSynchronized = 0x0020;  // method (only allowed on natives)
    static constexpr uint32_t kAccSuper =        0x0020;  // class (not used in dex)
    static constexpr uint32_t kAccVolatile =     0x0040;  // field
    static constexpr uint32_t kAccBridge =       0x0040;  // method (1.5)
    static constexpr uint32_t kAccTransient =    0x0080;  // field
    static constexpr uint32_t kAccVarargs =      0x0080;  // method (1.5)
    static constexpr uint32_t kAccNative =       0x0100;  // method
    static constexpr uint32_t kAccInterface =    0x0200;  // class, ic
    static constexpr uint32_t kAccAbstract =     0x0400;  // class, method, ic
    static constexpr uint32_t kAccStrict =       0x0800;  // method
    static constexpr uint32_t kAccSynthetic =    0x1000;  // class, field, method, ic
    static constexpr uint32_t kAccAnnotation =   0x2000;  // class, ic (1.5)
    static constexpr uint32_t kAccEnum =         0x4000;  // class, field, ic (1.5)

	class LocalJvmCharPtr{
        public:
        explicit LocalJvmCharPtr(jvmtiEnv* jvmtiEnv): mJvmtiEnv(jvmtiEnv),mPtr(nullptr) {}
        char** getPtr() { return &mPtr; }
        char* getValue() const { return mPtr; }
        ~LocalJvmCharPtr() {
            if (mJvmtiEnv != nullptr && mPtr != nullptr) {
                mJvmtiEnv->Deallocate((unsigned char*)mPtr);
            }
        }

        private:
            jvmtiEnv* mJvmtiEnv;
            char* mPtr;
    };

	// utilitys functions
	bool j2c_Utf8String(JNIEnv* env, jstring jni_string, std::string& c_string);
	bool CheckJvmti(jvmtiError error, const std::string& error_message);
	bool GetClassAndMethodName(JNIEnv* jniEnv, jvmtiEnv* jvmtiEnv, jmethodID inJmethodId, std::string& outClassName, std::string& outMethodName, std::string& outMethodSignature);
}
