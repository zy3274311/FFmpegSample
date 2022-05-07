//
// Created by 张英 on 2022/5/3.
//

#include "FFDemuxer.h"

void FFDemuxer::setDataSource(const char *url) {
//    avformat_network_init();

    int ret = avformat_open_input(&fmt_ctx, url, nullptr, nullptr);
    if (ret < 0) {
        LOGE(TAG, "avformat_open_input code:%d msg:%s", ret, av_err2str(ret));
        return;
    }
    ret = avformat_find_stream_info(fmt_ctx, nullptr);
    if (ret < 0) {
        LOGE(TAG, "avformat_find_stream_info code:%d msg:%s", ret, av_err2str(ret));
        return;
    }
    ret = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, nullptr, 0);
    if (ret < 0) {
        LOGE(TAG, "av_find_best_stream AVMEDIA_TYPE_VIDEO code:%d msg:%s", ret,
             av_err2str(ret));
        return;
    }

    ret = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    if (ret < 0) {
        LOGE(TAG, "av_find_best_stream AVMEDIA_TYPE_VIDEO code:%d msg:%s", ret,
             av_err2str(ret));
        return;
    }
    av_dump_format(fmt_ctx, 0, url, 0);
}

int FFDemuxer::getTrackCount() {
    return fmt_ctx->nb_streams;
}

void FFDemuxer::getTrackFormat(int index) {
    AVStream *streams = fmt_ctx->streams[index];
    AVDictionary *metadata = streams->metadata;
    int metadataCount = av_dict_count(metadata);
    LOGE(TAG, "metadataCount:%d", metadataCount);
    AVDictionaryEntry *entry = nullptr;
    while ((entry = av_dict_get(metadata, "", entry, AV_DICT_IGNORE_SUFFIX)) != nullptr) {
        LOGE(TAG, "av_dict_get key:%s value:%s", entry->key, entry->value);
    }
    AVMediaType codec_type = streams->codecpar->codec_type;
    int format = streams->codecpar->format;
    switch (codec_type) {
        case AVMEDIA_TYPE_VIDEO:
            break;
        case AVMEDIA_TYPE_AUDIO:
            break;
        case AVMEDIA_TYPE_UNKNOWN:
            break;
        case AVMEDIA_TYPE_DATA:
            break;
        case AVMEDIA_TYPE_SUBTITLE:
            break;
        case AVMEDIA_TYPE_ATTACHMENT:
            break;
        case AVMEDIA_TYPE_NB:
            break;
        default:
            break;
    }
    int width = streams->codecpar->width;
    int height = streams->codecpar->height;
    int profile = streams->codecpar->profile;

    AVCodecID codec_id = streams->codecpar->codec_id;
    const char *codec_name = avcodec_get_name(codec_id);
    const char *type = av_get_media_type_string(codec_type);
    const char *profile_name = avcodec_profile_name(codec_id, profile);

    LOGE(TAG,
         "codecpar\n"
         "width:%d \n"
         "height:%d \n"
         "format:%d \n"
         "codec_type:%s \n"
         "codec_name:%s\n"
         "profile_name:%s",
         width, height, format, type, codec_name, profile_name);
}

int FFDemuxer::readSampleData(void* buf, long capacity) {
    AVPacket *pkt = av_packet_alloc();
    if (!pkt) {
        LOGE(TAG, "av_packet_alloc fail");
    }
    int ret = av_read_frame(fmt_ctx, pkt);
    LOGE(TAG, "av_read_frame %d", ret);
    long pts = pkt->pts;
    LOGE(TAG, "av_read_frame pts:%ld", pts);
    long dts = pkt->dts;
    LOGE(TAG, "av_read_frame dts:%ld", dts);
    int size = pkt->size;
    LOGE(TAG, "av_read_frame size:%d", size);

    memcpy(buf, pkt->data, size);
    av_packet_unref(pkt);
    av_packet_free(&pkt);

//    AVFrame *frame = av_frame_alloc();

    return size;
}

void FFDemuxer::free() {
    if(fmt_ctx){
        avformat_close_input(&fmt_ctx);
        avformat_free_context(fmt_ctx);
//        avformat_network_deinit();
        fmt_ctx = nullptr;
    }
}
