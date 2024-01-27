#include <jni.h>
#include <string>
#include "jvmti.h"
#include <dexter/slicer/dex_ir_builder.h>
#include <dexter/slicer/code_ir.h>
#include <dexter/slicer/reader.h>
#include <dexter/slicer/writer.h>
#include "log.h"
#include "jni/jni_util.h"
#include "transform/transforms.h"
#include "transform/hook_transform.h"
#include "transform/modify_parameter_transform.h"
#include "hooker/hook_bridge.h"
#include "jtik_agent.h"

using namespace dex;
using namespace lir;

static jvmtiEnv* sAgentJvmtiEnv = nullptr;
static jobject sHookerClassLoader = nullptr;

class JvmtiAllocator : public dex::Writer::Allocator {
public:
    JvmtiAllocator(jvmtiEnv* jvmti) : jvmti_(jvmti) {}

    virtual void* Allocate(size_t size) {
        unsigned char* alloc = nullptr;
        jvmti_->Allocate(size, &alloc);
        return (void*)alloc;
    }

    virtual void Free(void* ptr) {
        if (ptr == nullptr) {
            return;
        }

        jvmti_->Deallocate((unsigned char*)ptr);
    }

private:
    jvmtiEnv* jvmti_;
};



int SetNeedCapabilities(jvmtiEnv *jvmti) {
    jvmtiCapabilities caps = {0};
    jvmtiError error;
    error = jvmti->GetPotentialCapabilities(&caps);
    ALOGI("GetPotentialCapabilities: retransform %d, %d, %d, %d", caps.can_retransform_classes, caps.can_retransform_any_class,caps.can_set_native_method_prefix,error);
    jvmtiCapabilities newCaps = {0};
    newCaps.can_retransform_classes  = 1;
    if (caps.can_set_native_method_prefix) {
        newCaps.can_set_native_method_prefix = 1;
    }
    return jvmti->AddCapabilities(&newCaps);
}

jvmtiEnv *CreateJvmtiEnv(JavaVM *vm) {
    CHECK_NULL_RETURN_NULL(vm,"CreateJvmtiEnv vm null")
    jvmtiEnv *jvmti_env;
    jint result = vm->GetEnv((void **) &jvmti_env, JVMTI_VERSION_1_2);
    if (result != JNI_OK) {
        return nullptr;
    }

    return jvmti_env;
}


const thread_local deploy::Transform* current_transform = nullptr;
// Event that fires when the agent loads a class file.
extern "C" void JNICALL HookClassFileLoadHook(
        jvmtiEnv* jvmti, JNIEnv* jni, jclass class_being_redefined, jobject loader,
const char* name, jobject protection_domain, jint class_data_len,
const unsigned char* class_data, jint* new_class_data_len,
unsigned char** new_class_data) {

ALOGI("HookClassFileLoadHook %s", name);
if (current_transform == nullptr ||
current_transform->GetClassName() != name) {
return;
}

// The class name needs to be in JNI-format.
std::string descriptor = current_transform->GetJniClassName();
ALOGI("HookClassFileLoadHook descriptor %s", descriptor.c_str());

dex::Reader reader(class_data, class_data_len);
auto class_index = reader.FindClassIndex(descriptor.c_str());
if (class_index == dex::kNoIndex) {
ALOGE("HookClassFileLoadHook not found class index");
return;
}

reader.CreateClassIr(class_index);
auto dex_ir = reader.GetIr();
current_transform->Apply(dex_ir);

size_t new_image_size = 0;
dex::u1* new_image = nullptr;
dex::Writer writer(dex_ir);

JvmtiAllocator allocator(jvmti);
new_image = writer.CreateImage(&allocator, &new_image_size);
#ifdef DEBUG_DEX_FILE
FILE* out_debug_check_file = fopen("/sdcard/Download/dex_debug.dex", "w");
    if(out_debug_check_file == nullptr) {
        ALOGI("out_debug_check_file open fail");
    } else {
        int write_cnt = fwrite(new_image, 1, new_image_size, out_debug_check_file);
        ALOGI("DexBuild wirte out_debug_check_file %d for %zu", write_cnt, new_image_size);
        fclose(out_debug_check_file);
    }
#endif
*new_class_data_len = new_image_size;
*new_class_data = new_image;
}

extern "C" JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM *vm, char *options,
                                                 void *reserved) {
    ALOGI("Agent_OnAttach enter");
    jvmtiEnv *jvmti_env = CreateJvmtiEnv(vm);
    sAgentJvmtiEnv = jvmti_env;

    if (jvmti_env == nullptr) {
        return JNI_ERR;
    }
    int setRet = SetNeedCapabilities(jvmti_env);
    if (setRet != JVMTI_ERROR_NONE) {
        ALOGE("Agent_OnAttach SetNeedCapabilities %d", setRet);
        return setRet;
    }

    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassFileLoadHook = &HookClassFileLoadHook;

    //callbacks.MethodEntry = &MethoddEntry;
    int error = jvmti_env->SetEventCallbacks(&callbacks, sizeof(callbacks));
    ALOGI("gent_OnAttach SetEventCallbacks %d", error);

    jvmti_env->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr);
    ALOGI("Agent_OnAttach end");
    return error;
}

