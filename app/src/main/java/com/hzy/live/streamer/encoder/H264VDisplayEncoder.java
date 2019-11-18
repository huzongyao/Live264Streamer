package com.hzy.live.streamer.encoder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class H264VDisplayEncoder extends BaseMediaEncoder {
    private static final int FRAME_RATE = 15;
    private static final int COMPRESS_RATIO = 128;
    private static final int I_FRAME_INTERVAL = 5;

    private final int mWidth;
    private final int mHeight;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public H264VDisplayEncoder(Context context, int width, int height,
                               int resultCode, @NonNull Intent resultData) {
        mWidth = width;
        mHeight = height;

        MediaFormat mediaFormat =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        int bitRate = height * width * 24 * FRAME_RATE / COMPRESS_RATIO;
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
        mediaFormat.setInteger(MediaFormat.KEY_COMPLEXITY,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mMediaCodec.configure(mediaFormat, null,
                    null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mMediaCodec.createInputSurface();
            MediaProjectionManager projectionManager = (MediaProjectionManager)
                    context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjection = projectionManager.getMediaProjection(resultCode, resultData);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealMetrics(displayMetrics);
            int dpi = displayMetrics.densityDpi;
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("H264VDisplay",
                    width, height, dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                    surface, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stop() {
        mMediaProjection.stop();
        mVirtualDisplay.release();
        super.stop();
    }
}
