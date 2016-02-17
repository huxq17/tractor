package com.andbase.tractor.task.threadpool;

import com.andbase.tractor.task.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * FixedThreadPool
 * 
 * @see Executors#newFixedThreadPool
 * @author xiaoqian.hu
 * 
 */
public class FixedThreadPool implements ThreadPool {
	private ExecutorService mFixedThreadPool;

	public FixedThreadPool() {
		init(DEFALUT_THREAD_NUM, null);
	}

	public FixedThreadPool(ThreadFactory threadFactory) {
		init(DEFALUT_THREAD_NUM, threadFactory);
	}

	public FixedThreadPool(int num) {
		init(num, null);
	}

	public FixedThreadPool(int num, ThreadFactory threadFactory) {
		init(num, threadFactory);
	}

	private void init(int num, ThreadFactory threadFactory) {
		num = num <= 0 ? DEFALUT_THREAD_NUM : num;
		if (threadFactory != null) {
			mFixedThreadPool = Executors.newFixedThreadPool(num, threadFactory);
		} else {
			mFixedThreadPool = Executors.newFixedThreadPool(num);
		}
	}
	
	public void setThreadPool(ExecutorService threadPool){
		mFixedThreadPool = threadPool;
	}

	@Override
	public void execute(Runnable command) {
		mFixedThreadPool.execute(command);
	}

}
