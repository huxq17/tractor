package com.andbase.demo.http.request;

import com.andbase.demo.http.body.FileBody;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Created by 2144 on 2015/11/26.
 */
public class RequestParams {
    private LinkedHashMap<String, Object> mParams;
    private LinkedHashMap<String, FileBody> mFileParams;

    public RequestParams() {
        mParams = new LinkedHashMap<>();
        mFileParams = new LinkedHashMap<>();
    }

    /**
     * 提交普通参数
     *
     * @param name
     * @param params
     */
    public void addParams(String name, Object params) {
        mParams.put(name, params);
    }

    /**
     * 提交文件
     *
     * @param name
     */
    public void addFile(String name, File file) {
        if (file == null || !file.exists()) {
            throw new RuntimeException("file==null||!file.exists()");
        }
        FileBody body = new FileBody(name, file, file.getName(), null);
        mFileParams.put(name, body);
    }

    /**
     * 提交文件
     *
     * @param name
     */
    public void addFile(String name, File file, String filename, String contentType) {
        FileBody body = new FileBody(name, file, filename, contentType);
        mFileParams.put(name, body);
    }

    public void clear() {
        mParams.clear();
        mFileParams.clear();
    }

    public LinkedHashMap<String, Object> getmParams() {
        return mParams;
    }

    public LinkedHashMap<String, FileBody> getmFileParams() {
        return mFileParams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LinkedHashMap.Entry set :
                mParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(set.getKey()).append("=").append(set.getValue());
        }
        return sb.toString();
    }
}
