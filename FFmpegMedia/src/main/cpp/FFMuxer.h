//
// Created by 张英 on 2022/5/6.
//

#ifndef FFMPEGSAMPLE_FFMUXER_H
#define FFMPEGSAMPLE_FFMUXER_H

extern "C" {
#include <libavformat/avformat.h>
#include "ndk_log.h"
class FFMuxer {
private:
    AVFormatContext *fmt_ctx = NULL;
    const char *TAG = "FFMuxer";
public:
    void free();

    void start();

    void writeSampleData();

    void stop();

    void setup(const char *url, int format);
};
}


#endif //FFMPEGSAMPLE_FFMUXER_H
