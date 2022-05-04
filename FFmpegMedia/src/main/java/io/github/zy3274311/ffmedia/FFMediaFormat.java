package io.github.zy3274311.ffmedia;

/**
 * codec_par.h
 * typedef struct AVCodecParameters{
 *      ...
 * }
 */
public class FFMediaFormat {
    private byte[] extradata;

    private int format;
    private int codec_type;
    private int codec_id;
    private int codec_tag;
    private long bit_rate;
    private int bits_per_coded_sample;
    private int bits_per_raw_sample;
    private int profile;
    private int level;
    private int width;
    private int height;
    /**
     *      Video only. The aspect ratio (width / height) which a single pixel
     *      should have when displayed.
     *
     *      When the aspect ratio is unknown / undefined, the numerator should be
     *      set to 0 (the denominator may have any value).
     *
     *      AVRational sample_aspect_ratio;
     */
    private float sample_aspect_ratio;
    /**
     *     Video only. The order of the fields in interlaced video.
     *
     *     enum AVFieldOrder                  field_order;
     *
     *     Video only. Additional colorspace characteristics.
     *
     *     enum AVColorRange                  color_range;
     *     enum AVColorPrimaries              color_primaries;
     *     enum AVColorTransferCharacteristic color_trc;
     *     enum AVColorSpace                  color_space;
     *     enum AVChromaLocation              chroma_location;
     */
    private int field_order;
    private int color_range;
    private int color_primaries;
    private int color_trc;
    private int color_space;
    private int chroma_location;

    private int sample_rate;
    private int block_align;
    private int frame_size;
    private int initial_padding;
    private int trailing_padding;
    private int seek_preroll;

    private long channel_layout;
    private int channels;
}
