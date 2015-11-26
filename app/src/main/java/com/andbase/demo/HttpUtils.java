package com.andbase.demo;

import android.text.TextUtils;

import com.andbase.demo.http.HttpBase;
import com.andbase.demo.http.OKHttp;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.listener.impl.LoadListenerImpl;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2015/11/16.
 */
public class HttpUtils {
    static HttpBase mHttpBase = new OKHttp();


    public static void post(final String url,
                                LinkedHashMap<String, String> header, final String params,
                                final LoadListener listener, Object... tag) {
         mHttpBase.post(url, header, params, listener, tag);
    }

    public static void post(final String url,
                                final String params, final LoadListener listener,
                                Object... tag) {

         mHttpBase.post(url, params, listener, tag);
    }

    public static void get(String url, final String params,
                               final LoadListener listener, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
//         mHttpBase.get(url, listener, tag);
    }
    static int completed = 0;
    public static void download(final String url, final String filePath, final int threadNum, final LoadListener listener, final Object... tag) {
        mHttpBase.header(url, new LoadListenerImpl() {
            @Override
            public void onSuccess(Object result) {
                super.onSuccess(result);
                final long filelength = (long) result;
                String sdcardPath = Util.getSdcardPath();
                if (TextUtils.isEmpty(sdcardPath)) {
                    listener.onFail("没有sd卡");
                    return;
                }
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                File saveFile = new File(filePath +  Util.getFilename(url));
               ;
                LogUtils.i("saveFile=" + filePath +  Util.getFilename(url));
                RandomAccessFile accessFile = null;
                try {
                    accessFile = new RandomAccessFile(saveFile, "rwd");
                    accessFile.setLength(filelength);// 设置本地文件的长度和下载文件相同
                    Util.closeQuietly(accessFile);
                    long block = filelength % threadNum == 0 ? filelength / threadNum
                            : filelength / threadNum + 1;
                    for (int i = 0; i < threadNum; i++) {
                        long startposition = i * block;
                        long endposition = (i + 1) * block - 1;
                        LinkedHashMap<String, String> header = new LinkedHashMap<>();
                        header.put("RANGE", "bytes=" + startposition + "-"
                                + endposition);
                        mHttpBase.download(url, filePath +  Util.getFilename(url), header, startposition, new LoadListenerImpl() {
                            @Override
                            public void onLoading(Object result) {
                                super.onLoading(result);
                                int process = (int) result;
                                completed +=process;
                                LogUtils.i("process = "+(1.0f*completed/filelength));
                            }
                        }, tag);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, tag);
    }
}
