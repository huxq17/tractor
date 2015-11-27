package com.andbase.demo.http;

import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.tractor.listener.LoadListener;

/**
 * http请求的基类
 */
public interface HttpBase {
    /**
     * get请求
     *
     * @param request  http请求
     * @param listener 监听
     * @param tag
     */
    public void get(HttpRequest request, LoadListener listener, Object tag);

    /**
     * post请求
     *
     * @param request  http请求
     * @param listener 监听
     * @param tag
     */
    public void post(HttpRequest request, LoadListener listener, Object tag);

    /**
     * post请求
     *
     * @param method  请求方法
     * @param request http请求
     * @param listener 监听
     * @param tag
     */
    public void request(HttpMethod method, HttpRequest request, LoadListener listener, Object tag);
}