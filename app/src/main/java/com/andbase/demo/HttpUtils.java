package com.andbase.demo;

import android.text.TextUtils;

import com.andbase.demo.http.HttpBase;
import com.andbase.demo.http.OKHttp;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.tractor.listener.LoadListener;

import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2015/11/16.
 */
public class HttpUtils {
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

    public static void get(String url, LinkedHashMap<String, String> headers, final String params,
                           final LoadListener listener, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        mHttpBase.get(builder.build(), listener, tag);
    }

    public static void header(final String url, final String filePath, final int threadNum, final LoadListener listener, final Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).method(HttpMethod.HEAD);
        mHttpBase.request(builder.build(), listener, tag);
    }

    public static void download(final String url, final String filePath, LinkedHashMap<String, String> header, long startposition, long endposition, final LoadListener listener, final Object... tag) {
        get(url,header,null,listener,tag);
//        mHttpBase.download(url, filePath + Util.getFilename(url), header, startposition,listener , tag);
    }

    private static void addHeaders(HttpRequest.Builder builder, LinkedHashMap<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            builder.setHeader(headers);
        }
    }
}
