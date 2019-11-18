package com.hzy.live.streamer.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.hzy.live.streamer.consts.StreamerAction;
import com.hzy.live.streamer.instance.LiveScreenInstance;

public class ScreenStreamService extends IntentService {

    public ScreenStreamService() {
        super("ScreenStreamService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(StreamerAction.ACTION_START)) {
                LogUtils.i("Start Screen Stream");
                LiveScreenInstance.INSTANCE.startScreenStream(this, intent);
            } else {
                LiveScreenInstance.INSTANCE.stopScreenStream();
            }
        }
    }
}
