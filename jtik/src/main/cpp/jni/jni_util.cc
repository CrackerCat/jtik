#include "jni_util.h"
#include "log.h"

namespace jni {
	bool j2c_Utf8String(JNIEnv* env, jstring jni_string, std::string& c_string)
	{
		// 错误参数
		if (! jni_string)
			return false;

		jboolean is_copy = 0;
		const char *u8_str = env->GetStringUTFChars(jni_string, &is_copy);
		if ( u8_str)
		{
			c_string = u8_str;
			env->ReleaseStringUTFChars(jni_string,u8_str);
			return true;
		}

		return false;
	}

    bool CheckJvmti(jvmtiError error, const std::string& error_message) {
        if (error != JVMTI_ERROR_NONE) {
            ALOGI("%d:%s", error, error_message.c_str());
            return false;
        }
        return true;
    }
	bool GetClassAndMethodName(JNIEnv* jniEnv,jvmtiEnv* jvmtiEnv, jmethodID inJmethodId,
							   std::string& outClassName, std::string& outMethodName, std::string& outMethodSignature)
	{
		if (jvmtiEnv == nullptr || !inJmethodId) {
			return false;
		}
		LocalJvmCharPtr methodName(jvmtiEnv),signature(jvmtiEnv),generic(jvmtiEnv);
		if(!jni::CheckJvmti(jvmtiEnv->GetMethodName(inJmethodId, methodName.getPtr(), signature.getPtr(), generic.getPtr()),
							"get method signature fail")) {
			return false;
		}
		outMethodName = methodName.getValue();
		outMethodSignature = signature.getValue();

		jclass nativeClass = nullptr;
		if(!jni::CheckJvmti(jvmtiEnv->GetMethodDeclaringClass(inJmethodId, &nativeClass),
							"get native class fail")) {
			return false;
		}

		LocalJvmCharPtr jniClassName(jvmtiEnv),jniClassGeneric(jvmtiEnv);
		if(!jni::CheckJvmti(jvmtiEnv->GetClassSignature(nativeClass, jniClassName.getPtr(), jniClassGeneric.getPtr()),
							"get native class name fail")) {
			if (nativeClass != nullptr) {
				jniEnv->DeleteLocalRef(nativeClass);
			}
			return false;
		}
		outClassName = jniClassName.getValue();
        if (nativeClass != nullptr) {
            jniEnv->DeleteLocalRef(nativeClass);
        }
		return true;
	}
}