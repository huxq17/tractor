package com.andbase.demo;

import android.text.TextUtils;

import com.andbase.tractor.http.CallWrap;
import com.andbase.tractor.http.HttpBase;
import com.andbase.tractor.http.OKHttp;
import com.andbase.tractor.listener.LoadListener;

import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2015/11/16.
 */
public class HttpUtils {
    static HttpBase mHttpBase = new OKHttp();


    public static CallWrap post(final String url,
                                LinkedHashMap<String, String> header, final String params,
                                final LoadListener listener, Object... tag) {
        return mHttpBase.post(url, header, params, listener, tag);
    }

    public static CallWrap post(final String url,
                                final String params, final LoadListener listener,
                                Object... tag) {

        return mHttpBase.post(url, params, listener, tag);
    }

    public static CallWrap get(String url, final String params,
                               final LoadListener listener, Object... tag) {
        if(!TextUtils.isEmpty(params)){
            url = url +"?"+params;
        }
        return mHttpBase.get(url, listener, tag);
    }
}
