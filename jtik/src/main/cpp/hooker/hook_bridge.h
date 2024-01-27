#ifndef JTIK_HOOK_BRIDGE_H
#define JTIK_HOOK_BRIDGE_H

#include <string>


class HookBridge {
public:
    static void Init(const std::string& className, const std::string& methodEntryName,
                     const std::string& methodExitName, const std::string& staticMethodExitName,
                     const std::string& modifyParamMethodName, const std::string& staticModifyParamMethodName) ;
    static const std::string &GetClassName();

    static const std::string &GetMethodEnterName();

    static const std::string &GetMethodExitName();

    static const std::string &GetStaticMethodExitName();

    static const std::string &GetModifyParamName();

    static const std::string &GetStaticModifyParamName();

private:
    static std::string sBridgeClassName;
    static std::string sBridgeMethodEnterName;
    static std::string sBridgeMethodExitName;
    static std::string sBridgeStaticMethodExitName;
    static std::string sBridgeModifyParamName;
    static std::string sBridgeStaticModifyParamName;

};


#endif //JTIK_HOOK_BRIDGE_H
