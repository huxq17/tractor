package com.andbase.demo.http.request;

/**
 * Created by huxq17 on 2015/11/24.
 */
public class HttpRequest {
    private HttpHeader header;
    private HttpMethod method;
    private RequestParams requestParams;
    private HttpRequest(Builder builder){
        this.header = builder.header;
        this.method = builder.method;
        this.requestParams = builder.requestParams;
    }
    public static class Builder{
        private HttpHeader header;
        private HttpMethod method;
        private RequestParams requestParams;

        public Builder(){
            header = new HttpHeader();
            method = HttpMethod.GET;
            requestParams = new RequestParams();
        }
        public Builder setHeader(HttpHeader header){
            if(header==null){
                throw  new RuntimeException("header==null");
            }
            this.header = header;
            return this;
        }

        public Builder addHeader(String name,String value){
            header.addHeader(name,value);
            return this;
        }
        public Builder
        public Builder post(){
            method = HttpMethod.POST;
            return this;
        }
        public HttpRequest build(){
            return new HttpRequest(this);
        }
    }
}
