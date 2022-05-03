package io.github.zy3274311.ffmpegmedia;

import io.github.zy3274311.ffmpegmedia.demuxer.DemuxerImpl;

public class FFmpegEngine {
    // Used to load the 'ffmpegmedia' library on application startup.
    static {
        System.loadLibrary("ffmpegmedia");
    }

    public native String stringFromJNI();


    public static Demuxer createDemuxer() {
        return new DemuxerImpl();
    }

    public static Muxer createMuxer() {
        return null;
    }
}
