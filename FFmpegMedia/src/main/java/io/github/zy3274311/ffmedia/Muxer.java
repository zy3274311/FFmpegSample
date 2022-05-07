package io.github.zy3274311.ffmedia;

import android.net.Uri;

import java.nio.ByteBuffer;

public interface Muxer {

    void setup(Uri uri, int format);

    void start();

    int addTrack(FFMediaFormat mediaFormat);

    void writeSampleData(int trackIndex, ByteBuffer byteBuf,
                         FFBufferInfo bufferInfo);

    void stop();

    void release();
}
