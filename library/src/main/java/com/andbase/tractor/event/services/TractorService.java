package com.andbase.tractor.event.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.andbase.tractor.EventDeliver;

/**
 * Created by huxq17 on 2016/12/20.
 */

public class TractorService extends Service {
    private IBinder binder = new EventDeliver.Stub() {
        @Override
        public void post(String event) throws RemoteException {
//            Object eventclass = Util.decode(event,  Router.Listener.class);
//            LogUtils.e("post service class=" + eventclass);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
