#include "hook_bridge.h"

std::string HookBridge::sBridgeClassName;
std::string HookBridge::sBridgeMethodEnterName;
std::string HookBridge::sBridgeMethodExitName;
std::string HookBridge::sBridgeStaticMethodExitName;
std::string HookBridge::sBridgeModifyParamName;
std::string HookBridge::sBridgeStaticModifyParamName;

void HookBridge::Init(const std::string &className, const std::string &methodEntryName,
                      const std::string &methodExitName, const std::string &staticMethodExitName,
                      const std::string &modifyParamMethodName, const std::string &staticModifyParamMethodName) {
    sBridgeClassName = className;
    sBridgeMethodEnterName = methodEntryName;
    sBridgeMethodExitName = methodExitName;
    sBridgeStaticMethodExitName = staticMethodExitName;
    sBridgeModifyParamName = modifyParamMethodName;
    sBridgeStaticModifyParamName = staticModifyParamMethodName;
}

const std::string &HookBridge::GetClassName() {
    return sBridgeClassName;
}

const std::string &HookBridge::GetMethodEnterName() {
    return sBridgeMethodEnterName;
}

const std::string &HookBridge::GetMethodExitName() {
    return sBridgeMethodExitName;
}

const std::string &HookBridge::GetStaticMethodExitName() {
    return sBridgeStaticMethodExitName;
}

const std::string &HookBridge::GetModifyParamName() {
    return sBridgeModifyParamName;
}

const std::string &HookBridge::GetStaticModifyParamName() {
    return sBridgeStaticModifyParamName;
}
