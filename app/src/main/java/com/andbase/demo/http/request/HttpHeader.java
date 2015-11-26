package com.andbase.demo.http.request;

import java.util.HashMap;

public final class HttpHeader {
    private HashMap<String, String> mHeader;

    public HttpHeader() {
        mHeader = new HashMap<>();
    }
    /**
     * Adds a header with {@code name} and {@code value}. Prefer this method for
     * multiply-valued headers like "Cookie".
     */
    public void addHeader(String name,String value){
       mHeader.put(name,value);
    }

    public void removeHeader(String name){
        mHeader.remove(name);
    }
    public void clear(){
        mHeader.clear();
    }


}
