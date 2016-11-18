package com.andbase.tractor.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;

/**
 * Created by huxq17 on 2016/11/10.
 */

public class PostHandler extends Handler {
    private EventTractor tractor;
    private ArrayList<PendingPost> pendingPosts = new ArrayList<>();

    public PostHandler(Looper looper, EventTractor tractor) {
        super(looper);
        this.tractor = tractor;
    }

    private boolean handlerActive;

    public void postToMainThread(PendingPost pendingPost) {
        synchronized (pendingPosts) {
            pendingPosts.add(pendingPost);
            sendMessage(obtainMessage());
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        PendingPost[] postArray;
        synchronized (pendingPosts) {
            postArray = new PendingPost[pendingPosts.size()];
            pendingPosts.toArray(postArray);
            pendingPosts.clear();
        }
        for (PendingPost pendingPost : postArray) {
            tractor.deliverToSubsriber(pendingPost);
        }
    }
}
