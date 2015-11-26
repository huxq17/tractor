package com.andbase.demo.http.request;

import java.io.File;

/**
 * Created by huxq17 on 2015/11/24.
 */
public class HttpRequest {
    private HttpHeader header;
    private HttpMethod method;
    private RequestParams requestParams;

    private HttpRequest(Builder builder) {
        this.header = builder.header;
        this.method = builder.method;
        this.requestParams = builder.requestParams;
    }

    public static class Builder {
        private HttpHeader header;
        private HttpMethod method;
        private RequestParams requestParams;
        public Builder() {
            header = new HttpHeader();
            method = HttpMethod.GET;
            requestParams = new RequestParams();
        }

        public Builder setHeader(HttpHeader header) {
            if (header == null) {
                throw new RuntimeException("header==null");
            }
            this.header = header;
            return this;
        }

        public Builder addHeader(String name, String value) {
            header.addHeader(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            header.removeHeader(name);
            return this;
        }

        public Builder setParams(RequestParams params) {
            if (params == null) {
                throw new RuntimeException("params==null");
            }
            this.requestParams = params;
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
