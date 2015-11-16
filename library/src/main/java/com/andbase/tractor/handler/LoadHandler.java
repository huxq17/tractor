package com.andbase.tractor.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.andbase.tractor.Constants.Constants;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.utils.LogUtils;

public class LoadHandler extends Handler {
	private LoadListener listener;
	private int mStatus = Constants.LOAD_START;

	public LoadHandler(Looper looper) {
		super(looper);
	}

	public LoadHandler(LoadListener listener) {
		this.listener = listener;
	}

	public LoadHandler(Looper looper, LoadListener listener) {
		super(looper);
		this.listener = listener;
	}

	public LoadHandler() {
	}

	public void clear() {
		mStatus = Constants.LOAD_START;
	}

	public int getStatus() {
		return mStatus;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		Bundle bundle = msg.getData();
		if (mStatus == Constants.LOAD_FAIL || mStatus == Constants.LOAD_SUCCESS
				|| mStatus == Constants.LOAD_CANCEL|| mStatus == Constants.LOAD_TIMEOUT) {
			LogUtils.d("mstatus=" + mStatus);
			return;
		}
		mStatus = msg.what;
		switch (msg.what) {
		case Constants.LOAD_START:// 开始加载
			if (listener != null) {
				listener.onStart();
			}
			break;
		case Constants.LOAD_ING:// 开始加载
			if (listener != null) {
				Object result = null;
				if (bundle != null) {
					result = bundle.get("result");
				}
				listener.onLoading(result);
			}
			break;
		case Constants.LOAD_SUCCESS:// 加载成功
			if (listener != null) {
				Object result = null;
				if (bundle != null) {
					result = bundle.get("result");
				}
				listener.onSuccess(result);
			}
			break;
		case Constants.LOAD_FAIL:// 加载失败
			if (listener != null) {
				Object result = null;
				if (bundle != null) {
					result = bundle.get("result");
				}
				listener.onFail(result);
			}
		case Constants.LOAD_CANCEL:// 加载取消
			if (listener != null) {
				Object result = null;
				if (bundle != null) {
					result = bundle.get("result");
				}
				listener.onCancel(result);
			}
			break;
		case Constants.LOAD_TIMEOUT:// 加载超时
			if (listener != null) {
				Object result = null;
				if (bundle != null) {
					result = bundle.get("result");
				}
				listener.onTimeout(result);
			}
			break;

		default:
			break;
		}
	}
}
