package io.github.zy3274311.ffmedia;


import android.net.Uri;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

public interface Demuxer {
    /**
     * If possible, seek to a sync sample at or before the specified time
     */
    public static final int SEEK_TO_PREVIOUS_SYNC       = 0;
    /**
     * If possible, seek to a sync sample at or after the specified time
     */
    public static final int SEEK_TO_NEXT_SYNC           = 1;
    /**
     * If possible, seek to the sync sample closest to the specified time
     */
    public static final int SEEK_TO_CLOSEST_SYNC        = 2;

    void setDataSource(Uri uri);

    int getTrackCount();

    FFMediaFormat getTrackFormat(int index);

    void selectTrack(int index);

    int readSampleData(ByteBuffer byteBuf, int offset);

    int getSampleTrackIndex();

    long getSampleTime();

    boolean advance();

    void seekTo(long timeUs, @SeekMode int mode);

    void release();

    @IntDef({
            SEEK_TO_PREVIOUS_SYNC,
            SEEK_TO_NEXT_SYNC,
            SEEK_TO_CLOSEST_SYNC,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface SeekMode {}
}
