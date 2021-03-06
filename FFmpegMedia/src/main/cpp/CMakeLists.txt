# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("ffmedia")

# configure lib dir
set(DISTRIBUTION_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)
# ffmpeg dir
set(FFMPEG_DIR ${DISTRIBUTION_DIR}/ffmpeg/${ANDROID_ABI})

add_library(avutil STATIC IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavutil.a)

add_library(swscale STATIC IMPORTED)
set_target_properties(swscale PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libswscale.a)

add_library(avformat STATIC IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavformat.a)

add_library(avfilter STATIC IMPORTED)
set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavfilter.a)

add_library(avdevice STATIC IMPORTED)
set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavdevice.a)

add_library(avcodec STATIC IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavcodec.a)

add_library(swresample STATIC IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libswresample.a)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
add_library( # Sets the name of the library.
        ffmedia

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ffmedia.cpp
        FFDemuxer.cpp
        FFMuxer.cpp)

# add ffmpeg include
target_include_directories(ffmedia PUBLIC ${FFMPEG_DIR}/include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        ffmedia

        #增加ffmpeg库，注意顺序（具体看lib/pkgconfig的依赖关系）
        avdevice
        avfilter
        avformat
        avcodec
        swresample
        swscale
        avutil
        #增加ffmpeg所需要的依赖库
        -lm -lz

        android
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )
