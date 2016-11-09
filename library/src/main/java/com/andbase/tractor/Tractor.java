package com.andbase.tractor;

import com.andbase.tractor.event.EventException;
import com.andbase.tractor.event.EventType;
import com.andbase.tractor.event.Subscriber;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by huxq17 on 2016/11/8.
 */

public class Tractor {
    private static final int mInvalidType = -1;
    private static final String SUBSCRIBER_METHOD = "onEvent";
    private static final ConcurrentHashMap<EventType, CopyOnWriteArrayList<Subscriber>> mSubscriptions = new ConcurrentHashMap<>();
    private volatile CopyOnWriteArrayList<Subscriber> mSubscribers = new CopyOnWriteArrayList<>();

    private static class InstanceHolder {
        private static final Tractor instance = new Tractor();
    }

    public static Tractor instance() {
        return InstanceHolder.instance;
    }

    public void register(Subscriber subscriber) {
        if (subscriber != null) {
            final EventType type = findEventType(subscriber);
            CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.get(type);
            if (subscribers == null) {
                subscribers = new CopyOnWriteArrayList<>();
                mSubscriptions.put(type, subscribers);
            }
            subscribers.add(subscriber);
        }
    }

    public void unregister(Subscriber subscriber) {
        final EventType type = findEventType(subscriber);
        mSubscriptions.remove(type);
    }


    public synchronized boolean isRegistered(Subscriber subscriber) {
        final EventType type = findEventType(subscriber);
        return mSubscriptions.containsKey(type);
    }

    public void post(int type, Object event) {
        CopyOnWriteArrayList<Subscriber> subscribers = null;
        if (type == mInvalidType) {
            subscribers = mSubscriptions.get(type);
        } else {
            Class<?> eventType = event.getClass();
            subscribers = mSubscriptions.get(eventType);
        }
        if (subscribers != null) {
            for (Subscriber subscriber : subscribers) {
                postSingleEvent(subscriber, event);
            }
        }
    }

    private void postSingleEvent(Subscriber subscriber, Object event) {
        subscriber.onEvent(event);
    }

    public void post(Object event) {
        post(mInvalidType, event);
    }

    private EventType findEventType(Subscriber subscriber) {
        EventType eventType = subscriber.eventType;
        if (eventType == null) {
            eventType = new EventType();
            Class<?> subscriberClass = subscriber.getClass();
            try {
                Method subscriberMethod = subscriberClass.getMethod(SUBSCRIBER_METHOD, Object.class);
                String methodName = subscriberMethod.getName();
                Class<?>[] paramsTypes = subscriberMethod.getParameterTypes();
                if (paramsTypes.length == 1) {
                    Class<?> type = paramsTypes[0];
                    eventType.spareType = type;
                } else {
                    throw new EventException("method " + methodName +
                            "must have exactly 1 parameter but has " + paramsTypes.length);
                }
            } catch (NoSuchMethodException e) {
                throw new EventException(e);
            }
        }
        return eventType;
    }

    public void clear() {
        mSubscriptions.clear();
    }
}
