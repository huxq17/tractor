package com.andbase.demo;

import android.os.Bundle;

import com.andbase.demo.base.base.BaseActivity;
import com.andbase.tractor.listener.impl.LoadListenerImpl;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.task.ThreadPool;
import com.andbase.tractor.utils.LogUtils;

/**
 * Created by Administrator on 2015/11/16.
 */
public class MainActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        HttpUtils.get("http://www.baidu.com", "", new LoadListenerImpl() {
            @Override
            public void onSuccess(Object result) {
                super.onSuccess(result);
                String response = (String) result;
                LogUtils.i("http="+response);
            }

            @Override
            public void onFail(Object result) {
                super.onFail(result);
                Exception exception = (Exception) result;
                exception.printStackTrace();
            }
        },this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TaskPool.getInstance().cancelTask(this);
    }
}
