package com.andbase.tractor.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by huxq17 on 2016/11/10.
 */

public class PostHandler extends Handler {
    private EventTractor tractor;
    private CopyOnWriteArrayList<PendingPost> pendingPosts = new CopyOnWriteArrayList<>();

    public PostHandler(Looper looper, EventTractor tractor) {
        super(looper);
        this.tractor = tractor;
    }

    private boolean handlerActive;

    public void postToMainThread(PendingPost pendingPost) {
        synchronized (this) {
            pendingPosts.add(pendingPost);
            sendMessage(obtainMessage());
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        synchronized (this) {
            for (PendingPost pendingPost : pendingPosts) {
                tractor.deliverToSubsriber(pendingPost);
            }
            pendingPosts.clear();
        }
    }
}
