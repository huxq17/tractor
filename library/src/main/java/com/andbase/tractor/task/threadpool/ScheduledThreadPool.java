package com.andbase.tractor.task.threadpool;

import com.andbase.tractor.task.ThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledThreadPool
 * 
 * @see Executors#newScheduledThreadPool
 * @author xiaoqian.hu
 */
public class ScheduledThreadPool implements ThreadPool {
	private ScheduledExecutorService mScheduledExecutorService;
	private ExecuteType meExecuteType = ExecuteType.OneShot;

	public ScheduledThreadPool() {
		init(DEFALUT_THREAD_NUM, null);
	}

	public ScheduledThreadPool(int num) {
		init(num, null);
	}

	public ScheduledThreadPool(int num, ThreadFactory threadFactory) {
		init(num, threadFactory);
	}

	private void init(int num, ThreadFactory threadFactory) {
		num = num <= 0 ? DEFALUT_THREAD_NUM : num;
		if (threadFactory != null) {
			mScheduledExecutorService = Executors.newScheduledThreadPool(num,
					threadFactory);
		} else {
			mScheduledExecutorService = Executors.newScheduledThreadPool(num);
		}
	}

	public void setExecutorService(ScheduledExecutorService scheduledExecutorService) {
		mScheduledExecutorService = scheduledExecutorService;
	}

	/**
	 * 任务第一次执行的延迟，单位为毫秒
	 */
	private int initialDelay;
	/**
	 * 执行任务的周期，上一个任务开始执行到下一个任务开始执行的时间间隔，如果任务执行时间大于这个周期，则等待此任务结束，单位为毫秒
	 */
	private int period = 2000;
	/**
	 * 任务之间的延迟，上一个任务结束到下一个任务开始执行的时间间隔，单位为毫秒
	 */
	private int delay = 1000;

	@Override
	public void execute(Runnable command) {
		switch (meExecuteType) {
		case OneShot:
			executeOneShot(command, delay);
			break;
		case FixedDelay:
			executeFixedDelay(command, initialDelay, delay);
			break;
		case FixedRate:
			executeFixedRate(command, initialDelay, period);
			break;

		default:
			break;
		}
	}

	/**
	 * 以固定周期频率执行任务
	 * 
	 * @param command
	 *            命令
	 * @param initialDelay
	 *            任务第一次执行的延迟，单位为毫秒
	 * @param period
	 *            执行任务的周期，上一个任务开始执行到下一个任务开始执行的时间间隔，如果任务执行时间大于这个周期，则等待此任务结束，单位为毫秒
	 */
	private void executeFixedRate(Runnable command, int initialDelay, int period) {
		mScheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period,
				TimeUnit.MILLISECONDS);
	}

	/**
	 * 以固定延迟时间进行执行。 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
	 * 
	 * @param command
	 *            命令
	 * @param initialDelay
	 *            任务第一次执行的延迟，单位为毫秒
	 * @param delay
	 *            任务之间的延迟，上一个任务结束到下一个任务开始执行的时间间隔，单位为毫秒
	 */
	private void executeFixedDelay(Runnable command, int initialDelay, int delay) {
		mScheduledExecutorService.scheduleWithFixedDelay(command, initialDelay,
				delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 
	 * @param command
	 * @param delay
	 */
	private void executeOneShot(Runnable command, int delay) {
		mScheduledExecutorService.schedule(command, delay, TimeUnit.MILLISECONDS);
	}

	public enum ExecuteType {
		OneShot, FixedDelay, FixedRate
	}

	/**
	 * 设置任务第一次执行的延迟，单位为毫秒
	 * 
	 * @param initialDelay
	 */
	public void setInitialDelay(int initialDelay) {
		this.initialDelay = initialDelay;
	}

	/**
	 * 设置任务之间的延迟，上一个任务结束到下一个任务开始执行的时间间隔，单位为毫秒
	 * 
	 * @param delay
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * 设置执行任务的周期，上一个任务开始执行到下一个任务开始执行的时间间隔，如果任务执行时间大于这个周期，则等待此任务结束，单位为毫秒
	 * 
	 * @param peroid
	 */
	public void setPeriod(int peroid) {
		this.period = peroid;
	}

	public void setMeExecuteType(ExecuteType meExecuteType) {
		this.meExecuteType = meExecuteType;
	}
}