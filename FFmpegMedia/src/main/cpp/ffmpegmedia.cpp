#include <jni.h>
#include <string>
extern "C"{
#include "libavformat/avformat.h"
}

extern "C" JNIEXPORT jstring JNICALL

Java_io_github_zy3274311_ffmpegmedia_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
    const char *conf = avformat_configuration();
    return env->NewStringUTF(conf);
}