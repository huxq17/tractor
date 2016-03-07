package com.andbase.tractor.task;

import com.andbase.tractor.task.threadpool.CachedThreadPool;
import com.andbase.tractor.utils.LogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TaskPool {
    private volatile CopyOnWriteArrayList<Task> mTaskQueue = new CopyOnWriteArrayList<Task>();
    private ThreadPool mThreadPool;
    private ExecutorService mInternalService;

    private TaskPool() {
        mInternalService = Executors.newCachedThreadPool();
    }

    public static TaskPool getInstance() {
        return TaskPoolHolder.instance;
    }

    /**
     * 配置执行任务的线程池
     *
     * @param threadPool
     */
    public void setExecutorService(ThreadPool threadPool) {
        initExecutorService(threadPool);
    }

    private static class TaskPoolHolder {
        private static TaskPool instance = new TaskPool();
    }

    private void initExecutorService(ThreadPool threadPool) {
        if (threadPool == null) {
            mThreadPool = new CachedThreadPool();
        } else {
            mThreadPool = threadPool;
        }
    }

    /**
     * 执行任务
     *
     * @param task 要执行的任务
     */
    public void execute(Task task) {
        if (task == null) {
            return;
        }
        initExecutorService(null);
        task.setLiftCycleListener(new Task.TaskLifeCycleListener() {
            @Override
            public void onStart(Task task) {
                mTaskQueue.add(task);
                LogUtils.d("task " + task + "开始运行;" + TaskPool.this.toString());
            }

            @Override
            public void onFinish(Task task) {
                mTaskQueue.remove(task);
                LogUtils.d("task " + task + "运行结束;" + TaskPool.this.toString());
            }
        });
        if (task instanceof TimeoutCountTask || task instanceof CancelTask) {
            mInternalService.execute(task);
        } else {
            mThreadPool.execute(task);
        }
    }

    public void shutdown(){
        mThreadPool.shutdown();
    }

    public List<Task> findTaskWithTag(Object tag) {
        if (tag == null) {
            return null;
        }
        LinkedList<Task> list = new LinkedList<Task>();
        for (Task task : mTaskQueue) {
            if (tag == task.getTag()) {
                list.add(task);
            }
        }
        return list;
    }

    /**
     * 取消任务
     *
     * @param tag 任务设置的tag
     */
    public void cancelTask(Object tag) {
        if (tag == null) {
            return;
        }
        List<Task> taskList = findTaskWithTag(tag);
        if (taskList == null) {
            return;
        }
        for (Task task : taskList) {
            cancelTask(task);
        }
    }

    /**
     * 取消任务
     *
     * @param task 要取消的任务
     */
    public void cancelTask(final Task task) {
        if (task == null) {
            return;
        }
        //先在ui上呈现给用户取消的效果
        task.cancel();
        //再异步取消任务
        execute(new CancelTask(task, null));
    }

    @Override
    public String toString() {
        return "现在正在运行的任务有" + mTaskQueue.size() + "个";
    }
}
