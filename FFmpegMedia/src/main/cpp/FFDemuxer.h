//
// Created by 张英 on 2022/5/3.
//

#ifndef FFMPEGSAMPLE_FFDEMUXER_H
#define FFMPEGSAMPLE_FFDEMUXER_H
extern "C" {
#include "libavformat/avformat.h"
#include "ndk_log.h"
#include "libavutil/log.h"

    class FFDemuxer {

    private:
        AVFormatContext *fmt_ctx = NULL;
        void ff_log(void* ptr, int level, const char* fmt, va_list vl) {
            LOGE(FFMEDIA_TAG, "%s   %s", fmt, vl);
        }
    public:
        void setDataSource(const char *string);

        int getTrackCount();

        void getTrackFormat(int index);

        int readSampleData();

        void free();
    };
}
#endif //FFMPEGSAMPLE_FFDEMUXER_H
