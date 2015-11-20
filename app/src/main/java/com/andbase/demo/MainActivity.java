package com.andbase.demo;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import com.andbase.demo.base.base.BaseActivity;
import com.andbase.tractor.handler.LoadHandler;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.listener.impl.LoadListenerImpl;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.Util;

import java.util.Random;

/**
 * Created by Administrator on 2015/11/16.
 */
public class MainActivity extends BaseActivity {
    private String downloadUrl = "https://github.com/huxq17/okhttp-utils/blob/master/gson-2.2.1.jar?raw=true";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_task_normal:
                //当LoadListenerImpl构造函数传入context，则显示progressdialog
                doNormalTask(new LoadListenerImpl(this) {
                    @Override
                    public void onStart() {
                        super.onStart();
                        setMessage("任务开始");
                    }

                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        String response = (String) result;
//                        toast(response);
                        setMessage("任务结束");
                    }

                    @Override
                    public void onFail(Object result) {
                        super.onFail(result);
                        String response = (String) result;
//                        toast(response);
                        setMessage(response);
                    }

                    @Override
                    public void onLoading(Object result) {
                        super.onLoading(result);
                        //以后不用写handler了，这样就可以处理了
                        int response = (int) result;
                        switch (response) {
                            case 1:
//                                toast("正在执行 response=" + response);
                                setMessage("正在执行 response=" + response);
                                break;
                            case 2:
//                                toast("正在执行 response=" + response);
                                setMessage("正在执行 response=" + response);
                                break;
                            case 3:
//                                toast("正在执行 response=" + response);
                                setMessage("正在执行 response=" + response);
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onCancel(Object result) {
                        super.onCancel(result);
                        toast("任务被取消了");
                    }

                    @Override
                    public void onCancelClick() {
                        super.onCancelClick();
                        TaskPool.getInstance().cancelTask(MainActivity.this);
                    }
                }, this);
                break;
            case R.id.bt_task_timeout:
                doTimeoutTask(500, new LoadListenerImpl() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        toast("超时任务开始执行");
                    }

                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        toast("超时任务执行成功");
                    }

                    @Override
                    public void onTimeout(Object result) {
                        super.onTimeout(result);
                        toast("任务超时");
                    }
                }, this);
                break;
            case R.id.bt_task_cancel:
                //当activity或者fragment销毁了，最好取消这个页面上的请求，防止一些异常
                toast("请求取消这个activity相关的所有任务");
                TaskPool.getInstance().cancelTask(this);
                break;
            case R.id.bt_task_get_normal:
                doNormalGet("https://raw.githubusercontent.com/huxq17/tractor/master/authorInfo.json", null, new LoadListenerImpl() {
                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        String response = (String) result;
                        toast(response);
                    }

                    @Override
                    public void onFail(Object result) {
                        super.onFail(result);
                    }

                    @Override
                    public void onCancel(Object result) {
                        super.onCancel(result);
                        toast("get请求被取消了");
                    }
                }, this);
                break;
            case R.id.bt_task_post_normal:
                break;
            case R.id.bt_task_download:
                String sdcardPath = Util.getSdcardPath();
                if (TextUtils.isEmpty(sdcardPath)) {
                    toast("没有sd卡");
                    return;
                }
                HttpUtils.download(downloadUrl, sdcardPath + "/tractor/download/" , 3, new LoadListenerImpl() {

                }, this);
                break;
        }
    }

    /**
     * 发起个普通的任务
     *
     * @param listener
     * @param tag
     */
    public void doNormalTask(LoadListener listener, Object tag) {
        //先拿到handler，如果不做这步，new Task()，Task构造函数就不需要传参数了
        LoadHandler handler = new LoadHandler(listener);
        TaskPool.getInstance().execute(new Task(tag, handler) {
            @Override
            public void onRun() {
                SystemClock.sleep(500);
                notifyLoading(1);
                SystemClock.sleep(500);
                notifyLoading(2);
                SystemClock.sleep(500);
                notifyLoading(3);
                SystemClock.sleep(500);
                Random random = new Random();
                //任务是模拟的，所以随机下
                if (random.nextBoolean()) {
                } else {
                    notifyFail("糟糕，任务失败了");
                }
            }

            @Override
            public void cancelTask() {

            }
        });
    }

    /**
     *
     * @param timeout 设置任务的timeout
     * @param listener
     * @param tag
     */
    public void doTimeoutTask(long timeout, LoadListener listener, Object tag) {
        LoadHandler handler = new LoadHandler(listener);
        TaskPool.getInstance().execute(new Task(timeout, tag, handler) {
            @Override
            public void onRun() {
                SystemClock.sleep(1000);
            }

            @Override
            public void cancelTask() {

            }
        });
    }

    public void doNormalGet(String url, String params, LoadListener listener, Object tag) {
        HttpUtils.get(url, params, listener, tag);
    }

    public void doNormalPost(String url, String params, LoadListener listener, Object tag) {
        HttpUtils.post(url, params, listener, tag);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
