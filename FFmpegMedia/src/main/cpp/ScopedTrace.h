//
// Created by 张英 on 2022/5/11.
//

#ifndef FFMPEGSAMPLE_SCOPEDTRACE_H
#define FFMPEGSAMPLE_SCOPEDTRACE_H

extern "C" {
    #include "android/trace.h"
    #define ATRACE_NAME(name) ScopedTrace ___tracer(name)

    // ATRACE_CALL is an ATRACE_NAME that uses the current function name.
    #define ATRACE_CALL() ATRACE_NAME(__FUNCTION__)

    class ScopedTrace {
    public:
        inline ScopedTrace(const char *name) {
            ATrace_beginSection(name);
        }

        inline ~ScopedTrace() {
            ATrace_endSection();
        }
    };
}
#endif //FFMPEGSAMPLE_SCOPEDTRACE_H
