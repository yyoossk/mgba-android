# app/src/main/jni/CMakeLists.txt

cmake_minimum_required(VERSION 3.22.1)
project(mgba-core)

# Enforce C11 standard
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# Create the shared library
add_library(mgba-core SHARED
    mgba_jni.c
)

# Find Android system libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
if(log-lib AND android-lib)
    target_link_libraries(mgba-core ${log-lib} ${android-lib})
else()
    message(FATAL_ERROR "Required Android libraries not found")
endif()
