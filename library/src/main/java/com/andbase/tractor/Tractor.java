package com.andbase.tractor;

import com.andbase.tractor.event.EventTractor;
import com.andbase.tractor.event.Subscriber;

import static android.R.attr.type;

/**
 * Created by huxq17 on 2016/11/10.
 */

public class Tractor {
    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Subscriber)}or {@link #unregister(int)} once they
     * are no longer interested in receiving events.
     * @param subscriber
     */
    public static void register(Subscriber subscriber) {
        EventTractor.instance().register(subscriber);
    }

    /**
     * unregister given subsriber
     *
     * @param subscriber
     */
    public static void unregister(Subscriber subscriber) {
        EventTractor.instance().unregister(subscriber);
    }

    /**
     * unregister multiple subscribers with given type.
     *
     * @param eventType
     */
    public static void unregister(int eventType) {
        EventTractor.instance().unregister(eventType);
    }

    /**
     * post the given value with given type and it will deliver to Subscribers that are interested in this.
     * @param type
     * @param event
     */
    public static void post(int eventType, Object event) {
        EventTractor.instance().post(eventType, event);
    }

    /**
     * post the given value and it will deliver to Subscribers that are interested in this.
     * @param event
     */
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
