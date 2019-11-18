package com.hzy.live.streamer.activity;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.hzy.live.streamer.R;
import com.hzy.live.streamer.encoder.EncoderListener;
import com.hzy.live.streamer.encoder.H264BufferEncoder;
import com.hzy.live.streamer.instance.CameraInstance;
import com.hzy.live.streamer.instance.Live555Instance;
import com.hzy.live.streamer.utils.QRUtils;
import com.hzy.live.streamer.widget.CameraSurfaceView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StreamCameraActivity extends AppCompatActivity
        implements Camera.PreviewCallback, EncoderListener {

    @BindView(R.id.camera_surface)
    CameraSurfaceView mCameraSurface;
    @BindView(R.id.stream_session_url)
    TextView mStreamSessionUrl;
    @BindView(R.id.qr_code_image)
    ImageView mQrCodeImage;
    private H264BufferEncoder mAvcEncoder;
    private int mWidth = 640;
    private int mHeight = 480;
    private int mCurrentCameraIndex = 0;
    private Live555Instance mInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = Live555Instance.INSTANCE;
        setContentView(R.layout.activity_stream_camera);
        ButterKnife.bind(this);
        mAvcEncoder = new H264BufferEncoder(mWidth, mHeight);
        mAvcEncoder.setListener(this);
        if (mInstance.isIsRunning()) {
            loadSessionUrl();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.permission(PermissionConstants.CAMERA)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        openCameraPreview();
                    }

                    @Override
                    public void onDenied() {

                    }
                }).request();
    }


    @OnClick(R.id.change_camera)
    public void onChangeCameraClicked() {
        new Thread() {
            @Override
            public void run() {
                mCurrentCameraIndex = 1 - mCurrentCameraIndex;
                CameraInstance.INSTANCE.startPreview(mCurrentCameraIndex,
                        mWidth, mHeight, mCameraSurface.getHolder());
            }
        }.start();
    }

    @OnClick(R.id.start_server)
    public void onMStartServerClicked() {
        startStreamer();
    }

    @OnClick(R.id.stop_server)
    public void onMStopServerClicked() {
        stopStreamer();
    }

    private void openCameraPreview() {
        try {
            CameraInstance.INSTANCE.setPreviewCallback(this);
            new Thread() {
                @Override
                public void run() {
                    CameraInstance.INSTANCE.startPreview(mCurrentCameraIndex,
                            mWidth, mHeight, mCameraSurface.getHolder());
                }
            }.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void startStreamer() {
        if (!mInstance.isIsRunning()) {
            mInstance.start();
            loadSessionUrl();
        }
        if (!mAvcEncoder.isIsRunning()) {
            mAvcEncoder.start();
        }
    }

    private void loadSessionUrl() {
        String sessionUrl = mInstance.getStreamUrl();
        mStreamSessionUrl.setText(sessionUrl);
        mQrCodeImage.setImageBitmap(QRUtils.generateQRImg(sessionUrl));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mAvcEncoder.isIsRunning()) {
            mAvcEncoder.queenFrame(data);
        }
    }

    @Override
    protected void onStop() {
        stopStreamer();
        CameraInstance.INSTANCE.stop();
        super.onStop();
    }

    private void stopStreamer() {
        if (mInstance.isIsRunning()) {
            mInstance.stopServer();
        }
        if (mAvcEncoder.isIsRunning()) {
            mAvcEncoder.stop();
        }
    }

    @Override
    public void onEncodeRaw(byte[] output) {
        try {
            mInstance.feedH264Data(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
