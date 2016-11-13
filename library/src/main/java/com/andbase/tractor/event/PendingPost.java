package com.andbase.tractor.event;

/**
 * Created by 2144 on 2016/11/10.
 */

public class PendingPost {
    public Subscriber subscriber;
    public Event event;

    public PendingPost(Subscriber subscriber, Event event) {
        this.subscriber = subscriber;
        this.event = event;
    }
}
