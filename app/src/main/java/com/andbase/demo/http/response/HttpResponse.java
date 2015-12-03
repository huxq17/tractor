package com.andbase.demo.http.response;

import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.internal.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import okio.BufferedSource;

import static com.squareup.okhttp.internal.Util.UTF_8;

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
                LogUtils.d(";length=" + length);
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
            BufferedSource source = null;
            try {
//                String result = responseBody.string();
                source = responseBody.source();
                byte[] bytes = source.readByteArray();
                MediaType contentType = responseBody.contentType();
                Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;
                String result = new String(bytes, charset.name());
                LogUtils.d("okresult=" + result);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeSourceSafely(source);
            }
        }
        return null;
    }

    public void closeSourceSafely(final BufferedSource source) {
        if (source == null) {
            return;
        }
        TaskPool.getInstance().execute(new Task() {
            @Override
            public void onRun() {
                Util.closeQuietly(source);
            }

            @Override
            public void cancelTask() {
            }

            @Override
            public String toString() {
                return "closeSourceTask";
            }
        });
    }

}