extern "C" JNIEXPORT void agent_set_bridge_function_info(const char* className, const char* methodEntryName,
                                                         const char*methodExitName, const char* staticMethodExitName,
                                                         const char* modifyParamMethodName, const char* staticModifyParamMethodName) {
    HookBridge::Init(className, methodEntryName, methodExitName, staticMethodExitName,
                     modifyParamMethodName, staticModifyParamMethodName);
}
extern "C" JNIEXPORT jlong agent_do_transform(JNIEnv *env,
                                              jobject jTargetMethod,
                                              jboolean jhookEnter,
                                              jboolean jhookExit,
                                              jintArray jparaIndexes) {

    if (sAgentJvmtiEnv == nullptr) {
        ALOGE("agent hook jvmti null");
        return 0;
    }
    jmethodID nativeTargetMethodID = env->FromReflectedMethod(jTargetMethod);
    if (!nativeTargetMethodID) {
        ALOGE("agent native method not found");
        return 0;
    }
    std::string targetClassName, targetMethodName, targetMethodSignature;
    if(!jni::GetClassAndMethodName(env, sAgentJvmtiEnv, nativeTargetMethodID,
                                   targetClassName, targetMethodName, targetMethodSignature)) {
        ALOGE("agent get target name or method failed");
        return 0;
    }

    auto hookType = (unsigned )deploy::MethodHooks::HookType::None;
    if (jhookEnter) {
        hookType |= (unsigned )deploy::MethodHooks::HookType::OnEntry;
    }
    if (jhookExit) {
        hookType |= (unsigned )deploy::MethodHooks::HookType::OnExit;
    }

    std::vector<int> paraVector;
    if (jparaIndexes != nullptr) {
        jsize size = env->GetArrayLength(jparaIndexes);
        if (size > 0) {
            paraVector.resize(size);
            env->GetIntArrayRegion(jparaIndexes,0,size,&paraVector[0]);
            hookType |= (unsigned )deploy::MethodHooks::HookType::ModParam;
        }
    }
    ALOGI("agent native hooking method %s,%s,%s,%#x,%d", targetClassName.c_str(), targetMethodName.c_str(), targetMethodSignature.c_str(), nativeTargetMethodID,hookType);

    jclass nativeClass = nullptr;
    if(!jni::CheckJvmti(sAgentJvmtiEnv->GetMethodDeclaringClass(nativeTargetMethodID, &nativeClass),
                        "agent get native class fail")) {
        return false;
    }
    int modifiers;
    if(!jni::CheckJvmti(sAgentJvmtiEnv->GetMethodModifiers(nativeTargetMethodID, &modifiers),
                        "agent get native class modifiers fail")) {
        return false;
    }

    bool isStatic = (modifiers & jni::kAccStatic) != 0;

    if (current_transform != nullptr) {
        delete current_transform;
        current_transform = nullptr;
    }

    current_transform = new deploy::HookTransform(targetClassName, targetMethodName,
                                                  targetMethodSignature,
                                                  reinterpret_cast<long>(nativeTargetMethodID),
                                                  isStatic, hookType,paraVector);

    bool transRet = jni::CheckJvmti(sAgentJvmtiEnv->RetransformClasses(1, &nativeClass),
                                    "agent RetransformClasses fail");
    if (nativeClass != nullptr) {
        env->DeleteLocalRef(nativeClass);
    }
    return transRet ? reinterpret_cast<jlong>(nativeTargetMethodID) : 0;
}

extern "C" JNIEXPORT jboolean JNICALL agent_do_unHook_transform(JNIEnv *env,jlong jTargetMethod){

    if (sAgentJvmtiEnv == nullptr) {
        ALOGE("agent unhook jvmti null");
        return false;
    }

    std::string targetClassName, targetMethodName, targetMethodSignature;
    if(!jni::GetClassAndMethodName(env, sAgentJvmtiEnv, reinterpret_cast<jmethodID>(jTargetMethod),
                                   targetClassName, targetMethodName, targetMethodSignature)) {
        ALOGE("agent unhook get target name or method failed when unhook");
        return false;
    }

    ALOGI("agent native unhooking method %s,%s,%s", targetClassName.c_str(), targetMethodName.c_str(), targetMethodSignature.c_str());

    jclass nativeClass = nullptr;
    if(!jni::CheckJvmti(sAgentJvmtiEnv->GetMethodDeclaringClass(reinterpret_cast<jmethodID>(jTargetMethod), &nativeClass),
                        "agent unhook get native class fail")) {
        return false;
    }
    if (current_transform != nullptr) {
        delete current_transform;
        current_transform = nullptr;
    }
    current_transform = new deploy::HookTransform(targetClassName);

    bool transRet = jni::CheckJvmti(sAgentJvmtiEnv->RetransformClasses(1, &nativeClass),
                                    "agent unhook RetransformClasses fail");
    if (nativeClass != nullptr) {
        env->DeleteLocalRef(nativeClass);
    }
    return transRet;
}

extern "C" JNIEXPORT int agent_add_boot_classloader(const char* pathToAdd) {
    if (sAgentJvmtiEnv == nullptr) {
        ALOGE("agent agent_add_boot_classloader jvmti null");
        return -1;
    }

    if (pathToAdd == nullptr) {
        ALOGE("agent agent_add_boot_classloader path null");
        return -2;
    }
    return sAgentJvmtiEnv->AddToBootstrapClassLoaderSearch(pathToAdd);
}

extern "C" JNIEXPORT void agent_save_hooker_classloader(jobject classLoader) {
    sHookerClassLoader = classLoader;
}

extern "C" JNIEXPORT jobject agent_get_hooker_classloader() {
    return sHookerClassLoader;
}
