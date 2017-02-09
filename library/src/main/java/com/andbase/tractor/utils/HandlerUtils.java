package com.andbase.tractor.utils;

import android.os.Handler;
import android.os.Message;

public class HandlerUtils {
    public static void sendMsg(Handler handler, int what, Object... list) {
        if (handler == null) {
            return;
        }
        if (list != null && list.length > 0 && list[0] != null) {
            Message msg = Message.obtain();
            msg.obj = list[0];
            msg.what = what;
            handler.sendMessage(msg);
        } else {
            handler.sendEmptyMessage(what);
        }
    }
}
