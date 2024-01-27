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

#ifndef HOOK_TRANSFORM_H
#define HOOK_TRANSFORM_H

#include <jvmti.h>

#include <memory>

#include "dexter/slicer/dex_ir.h"
#include "dexter/slicer/instrumentation.h"
#include "dexter/slicer/reader.h"
#include "dexter/slicer/writer.h"
#include "transforms.h"

namespace deploy {

    struct MethodHooks {
        enum class HookType {
            None = 0,
            OnEntry = 1 << 0,
            OnExit = 1 << 1,
            ModParam = 1 << 2,
        };

        const std::string method_name;
        const std::string method_signature;
        unsigned int hook_type;
        std::vector<int> paramIndexes;
        const long j_method_id;
        bool is_static;

        explicit MethodHooks(const std::string& method_name,
                    const std::string& method_signature,
                    long j_method_id,
                    bool is_static,
                    unsigned int  hook_type, const std::vector<int>& paramIndexes)
                : method_name(method_name),
                  method_signature(method_signature),
                  j_method_id(j_method_id),
                  is_static(is_static),
                  hook_type(hook_type),
                  paramIndexes(paramIndexes){}
    };

    class HookTransform : public Transform {
    public:
        HookTransform(const std::string& class_name, const std::string& method_name,
                      const std::string& method_signature,
                      long j_method_id, bool is_static,
                      unsigned int hookType, const std::vector<int>& paramIndexes)
                : Transform(class_name) {
            hooks_.emplace_back(method_name, method_signature, j_method_id, is_static, hookType, paramIndexes);
        }
        HookTransform(const std::string& class_name)
                : Transform(class_name) {
            hooks_.clear();
        }

        void Apply(std::shared_ptr<ir::DexFile> dex_ir) const override;

    private:
        std::vector<MethodHooks> hooks_;
    };

}  // namespace deploy

#endif