//
// Created by 张英 on 2022/5/3.
//

#ifndef FFMPEGSAMPLE_FFDEMUXER_H
#define FFMPEGSAMPLE_FFDEMUXER_H
extern "C" {
#include "libavformat/avformat.h"
#include "ndk_log.h"

    class FFDemuxer {
    private:
        AVFormatContext *fmt_ctx = NULL;
        const char *TAG = "FFMuxer";
    public:
        void setDataSource(const char *string);

        int getTrackCount();

        void getTrackFormat(int index);

        int readSampleData(void* buf, long capacity);

        void free();
    };
}
#endif //FFMPEGSAMPLE_FFDEMUXER_H
