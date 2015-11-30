package com.andbase.demo.bean;

/**
 *记载下载器详细信息的类 
 */
public class DownloadInfo {
    public int threadId;//下载器id
    public long startPos;//开始点
    public long endPos;//结束点
    public long compeleteSize;//完成度
    public String url;//下载器网络标识
    public DownloadInfo(int threadId, int startPos, int endPos,
            int compeleteSize,String url) {
        this.threadId = threadId;
        this.startPos = startPos;
        this.endPos = endPos;
        this.compeleteSize = compeleteSize;
        this.url=url;
    }
    public DownloadInfo() {
    }
    @Override
    public String toString() {
        return "DownloadInfo [threadId=" + threadId
                + ", startPos=" + startPos + ", endPos=" + endPos
                + ", compeleteSize=" + compeleteSize +"]";
    }
}