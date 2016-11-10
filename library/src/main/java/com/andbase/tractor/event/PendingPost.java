package com.andbase.tractor.event;

/**
 * Created by 2144 on 2016/11/10.
 */

public class PendingPost {
    public Subscriber subscriber;
    public Object event;

    public PendingPost(Subscriber subscriber, Object event) {
        this.subscriber = subscriber;
        this.event = event;
    }
}
