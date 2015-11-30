package com.andbase.demo.http.request;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Created by huxq17 on 2015/11/24.
 */
public class HttpRequest {
    private HttpHeader header;
    private HttpMethod method;
    private RequestParams requestParams;
    private String url;
    private boolean synchron = false;

    public HttpHeader getHeader() {
        return header;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public String getUrl() {
        return url;
    }
    public boolean isSynchron(){
        return synchron;
    }

    private HttpRequest(Builder builder) {
        this.header = builder.header;
        this.method = builder.method;
        this.requestParams = builder.requestParams;
        this.url = builder.url;
        this.synchron = builder.synchron;
    }

    public static class Builder {
        private HttpHeader header;
        private HttpMethod method;
        private RequestParams requestParams;
        private String url;
        private boolean synchron = false;

        public Builder() {
            header = new HttpHeader();
            method = HttpMethod.GET;
            requestParams = new RequestParams();
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder addHeader(String name, String value) {
            header.addHeader(name, value);
            return this;
        }

        /**
         *  同步请求，默认为异步请求
         * @return
         */
        public Builder synchron(){
            this.synchron = true;
            return this;
        }
        public Builder setHeader(LinkedHashMap<String,String> header){
            this.header.setHeader(header);
            return this;
        }

        public Builder removeHeader(String name) {
            header.removeHeader(name);
            return this;
        }

        public Builder setParams(LinkedHashMap<String,Object> params) {
            if (params == null) {
                throw new RuntimeException("params==null");
            }
            this.requestParams.setParams(params);
            return this;
        }

        public Builder setStringParams(String params) {
            requestParams.setStringParams(params);
            return this;
        }

        public Builder contentType(String contentType) {
            requestParams.setContentType(contentType);
            return this;
        }

        public Builder charSet(String charset) {
            requestParams.setCharSet(charset);
            return this;
        }

        public Builder addParam(String name, Object value) {
            requestParams.addParams(name, value);
            return this;
        }

        public Builder addFile(String name, File file) {
            requestParams.addFile(name, file);
            return this;
        }

        public Builder addFile(String name, File file, String filename, String contentType) {
            requestParams.addFile(name, file, filename, contentType);
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder post() {
            method = HttpMethod.POST;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}