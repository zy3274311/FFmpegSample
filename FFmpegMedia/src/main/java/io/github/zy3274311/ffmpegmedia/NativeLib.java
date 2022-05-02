package io.github.zy3274311.ffmpegmedia;

public class NativeLib {

    // Used to load the 'ffmpegmedia' library on application startup.
    static {
        System.loadLibrary("ffmpegmedia");
    }

    /**
     * A native method that is implemented by the 'ffmpegmedia' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}