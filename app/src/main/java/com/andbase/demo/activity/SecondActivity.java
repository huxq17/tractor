package com.andbase.demo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.andbase.demo.R;
import com.andbase.demo.adapter.DownLoadListAdapter;
import com.andbase.demo.base.BaseActivity;
import com.andbase.demo.bean.GamesBean;
import com.andbase.tractor.Tractor;
import com.andbase.tractor.event.Subscribe;
import com.andbase.tractor.event.Subscriber;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andview.refreshview.XRefreshView;

import java.util.List;

/**
 * Created by huxq17 on 2016/3/30.
 */
public class SecondActivity extends BaseActivity {
    private com.andview.refreshview.XRefreshView xRefreshView;
    private RecyclerView mRecyclerView;
    private DownLoadListAdapter adapter;
    private List<GamesBean> list;
    private Subscriber subscriber1 = new Subscriber<String>() {
        @Override
        @Subscribe(mainThread = true, sticky = false)
        public void onEvent(String event) {
            toast("subscriber1 event = " + event);
            LogUtils.d("subscriber1 event = " + event);
        }
    };
    private Subscriber subscriber2 = new Subscriber<String>(1) {
        @Override
        @Subscribe(mainThread = true, sticky = false)
        public void onEvent(String event) {
            toast("subscriber2 event = " + event);
            LogUtils.d("subscriber2 event = " + event);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_second);
        initView();
        xRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh() {
                super.onRefresh();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xRefreshView.stopRefresh();
                    }
                }, 2000);
            }
        });
        Tractor.register(subscriber1);
        Tractor.register(subscriber2);
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
        xRefreshView = (XRefreshView) findViewById(R.id.xrefreshview);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        adapter = new DownLoadListAdapter(this, list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
    }
}
