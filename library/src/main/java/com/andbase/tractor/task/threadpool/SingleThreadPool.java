package com.andbase.tractor.task.threadpool;

import com.andbase.tractor.task.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * 只有一个线程的线程池
 * 
 * @see Executors#newSingleThreadExecutor
 * @author xiaoqian.hu
 * 
 */
public class SingleThreadPool implements ThreadPool {
	private ExecutorService mSingleThreadPool;

	public SingleThreadPool() {
		init(null);
	}

	public SingleThreadPool(ThreadFactory threadFactory) {
		init(threadFactory);
	}

	private void init(ThreadFactory threadFactory) {
		if (threadFactory != null) {
			mSingleThreadPool = Executors
					.newSingleThreadExecutor(threadFactory);
		} else {
			mSingleThreadPool = Executors.newSingleThreadExecutor();
		}
	}

	public void setThreadPool(ExecutorService threadPool) {
		mSingleThreadPool = threadPool;
	}

	@Override
	public void execute(Runnable command) {
		mSingleThreadPool.execute(command);
	}

}
