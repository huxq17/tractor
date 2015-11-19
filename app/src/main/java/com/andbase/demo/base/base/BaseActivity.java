package com.andbase.demo.base.base;

import android.app.Activity;
import android.content.Context;

import com.andbase.tractor.task.TaskPool;

public class BaseActivity extends Activity {

	private Base mBase;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
		mBase = new Base(this);
	}

	/**
	 * 格式化字符串
	 * 
	 * @param format
	 * @param args
	 */
	public String format(String format, Object... args) {
		if (mBase != null) {
			return mBase.format(format, args);
		} else {
			return null;
		}
	}

	public void setText(Object obj, String str) {
		if (mBase != null) {
			mBase.setText(obj, str);
		}
	}

	/**
	 * 获取edittext，textView,checkbox和button的文字
	 * 
	 * @param obj
	 * @return
	 */
	public String getText(Object obj) {
		if (mBase != null) {
			return mBase.getText(obj);
		} else {
			return "";
		}
	}

	public boolean isEmpty(Object obj) {
		if (mBase != null) {
			return mBase.isEmpty(obj);
		} else {
			return true;
		}
	}

	public boolean isEmpty(String str) {
		return mBase != null ? mBase.isEmpty(str) : true;
	}

	public void toast(String msg) {
		if (mBase != null) {
			mBase.toast(msg);
		}
	}

	public void toastAll(String msg) {
		if (mBase != null) {
			toastAll(msg);
		}
	}

	public void toastL(String msg) {
		if (mBase != null) {
			mBase.toastL(msg);
		}
	}

	public void toastAllL(String msg) {
		if (mBase != null) {
			mBase.toastAllL(msg);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//当activity或者fragment销毁了，最好取消这个页面上的请求，防止一些异常
		TaskPool.getInstance().cancelTask(this);
	}
}
