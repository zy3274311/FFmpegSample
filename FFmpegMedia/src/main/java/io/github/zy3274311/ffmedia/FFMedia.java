package io.github.zy3274311.ffmedia;

import io.github.zy3274311.ffmedia.demuxer.FFDemuxer;
import io.github.zy3274311.ffmedia.muxer.FFMuxer;

public class FFMedia {
    // Used to load the 'ffmedia' library on application startup.
    static {
        System.loadLibrary("ffmedia");
    }

    public native String stringFromJNI();


    public static Demuxer createDemuxer() {
        return new FFDemuxer();
    }

    public static Muxer createMuxer() {
        return new FFMuxer();
    }
}
