package com.andbase.tractor.http;

import com.squareup.okhttp.Call;
/**
 * 用来取消单次请求的类
 * @author huxq17
 *
 */
public class CallWrap {
	private Call mCall;

	public void setCall(Call call) {
		mCall = call;
	}

	public void cancel() {
		if (mCall != null) {
			mCall.cancel();
		}
	}
}
