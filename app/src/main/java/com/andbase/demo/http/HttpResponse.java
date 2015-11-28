package com.andbase.demo.http;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by huxq17 on 2015/11/28.
 */
public class HttpResponse implements Serializable{
    private long contentLength;
    private String string;
    private InputStream inputStream;

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String string() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
