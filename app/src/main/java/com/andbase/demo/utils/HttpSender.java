package com.andbase.demo.utils;


import android.content.Context;
import android.text.TextUtils;

import com.andbase.demo.bean.DownloadInfo;
import com.andbase.demo.http.HttpBase;
import com.andbase.demo.http.OKHttp;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.demo.http.response.ResponseType;
import com.andbase.demo.task.DownLoadTask;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.TaskPool;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Created by huxq17 on 2015/11/16.
 */
public class HttpSender {
    static HttpBase mHttpBase = new OKHttp();

    private static class InstanceHolder {
        private static HttpSender instance = new HttpSender();
    }

    public static HttpSender instance() {
        return InstanceHolder.instance;
    }


    public void post(final String url,
                     LinkedHashMap<String, String> headers, final String params,
                     final LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setStringParams(params);
        //如无特殊需求，这步可以省去，但如果传的参数是json则应该设置contentType为application/json
        builder.contentType("application/x-www-form-urlencoded").charSet("utf-8");
        //网络请求返回的默认类型就是string,如果是String类型就不用设置，如果下载文件和加载图片需要用到InputStream，则设置为ResponseType.InputStream
        //        builder.setResponseType(ResponseType.InputStream);
        HttpRequest request = builder.build();
        mHttpBase.post(request, listener, tag);
    }

    public void post(final String url,
                     LinkedHashMap<String, String> headers, LinkedHashMap<String, Object> params,
                     LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setParams(params);
        HttpRequest request = builder.build();
        mHttpBase.post(request, listener, tag);
    }

    public void get(String url, LinkedHashMap<String, String> headers, String params,
                    final LoadListener listener, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setResponseType(ResponseType.String);
        mHttpBase.get(builder.build(), listener, tag);
    }

    public HttpResponse getInputStreamSync(String url, LinkedHashMap<String, String> headers, String params, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).synchron().setResponseType(ResponseType.InputStream);
        addHeaders(builder, headers);
        return mHttpBase.get(builder.build(), null, tag);
    }

    public void header(String url, LoadListener listener, Object... tag) {
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
    public HttpResponse headerSync(String url, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).synchron().method(HttpMethod.HEAD);
        HttpResponse response = mHttpBase.request(builder.build(), null, tag);
        return response;
    }

    public HttpResponse getSync(String url, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        //可以设置不返回body，适用于只获取文件大小的情况
        builder.url(url).synchron().noBody();
        HttpResponse response = mHttpBase.get(builder.build(), null, tag);
        return response;
    }

    public void upload(String url, LinkedHashMap<String, String> headers, LinkedHashMap<String, File> files, LinkedHashMap<String, Object> params, LoadListener listener, Object tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).setParams(params);
        addHeaders(builder, headers);
        for (LinkedHashMap.Entry<String, File> set : files.entrySet()) {
            builder.addFile(set.getKey(), set.getValue());
        }
        mHttpBase.post(builder.build(), listener, tag);
    }

    /**
     * 下载文件，支持多线程下载
     *
     * @param info     下载信息，url,文件保存路径等
     * @param listener
     * @param tag
     */
    public void download(final DownloadInfo info, final Context context, final LoadListener listener, final Object tag) {
        TaskPool.getInstance().execute(new DownLoadTask(info, context, listener, tag));
    }

    private void addHeaders(HttpRequest.Builder builder, LinkedHashMap<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            builder.setHeader(headers);
        }
    }
}