package io.github.zy3274311.ffmpegmedia.demuxer;

import android.net.Uri;

import java.nio.ByteBuffer;

import io.github.zy3274311.ffmpegmedia.Demuxer;
import io.github.zy3274311.ffmpegmedia.Format;

public class DemuxerImpl implements Demuxer {
    @Override
    public void setDataSource(Uri uri) {

    }

    @Override
    public int getTrackCount() {
        return 0;
    }

    @Override
    public Format getTrackFormat(int index) {
        return null;
    }

    @Override
    public void selectTrack(int index) {

    }

    @Override
    public int readSampleData(ByteBuffer byteBuf, int offset) {
        return 0;
    }

    @Override
    public int getSampleTrackIndex() {
        return 0;
    }

    @Override
    public long getSampleTime() {
        return 0;
    }

    @Override
    public boolean advance() {
        return false;
    }

    @Override
    public void seekTo(long timeUs, int mode) {

    }

    @Override
    public void release() {

    }
}
