package com.andbase.demo.activity;

import android.os.Bundle;

import com.andbase.demo.R;
import com.andbase.demo.base.BaseActivity;
import com.andbase.tractor.Tractor;
import com.andbase.tractor.event.Subscribe;
import com.andbase.tractor.event.Subscriber;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;

/**
 * Created by huxq17 on 2016/3/30.
 */
public class EventActivity extends BaseActivity {
    private Subscriber subscriber1 = new Subscriber<String>() {
        @Override
        @Subscribe(mainThread = true, sticky = false)
        public void onEvent(String event) {
            toast("subscriber1 value = " + event);
            LogUtils.d("subscriber1 value = " + event);
        }
    };
    private Subscriber subscriber2 = new Subscriber<String>(1) {
        @Override
        @Subscribe(mainThread = true, sticky = false)
        public void onEvent(String event) {
            toast("subscriber2 value = " + event);
            LogUtils.d("subscriber2 value = " + event);
        }
    };

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        LogUtils.e("Tractor.removeStickyEvent(new Integer(1234)) result=" + Tractor.removeStickyEvent(new Integer(1234)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_event);
        initView();
        Tractor.register(subscriber1);
        Tractor.register(subscriber2);
        Tractor.register(new Subscriber<Integer>(1245) {
            @Override
            public void onEvent(Integer event) {
                toast("stickyevent event=" + event);
                LogUtils.e("stickyevent event=" + event);
            }
        });
        post();
    }

    private void post() {
        Tractor.post("来自主线程");
        Tractor.post(1, "来自主线程2");
        TaskPool.getInstance().execute(new Task() {
            @Override
            public void onRun() {
                //在非主线程发消息
                Tractor.post("来自非主线程");
                Tractor.post(1, "来自非主线程2");
            }

            @Override
            public void cancelTask() {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tractor.unregister(subscriber1);
        Tractor.unregister(1);
        post();
    }

    private void initView() {
    }
}
