package com.andbase.tractor.event;

import android.os.Looper;

import com.andbase.tractor.utils.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by huxq17 on 2016/11/8.
 */

public class EventTractor {
    private static final int mInvalidType = -1;
    private final PostHandler mainThreadHandler;
    private static final String SUBSCRIBER_METHOD = "onEvent";
    private static final ConcurrentHashMap<EventType, CopyOnWriteArrayList<Subscriber>> mSubscriptions = new ConcurrentHashMap<>();
    private volatile CopyOnWriteArrayList<Event> mStickyEvents = new CopyOnWriteArrayList<>();

    private static class InstanceHolder {
        private static final EventTractor instance = new EventTractor();
    }

    public static EventTractor instance() {
        return InstanceHolder.instance;
    }

    private EventTractor() {
        mainThreadHandler = new PostHandler(Looper.getMainLooper(), this);
    }

    public void register(Subscriber subscriber) {
        if (subscriber != null) {
            final EventType type = findEventType(subscriber);
            if (type == null) {
                LogUtils.e("can not find value type,register fail!");
                return;
            }
            CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.get(type);
            if (subscribers == null) {
                subscribers = new CopyOnWriteArrayList<>();
                mSubscriptions.put(type, subscribers);
            }
            subscribers.add(subscriber);
            subscriber.isRegistered = true;
        }
    }

    public void unregister(Subscriber subscriber) {
        if (subscriber != null) {
            final EventType type = subscriber.eventType;
            unregisterSubscriberByType(type, subscriber);
        }
    }

    public void unregister(int type) {
        if (type != mInvalidType) {
            final EventType eventType = new EventType(type);
            CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.remove(eventType);
            if (subscribers == null) return;
            for (Subscriber subscriber : subscribers) {
                subscriber.isRegistered = false;
            }
        }
    }

    private synchronized void unregisterSubscriberByType(EventType type, Subscriber subscriber) {
        CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.get(type);
        if (subscribers != null && subscribers.size() > 0) {
            subscribers.remove(subscriber);
            subscriber.isRegistered = false;
        }
    }

    public synchronized boolean isRegistered(Subscriber subscriber) {
        if (subscriber != null) {
            return subscriber.isRegistered;
        }
        return false;
    }

    public synchronized boolean isRegistered(int eventType) {
        final EventType type = new EventType(eventType);
        return mSubscriptions.containsKey(type);
    }

    private void checkEvent(Subscriber subscriber, Object event) {
        Class subscriberType = subscriber.eventType.spareType;
        Class postEventType = event.getClass();
        if (!subscriberType.equals(postEventType)) {
            throw new EventException("Subscriber's onEvent method's parameter is " + subscriberType.getCanonicalName() +
                    ",but value is " + postEventType.getCanonicalName() + ",please keep they consistent.");
        }
    }

    private void postSingleEvent(Subscriber subscriber, Event event) {
        checkEvent(subscriber, event);
        PendingPost pendingPost = new PendingPost(subscriber, event);
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (subscriber.mainThread) {
            if (!isMainThread) {
                mainThreadHandler.postToMainThread(pendingPost);
            } else {
                deliverToSubsriber(pendingPost);
            }
        } else {
            deliverToSubsriber(pendingPost);
        }
    }

    protected void deliverToSubsriber(PendingPost pendingPost) {
        Object event = pendingPost.event.value;
        boolean sticky = pendingPost.event.sticky;
        Subscriber subscriber = pendingPost.subscriber;
        subscriber.onEvent(event);
        if (sticky) {
            mStickyEvents.remove(pendingPost.event);
        }
    }

    public void post(int type, Object event) {
        post(type, new Event(event));
    }

    private void post(int type, Event event) {
        CopyOnWriteArrayList<Subscriber> subscribers = null;
        if (type != mInvalidType) {
            subscribers = mSubscriptions.get(new EventType(type));
        } else {
            Class<?> eventType = event.value.getClass();
            subscribers = mSubscriptions.get(new EventType(eventType));
        }
        if (subscribers != null) {
            for (Subscriber subscriber : subscribers) {
                postSingleEvent(subscriber, event);
            }
        }
    }

    public void post(Object event) {
        post(mInvalidType, event);
    }

    public void postStick(int type, Object stickyEvent) {
        Event event = new Event(stickyEvent, true);
        mStickyEvents.add(event);
        post(type, event);
    }

    public void postStick(Object stickyEvent) {
        postStick(mInvalidType, stickyEvent);
    }


    private EventType findEventType(Subscriber subscriber) {
        EventType eventType = subscriber.eventType;
        Class<?> subscriberClass = subscriber.getClass();
        Type genType = subscriberClass.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        try {
            Method subscriberMethod = subscriberClass.getMethod(SUBSCRIBER_METHOD, Object.class);
            Subscribe subscribeAnnotation = subscriberMethod.getAnnotation(Subscribe.class);
            if (subscribeAnnotation != null) {
                subscriber.mainThread = subscribeAnnotation.mainThread();
                subscriber.sticky = subscribeAnnotation.sticky();
            }
//                String methodName = subscriberMethod.getName();
            if (params.length == 1) {
                Class clazz = (Class) params[0];
                if (eventType == null) {
                    eventType = new EventType(clazz);
                }
            } else {
                throw new EventException("class " + subscriberClass.getName() +
                        "must have exactly 1 parameter but has " + params.length);
            }
        } catch (NoSuchMethodException e) {
            throw new EventException(e);
        }
        eventType.spareType = (Class) params[0];
        if (subscriber.eventType == null) {
            subscriber.eventType = eventType;
        }
        return eventType;
    }

    public synchronized void clear() {
        mSubscriptions.clear();
        Set<Map.Entry<EventType, CopyOnWriteArrayList<Subscriber>>> entrySet = mSubscriptions.entrySet();
        List<EventType> eventTypes = new ArrayList<>();
        for (Map.Entry<EventType, CopyOnWriteArrayList<Subscriber>> entry : entrySet) {
            eventTypes.add(entry.getKey());
        }
        for (EventType eventType : eventTypes) {
            CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.remove(eventType);
            if (subscribers == null) return;
            for (Subscriber subscriber : subscribers) {
                subscriber.isRegistered = false;
            }
        }
        eventTypes.clear();
    }
}
