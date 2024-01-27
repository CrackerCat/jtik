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

#include "modify_parameter_transform.h"

#include <memory>

#include "dexter/slicer/dex_ir.h"
#include "dexter/slicer/instrumentation.h"
#include "dexter/slicer/reader.h"
#include "dexter/slicer/writer.h"
#include "log.h"

namespace deploy {

    bool ModifyParameter::Apply(lir::CodeIr* code_ir) {
        BytecodeConvertingVisitor visitor;
        for (auto instr : code_ir->instructions) {
            instr->Accept(&visitor);
            if (visitor.out != nullptr) {
                break;
            }
        }

        auto ir_method = code_ir->ir_method;
        auto args_count = ir_method->code->ins_count;

        if (!ir_method->decl->prototype->param_types) {
            ALOGE("Cannot modify parameter of method with no parameters");
            return false;
        }

        auto types = ir_method->decl->prototype->param_types->types;
        if (param_idx_ >= types.size()) {
            ALOGE("Index %u out of range for method with parameter count %u",
                  param_idx_, types.size());
            return false;
        }

        ir::Builder builder(code_ir->dex_ir);

        bool isStatic = (ir_method->access_flags & dex::kAccStatic) != 0;
        auto nonePareRegCount = ir_method->code->registers - ir_method->code->ins_count;
        bool isVRegEnough = nonePareRegCount >= 3; //can use v0 v1 v2 to store jmethod id and param index?
        auto regShifted = 0;
        if (!isVRegEnough) {
            setOrgRegCount(code_ir->ir_method->code->registers);
            regShifted = 3 - nonePareRegCount;
            code_ir->ir_method->code->registers += regShifted;
            ALOGI("transform: add reg %d in modify param %d", regShifted, param_idx_);
        }

        auto param_type = types[param_idx_];
        bool is_wide = param_type->GetCategory() == ir::Type::Category::WideScalar;
        std::vector<ir::Type*> hook_param_types;
        hook_param_types.push_back(builder.GetType("J"));//jmethod id
        hook_param_types.push_back(builder.GetType("I"));//param index
        if (!isStatic) {
            hook_param_types.push_back(builder.GetType("Ljava/lang/Object;"));
        }
        ir::Type* return_type;
        if (param_type->GetCategory() == ir::Type::Category::Reference) {
            hook_param_types.push_back(builder.GetType("Ljava/lang/Object;"));
            return_type = builder.GetType("Ljava/lang/Object;");
        } else {
            hook_param_types.push_back(param_type);
            return_type = param_type;
        }
        auto param_transform_decl = builder.GetMethodDecl(
                builder.GetAsciiString(transform_method_.c_str()),
                builder.GetProto(return_type, builder.GetTypeList({hook_param_types})),
                builder.GetType(transform_class_.c_str()));
        auto param_transform_method = code_ir->Alloc<lir::Method>(
                param_transform_decl, param_transform_decl->orig_index);

        int wideNumBefore = 0;
        for (int i = 0; i < param_idx_; ++i) {
            if (types[i]->GetCategory() == ir::Type::Category::WideScalar) {
                wideNumBefore ++;
            }
        }

        auto param_index_in_func = !isStatic ? (param_idx_ + 1) : param_idx_;
        ALOGD("wideNumBefore %d, param_idx_ %d, is_wide %d", wideNumBefore,param_idx_, is_wide);

        //process jmethod id
        auto const_wide_op = code_ir->Alloc<lir::Bytecode>();
        const_wide_op->opcode = dex::OP_CONST_WIDE;
        dex::u2 regIndexMethodId = 0;
        const_wide_op->operands.push_back(code_ir->Alloc<lir::VRegPair>(regIndexMethodId)); // dst
        const_wide_op->operands.push_back(code_ir->Alloc<lir::Const64>(j_method_id_)); // src
        code_ir->instructions.InsertBefore(visitor.out, const_wide_op);

        //process parameter modify index
        auto const_index_op = code_ir->Alloc<lir::Bytecode>();
        const_index_op->opcode = dex::OP_CONST;
        const_index_op->operands.push_back(code_ir->Alloc<lir::VReg>(regIndexMethodId + 2)); // dst
        const_index_op->operands.push_back(code_ir->Alloc<lir::Const32>(param_idx_)); // src
        code_ir->instructions.InsertBefore(visitor.out, const_index_op);


        auto reg = code_ir->Alloc<lir::VReg>(ir_method->code->registers - args_count + param_index_in_func + wideNumBefore);
        auto args = code_ir->Alloc<lir::VRegRange>(reg->reg, 1+ (!is_wide ? 0 : 1));

        auto get_flag = code_ir->Alloc<lir::Bytecode>();
        auto v_reg_list = code_ir->Alloc<lir::VRegList>();
        bool reg_list_contiguous = true;
        v_reg_list->registers.push_back(regIndexMethodId);
        v_reg_list->registers.push_back(regIndexMethodId+1);
        v_reg_list->registers.push_back(regIndexMethodId+2);
        if (!isStatic) {
            auto regThisObjIndex = ir_method->code->registers - ir_method->code->ins_count;
            if (v_reg_list->registers[(v_reg_list->registers).size()-1] != regThisObjIndex-1) {
                reg_list_contiguous = false;
            }
            v_reg_list->registers.push_back(regThisObjIndex);//this object
        }
        for(int i = 0; i < args->count; i++) {
            if ( i == 0 && v_reg_list->registers[(v_reg_list->registers).size()-1] != args->base_reg-1) {
                reg_list_contiguous = false;
            }
            v_reg_list->registers.push_back(args->base_reg + i);
        }
        if (reg_list_contiguous) {
            get_flag->opcode = dex::OP_INVOKE_STATIC_RANGE;
            auto v_reg_range = code_ir->Alloc<lir::VRegRange>(v_reg_list->registers[0], v_reg_list->registers.size());
            get_flag->operands.push_back(v_reg_range);
        } else {
            get_flag->opcode = dex::OP_INVOKE_STATIC;
            get_flag->operands.push_back(v_reg_list);
        }
        get_flag->operands.push_back(param_transform_method);
        code_ir->instructions.InsertBefore(visitor.out, get_flag);

        auto mov_flag = code_ir->Alloc<lir::Bytecode>();
        if (param_type->GetCategory() == ir::Type::Category::Reference) {
            mov_flag->opcode = dex::OP_MOVE_RESULT_OBJECT;
        } else if (param_type->GetCategory() == ir::Type::Category::WideScalar) {
            mov_flag->opcode = dex::OP_MOVE_RESULT_WIDE;
        } else {
            mov_flag->opcode = dex::OP_MOVE_RESULT;
        }
        if (is_wide) {
            mov_flag->operands.push_back(code_ir->Alloc<lir::VRegPair>(reg->reg));
        } else {
            mov_flag->operands.push_back(reg);
        }
        code_ir->instructions.InsertBefore(visitor.out, mov_flag);

        if (param_type->GetCategory() == ir::Type::Category::Reference) {
            auto check_cast = code_ir->Alloc<lir::Bytecode>();
            check_cast->opcode = dex::OP_CHECK_CAST;
            check_cast->operands.push_back(code_ir->Alloc<lir::VReg>(reg->reg));
            check_cast->operands.push_back(
                    code_ir->Alloc<lir::Type>(param_type, param_type->orig_index));
            code_ir->instructions.InsertBefore(visitor.out, check_cast);
        }

        if (first_param_modifier_ && regShifted > 0) {
            slicer::GenerateShiftParamsCode(code_ir,visitor.out, regShifted);
            ALOGI("transform: shift back reg %d in modify param %d", regShifted, param_idx_);
        }

        return true;
    }

//    void ModifyParameterTransform::Apply(
//            std::shared_ptr<ir::DexFile> dex_ir) const {
//        const std::string jni_name = GetJniClassName();
//
//        slicer::MethodInstrumenter mi(dex_ir);
//        ir::MethodId id(jni_name.c_str(), method_name_.c_str(),
//                        method_signature_.c_str());
//        mi.AddTransformation<ModifyParameter>(param_idx_, kHookClassName,
//                                              transform_method_);
//        if (!mi.InstrumentMethod(id)) {
//            ALOGW("ModifyParameterTransform failed: %s", jni_name.c_str());
//        }
//    }

}  // namespace deploy