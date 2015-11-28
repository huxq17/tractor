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
import com.andbase.demo.http.HttpResponse;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.listener.impl.LoadListenerImpl;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Created by Administrator on 2015/11/16.
 */
public class MainActivity extends BaseActivity {
    private String downloadUrl = "https://github.com/huxq17/okhttp-utils/blob/master/gson-2.2.1.jar?raw=true";
    int completed = 0;
    long filelength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        verifyStoragePermissions(this);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
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
            case R.id.bt_task_download:
                String sdcardPath = Util.getSdcardPath();
                if (TextUtils.isEmpty(sdcardPath)) {
                    toast("没有sd卡");
                    return;
                }
                final String filePath = sdcardPath;
                LogUtils.i("filePath ="+filePath);
                final int threadNum = 3;
                requestHeader(downloadUrl, filePath, threadNum, new LoadListenerImpl(this) {
                    @Override
                    public void onLoading(Object result) {
                        super.onLoading(result);
                        int process = (int) result;
                        completed += process;
                        int done = (int) (100 * (1.0f * completed / filelength));
                        setMessage("已下载 " + done + "%");
                    }
                }, this);
                break;
        }
    }

    private void requestHeader(final String url, final String filePath, final int threadNum, final LoadListener listener, final Object... tag) {
        HttpUtils.header(downloadUrl, filePath, threadNum, new LoadListenerImpl() {
            @Override
            public void onSuccess(Object result) {
                super.onSuccess(result);
                HttpResponse response = (HttpResponse) result;
                filelength = response.getContentLength();
                String sdcardPath = Util.getSdcardPath();
                if (TextUtils.isEmpty(sdcardPath)) {
                    listener.onFail("没有sd卡");
                    return;
                }
                File saveFile = new File(filePath,  Util.getFilename(url));
                LogUtils.i("saveFile=" + saveFile.getAbsolutePath());
                RandomAccessFile accessFile = null;
                try {
                    accessFile = new RandomAccessFile(saveFile, "rwd");
                    accessFile.setLength(filelength);// 设置本地文件的长度和下载文件相同
                    Util.closeQuietly(accessFile);
                    long block = filelength % threadNum == 0 ? filelength / threadNum
                            : filelength / threadNum + 1;
                    for (int i = 0; i < threadNum; i++) {
                        final long startposition = i * block;
                        final long endposition = (i + 1) * block - 1;
                        LinkedHashMap<String, String> header = new LinkedHashMap<>();
                        header.put("RANGE", "bytes=" + startposition + "-"
                                + endposition);
                        final String filepath = saveFile.getAbsolutePath();
                        HttpUtils.download(downloadUrl, header, new LoadListenerImpl() {
                            @Override
                            public void onSuccess(Object result) {
                                super.onSuccess(result);
                                final HttpResponse httpResponse = (HttpResponse) result;
                                TaskPool.getInstance().execute(new Task(MainActivity.this,new LoadListenerImpl(){
                                    @Override
                                    public void onLoading(Object result) {
                                        super.onLoading(result);
                                        listener.onLoading(result);
                                    }
                                }) {
                                    @Override
                                    public void onRun() {
                                        InputStream inStream = httpResponse.getInputStream();
                                        RandomAccessFile accessFile = null;
                                        try {
                                            LogUtils.i("onsucess="+filepath);
                                            File saveFile = new File(filepath);
                                            accessFile = new RandomAccessFile(saveFile, "rwd");
                                            accessFile.seek(startposition);// 设置从什么位置开始写入数据

                                            byte[] buffer = new byte[1024];
                                            int len = 0;
                                            int total = 0;
                                            while ((len = inStream.read(buffer)) != -1) {
                                                accessFile.write(buffer, 0, len);
                                                total += len;
                                                // 实时更新进度
                                                notifyLoading(len);
                                            }
                                        } catch (Exception e) {

                                        } finally {
                                            Util.closeQuietly(inStream);
                                            Util.closeQuietly(accessFile);
                                        }
                                    }

                                    @Override
                                    public void cancelTask() {

                                    }
                                });

                            }

                        }, this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, this);
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
        HttpUtils.get(url, null, params, listener, tag);
    }

    public void doNormalPost(String url, String params, LoadListener listener, Object tag) {
        HttpUtils.post(url, null, params, listener, tag);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
