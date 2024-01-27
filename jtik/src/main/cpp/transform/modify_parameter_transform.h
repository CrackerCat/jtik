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

#ifndef MODIFY_PARAMETER_TRANSFORM_H
#define MODIFY_PARAMETER_TRANSFORM_H

#include <jvmti.h>

#include <string>

#include "dexter/slicer/dex_ir.h"
#include "transforms.h"

namespace deploy {

    class ModifyParameter : public slicer::Transformation {
    public:
        explicit ModifyParameter(dex::u4 param_idx,
                                 const std::string& transform_class,
                                 const std::string& transform_method,
                                 long jmethod_id,
                                 bool first_param_modifier)
                : param_idx_(param_idx),
                  transform_class_(transform_class),
                  transform_method_(transform_method),
                  j_method_id_(jmethod_id),
                  first_param_modifier_(first_param_modifier){}

        virtual bool Apply(lir::CodeIr* code_ir) override;

    private:
        dex::u4 param_idx_;
        std::string transform_class_;
        std::string transform_method_;
        long j_method_id_;
        bool first_param_modifier_;
    };

// Transform that accepts a target method, parameter index, and transform
// function. Adds a dex prologue to the target method that applies the
// transform function to the specified parameter and sets the value of the
// parameter to the transformed value.
//    class ModifyParameterTransform : public Transform {
//    public:
//        ModifyParameterTransform(const std::string& class_name,
//                                 const std::string& method_name,
//                                 const std::string& method_signature,
//                                 dex::u4 param_idx,
//                                 const std::string& transform_method)
//                : Transform(class_name),
//                  method_name_(method_name),
//                  method_signature_(method_signature),
//                  param_idx_(param_idx),
//                  transform_method_(transform_method) {}
//
//        void Apply(std::shared_ptr<ir::DexFile> dex_ir) const override;
//
//    private:
//        const char* kHookClassName =
//                "Lcom/zxc/jtik/HookBridge;";
//
//        std::string method_name_;
//        std::string method_signature_;
//        dex::u4 param_idx_;
//        std::string transform_method_;
//    };

}  // namespace deploy

#endif