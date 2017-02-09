package com.andbase.tractor;

import android.content.Context;

import com.andbase.tractor.event.Router;
import com.andbase.tractor.event.Subscriber;

/**
 * Created by huxq17 on 2016/11/10.
 */

public class Tractor {
    private static final Router mRouter = new Router();

    public static void init(Context context) {
        mRouter.init(context);
    }

    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Subscriber)}or {@link #unregister(int)} once they
     * are no longer interested in receiving events.
     *
     * @param subscriber
     */
    public static void register(Subscriber subscriber) {
        mRouter.register(subscriber);
    }

    /**
     * unregister given subsriber
     *
     * @param subscriber
     */
    public static void unregister(Subscriber subscriber) {
        mRouter.unregister(subscriber);
    }

    /**
     * unregister multiple subscribers with given type.
     *
     * @param eventType
     */
    public static void unregister(int eventType) {
        mRouter.unregister(eventType);
    }

    /**
     * post the given value with given type and it will deliver to Subscribers that are interested in this.
     *
     * @param eventType
     * @param event
     */
    public static void post(int eventType, Object event) {
        mRouter.post(eventType, event);
    }

    /**
     * post the given value and it will deliver to Subscribers that are interested in this.
     *
     * @param event
     */
    public static void post(Object event) {
        mRouter.post(event);
    }

    public static void postSticky(Object stickyEvent) {
        mRouter.postSticky(stickyEvent);
    }

    public static void postSticky(int type, Object stickyEvent) {
        mRouter.postSticky(type, stickyEvent);
    }

    public static boolean removeStickyEvent(int type) {
        return mRouter.removeStickyEvent(type);
    }

    public static boolean removeStickyEvent(Object stickyEvent) {
        return mRouter.removeStickyEvent(stickyEvent);
    }

    public static boolean isRegistered(Subscriber subscriber) {
        return mRouter.isRegistered(subscriber);
    }

    public static boolean isRegistered(int eventType) {
        return mRouter.isRegistered(eventType);
    }

    public static void clear() {
        mRouter.clear();
    }
}
