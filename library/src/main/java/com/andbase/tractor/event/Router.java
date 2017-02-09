package com.andbase.tractor.event;

import android.content.Context;

/**
 * Created by huxq17 on 2016/12/21.
 */

public class Router {
    private static Context mContext;
    private static boolean mIsMainProcess;
    private static Router mRouter;

    public Router() {
    }

    public void init(Context context) {
        mRouter = new Router();
        mContext = context.getApplicationContext();
//        mIsMainProcess = Util.isMainProcess(mContext);
//        if (!mIsMainProcess) {
//            Intent intent = new Intent(mContext, TractorService.class);
//            mContext.bindService(intent, aidlconnection, Context.BIND_AUTO_CREATE);
//        }
    }

    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Subscriber)}or {@link #unregister(int)} once they
     * are no longer interested in receiving events.
     *
     * @param subscriber
     */
    public void register(Subscriber subscriber) {
        EventTractor.instance().register(subscriber);
    }

    /**
     * unregister given subsriber
     *
     * @param subscriber
     */
    public void unregister(Subscriber subscriber) {
        EventTractor.instance().unregister(subscriber);
    }

    /**
     * unregister multiple subscribers with given type.
     *
     * @param eventType
     */
    public void unregister(int eventType) {
        EventTractor.instance().unregister(eventType);
    }

    /**
     * post the given value with given type and it will deliver to Subscribers that are interested in this.
     *
     * @param eventType
     * @param event
     */
    public void post(int eventType, Object event) {
        EventTractor.instance().post(eventType, event);
    }

    /**
     * post the given value and it will deliver to Subscribers that are interested in this.
     *
     * @param event
     */
    public void post(Object event) {
        EventTractor.instance().post(event);
    }

    public void postSticky(Object stickyEvent) {
        EventTractor.instance().postStick(stickyEvent);
    }

    public void postSticky(int type, Object stickyEvent) {
        EventTractor.instance().postStick(type, stickyEvent);
    }

    public boolean removeStickyEvent(int type) {
        return EventTractor.instance().removeStickyEvent(type);
    }

    public boolean removeStickyEvent(Object stickyEvent) {
        return EventTractor.instance().removeStickyEvent(stickyEvent);
    }

    public boolean isRegistered(Subscriber subscriber) {
        return EventTractor.instance().isRegistered(subscriber);
    }

    public boolean isRegistered(int eventType) {
        return EventTractor.instance().isRegistered(eventType);
    }

    public void clear() {
        EventTractor.instance().clear();
    }

    //    public class Listener {
//        int count;
//
//        public int getCount() {
//            return count;
//        }
//        public void onClick(){
//
//        }
//    }

//    EventDeliver subDeliver;
//    private ServiceConnection aidlconnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            subDeliver = EventDeliver.Stub.asInterface(service);
//            Listener bean = new Listener();
//            String result = Util.encode(bean);
//            LogUtils.e("onServiceConnected result=" + result);
//            try {
//                subDeliver.post(Util.encode(bean));
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };
}
