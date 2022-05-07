//
// Created by 张英 on 2022/5/6.
//

#include "FFMuxer.h"

void FFMuxer::setup(const char *url, int format) {
    int ret = avformat_open_input(&fmt_ctx, url, nullptr, nullptr);
    if (ret < 0) {
        LOGE(TAG, "avformat_open_input code:%d msg:%s", ret, av_err2str(ret));
        return;
    }
}

void FFMuxer::start() {

}


void FFMuxer::writeSampleData() {

}


void FFMuxer::stop() {

}


void FFMuxer::free() {
    avformat_free_context(fmt_ctx);
}