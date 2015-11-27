package com.andbase.demo.http;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;

import com.andbase.demo.http.request.HttpHeader;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.demo.http.request.RequestParams;
import com.andbase.tractor.handler.LoadHandler;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
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
import java.util.HashMap;
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
    public void get(HttpRequest request, LoadListener listener, Object tag) {
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        String params = requestParams.toString();
        if (TextUtils.isEmpty(params)) {
            url += "?" + params;
        }
        LogUtils.d("get url=" + url);

        Request.Builder builder = getBuilder().url(url);
        addHeader(builder, header);
        addTag(builder, tag);
        execute(builder.build(), listener, getTag(tag));
    }

    @Override
    public void post(HttpRequest request, LoadListener listener, Object tag) {
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        String params = requestParams.toString();
        String contentType = requestParams.getContentType();
        String charset = requestParams.getCharSet();
        if(params==null|| TextUtils.isEmpty(contentType)||TextUtils.isEmpty(charset)){
            throw new RuntimeException("params is null");
        }
        Request.Builder builder = getBuilder().url(url).post(RequestBody.create(MediaType.parse(contentType + ";" + charset), params));
        addHeader(builder, header);
        addTag(builder, tag);
        execute(builder.build(), listener, getTag(tag));
    }

    @Override
    public void request(HttpMethod method, HttpRequest request, LoadListener listener, Object tag) {
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        String params = requestParams.toString();

    }

    private void addHeader(Request.Builder builder,HttpHeader header) {
        HashMap<String, String> headers = header.getHeaders();
        if (headers != null && headers.size() > 0) {
            for (HashMap.Entry<String, String> map :
                    headers.entrySet()) {
                builder.addHeader(map.getKey(), map.getValue());
            }
        }
    }


    public void header(String url, LoadListener listener, Object... tag) {
        Request.Builder builder = getBuilder().url(url).head();
        addTag(builder, tag);
        executeHeader(builder.build(), listener, getTag(tag));
    }

//    public RequestBody addParams(LinkedHashMap<String, String> params) {
//        FormEncodingBuilder formbuiBuilder = new FormEncodingBuilder();
//        if (params != null && params.size() > 0) {
//            for (String key : params.keySet()) {
//                formbuiBuilder.add(key, params.get(key));
//            }
//            return formbuiBuilder.build();
//        }
//        return null;
//    }

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

    private void execute(final Request request,
                         final LoadListener listener, Object tag) {
        final LoadHandler handler = new LoadHandler(listener);
        final Call call = mOkHttpClient.newCall(request);
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
    }

    private void executeHeader(final Request request,
                               final LoadListener listener, Object tag) {
        final LoadHandler handler = new LoadHandler(listener);
        final Call call = mOkHttpClient.newCall(request);
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
    }

    public void download(String url, final String filepath, LinkedHashMap<String, String> header, final long startposition, final LoadListener listener, Object tag) {
        Request.Builder builder = getBuilder().url(url);
        if (header != null) {
            for (LinkedHashMap.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        addTag(builder, tag);
        final LoadHandler handler = new LoadHandler(listener);
        final Call call = mOkHttpClient.newCall(builder.build());
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
    }
}
