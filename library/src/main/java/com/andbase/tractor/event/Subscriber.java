package com.andbase.tractor.event;

/**
 * Created by 2144 on 2016/11/8.
 */

public abstract class Subscriber {
    public EventType eventType;

    public Subscriber() {
    }

    public Subscriber(int eventType) {
        this.eventType = new EventType(eventType);
    }

    public abstract void onEvent(Object event);
}
