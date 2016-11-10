package com.andbase.tractor;

import com.andbase.tractor.event.EventTractor;
import com.andbase.tractor.event.Subscriber;

/**
 * Created by huxq17 on 2016/11/10.
 */

public class Tractor {
    public static void register(Subscriber subscriber) {
        EventTractor.instance().register(subscriber);
    }

    public static void unregister(Subscriber subscriber) {
        EventTractor.instance().unregister(subscriber);
    }

    public static void post(int type, Object event) {
        EventTractor.instance().post(type, event);
    }

    public static void post(Object event) {
        EventTractor.instance().post(event);
    }

    public static void postSticky(Object event) {

    }

    public synchronized boolean isRegistered(Subscriber subscriber) {
        return EventTractor.instance().isRegistered(subscriber);
    }

    public synchronized boolean isRegistered(int eventType) {
        return EventTractor.instance().isRegistered(eventType);
    }

    public static void clear() {
        EventTractor.instance().clear();
    }
}
