package com.andbase.tractor.event;

import android.os.Looper;

import com.andbase.tractor.utils.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by huxq17 on 2016/11/8.
 */

public class EventTractor {
    private static final int mInvalidType = -1;
    private final PostHandler mainThreadHandler;
    private static final String SUBSCRIBER_METHOD = "onEvent";
    private final ConcurrentHashMap<EventType, CopyOnWriteArrayList<Subscriber>> mSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<EventType, Object> mStickyEvents = new ConcurrentHashMap<>();

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
                LogUtils.e("Can not find value type,register fail!");
                return;
            }
            synchronized (mSubscriptions) {
                CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.get(type);
                if (subscribers == null) {
                    subscribers = new CopyOnWriteArrayList<>();
                    mSubscriptions.put(type, subscribers);
                }
                subscribers.add(subscriber);
            }
            //Post sticky event.
            Object stickyEvent = null;
            synchronized (mStickyEvents) {
                stickyEvent = mStickyEvents.get(type);
            }
            if (stickyEvent != null) {
                postSingleEvent(subscriber, stickyEvent);
            }
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
            mSubscriptions.remove(eventType);
        }
    }

    private void unregisterSubscriberByType(EventType type, Subscriber subscriber) {
        synchronized (mSubscriptions) {
            CopyOnWriteArrayList<Subscriber> subscribers = mSubscriptions.get(type);
            if (subscribers != null && subscribers.size() > 0) {
                subscribers.remove(subscriber);
            }
        }
    }

    public boolean isRegistered(Subscriber subscriber) {
        if (subscriber != null) {
            synchronized (mSubscriptions) {
                return mSubscriptions.containsKey(subscriber.eventType);
            }
        }
        return false;
    }

    public boolean isRegistered(int eventType) {
        synchronized (mSubscriptions) {
            final EventType type = new EventType(eventType);
            return mSubscriptions.containsKey(type);
        }
    }

    private void checkEvent(Subscriber subscriber, Object event) {
        Class subscriberType = subscriber.eventType.spareType;
        Class postEventType = event.getClass();
        if (!subscriberType.equals(postEventType)) {
            throw new EventException("Subscriber's onEvent method's parameter is " + subscriberType.getCanonicalName() +
                    ",but value is " + postEventType.getCanonicalName() + ",please keep they consistent.");
        }
    }

    private void postSingleEvent(Subscriber subscriber, Object event) {
        checkEvent(subscriber, event);
        PendingPost pendingPost = new PendingPost(subscriber, event);
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        int pid = android.os.Process.myPid();
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
        Object event = pendingPost.event;
        Subscriber subscriber = pendingPost.subscriber;
        subscriber.onEvent(event);
    }

    public void post(int type, Object event) {
        CopyOnWriteArrayList<Subscriber> subscribers = null;
        if (type != mInvalidType) {
            subscribers = mSubscriptions.get(new EventType(type));
        } else {
            Class<?> eventType = event.getClass();
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

    public void postStick(int type, Object event) {
        synchronized (mStickyEvents) {
            EventType eventType = null;
            if (type != mInvalidType) {
                eventType = new EventType(type);
            } else {
                eventType = new EventType(event.getClass());
            }
            mStickyEvents.put(eventType, event);
            post(type, event);
        }
    }

    public boolean removeStickyEvent(int type) {
        if (type != mInvalidType) {
            EventType eventType = new EventType(type);
            synchronized (mStickyEvents) {
                return mStickyEvents.remove(eventType) != null;
            }
        }
        return false;
    }

    public boolean removeStickyEvent(Object stickyEvent) {
        if (stickyEvent != null) {
            synchronized (mStickyEvents) {
                Collection<Object> values = mStickyEvents.values();
                for (Object object : values) {
                    if (object.equals(stickyEvent)) {
                        values.remove(stickyEvent);
                        return true;
                    }
                }
            }
        }
        return false;
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
                    subscriber.eventType = eventType;
                }
            } else {
                throw new EventException("class " + subscriberClass.getName() +
                        "must have exactly 1 parameter but has " + params.length);
            }
        } catch (NoSuchMethodException e) {
            throw new EventException(e);
        }
        eventType.spareType = (Class) params[0];
        return eventType;
    }

    public void clear() {
        synchronized (mSubscriptions) {
            mSubscriptions.clear();
        }
        synchronized (mStickyEvents) {
            mStickyEvents.clear();
        }
    }
}
