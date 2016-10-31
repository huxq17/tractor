package com.andbase.demo.http;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.andbase.demo.http.body.FileBody;
import com.andbase.demo.http.cookie.PersistentCookieStore;
import com.andbase.demo.http.request.CountingRequestBody;
import com.andbase.demo.http.request.HttpHeader;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.demo.http.request.RequestParams;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.demo.http.response.ResponseType;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by xiaoqian.hu on 2015/10/15.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OKHttp implements HttpBase {

    private static OkHttpClient mOkHttpClient;
    private NetWorkTask netWorkTask;
    private static boolean hasInited = false;

    public static void init(Context context) {
        if (hasInited) {
            return;
        }
        if (context == null) {
            throw new RuntimeException("context==null");
        }
        hasInited = true;
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setRetryOnConnectionFailure(true);
        mOkHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(15, TimeUnit.SECONDS);
//        mOkHttpClient.networkInterceptors().add(new RedirectInterceptor());
        int versionCode = Build.VERSION.SDK_INT;
        if (versionCode >= 9) {
            mOkHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
            mOkHttpClient.setCookieHandler(new CookieManager(new PersistentCookieStore(context), CookiePolicy.ACCEPT_ALL));
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
//            StrictMode.setThreadPolicy(policy);
        }
    }

    public static boolean hasInit() {
        return hasInited;
    }

    public void prepareTask() {
        netWorkTask = new NetWorkTask();
    }

    private void assertInited() {
        if (!hasInited) {
            throw new RuntimeException("please invoke init method first");
        }
    }

    @Override
    public HttpResponse get(HttpRequest request, LoadListener listener, Object... tag) {
        assertInited();
        prepareTask();
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();
        boolean noBody = request.noBody();
        String params = requestParams.toString();
        if (!TextUtils.isEmpty(params)) {
            url += "?" + params;
        }
        LogUtils.d("get url=" + url);

        Request.Builder builder = getBuilder().url(url);
        addHeader(builder, header);
        addTag(builder, tag);
        ResponseType responseType = request.responseType();
        return execute(responseType, builder.build(), synchron, noBody, listener, getTag(tag));
    }

    @Override
    public HttpResponse post(HttpRequest request, LoadListener listener, Object... tag) {
        assertInited();
        prepareTask();
        String url = request.getUrl();
        LogUtils.d("post url=" + url);
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();
        boolean noBody = request.noBody();

        RequestBody requestBody = buildRequestBody(requestParams);
        if (requestBody == null) {
            throw new RuntimeException("requestBody==null");
        }
        Request.Builder builder = getBuilder().url(url).post(requestBody);
        addHeader(builder, header);
        addTag(builder, tag);
        ResponseType responseType = request.responseType();
        return execute(responseType, builder.build(), synchron, noBody, listener, getTag(tag));
    }

    @Override
    public HttpResponse request(HttpRequest request, LoadListener listener, Object... tag) {
        assertInited();
        prepareTask();
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();
        boolean noBody = request.noBody();

        HttpMethod method = request.getMethod();
        LogUtils.d(method.toString() + " url=" + url);
        Request.Builder builder = getBuilder().url(url);
        String contentType = requestParams.getContentType();
        String charset = requestParams.getCharSet();

        if (TextUtils.isEmpty(contentType) || TextUtils.isEmpty(charset)) {
            throw new RuntimeException("contentType is empty || charset is empty");
        }
        addHeader(builder, header);

        if (HttpMethod.permitsRequestBody(method)) {
            if (HttpMethod.requiresRequestBody(method)) {
                if (requestParams.isEmpty()) {
                    throw new RuntimeException("method:" + method.toString() + "must have  params");
                }
            }
            if (!requestParams.isEmpty()) {
                builder.method(method.toString(), buildRequestBody(requestParams));
            } else {
                builder.method(method.toString(), null);
            }
        } else {
            builder.method(method.toString(), null);
        }
        addTag(builder, tag);
        ResponseType responseType = request.responseType();
        return execute(responseType, builder.build(), synchron, noBody, listener, getTag(tag));
    }

    private RequestBody buildRequestBody(RequestParams requestParams) {
        List<FileBody> files = requestParams.getFiles();
        String contentType = requestParams.getContentType();
        String charset = requestParams.getCharSet();
        String params = requestParams.toString();
        LinkedHashMap<String, Object> paramsHashmap = requestParams.getmParams();
        if (params == null || TextUtils.isEmpty(contentType) || TextUtils.isEmpty(charset)) {
            throw new RuntimeException("params is null");
        }
        if (files != null && files.size() > 0) {
            LogUtils.d("upload file.size=" + files.size());
//            MultipartBuilder builder = new MultipartBuilder()
//                    .type(MultipartBuilder.FORM);
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (FileBody body : files) {
                String paramName = body.getParameterName();
                File file = body.getFile();
                contentType = body.getContentType();
                RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + paramName + "\"; filename=\"" + file.getName() + "\""),
                        fileBody);
                LogUtils.d("upload paramName=" + paramName + ";filename=" + file.getName());
            }
            for (LinkedHashMap.Entry set : paramsHashmap.entrySet()) {
                Object value = set.getValue();
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + set.getKey() + "\""),
                        RequestBody.create(null, value.toString()));
            }
            RequestBody requestBody = new CountingRequestBody(builder.build(), netWorkTask);
            return requestBody;
