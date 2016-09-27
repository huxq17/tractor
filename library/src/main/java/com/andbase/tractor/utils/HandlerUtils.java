package com.andbase.tractor.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;

public class HandlerUtils {
	public static void sendMsg(Handler handler, int what, Object... list) {
		if (handler == null) {
			return;
		}
		synchronized (handler) {
			if (list != null && list.length > 0 && list[0] != null) {
				Message msg = Message.obtain();
				Bundle bundle = new Bundle();
				bundle.putSerializable("result", (Serializable) list[0]);
				msg.setData(bundle);
				msg.what = what;
				handler.sendMessage(msg);
			} else {
				handler.sendEmptyMessage(what);
			}
		}
	}
}
