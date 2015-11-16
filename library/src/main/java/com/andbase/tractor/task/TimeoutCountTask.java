package com.andbase.tractor.task;

import android.os.SystemClock;

import com.andbase.tractor.utils.LogUtils;

public abstract class TimeoutCountTask extends Task {
	private long mStopTimeInFuture;
	/**
	 * Millis since epoch when alarm should stop.
	 */
	private final long mMillisInFuture;

	/**
	 * boolean representing if the timer was cancelled
	 */
	private boolean mCancelled = false;
	private boolean mFinished = false;
	private Thread mCurrentThread;

	public TimeoutCountTask(long millisInFuture) {
		mMillisInFuture = millisInFuture;
	}

	@Override
	public void onRun() {
		mFinished = false;
		mCancelled = false;
		if (mMillisInFuture <= 0) {
			onFinish();
		}
		mCurrentThread = Thread.currentThread();
		mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
		try {
			Thread.sleep(mMillisInFuture);
		} catch (InterruptedException e) {
			LogUtils.d("main task is finished");
		}
		LogUtils.d("timeout = "+mMillisInFuture);
		mFinished = true;
		onFinish();
	}

	@Override
	public void cancelTask() {
		cancel();
	}

	/**
	 * Callback fired when the time is up.
	 */
	public abstract void onFinish();

	/**
	 * Cancel the countdown.
	 */
	public synchronized final void cancel() {
		mCancelled = true;
		if (mCurrentThread != null) {
			mCurrentThread.interrupt();
		}
	}

	public boolean hasFinished() {
		return mFinished;
	}

	@Override
	public String toString() {
		return "TimeoutCountTask";
	}
}
