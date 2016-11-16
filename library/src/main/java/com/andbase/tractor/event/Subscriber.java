package com.andbase.tractor.event;

/**
 * Created by huxq17 on 2016/11/8.
 */

public abstract class Subscriber<T> {
    protected EventType eventType;
    protected boolean mainThread = true;
    protected boolean sticky = false;

    public Subscriber() {
    }

    public Subscriber(int eventType) {
        this.eventType = new EventType(eventType);
    }

    public abstract void onEvent(T event);
}
