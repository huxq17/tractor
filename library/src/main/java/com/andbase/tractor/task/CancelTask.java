package com.andbase.tractor.task;

import com.andbase.tractor.listener.LoadListener;

public class CancelTask extends Task {
    private Task mTask;


    public CancelTask(Task task,LoadListener listener) {
        super(null, listener);
        mTask = task;
    }

    @Override
    public void onRun() {
        mTask.cancelTask();
    }

    @Override
    public void cancelTask() {
    }

    @Override
    public String toString() {
        return "CancelTask";
    }
}
