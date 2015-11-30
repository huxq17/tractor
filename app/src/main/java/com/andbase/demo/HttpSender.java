package com.andbase.demo;

import android.text.TextUtils;

import com.andbase.demo.http.HttpBase;
import com.andbase.demo.http.OKHttp;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.listener.impl.LoadListenerImpl;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2015/11/16.
 */
public class HttpSender {
    static HttpBase mHttpBase = new OKHttp();


    public static void post(final String url,
                            LinkedHashMap<String, String> headers, final String params,
                            final LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setStringParams(params);
        //如无特殊需求，这步可以省去
        builder.contentType("application/x-www-form-urlencoded").charSet("utf-8");
        HttpRequest request = builder.build();
        mHttpBase.post(request, listener, tag);
    }

    public static void post(final String url,
                            LinkedHashMap<String, String> headers, LinkedHashMap<String, Object> params,
                            LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setParams(params);
        //如无特殊需求，这步可以省去
        builder.contentType("application/x-www-form-urlencoded").charSet("utf-8");
        HttpRequest request = builder.build();
        mHttpBase.post(request, listener, tag);
    }

    public static void get(String url, LinkedHashMap<String, String> headers, String params,
                           final LoadListener listener, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        mHttpBase.get(builder.build(), listener, tag);
    }

    public static HttpResponse getSync(String url, LinkedHashMap<String, String> headers, String params, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).synchron();
        addHeaders(builder, headers);
        return mHttpBase.get(builder.build(), null, tag);
    }

    public static void header(String url, LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).method(HttpMethod.HEAD);
        mHttpBase.request(builder.build(), listener, tag);
    }

    /**
     * 同步请求应当在非ui线程中调用
     *
     * @param url
     * @param tag
     */
    public static HttpResponse headerSync(String url, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).synchron().method(HttpMethod.HEAD);
        HttpResponse response = mHttpBase.request(builder.build(), null, tag);
        return response;
    }

    /**
     * 下载文件，支持多线程下载
     *
     * @param url       下载地址
     * @param fileDir   下载文件保存路径
     * @param fileName  文件名
     * @param threadNum 线程数
     * @param listener
     * @param tag
     */
    public static void download(final String url, long filelength, final String fileDir, final String fileName, final int threadNum, final LoadListener listener, final Object tag) {
        setFileLength(fileDir, fileName, filelength);
        long block = filelength % threadNum == 0 ? filelength / threadNum
                : filelength / threadNum + 1;
        String filepath = fileDir + fileName;
        listener.onStart();
        LogUtils.i("");
        for (int i = 0; i < threadNum; i++) {
            doDownload(url, i, filepath, block, listener, tag);
        }
    }

    public static void doDownload(final String url, final int i, final String filepath, final long block, final LoadListener listener, final Object tag) {
        TaskPool.getInstance().execute(new Task(tag, new LoadListenerImpl() {
            @Override
            public void onLoading(Object result) {
                super.onLoading(result);
                listener.onLoading(result);
            }
        }) {
            @Override
            public void onRun() {
                final long startposition = i * block;
                final long endposition = (i + 1) * block - 1;
                LogUtils.d("startposition=" + startposition + ";endposition=" + endposition);
                LinkedHashMap<String, String> header = new LinkedHashMap<>();
                header.put("RANGE", "bytes=" + startposition + "-"
                        + endposition);
                HttpResponse downloadResponse = getSync(url, header, null, null, tag);
                InputStream inStream = null;
                if (downloadResponse != null) {
                    inStream = downloadResponse.getInputStream();
                } else {
                    notifyFail("获取网络文件返回输入流失败");
                    return;
                }
                RandomAccessFile accessFile = null;
                try {
                    File saveFile = new File(filepath);
                    accessFile = new RandomAccessFile(saveFile, "rwd");
                    accessFile.seek(startposition);// 设置从什么位置开始写入数据

                    byte[] buffer = new byte[2048];
                    int len = 0;
                    int total = 0;
                    while ((len = inStream.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, len);
                        total += len;
                        // 实时更新进度
                        synchronized (HttpSender.class) {
                            total += len;
                            notifyLoading(len);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Util.closeQuietly(inStream);
                    Util.closeQuietly(accessFile);
                }
            }

            @Override
            public void cancelTask() {

            }
        });

    }

    public static void setFileLength(final String fileDir, final String fileName, long filelength) {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        RandomAccessFile accessFile = null;
        try {
            File downloadFile = new File(fileDir, fileName);
            accessFile = new RandomAccessFile(downloadFile, "rwd");
            accessFile.setLength(filelength);// 设置本地文件的长度和下载文件相同
            accessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(accessFile);
        }
    }

    private static void addHeaders(HttpRequest.Builder builder, LinkedHashMap<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            builder.setHeader(headers);
        }
    }
}
