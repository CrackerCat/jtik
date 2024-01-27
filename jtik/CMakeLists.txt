# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -frtti -Wall -Wno-unused-parameter -Wno-unused-variable -Wno-shift-count-overflow -Wno-error=non-virtual-dtor -Wno-sign-compare -Wno-switch -Wno-missing-braces")

#add_definitions(-DDEBUG_DEX_FILE)

add_library(
        dexter
        STATIC
        src/main/cpp/dexter/bytecode_encoder.cc
        src/main/cpp/dexter/code_ir.cc
        src/main/cpp/dexter/common.cc
        src/main/cpp/dexter/control_flow_graph.cc
        src/main/cpp/dexter/debuginfo_encoder.cc
        src/main/cpp/dexter/dex_bytecode.cc
        src/main/cpp/dexter/dex_format.cc
        src/main/cpp/dexter/dex_ir.cc
        src/main/cpp/dexter/dex_ir_builder.cc
        src/main/cpp/dexter/dex_utf8.cc
        src/main/cpp/dexter/instrumentation.cc
        src/main/cpp/dexter/reader.cc
        src/main/cpp/dexter/tryblocks_encoder.cc
        src/main/cpp/dexter/writer.cc

)
include_directories(src/main/cpp)
add_library( # Sets the name of the library.
        jtik

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/jtik.cc
        src/main/cpp/jni/jni_util.cc
        src/main/cpp/jni/dl_util.cc)

add_library( # Sets the name of the library.
        jtik_agent

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/agent/jtik_agent.cc
        src/main/cpp/transform/transforms.cc
        src/main/cpp/transform/hook_transform.cc
        src/main/cpp/transform/modify_parameter_transform.cc
        src/main/cpp/jni/jni_util.cc
        src/main/cpp/hooker/hook_bridge.cc)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
        jtik
        z
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
target_link_libraries( # Specifies the target library.
        jtik_agent
        dexter
        z
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})