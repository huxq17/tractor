package com.andbase.demo.http;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;

import com.andbase.demo.http.body.FileBody;
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
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by xiaoqian.hu on 2015/10/15.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OKHttp implements HttpBase {

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private NetWorkTask netWorkTask;

    static {
        mOkHttpClient.setRetryOnConnectionFailure(true);
        mOkHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(15, TimeUnit.SECONDS);
//        mOkHttpClient.networkInterceptors().add(new RedirectInterceptor());
        int versionCode = Build.VERSION.SDK_INT;
        if (versionCode >= 9) {
            mOkHttpClient.setCookieHandler(new CookieManager(null,
                    CookiePolicy.ACCEPT_ORIGINAL_SERVER));
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
//            StrictMode.setThreadPolicy(policy);
        }
    }

    public void prepareTask() {
        netWorkTask = new NetWorkTask();
    }

    @Override
    public HttpResponse get(HttpRequest request, LoadListener listener, Object... tag) {
        prepareTask();
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
        ResponseType responseType = request.responseType();
        return execute(responseType, HttpMethod.GET, builder.build(), synchron, listener, getTag(tag));
    }

    @Override
    public HttpResponse post(HttpRequest request, LoadListener listener, Object... tag) {
        prepareTask();
        String url = request.getUrl();
        LogUtils.d("post url=" + url);
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();

        RequestBody requestBody = buildRequestBody(requestParams);
        if (requestBody == null) {
            throw new RuntimeException("requestBody==null");
        }
        Request.Builder builder = getBuilder().url(url).post(requestBody);
        addHeader(builder, header);
        addTag(builder, tag);
        ResponseType responseType = request.responseType();
        return execute(responseType, HttpMethod.POST, builder.build(), synchron, listener, getTag(tag));
    }

    @Override
    public HttpResponse request(HttpRequest request, LoadListener listener, Object... tag) {
        prepareTask();
        String url = request.getUrl();
        RequestParams requestParams = request.getRequestParams();
        HttpHeader header = request.getHeader();
        boolean synchron = request.isSynchron();

        HttpMethod method = request.getMethod();
        LogUtils.d(method.toString()+" url=" + url);
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
        return execute(responseType, method, builder.build(), synchron, listener, getTag(tag));
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
        LogUtils.d("upload file.size=" + files.size());
        if (files != null && files.size() > 0) {
            MultipartBuilder builder = new MultipartBuilder()
                    .type(MultipartBuilder.FORM);
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
                if (value instanceof String) {
                    builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + set.getKey() + "\""),
                            RequestBody.create(null, value.toString()));
                }
            }

            return new CountingRequestBody(builder.build(), netWorkTask);
//            return builder.build();
        } else {
            return RequestBody.create(MediaType.parse(contentType + ";" + charset), params);
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

    private HttpResponse execute(ResponseType type, HttpMethod method, Request request, boolean synchron, LoadListener listener, Object tag) {
        final Call call = mOkHttpClient.newCall(request);
        HttpResponse httpResponse = null;
        if (synchron) {
            try {
                Response response = call.execute();
                httpResponse = new HttpResponse();
                httpResponse.setResponseType(type);
                String string = null;
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
                httpResponse.setString(string);
                httpResponse.setContentLength(response.body().contentLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            netWorkTask.setCall(type, method, call, tag, listener);
            TaskPool.getInstance().execute(netWorkTask);
        }
        return httpResponse;
    }

    public class NetWorkTask extends Task {
        private Call mCall;
        private HttpMethod mMethod;
        private ResponseType mType;

        public void setCall(ResponseType type, HttpMethod method, Call call, Object tag, LoadListener listener) {
            mCall = call;
            mMethod = method;
            setTag(tag);
            setListener(listener);
            mType = type;
        }

        @Override
        public void onRun() {
            execute();
//            enqueue();
        }

        private void execute() {
            try {
                Response response = mCall.execute();
                HttpResponse httpResponse = new HttpResponse();
                String string = null;
                httpResponse.setResponseType(mType);
                switch (mType) {
                    case String:
                        try {
                            string = response.body().string();
                        } catch (IOException e) {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        httpResponse.setString(string);
                        LogUtils.d("okresult: " + string+";contentLength="+response.body().contentLength());
                        break;
                    case InputStream:
                        httpResponse.setInputStream(response.body().byteStream());
                        break;
                }
                httpResponse.setString(string);
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