package io.github.zy3274311.ffmedia.muxer;

import android.net.Uri;

import java.nio.ByteBuffer;

import io.github.zy3274311.ffmedia.FFBufferInfo;
import io.github.zy3274311.ffmedia.FFMediaFormat;
import io.github.zy3274311.ffmedia.Muxer;

public class FFMuxer implements Muxer {
    private final long ptr;

    public FFMuxer() {
        ptr = _init();
    }

    @Override
    public void setup(Uri uri, int format) {
        _setup(ptr, uri.toString(), format);
    }

    @Override
    public void start() {

    }

    @Override
    public int addTrack(FFMediaFormat mediaFormat) {
        return 0;
    }

    @Override
    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, FFBufferInfo bufferInfo) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {
        _release(ptr);
    }

    private native long _init();

    private native void _setup(long ptr, String url, int format);

    private native void _start(long ptr);

    private native void _writeSampleData(long ptr, int trackIndex, ByteBuffer byteBuf, FFBufferInfo bufferInfo);

    private native void _stop(long ptr);

    private native void _release(long ptr);
}
