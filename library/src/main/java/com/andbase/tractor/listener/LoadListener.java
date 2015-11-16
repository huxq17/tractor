package com.andbase.tractor.listener;


/**
 * 一个抽象的加载监听接口
 * @author xiaoqian.hu
 *
 */
public interface LoadListener {
	/**
	 * 任务加载开始
	 */
	void onStart();
	/**
	 * 任务加载中
	 * @param result
	 */
	void onLoading(Object result);
	/**
	 * 任务加载失败
	 * @param result
	 */
	void onFail(Object result);
	/**
	 * 任务加载成功
	 * @param result
	 */
	void onSuccess(Object result);
	/**
	 * 任务取消
	 * @param result
	 */
	void onCancel(Object result);
	/**
	 * 任务执行超时
	 * @param result
	 */
	void onTimeout(Object result);
	/**
	 * 等待框中取消按钮的点击事件
	 * @return
	 */
	void onCancelClick();
}