/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include "hook_transform.h"
#include "log.h"
#include "hooker/hook_bridge.h"
#include "modify_parameter_transform.h"

namespace deploy {

    void HookTransform::Apply(std::shared_ptr<ir::DexFile> dex_ir) const {
        for (const MethodHooks& hook : hooks_) {
            slicer::MethodInstrumenter mi(dex_ir);
            auto paramModifierSize = hook.paramIndexes.size();
            if ((hook.hook_type & (unsigned)MethodHooks::HookType::ModParam) != 0 && paramModifierSize > 0) {
                for(int i = 0; i < paramModifierSize; i++) {
                    auto param_idx_ = hook.paramIndexes[i];
                    const char* methodName = hook.is_static ? HookBridge::GetStaticModifyParamName().c_str() : HookBridge::GetModifyParamName().c_str();
                    mi.AddTransformation<ModifyParameter>(param_idx_, HookBridge::GetClassName().c_str(),
                                                          methodName,hook.j_method_id, i == 0);
                }
            }
            if ((hook.hook_type & (unsigned )MethodHooks::HookType::OnEntry) != 0) {
                const ir::MethodId entry_hook(HookBridge::GetClassName().c_str(),
                                              HookBridge::GetMethodEnterName().c_str());
                mi.AddTransformation<slicer::EntryHook>(
                        entry_hook, hook.j_method_id, slicer::EntryHook::Tweak::ArrayParams);//zxc change from ThisAsObject
            }
            if ((hook.hook_type & (unsigned)MethodHooks::HookType::OnExit) != 0) {
                const char* methodName = hook.is_static ? HookBridge::GetStaticMethodExitName().c_str() : HookBridge::GetMethodExitName().c_str();
                const ir::MethodId exit_hook(HookBridge::GetClassName().c_str(), methodName);
                mi.AddTransformation<slicer::ExitHook>(exit_hook, hook.j_method_id,
                                                       slicer::ExitHook::Tweak::ReturnAsObject);
            }
            const std::string jni_name = GetJniClassName();
            const ir::MethodId target_method(jni_name.c_str(), hook.method_name.c_str(),
                                             hook.method_signature.c_str());
            if (!mi.InstrumentMethod(target_method)) {
                ALOGE("Failed to instrument: %s", GetClassName().c_str());
            }
        }
    }



}  // namespace deploy