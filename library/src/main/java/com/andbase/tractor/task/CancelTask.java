package com.andbase.tractor.task;

import com.andbase.tractor.handler.LoadHandler;

public class CancelTask extends Task {
    private Task mTask;


    public CancelTask(Task task,LoadHandler handler) {
        super(null, handler);
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
