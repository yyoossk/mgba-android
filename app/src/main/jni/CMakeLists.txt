# app/src/main/jni/CMakeLists.txt
# Simplified CMakeLists.txt with minimal dependencies

cmake_minimum_required(VERSION 3.22.1)
project(mgba-android)

# C/C++ standards
set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# JNIラッパー
set(JNI_SOURCES
    mgba_jni.c
)

# ネイティブライブラリ作成（最小化版）
add_library(mgba-core SHARED
    ${JNI_SOURCES}
)

# Androidネイティブライブラリの依存関係
find_library(log-lib log)
find_library(android-lib android)

# リンク
target_link_libraries(mgba-core
    ${log-lib}
    ${android-lib}
)

# 最適化フラグ
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(mgba-core PRIVATE -O3)
else()
    target_compile_options(mgba-core PRIVATE -O0 -g)
endif()
