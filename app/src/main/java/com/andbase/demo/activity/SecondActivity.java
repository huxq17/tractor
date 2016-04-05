package com.andbase.demo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.andbase.demo.R;
import com.andbase.demo.adapter.DownLoadListAdapter;
import com.andbase.demo.base.BaseActivity;
import com.andbase.demo.bean.GamesBean;
import com.andview.refreshview.XRefreshView;

import java.util.List;

/**
 * Created by huxq17 on 2016/3/30.
 */
public class SecondActivity extends BaseActivity {
    private XRefreshView xRefreshView;
    private RecyclerView mRecyclerView;
    private DownLoadListAdapter adapter;
    private List<GamesBean> list;

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
