/*
 * Copyright (C) 2020 The Android Open Source Project
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

#ifndef TRANSFORMS_H
#define TRANSFORMS_H

#include "jvmti.h"

#include <memory>

#include "dexter/slicer/dex_ir.h"
#include "dexter/slicer/instrumentation.h"
#include "dexter/slicer/reader.h"
#include "dexter/slicer/writer.h"

namespace deploy {

    class Transform {
    public:
        Transform(const std::string& class_name) : class_name_(class_name) {}
        virtual ~Transform() = default;

        std::string GetJniClassName() const { return class_name_; }
        std::string GetClassName() const {
            if(class_name_.length() > 2) {
                return class_name_.substr(1, class_name_.length() - 2);
            }
            return class_name_;
        }
        virtual void Apply(std::shared_ptr<ir::DexFile> dex_ir) const = 0;

    private:
        const std::string class_name_;
    };

    struct BytecodeConvertingVisitor : public lir::Visitor {
        lir::Bytecode* out = nullptr;
        bool Visit(lir::Bytecode* bytecode) {
            out = bytecode;
            return true;
        }
    };

}  // namespace deploy

#endif