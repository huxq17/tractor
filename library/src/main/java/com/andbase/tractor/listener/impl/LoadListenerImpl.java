package com.andbase.tractor.listener.impl;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;

import com.andbase.tractor.LittleDialog;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;

import java.lang.ref.WeakReference;

public class LoadListenerImpl implements LoadListener {
    private WeakReference<Context> context;
    private ProgressDialog mProgressDialog;
    private String mMessage = "加载中...";
    private long mDismissTime = 500;

    /**
     * 不显示progressdialog
     */
    public LoadListenerImpl() {
    }

    /**
     * 显示progressdialog，其上显示的文字是默认的
     *
     * @param context
     */
    public LoadListenerImpl(Context context) {
        init(context, null);
    }

    /**
     * 显示progressdialog,其上显示的文字是message
     *
     * @param context
     * @param message
     */
    public LoadListenerImpl(Context context, String message) {
        init(context, message);
    }

    /**
     * 设置自定义的progressdialog，如果不设置则使用tractor自带的
     *
     * @param progressDialog
     */
    public void setProgressDialog(ProgressDialog progressDialog) {
        mProgressDialog = progressDialog;
    }

    private void init(Context context, String message) {
        if (message != null) {
            mMessage = message;
        }
        if (context != null) {
            this.context = new WeakReference<>(context);
            initProgressDialog(mMessage);
        }
    }

    @Override
    public void onFail(Object result) {
        dimiss(mDismissTime);
    }

    @Override
    public void onStart(Object result) {
        show();
    }

    /**
     * @param time
     */
    public void setDismissTime(long time) {
        mDismissTime = time;
    }

    /**
     * @param dismissTime 在dialog消失前延迟的时间，不传则不延迟
     */
    public void dimiss(final long... dismissTime) {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            if (dismissTime != null && dismissTime.length > 0 && dismissTime[0] > 0) {
                LoadListenerImpl listener = new LoadListenerImpl() {
                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        doDimiss();
                    }
                };
                //做一个延迟关闭dialog的任务
                TaskPool.getInstance().execute(new Task("dimissTask", listener) {
                    @Override
                    public void onRun() {
                        SystemClock.sleep(dismissTime[0]);
                    }

                    @Override
                    public void cancelTask() {

                    }
                });
            } else {
                doDimiss();
            }
        }
    }

    private void doDimiss() {
        try {
            if (null != mProgressDialog && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show() {
        if (null != mProgressDialog && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }


    public void setMessage(String msg) {
        mMessage = msg;
        if (null != mProgressDialog) {
            mProgressDialog.setMessage(mMessage);
        }
    }

    private void initProgressDialog(String msg) {
        if (null == mProgressDialog && context != null && context.get() != null) {
            mProgressDialog = new LittleDialog(context.get(), msg, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelClick();
                }
            });
        }
    }

    @Override
    public void onSuccess(Object result) {
        dimiss(mDismissTime);
    }

    @Override
    public void onLoading(Object result) {

    }

    @Override
    public void onCancel(Object result) {
        dimiss(mDismissTime);
    }

    @Override
    public void onTimeout(Object result) {
        dimiss(mDismissTime);
    }

    @Override
    public void onCancelClick() {
    }

}
