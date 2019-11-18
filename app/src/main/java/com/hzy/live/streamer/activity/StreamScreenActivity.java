package com.hzy.live.streamer.activity;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.hzy.live.streamer.R;
import com.hzy.live.streamer.consts.RequestCode;
import com.hzy.live.streamer.consts.StreamerAction;
import com.hzy.live.streamer.instance.Live555Instance;
import com.hzy.live.streamer.service.ScreenStreamService;
import com.hzy.live.streamer.utils.QRUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StreamScreenActivity extends AppCompatActivity {

    @BindView(R.id.stream_session_url)
    TextView mStreamSessionUrl;
    @BindView(R.id.qr_code_image)
    ImageView mQrCodeImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_screen);
        ButterKnife.bind(this);
        if (Live555Instance.INSTANCE.isIsRunning()) {
            updateSessionUrl();
        }
    }

    @OnClick(R.id.btn_start_server)
    public void onBtnStartServerClicked() {
        startScreenStreamer();
    }

    @OnClick(R.id.btn_stop_server)
    public void onBtnStopServerClicked() {
        stopScreenStreamer();
    }

    private void startScreenStreamer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager manager = (MediaProjectionManager)
                    this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            Intent intent = manager.createScreenCaptureIntent();
            startActivityForResult(intent, RequestCode.GET_SCREEN_RECORD_INTENT);
        } else {
            ToastUtils.showShort("Require Android Version LOLLIPOP!");
        }
    }

    private void stopScreenStreamer() {
        Intent intent = new Intent(this, ScreenStreamService.class);
        intent.setAction(StreamerAction.ACTION_STOP);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.GET_SCREEN_RECORD_INTENT) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ScreenStreamService.class);
                intent.setAction(StreamerAction.ACTION_START);
                intent.putExtra("resultCode", resultCode);
                intent.putExtra("data", data);
                startService(intent);
                updateSessionUrl();
            } else {
                finish();
            }
        }
    }

    private void updateSessionUrl() {
        mStreamSessionUrl.postDelayed(() -> {
            Live555Instance instance = Live555Instance.INSTANCE;
            String sessionUrl = instance.getStreamUrl();
            mStreamSessionUrl.setText(sessionUrl);
            mQrCodeImage.setImageBitmap(QRUtils.generateQRImg(sessionUrl));
        }, 300);
    }
}
