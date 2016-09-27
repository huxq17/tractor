package com.andbase.tractor.task;

import android.text.TextUtils;

import com.andbase.tractor.Constants.Constants;
import com.andbase.tractor.handler.LoadHandler;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.utils.HandlerUtils;

import java.lang.ref.WeakReference;


/**
 * Created by xiaoqian.hu on 2015/11/4.
 */
public abstract class Task implements Runnable {
    private WeakReference<Object> mTag;
    private boolean mRunning = false;
    private volatile LoadHandler mHandler;
    private String mTaskName;
    /**
     * 任务的超时时间，值小于0，代表无限制 ，单位为毫秒
     */
    private long mTaskTimeout = -1;
    /**
     *
     */
    private TimeoutCountTask timeoutCountTask;

    public Task(Object tag, LoadListener listener) {
        init(null, 0, tag, listener);
    }

    public Task() {
        init(null, 0, null, null);
    }

    public Task(String taskName) {
        init(taskName, 0, null, null);
    }

    public Task(String taskName, Object tag, LoadListener listener) {
        init(taskName, 0, tag, listener);
    }

    public Task(long timeout) {
        init(null, timeout, null, null);
    }

    public Task(String taskName, long timeout) {
        init(taskName, timeout, null, null);
    }

    public Task(long timeout, Object tag, LoadListener listener) {
        init(null, timeout, tag, listener);
    }

    public Task(String taskName, long timeout, Object tag, LoadListener listener) {
        init(taskName, timeout, tag, listener);
    }

    private void init(String name, long timeout, Object tag, LoadListener listener) {
        setTaskName(name);
        setTaskTimeout(timeout);
        setTag(tag);
        setListener(listener);
    }

    @Override
    public final void run() {
        start();
        onRun();
        finish();
    }

    /**
     * 设置任务的超时时间，值小于0，代表无限制 ，单位为毫秒
     *
     * @param timeout
     */
    public void setTaskTimeout(long timeout) {
        mTaskTimeout = timeout;
    }

    /**
     * 实现这个方法来执行具体的任务
     */
    public abstract void onRun();

    /**
     * 实现这个方法来执行具体取消任务的操作，执行在非ui线程
     */
    public abstract void cancelTask();

    /**
     * 通知调用者任务开始
     */
    public final void notifyStart(Object result) {
        mStatus = Status.READY;
        if (mHandler != null) {
            HandlerUtils.sendMsg(mHandler, Constants.LOAD_START, result);
        }
    }

    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuffer name = new StringBuffer();
        name.append(TextUtils.isEmpty(mTaskName) ? getClass().getName() : mTaskName);
        if (mTag != null && mTag.get() != null) {
            name.append(";tag:" + mTag.get());
        }
        return name.toString();
    }

    /**
     * 通知调用者任务正在进行
     *
     * @param result 要传的参数
     */
    public final void notifyLoading(Object result) {
        if (mHandler != null) {
            HandlerUtils.sendMsg(mHandler, Constants.LOAD_ING, result);
        }
    }

    /**
     * 通知调用者任务执行成功
     *
     * @param result 要传的参数
     */
    public final void notifySuccess(Object result) {
        mStatus = Status.SUCCESS;
        if (mHandler != null) {
            HandlerUtils.sendMsg(mHandler, Constants.LOAD_SUCCESS, result);
        }
        clear();
    }

    /**
     * 通知调用者任务执行失败
     *
     * @param result 要传的参数
     */
    public final void notifyFail(Object result) {
        mStatus = Status.FAIL;
        if (mHandler != null) {
            HandlerUtils.sendMsg(mHandler, Constants.LOAD_FAIL, result);
        }
        clear();
    }

    /**
     * 通知调用者任务取消
     *
     * @param result 要传的参数
     */
    public final void notifyCancel(Object result) {
        mStatus = Status.CANCELED;
        if (mHandler != null) {
            HandlerUtils.sendMsg(mHandler, Constants.LOAD_CANCEL, result);
        }
        clear();
    }

    /**
     * 通知调用者任务超时
     *
     * @param result 要传的参数
     */
    public final void notifyTimeout(Object result) {
        mStatus = Status.TIMEOUT;
        if (mHandler != null) {
            HandlerUtils.sendMsg(mHandler, Constants.LOAD_TIMEOUT, result);
        }
        clear();
    }

    public Task setTimeout(long timeout) {
        mTaskTimeout = timeout;
        return this;
    }

    public Task setTaskName(String taskName) {
        mTaskName = taskName;
        return this;
    }

    public Task setTag(Object tag) {
        mTag = new WeakReference<Object>(tag);
        return this;
    }

    public void setListener(LoadListener listener) {
        if (listener != null) {
            mHandler = new LoadHandler(listener);
        }
    }

    public Object getTag() {
        if (mTag == null) {
            return null;
        } else {
            return mTag.get();
        }
    }

    public void cancel() {
//		cancelTask();
        notifyCancel(null);
        mHandler = null;
    }

    private void finish() {
        if (isRunning()) {
            // 默认加载成功
            mStatus = Status.SUCCESS;
            notifySuccess(null);
        } else {
            clear();
        }
    }

    private synchronized void clear() {
        if (isRunning()) {
            if (timeoutCountTask != null && !timeoutCountTask.hasFinished()) {
                timeoutCountTask.cancel();
            }
            if (mLifeCycleListener != null) {
                mLifeCycleListener.onFinish(this);
            }
            if (mTag != null) {
                mTag.clear();
                mTag = null;
            }
            if (mHandler != null) {
                mHandler = null;
            }
            mRunning = false;
        }
    }

    public boolean isRunning() {
        return mRunning;
    }

    private void start() {
        notifyStart(null);
        if (mTaskTimeout > 0) {
            startTimeCountTask(mTaskTimeout);
        }
        mRunning = true;
        mStatus = Status.READY;
        if (mLifeCycleListener != null) {
            mLifeCycleListener.onStart(this);
        }
    }

    /**
     * 开启计时器，用于计算超时
     *
     * @param timeout
     */
    public void startTimeCountTask(long timeout) {
        timeoutCountTask = new TimeoutCountTask(timeout) {

            @Override
            public void onFinish() {
                timeout();
            }
        };
        TaskPool.getInstance().execute(timeoutCountTask);
    }

    private void timeout() {
        if (isRunning()) {
            // 任务超时以后，先取消任务执行，再通知调用者任务超时
            cancelTask();
            notifyTimeout(null);
        }
    }

    private TaskLifeCycleListener mLifeCycleListener;

    public void setLiftCycleListener(TaskLifeCycleListener lifeCycleListener) {
        mLifeCycleListener = lifeCycleListener;
    }

    /**
     * 任务生命周期监听类
     */
    public interface TaskLifeCycleListener {
        /**
         * 任务开始运行
         */
        void onStart(Task task);

        /**
         * 任务运行结束
         */
        void onFinish(Task task);
    }

    private Status mStatus = Status.READY;

    public Status getStatus() {
        return mStatus;
    }

    public enum Status {
        READY, RUNNING, SUCCESS, FAIL, CANCELED, TIMEOUT;
    }
}
