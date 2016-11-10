package com.andbase.tractor.event;

import android.os.Looper;

import com.andbase.tractor.utils.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    private volatile CopyOnWriteArrayList<Subscriber> mSubscribers = new CopyOnWriteArrayList<>();

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
                LogUtils.e("can not find event type,register fail!");
                return;
            }
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

    public synchronized boolean isRegistered(int eventType) {
        final EventType type = new EventType(eventType);
        return mSubscriptions.containsKey(type);
    }

    public void post(int type, Object event) {
        CopyOnWriteArrayList<Subscriber> subscribers = null;
        if (type != mInvalidType) {
            subscribers = mSubscriptions.get(type);
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

    private void postSingleEvent(Subscriber subscriber, Object event) {
        PendingPost pendingPost = new PendingPost(subscriber, event);
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (subscriber.mainThread) {
            if (!isMainThread) {
                mainThreadHandler.postToMainThread(pendingPost);
            } else {
                deliverSubsriber(pendingPost);
            }
        } else {
            deliverSubsriber(pendingPost);
        }
    }

    public void deliverSubsriber(PendingPost pendingPost) {
        Object event = pendingPost.event;
        Subscriber subscriber = pendingPost.subscriber;
        subscriber.onEvent(event);
    }

    public void post(Object event) {
        post(mInvalidType, event);
    }

    private EventType findEventType(Subscriber subscriber) {
        EventType eventType = subscriber.eventType;
        if (eventType == null) {

            Class<?> subscriberClass = subscriber.getClass();
            try {
                Method subscriberMethod = subscriberClass.getMethod(SUBSCRIBER_METHOD, Object.class);

                Subscribe subscribeAnnotation = subscriberMethod.getAnnotation(Subscribe.class);
                if (subscribeAnnotation != null) {
                    subscriber.mainThread = subscribeAnnotation.mainThread();
                    subscriber.sticky = subscribeAnnotation.sticky();
                }

                Type genType = subscriberClass.getGenericSuperclass();
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

//                String methodName = subscriberMethod.getName();
                if (params.length == 1) {
                    Class clazz = (Class) params[0];
                    eventType = new EventType(clazz);
                } else {
                    throw new EventException("class " + subscriberClass.getName() +
                            "must have exactly 1 parameter but has " + params.length);
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
