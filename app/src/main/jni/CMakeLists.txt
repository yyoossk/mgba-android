# app/src/main/jni/CMakeLists.txt

cmake_minimum_required(VERSION 3.22.1)
project(mgba-android)

# C標準
set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Android固有の設定
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-undefined")

# プロジェクト構造（ダウンロード済みmGBAソースを想定）
# https://github.com/mgba-emu/mgba から src/core, src/util, include をダウンロード

# mGBAコアのソースファイル
file(GLOB_RECURSE MGBA_CORE_SOURCES
    "mgba/src/core/*.c"
    "mgba/arm/*.c"
)

# ユーティリティ関数
file(GLOB_RECURSE MGBA_UTIL_SOURCES
    "mgba/util/*.c"
)

# テストファイルは除外
list(FILTER MGBA_UTIL_SOURCES EXCLUDE REGEX "test/")
list(FILTER MGBA_UTIL_SOURCES EXCLUDE REGEX "gui/")

# 全ソースをまとめる
set(MGBA_ALL_SOURCES
    ${MGBA_CORE_SOURCES}
    ${MGBA_UTIL_SOURCES}
)

# JNIラッパー
set(JNI_SOURCES
    mgba_jni.c
    emulator_bridge.c
    audio_callback.c
)

# ネイティブライブラリ作成
add_library(mgba-core SHARED
    ${MGBA_ALL_SOURCES}
    ${JNI_SOURCES}
)

# インクルードディレクトリ
target_include_directories(mgba-core PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/mgba/include
    ${CMAKE_CURRENT_SOURCE_DIR}/mgba/src
    ${CMAKE_CURRENT_SOURCE_DIR}/mgba
)

# プリプロセッサ定義
target_compile_definitions(mgba-core PRIVATE
    -DUSE_ANDROID_BACKEND
    -DUSE_GLES2
    -DUSE_MEDIAFOUNDATION=0
)

# Androidネイティブライブラリの依存関係
find_library(log-lib log)
find_library(android-lib android)
find_library(glesv2-lib GLESv2)
find_library(opensl-lib OpenSLES)

# リンク
target_link_libraries(mgba-core
    ${log-lib}
    ${android-lib}
    ${glesv2-lib}
    ${opensl-lib}
)

# 最適化フラグ
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(mgba-core PRIVATE -O3 -march=native)
else()
    target_compile_options(mgba-core PRIVATE -O0 -g)
endif()
