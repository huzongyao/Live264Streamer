package com.hzy.live.streamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hzy.live.streamer.R;

import java.util.Timer;
import java.util.TimerTask;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.core.ScanBoxView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class QRScanActivity extends AppCompatActivity
        implements QRCodeView.Delegate {

    public static final String EXTRA_CONTENT = "EXTRA_CONTENT";
    public static final String EXTRA_VIBERATE = "EXTRA_VIBERATE";
    private static final long SCAN_TIME_OUT = 30_000L;
    private ZXingView mZXingView;
    private String mAmbientBrightnessTip;
    private Timer mFinishTimer;
    private ImageView mFlashTogger;
    private boolean mIsFlashOn = false;
    private TimerTask mFishTask;
    private boolean mViberateOnFinish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxing);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mAmbientBrightnessTip = getString(R.string.qr_tips_too_dark);
        mZXingView = findViewById(R.id.zxing_view);
        mFlashTogger = findViewById(R.id.button_flash);
        mZXingView.setDelegate(this);
        mFishTask = new TimerTask() {
            @Override
            public void run() {
                ToastUtils.showShort(R.string.qr_tip_scan_timeout);
                finish();
            }
        };
        mViberateOnFinish = getIntent().getBooleanExtra(EXTRA_VIBERATE, true);
        mFlashTogger.setOnClickListener(v -> switchFlashLight());
    }

    private void switchFlashLight() {
        if (mIsFlashOn) {
            mZXingView.closeFlashlight();
            mFlashTogger.setImageResource(R.drawable.ic_flash_off);
        } else {
            mZXingView.openFlashlight();
            mFlashTogger.setImageResource(R.drawable.ic_flash_on);
        }
        mIsFlashOn = !mIsFlashOn;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCameraAndSpot();
    }

    private void startCameraAndSpot() {
        PermissionUtils.permission(PermissionConstants.CAMERA)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        mIsFlashOn = false;
                        mFlashTogger.setVisibility(View.GONE);
                        mZXingView.startCamera();
                        mZXingView.startSpotAndShowRect();
                        mFinishTimer = new Timer();
                        mFinishTimer.schedule(mFishTask, SCAN_TIME_OUT);
                    }

                    @Override
                    public void onDenied() {
                    }
                }).request();
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera();
        mFinishTimer.cancel();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        if (mViberateOnFinish) {
            vibrate();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONTENT, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        String tipText = mZXingView.getScanBoxView().getTipText();
        ScanBoxView scanBoxView = mZXingView.getScanBoxView();
        int toggleVisible = View.VISIBLE;
        if (isDark) {
            if (!tipText.contains(mAmbientBrightnessTip)) {
                scanBoxView.setTipText(tipText + mAmbientBrightnessTip);
            }
        } else {
            if (tipText.contains(mAmbientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(mAmbientBrightnessTip));
                scanBoxView.setTipText(tipText);
            }
            if (!mIsFlashOn) {
                toggleVisible = View.GONE;
            }
        }
        if (mFlashTogger.getVisibility() != toggleVisible) {
            mFlashTogger.setVisibility(toggleVisible);
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }
}
