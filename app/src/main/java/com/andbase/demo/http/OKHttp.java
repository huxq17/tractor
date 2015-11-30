package com.andbase.demo.http;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;

import com.andbase.demo.http.request.HttpHeader;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.demo.http.request.RequestParams;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


/**
 * Created by xiaoqian.hu on 2015/10/15.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OKHttp implements HttpBase {

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    static {
        mOkHttpClient.setRetryOnConnectionFailure(true);
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
    public HttpResponse get(HttpRequest request, LoadListener listener, Object... tag) {
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();
        String params = requestParams.toString();
        if (!TextUtils.isEmpty(params)) {
            url += "?" + params;
        }
        LogUtils.d("get url=" + url);

        Request.Builder builder = getBuilder().url(url);
        addHeader(builder, header);
        addTag(builder, tag);
        return execute(builder.build(), synchron, listener, getTag(tag));
    }

    @Override
    public HttpResponse post(HttpRequest request, LoadListener listener, Object... tag) {
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();

        String params = requestParams.toString();
        String contentType = requestParams.getContentType();
        String charset = requestParams.getCharSet();
        if (params == null || TextUtils.isEmpty(contentType) || TextUtils.isEmpty(charset)) {
            throw new RuntimeException("params is null");
        }
        Request.Builder builder = getBuilder().url(url).post(RequestBody.create(MediaType.parse(contentType + ";" + charset), params));
        addHeader(builder, header);
        addTag(builder, tag);
        return execute(builder.build(), synchron, listener, getTag(tag));
    }

    @Override
    public HttpResponse request(HttpRequest request, LoadListener listener, Object... tag) {
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();
        String params = requestParams.toString();

        HttpMethod method = request.getMethod();
        Request.Builder builder = getBuilder().url(url);
        String contentType = requestParams.getContentType();
        String charset = requestParams.getCharSet();

        if (TextUtils.isEmpty(contentType) || TextUtils.isEmpty(charset)) {
            throw new RuntimeException("contentType is empty || charset is empty");
        }
        addHeader(builder, header);
        if (!TextUtils.isEmpty(params)) {
            builder.method(method.toString(), RequestBody.create(MediaType.parse(contentType + ";" + charset), params));
        }
        switch (method) {
            case HEAD:
                builder.head();
                break;
        }
        addTag(builder, tag);
        return execute(builder.build(), synchron, listener, getTag(tag));
    }

    private void addHeader(Request.Builder builder, HttpHeader header) {
        HashMap<String, String> headers = header.getHeaders();
        if (headers != null && headers.size() > 0) {
            for (HashMap.Entry<String, String> map :
                    headers.entrySet()) {
                builder.addHeader(map.getKey(), map.getValue());
            }
        }
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

    private HttpResponse execute(final Request request, final boolean synchron,
                                 final LoadListener listener, Object tag) {
        final Call call = mOkHttpClient.newCall(request);
        HttpResponse httpResponse = null;
        if (synchron) {
            try {
                Response response = call.execute();
                httpResponse = new HttpResponse();
                httpResponse.setResponseBody(response.body());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Task netWorkTask = new Task(tag, listener) {
                @Override
                public void onRun() {
                    try {
                        Response response = call.execute();
                        HttpResponse httpResponse = new HttpResponse();
                        httpResponse.setResponseBody(response.body());
                        notifySuccess(httpResponse);
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
        return httpResponse;
    }
}
