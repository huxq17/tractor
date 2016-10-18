package com.andbase.demo.bean;

import com.andbase.tractor.task.Task;
import com.andbase.tractor.utils.LogUtils;

/**
 * 记载下载器详细信息的类
 */
public class DownloadInfo {
    public int downloadId;//下载器id
    public long startPos = -1;//开始点
    public long endPos = -1;//结束点
    public long completeSize;//完成度
    public String url;//下载器网络标识
    public long fileLength;
    public String filePath;//文件保存路径
    public String fileDir;//保存文件的文件夹路径
    public String filename;//保存的文件名
    public int threadNum;
    private int process;

    public DownloadInfo(String url, String fileDir, String filename, int threadNum) {
        this.url = url;
        this.fileDir = fileDir;
        this.filename = filename;
        this.filePath = this.fileDir + this.filename;
        this.threadNum = threadNum;
    }

    public DownloadInfo(int downloadId,
                        long startPosition, long endPosition, String url) {
        this.downloadId = downloadId;
        this.startPos = startPosition;
        this.endPos = endPosition;
        this.url = url;
    }

    /**
     * 计算进度，并通知ui更新
     *
     * @param done
     */
    public boolean compute(Task task, long done) {
//        LogUtils.d("completeSize=" + completeSize + ";done=" + done+";task.isRunning="+task.isRunning());
        if (task == null || !task.isRunning()) {
            //当下载任务不在运行时，返回false，主要用于其他下载子线程停止下载动作
            return false;
        }
        completeSize += done;
        int process = (int) (100 * (1.0f * completeSize / fileLength));
        if (process != this.process) {
            this.process = process;
            task.notifyLoading(this.process);
        }
//        if (hasDownloadSuccess()) {
////            synchronized (task) {
//                task.notify();
////            }
//        }
        return true;
    }

    public boolean hasDownloadSuccess() {
        LogUtils.d("hasDownloadSuccess completeSize=" + completeSize + ";fileLength=" + fileLength);
        return completeSize >= fileLength;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", compeleteSize=" + completeSize +
                ", url='" + url + '\'' +
                ", fileLength=" + fileLength +
                ", filePath='" + filePath + '\'' +
                ", fileDir='" + fileDir + '\'' +
                ", filename='" + filename + '\'' +
                ", threadNum=" + threadNum +
                '}';
    }

}