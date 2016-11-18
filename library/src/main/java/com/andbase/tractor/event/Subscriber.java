package com.andbase.tractor.event;

/**
 * Created by huxq17 on 2016/11/8.
 */

public abstract class Subscriber<T> {
    protected EventType eventType;
    protected boolean mainThread = true;
    protected boolean sticky = false;
    protected int pid;

    public Subscriber() {
        pid = android.os.Process.myPid();
    }

    public Subscriber(int eventType) {
        this();
        this.eventType = new EventType(eventType);
    }

    public abstract void onEvent(T event);
}
