package com.andbase.demo.http.response;

import com.andbase.tractor.utils.LogUtils;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by huxq17 on 2015/11/28.
 */
public class HttpResponse implements Serializable {
    private ResponseBody responseBody;

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public long getContentLength() {
        if (responseBody != null) {
            try {
                long length = responseBody.contentLength();
                LogUtils.d( ";length=" + length);
                return length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public InputStream getInputStream() {
        if (responseBody != null) {
            try {
                return responseBody.byteStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String string() {
        if (responseBody != null) {
            try {
                String result = responseBody.string();
                LogUtils.d("okresult=" + result);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
