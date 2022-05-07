#include <jni.h>
#include <string>

extern "C"{
#include "FFMuxer.h"
#include "FFDemuxer.h"
#include "ndk_log.h"
#include "libavformat/avformat.h"
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_zy3274311_ffmedia_FFMedia_stringFromJNI(JNIEnv *env, jobject thiz) {
    const char *conf = avformat_configuration();
    return env->NewStringUTF(conf);
}

//FFDemuxer
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_FFDemuxer__1setUp(JNIEnv *env, jobject thiz) {
    auto* demuxer = new FFDemuxer();
    return reinterpret_cast<jlong>(demuxer);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_FFDemuxer__1setDataSource(JNIEnv *env, jobject thiz,
                                                                   jlong ptr, jstring url) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    jboolean isCopy = JNI_TRUE;
    const char* u = env->GetStringUTFChars(url, &isCopy);
    LOGE(FFMEDIA_TAG, "setDataSource(%s)", u);
    demuxer->setDataSource(u);
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_FFDemuxer__1getTrackCount(JNIEnv *env, jobject thiz,
                                                                   jlong ptr) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    int c = demuxer->getTrackCount();
    LOGE(FFMEDIA_TAG, "getTrackCount() %d", c);
    return c;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_FFDemuxer__1release(JNIEnv *env, jobject thiz,
                                                             jlong ptr) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    demuxer->free();
    delete demuxer;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_FFDemuxer__1getTrackFormat(JNIEnv *env, jobject thiz,
                                                                    jlong ptr, jint index) {
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    demuxer->getTrackFormat(index);
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_github_zy3274311_ffmedia_demuxer_FFDemuxer__1readSampleData(JNIEnv *env, jobject thiz,
                                                                    jlong ptr,
                                                                    jobject byte_buf,
                                                                    jint offset) {
    void* buf = env->GetDirectBufferAddress(byte_buf);
    jlong capacity = env->GetDirectBufferCapacity(byte_buf);
    auto* demuxer = reinterpret_cast<FFDemuxer *>(ptr);
    return demuxer->readSampleData(buf, capacity);
}

//FFMuxer
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_zy3274311_ffmedia_muxer_FFMuxer__1init(JNIEnv *env, jobject thiz) {
    auto *muxer = new FFMuxer();
    return reinterpret_cast<jlong>(muxer);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_muxer_FFMuxer__1release(JNIEnv *env, jobject thiz, jlong ptr) {
    auto* muxer = reinterpret_cast<FFMuxer *>(ptr);
    muxer->free();
    delete muxer;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_muxer_FFMuxer__1setup(JNIEnv *env, jobject thiz, jlong ptr,
                                                       jstring url, jint format) {
    auto* muxer = reinterpret_cast<FFMuxer *>(ptr);
    jboolean isCopy = JNI_TRUE;
    const char* u = env->GetStringUTFChars(url, &isCopy);
    muxer->setup(u, format);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_muxer_FFMuxer__1start(JNIEnv *env, jobject thiz, jlong ptr) {
    auto* muxer = reinterpret_cast<FFMuxer *>(ptr);
    muxer->start();
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_muxer_FFMuxer__1stop(JNIEnv *env, jobject thiz, jlong ptr) {
    auto* muxer = reinterpret_cast<FFMuxer *>(ptr);
    muxer->stop();
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_zy3274311_ffmedia_muxer_FFMuxer__1writeSampleData(JNIEnv *env, jobject thiz,
                                                                 jlong ptr, jint track_index,
                                                                 jobject byte_buf,
                                                                 jobject buffer_info) {
    auto* muxer = reinterpret_cast<FFMuxer *>(ptr);
    muxer->writeSampleData();
}