#include <jni.h>
#include <string>

extern "C"{
#include "FFDemuxer.h"
#include "ndk_log.h"
#include "libavformat/avformat.h"
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_zy3274311_ffmedia_FFmpegEngine_stringFromJNI(JNIEnv *env, jobject thiz) {
    const char *conf = avformat_configuration();
    return env->NewStringUTF(conf);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_DemuxerImpl__1setUp(JNIEnv *env, jobject thiz) {
    auto* demuxer = new FFDemuxer();
    return reinterpret_cast<jlong>(demuxer);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_DemuxerImpl__1setDataSource(JNIEnv *env, jobject thiz,
                                                                     jlong ptr, jstring url) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    jboolean isCopy = JNI_TRUE;
    const char* u = env->GetStringUTFChars(url, &isCopy);
    LOGE(FFMEDIA_TAG, "setDataSource(%s)", u);
    demuxer->setDataSource(u);
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_DemuxerImpl__1getTrackCount(JNIEnv *env, jobject thiz,
                                                                     jlong ptr) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    int c = demuxer->getTrackCount();
    LOGE(FFMEDIA_TAG, "getTrackCount() %d", c);
    return c;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_DemuxerImpl__1release(JNIEnv *env, jobject thiz,
                                                               jlong ptr) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    delete demuxer;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_DemuxerImpl__1getTrackFormat(JNIEnv *env, jobject thiz,
                                                                      jlong ptr, jint index) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    demuxer->getTrackFormat(index);
}