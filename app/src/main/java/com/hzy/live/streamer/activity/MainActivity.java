package com.hzy.live.streamer.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hzy.live.streamer.R;
import com.hzy.live.streamer.consts.RequestCode;
import com.hzy.rtsp.live555.Live555Api;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_live_info)
    TextView mTextLiveInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadServerInfo();
    }

    private void loadServerInfo() {
        String liveVersionInfo = Live555Api.getVersionInfo();
        String serverString = "Live555 Info:\n" + liveVersionInfo;
        NetworkUtils.NetworkType networkType = NetworkUtils.getNetworkType();
        serverString += "\nNetwork Type: " + networkType.toString();
        if (networkType == NetworkUtils.NetworkType.NETWORK_WIFI ||
                networkType == NetworkUtils.NetworkType.NETWORK_ETHERNET) {
            if (NetworkUtils.isConnected()) {
                String ipAddress = NetworkUtils.getIpAddressByWifi();
                serverString += "\nIP Address: " + ipAddress;
            }
        }
        mTextLiveInfo.setText(serverString);
    }

    @OnClick(R.id.btn_stream_screen)
    public void onBtnStreamScreenClicked() {
        startActivity(new Intent(this, StreamScreenActivity.class));
    }

    @OnClick(R.id.btn_stream_camera)
    public void onBtnStreamCameraClicked() {
        startActivity(new Intent(this, StreamCameraActivity.class));
    }

    @OnClick(R.id.btn_scan_to_show)
    public void onBtnScanPlayClicked() {
        startActivityForResult(new Intent(this, QRScanActivity.class),
                RequestCode.SCAN_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.SCAN_QR_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                String content = data.getStringExtra(QRScanActivity.EXTRA_CONTENT);
                if (!StringUtils.isTrimEmpty(content)) {
                    Intent intent = new Intent(this, VideoPlayActivity.class);
                    intent.setData(Uri.parse(content));
                    startActivity(intent);
                }
            }
        }
    }
}
