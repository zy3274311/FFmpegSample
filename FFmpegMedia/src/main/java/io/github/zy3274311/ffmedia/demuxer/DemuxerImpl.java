package io.github.zy3274311.ffmedia.demuxer;

import android.net.Uri;

import java.nio.ByteBuffer;

import io.github.zy3274311.ffmedia.Demuxer;
import io.github.zy3274311.ffmedia.FFMediaFormat;

public class DemuxerImpl implements Demuxer {
    private final long ptr;
    public DemuxerImpl() {
        ptr = _setUp();
    }

    @Override
    public void setDataSource(Uri uri) {
        _setDataSource(ptr, uri.toString());
    }

    @Override
    public int getTrackCount() {
        return _getTrackCount(ptr);
    }

    @Override
    public FFMediaFormat getTrackFormat(int index) {
        _getTrackFormat(ptr, index);
        return null;
    }

    @Override
    public void selectTrack(int index) {

    }

    @Override
    public int readSampleData(ByteBuffer byteBuf, int offset) {
        return _readSampleData(ptr, byteBuf, offset);
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
        _release(ptr);
    }

    private native long _setUp();

    private native void _setDataSource(long ptr, String url);

    private native int _getTrackCount(long ptr);

    private native void _getTrackFormat(long ptr, int index);

    public native int _readSampleData(long ptr, ByteBuffer byteBuf, int offset);

    private native void _release(long ptr);
}
