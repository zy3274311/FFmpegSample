package io.github.zy3274311.ffmedia;

import io.github.zy3274311.ffmedia.demuxer.DemuxerImpl;

public class FFmpegEngine {
    // Used to load the 'ffmedia' library on application startup.
    static {
        System.loadLibrary("ffmedia");
    }

    public native String stringFromJNI();


    public static Demuxer createDemuxer() {
        return new DemuxerImpl();
    }

    public static Muxer createMuxer() {
        return null;
    }
}