//            return builder.build();
        } else {
            LogUtils.i("params=" + params + ";mediaType=" + contentType + "; charset=" + charset);
            return RequestBody.create(MediaType.parse(contentType + "; charset=" + charset), params);
        }
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

    private HttpResponse execute(ResponseType type, Request request, boolean synchron, boolean nobody, LoadListener listener, Object tag) {
        final Call call = mOkHttpClient.newCall(request);
        HttpResponse httpResponse = null;
        Response response = null;
        if (synchron) {
            try {
                response = call.execute();
                httpResponse = new HttpResponse();
                httpResponse.setResponseType(type);
                String string = null;
                if (!nobody) {
                    switch (type) {
                        case String:
                            try {
                                string = response.body().string();
                            } catch (IOException e) {
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            httpResponse.setString(string);
                            LogUtils.i("okresult: " + string);
                            break;
                        case InputStream:
                            httpResponse.setInputStream(response.body().byteStream());
                            break;
                    }
                } else {
                    closeResponseBody(response);
                }
                httpResponse.setCode(response.code());
                httpResponse.setString(string);
                httpResponse.setContentLength(response.body().contentLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            netWorkTask.setCall(type, call, nobody, tag, listener);
            TaskPool.getInstance().execute(netWorkTask);
        }
        return httpResponse;
    }

    private void closeResponseBody(Response response) {
        if (response != null) {
            Util.closeQuietly(response.body());
        }
    }

    public class NetWorkTask extends Task {
        private Call mCall;
        private ResponseType mType;
        private boolean nobody;

        public void setCall(ResponseType type, Call call, boolean nobody, Object tag, LoadListener listener) {
            mCall = call;
            setTag(tag);
            setListener(listener);
            mType = type;
            this.nobody = nobody;
        }

        @Override
        public void onRun() {
            execute();
        }

        private void execute() {
            try {
                Response response = mCall.execute();
                HttpResponse httpResponse = new HttpResponse();
                String string = null;
                httpResponse.setContentLength(response.body().contentLength());
                httpResponse.setResponseType(mType);
                if (!nobody) {
                    switch (mType) {
                        case String:
                            try {
                                string = response.body().string();
                            } catch (IOException e) {
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            httpResponse.setString(string);
                            LogUtils.d("okresult: " + string + ";contentLength=" + response.body().contentLength());
                            break;
                        case InputStream:
                            httpResponse.setInputStream(response.body().byteStream());
                            break;
                    }
                } else {
                    closeResponseBody(response);
                }
                httpResponse.setCode(response.code());
                httpResponse.setContentLength(response.body().contentLength());
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

        private void enqueue() {
            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    synchronized (NetWorkTask.this) {
                        NetWorkTask.this.notifyFail(e);
                        NetWorkTask.this.notify();
                    }
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    synchronized (NetWorkTask.this) {
                        HttpResponse httpResponse = new HttpResponse();
                        String string = null;
                        switch (mType) {
                            case String:
                                try {
                                    string = response.body().string();
                                } catch (IOException e) {
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                httpResponse.setString(string);
                                LogUtils.d("okresult: " + string);
                                break;
                            case InputStream:
                                httpResponse.setInputStream(response.body().byteStream());
                                break;
                        }
                        httpResponse.setString(string);
                        httpResponse.setContentLength(response.body().contentLength());
                        NetWorkTask.this.notifySuccess(httpResponse);
                        NetWorkTask.this.notify();
                    }
                }
            });
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void cancelTask() {
            mCall.cancel();
        }
    }
}