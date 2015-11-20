package com.andbase.demo.http.http;

import android.annotation.TargetApi;
import android.os.Build;

import com.andbase.tractor.handler.LoadHandler;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Created by xiaoqian.hu on 2015/10/15.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OKHttp implements HttpBase {

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    static {
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.networkInterceptors().add(new RedirectInterceptor());
        int versionCode = Build.VERSION.SDK_INT;
        if (versionCode >= 9) {
            mOkHttpClient.setCookieHandler(new CookieManager(null,
                    CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        }
    }

    static class RedirectInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Response response = chain.proceed(originalRequest);
            return response;
        }
    }

    @Override
    public CallWrap get(String url, LoadListener listener, Object... tag) {
        Request.Builder builder = getBuilder().url(url);
        addTag(builder, tag);
        return execute(builder.build(), listener, getTag(tag));
    }

    public CallWrap get(String url, LinkedHashMap<String, String> header,
                          LoadListener listener, Object... tag) {
        Request.Builder builder = getBuilder().url(url);
        if (header != null) {
            for (LinkedHashMap.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        addTag(builder, tag);
        return execute(builder.build(), listener, getTag(tag));
    }

    @Override
    public CallWrap post(String url, String params, LoadListener listener,
                         Object... tag) {
        if (params == null) {
            throw new RuntimeException("params is null");
        }
        Request.Builder builder = getBuilder().url(url).post(
                RequestBody.create(com.andbase.demo.http.http.MediaTypeWrap.MEDIA_TYPE_MARKDOWN, params));
        addTag(builder, tag);
        return execute(builder.build(), listener, getTag(tag));
    }

    @Override
    public CallWrap post(String url, LinkedHashMap<String, String> header,
                         String params, LoadListener listener, Object... tag) {
        Request.Builder builder = getBuilder().url(url).post(
                RequestBody.create(MediaTypeWrap.MEDIA_TYPE_MARKDOWN, params));
        if (header != null) {
            for (LinkedHashMap.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        addTag(builder, tag);
        return execute(builder.build(), listener, getTag(tag));
    }

    @Override
    public CallWrap post(String url, LinkedHashMap<String, String> params,
                         LoadListener listener, Object... tag) {
        RequestBody formBody = addParams(params);
        if (formBody == null) {
            throw new RuntimeException("params is null");
        } else {
            Request.Builder builder = getBuilder().url(url).post(formBody);
            addTag(builder, tag);
            return execute(builder.build(), listener, getTag(tag));
        }
    }

    public CallWrap header(String url, LoadListener listener, Object... tag) {
        Request.Builder builder = getBuilder().url(url).head();
        addTag(builder, tag);
        return executeHeader(builder.build(), listener, getTag(tag));
    }

    public RequestBody addParams(LinkedHashMap<String, String> params) {
        FormEncodingBuilder formbuiBuilder = new FormEncodingBuilder();
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                formbuiBuilder.add(key, params.get(key));
            }
            return formbuiBuilder.build();
        }
        return null;
    }

    public void addTag(Request.Builder builder, Object... tag) {
        if (tag != null && tag.length == 1) {
            builder.tag(tag[0]);
        }
    }

    private Object getTag(Object... tag) {
        if (tag != null && tag.length == 1) {
            return tag[0];
        }
        return null;
    }

    @Override
    public void cancel(Object... tag) {
        if (tag != null && tag.length == 1) {
            for (int i = 0; i < tag.length; i++) {
                mOkHttpClient.cancel(tag[i]);
            }
        }
    }

    private Request.Builder getBuilder() {
        return new Request.Builder();
    }

    private CallWrap execute(final Request request,
                             final LoadListener listener, Object tag) {
        final LoadHandler handler = new LoadHandler(listener);
        CallWrap callWrap = new CallWrap();
        final Call call = mOkHttpClient.newCall(request);
        callWrap.setCall(call);
        Task netWorkTask = new Task(tag, handler) {
            @Override
            public void onRun() {
                try {
                    Response response = call.execute();
                    long length = response.body().contentLength();
                    String result = response.body().string();
                    LogUtils.d("okresult=" + result + ";length=" + length);
                    notifySuccess(result);
                } catch (Exception e) {
                    if (e.toString().toLowerCase().contains("canceled")
                            || e.toString().toLowerCase().contains("closed")) {
                        notifyCancel(e);
                    } else {
                        notifyFail(e);
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void cancelTask() {
                call.cancel();
            }
        };
        TaskPool.getInstance().execute(netWorkTask);
        return callWrap;
    }

    private CallWrap executeHeader(final Request request,
                             final LoadListener listener, Object tag) {
        final LoadHandler handler = new LoadHandler(listener);
        CallWrap callWrap = new CallWrap();
        final Call call = mOkHttpClient.newCall(request);
        callWrap.setCall(call);
        Task netWorkTask = new Task(tag, handler) {
            @Override
            public void onRun() {
                try {
                    Response response = call.execute();
                    long length = response.body().contentLength();
//                    String result = response.body().string();
                    LogUtils.d("length=" + length);
                    notifySuccess(length);
                } catch (Exception e) {
                    if (e.toString().toLowerCase().contains("canceled")
                            || e.toString().toLowerCase().contains("closed")) {
                        notifyCancel(e);
                    } else {
                        notifyFail(e);
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void cancelTask() {
                call.cancel();
            }
        };
        TaskPool.getInstance().execute(netWorkTask);
        return callWrap;
    }

    public CallWrap download(String url,final String filepath,LinkedHashMap<String,String> header, final long startposition,final LoadListener listener, Object tag) {
        Request.Builder builder = getBuilder().url(url);
        if (header != null) {
            for (LinkedHashMap.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        addTag(builder, tag);
        final LoadHandler handler = new LoadHandler(listener);
        CallWrap callWrap = new CallWrap();
        final Call call = mOkHttpClient.newCall(builder.build());
        callWrap.setCall(call);
        TaskPool.getInstance().execute(new Task(tag, handler) {
            @Override
            public void onRun() {
                try {
                    Response response = call.execute();
                    InputStream inStream = response.body().byteStream();
                    File saveFile = new File(filepath);
                    RandomAccessFile accessFile = new RandomAccessFile(saveFile, "rwd");
                    accessFile.seek(startposition);// 设置从什么位置开始写入数据

                     byte[] buffer = new byte[1024];
                    int len = 0;
                    int total = 0;
                    while ((len = inStream.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, len);
                        total += len;
                        // 实时更新进度
                        notifyLoading(len);
                    }
                    Util.closeAll(inStream, accessFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void cancelTask() {

            }
        });
        return callWrap;
    }
}
