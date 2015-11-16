package com.andbase.tractor.task.threadpool;

import com.andbase.tractor.task.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * CachedThreadPool
 * 
 * @see Executors#newCachedThreadPool
 * @author xiaoqian.hu
 * 
 */
public class CachedThreadPool implements ThreadPool {
	private ExecutorService mCachedThreadPool;

	public CachedThreadPool() {
		init(null);
	}
	public CachedThreadPool(ThreadFactory threadFactory){
		init(threadFactory);
	}

	private void init(ThreadFactory threadFactory) {
		if(threadFactory!=null){
			mCachedThreadPool = Executors.newCachedThreadPool(threadFactory);
		}else{
			mCachedThreadPool = Executors.newCachedThreadPool();
		}
	}
	
	public void setThreadPool(ExecutorService threadPool){
		mCachedThreadPool = threadPool;
	}

	@Override
	public void execute(Runnable command) {
		mCachedThreadPool.execute(command);
	}

}
