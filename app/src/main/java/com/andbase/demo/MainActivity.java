package com.andbase.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;

import com.andbase.demo.base.BaseActivity;
import com.andbase.demo.bean.DownloadInfo;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.listener.impl.LoadListenerImpl;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.util.Random;

/**
 * Created by huxq17 on 2015/11/16.
 */
public class MainActivity extends BaseActivity {
//    private String domin = "http://192.168.2.199:8080/";
        private String domin = "http://192.168.2.103:8080/";
    private String downloadUrl = domin + "test/firetweet.apk";
    private String uploadUrl = domin + "UploadTest/Upload";
    private String sdcardPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        verifyStoragePermissions(this);
        sdcardPath = Util.getSdcardPath();

    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_task_normal:
                //当LoadListenerImpl构造函数传入context，则显示progressdialog
                doNormalTask(new LoadListenerImpl(this) {
                    @Override
                    public void onStart(Object result) {
                        super.onStart(result);
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
                    public void onStart(Object result) {
                        super.onStart(result);
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
                        HttpResponse response = (HttpResponse) result;
                        toast(response.string());
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
            case R.id.bt_task_upload:
                String dir = sdcardPath + "/tractor/down/";
                File file = new File(dir);
                if (!file.exists()) {
                    toast("上传路径不存在！");
                    return;
                }
                File[] files = file.listFiles();
                HttpSender.upload(uploadUrl, null, files, new LoadListenerImpl(this) {
                    @Override
                    public void onStart(Object result) {
                        super.onStart(result);
                        setMessage("准备上传中...");
                    }

                    @Override
                    public void onLoading(Object result) {
                        super.onLoading(result);
                        int process = (int) result;
                        setMessage("已上传 " + process + "%");
                        LogUtils.d("已上传 " + process + "%");
                    }

                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        HttpResponse response = (HttpResponse) result;
                        setMessage(response.string());
                    }

                    @Override
                    public void onFail(Object result) {
                        super.onFail(result);
                        setMessage("上传失败!");
                    }

                    @Override
                    public void onCancelClick() {
                        super.onCancelClick();
                        setMessage("正在取消上传...");
                        TaskPool.getInstance().cancelTask(MainActivity.this);
                    }

                    @Override
                    public void onCancel(Object result) {
                        super.onCancel(result);
                        setMessage("已取消上传");
                    }
                }, this);
                break;
            case R.id.bt_task_download:
                if (isEmpty(sdcardPath)) {
                    toast("没有sd卡");
                    return;
                }
                final String filedir = sdcardPath + "/tractor/down/";
                LogUtils.i("filedir =" + filedir);
                final String filename = Util.getFilename(downloadUrl);
                final int threadNum = 3;
                DownloadInfo info = new DownloadInfo(downloadUrl, filedir, filename, threadNum);
                HttpSender.download(info, new LoadListenerImpl(this) {
                    @Override
                    public void onStart(Object result) {
                        super.onStart(result);
                        setMessage("准备下载中...");
                    }

                    @Override
                    public void onLoading(Object result) {
                        super.onLoading(result);
                        int process = (int) result;
                        LogUtils.i("已下载 " + process + "%");
                        setMessage("已下载 " + process + "%");
                    }

                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        setMessage("下载成功");
                        if (filename.endsWith(".apk")) {
                            String filePath = filedir + filename;
                            Utils.install(MainActivity.this, filePath);
                        }
                    }

                    @Override
                    public void onFail(Object result) {
                        super.onFail(result);
                        String response = null;
                        if (result != null && result instanceof String) {
                            response = (String) result;
                        }
                        if (TextUtils.isEmpty(response)) {
                            response = "下载失败";
                        }
                        setMessage(response);
                    }

                    @Override
                    public void onCancelClick() {
                        super.onCancelClick();
                        setMessage("正在取消下载...");
                        TaskPool.getInstance().cancelTask(MainActivity.this);
                    }

                    @Override
                    public void onCancel(Object result) {
                        super.onCancel(result);
                        setMessage("已取消下载");
                    }
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
        TaskPool.getInstance().execute(new Task(tag, listener) {
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
     * @param timeout  设置任务的timeout
     * @param listener
     * @param tag
     */
    public void doTimeoutTask(long timeout, LoadListener listener, Object tag) {
        TaskPool.getInstance().execute(new Task(timeout, tag, listener) {
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
        HttpSender.get(url, null, params, listener, tag);
    }

    public void doNormalPost(String url, String params, LoadListener listener, Object tag) {
        HttpSender.post(url, null, params, listener, tag);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
