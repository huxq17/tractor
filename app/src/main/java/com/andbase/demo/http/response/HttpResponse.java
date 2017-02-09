package com.andbase.demo.http.response;

import java.io.InputStream;

/**
 * Created by huxq17 on 2015/11/28.
 */
public class HttpResponse {
    private long contentLength;
    private ResponseType type;
    private int code;
    private Body mBody;

    public HttpResponse() {
        mBody = new Body();
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public class Body {
        private String string;
        private InputStream inputStream;

        public void setString(String string) {
            this.string = string;
        }

        public String string() {
            check(ResponseType.String);
            return string;
        }

        public void setInputStream(InputStream inputStream) {
            check(ResponseType.InputStream);
            this.inputStream = inputStream;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }

    public Body body() {
        return mBody;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }


    private void check(ResponseType requstType) {
        if (requstType != type) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Response type mismatch,you need type is ").append(requstType)
                    .append(" ，but your configuration is ").append(type).append(" ,so you should config responseType like this:");
            switch (requstType) {
                case String:
                    stringBuffer.append(" builder.setResponseType(ResponseType.String);");
                    break;
                case InputStream:
                    stringBuffer.append(" builder.setResponseType(ResponseType.InputStream);");
                    break;
            }
            throw new RuntimeException(stringBuffer.toString());
        }
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setResponseType(ResponseType type) {
        this.type = type;
    }

    public static String getString(Object result) {
        if (result instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) result;
            return httpResponse.body().string();
        }
        return null;
    }
}
