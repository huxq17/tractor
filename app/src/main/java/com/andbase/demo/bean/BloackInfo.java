package com.andbase.demo.bean;

/**
 * Created by Administrator on 2015/12/13.
 */
public class BloackInfo {
    private int threadId;//下载器id
    private int compeleteSize;//完成度
    private String url;//下载器网络标识
    private long totalSize;

    public BloackInfo(int threadId,
                      int compeleteSize, long totalSize, String url) {
        this.threadId = threadId;
        this.compeleteSize = compeleteSize;
        this.url = url;
        this.totalSize = totalSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getCompeleteSize() {
        return compeleteSize;
    }

    public void setCompeleteSize(int compeleteSize) {
        this.compeleteSize = compeleteSize;
    }

    @Override
    public String toString() {
        return "BloackInfo [threadId=" + threadId
                + ", compeleteSize=" + compeleteSize + "]";
    }
}