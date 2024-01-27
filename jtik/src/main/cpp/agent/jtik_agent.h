#ifndef JTIK_JTIK_AGENT_H
#define JTIK_JTIK_AGENT_H

extern "C" JNIEXPORT void agent_set_bridge_function_info(const char* className, const char* methodEntryName,
                                                         const char* methodExitName, const char* staticMethodExitName,
                                                         const char* modifyParamMethodName, const char* staticModifyParamMethodName);
typedef void (*AgentSetBridgeInfoFn)(const char* className, const char*methodEntryName,
                                      const char* methodExitName, const char* staticMethodExitName,
                                      const char* modifyParamMethodName, const char* staticModifyParamMethodName);
const std::string  AgentSetBridgeInfoFnName = "agent_set_bridge_function_info";


extern "C" JNIEXPORT jlong agent_do_transform(JNIEnv *env,
                                                       jobject jTargetMethod,
                                                       jboolean jhookEnter,
                                                       jboolean jhookExit,
                                                       jintArray jparaIndexes);
typedef jlong (*AgentDoTransformFn)(JNIEnv *env,jobject jTargetMethod,jboolean jhookEnter,jboolean jhookExit,jintArray jparaIndexes);
const std::string  AgentDoTransformFnName = "agent_do_transform";

extern "C" JNIEXPORT jboolean JNICALL agent_do_unHook_transform(JNIEnv *env,jlong jTargetMethod);
typedef jboolean (*AgentDoUnHookTransformFn)(JNIEnv *env,jlong jTargetMethod);
const std::string  AgentDoUnHookTransformFnName = "agent_do_unHook_transform";

extern "C" JNIEXPORT int agent_add_boot_classloader(const char* pathToAdd);
typedef jboolean (*AgentAddBootClassLoaderFn)(const char* pathToAdd);
const std::string  AgentAddBootClassLoaderFnName = "agent_add_boot_classloader";

extern "C" JNIEXPORT void agent_save_hooker_classloader(jobject classLoader);
typedef void (*AgentSaveHookerClassloaderFn)(jobject classLoader);
const std::string  AgentSaveHookerClassloaderFnName = "agent_save_hooker_classloader";

extern "C" JNIEXPORT jobject agent_get_hooker_classloader();
typedef jobject (*AgentGetHookerClassloaderFn)();
const std::string  AgentGetHookerClassloaderFnName = "agent_get_hooker_classloader";

#endif //JTIK_JTIK_AGENT_H
