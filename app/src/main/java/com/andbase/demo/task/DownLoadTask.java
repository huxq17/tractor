package com.andbase.demo.task;

import android.content.Context;

import com.andbase.demo.HttpSender;
import com.andbase.demo.bean.DownloadInfo;
import com.andbase.demo.db.DBService;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by huxq17 on 2015/12/16.
 */
public class DownLoadTask extends Task {
    private DownloadInfo mDownloadInfo;
    private Context mContext;

    public DownLoadTask(final DownloadInfo info, final Context context, final LoadListener listener, final Object tag) {
        super(tag,listener);
        this.mDownloadInfo = info;
        mContext = context;
    }

    @Override
    public void onRun() {
        notifyStart("开始下载...");
        String url = mDownloadInfo.url;
        int threadNum = mDownloadInfo.threadNum;
        if (threadNum < 1) {
            throw new RuntimeException("threadNum<1");
        }
        long block = -1;
        final long starttime = System.currentTimeMillis();
        int freeMemory = ((int) Runtime.getRuntime().freeMemory());// 获取应用剩余可用内存
        int allocated = freeMemory / 6 / threadNum;//给每个线程分配的内存
        LogUtils.d("spendTime allocated = " + allocated);
        mDownloadInfo.setTask(this);
        int completed = 0;
        List<DownloadInfo> donelist = DBService.getInstance(mContext).getInfos(url);
        if (threadNum > 1) {
            HttpResponse headResponse = HttpSender.instance().headerSync(url, getTag());
            if (headResponse == null) {
                notifyFail("获取下载文件信息失败");
                return;
            }
            long filelength = headResponse.getContentLength();
            mDownloadInfo.fileLength = filelength;
            if (filelength < 0) {
                notifyFail("获取下载文件信息失败");
            }
            setFileLength(mDownloadInfo.fileDir, mDownloadInfo.filename, mDownloadInfo.fileLength);
            threadNum = donelist.size() == 0 ? threadNum : donelist.size();
            block = filelength % threadNum == 0 ? filelength / threadNum
                    : filelength / threadNum + 1;
            for (int i = 0; i < threadNum; i++) {
                final long startposition = i * block;
                final long endposition = (i + 1) * block - 1;
                mDownloadInfo.startPos = startposition;
                mDownloadInfo.endPos = endposition;
                if (i < donelist.size()) {
                    DownloadInfo blockInfo = donelist.get(i);
                    mDownloadInfo.downloadId = blockInfo.downloadId;
                    completed += blockInfo.startPos - mDownloadInfo.startPos;
                    mDownloadInfo.startPos = blockInfo.startPos;
                    mDownloadInfo.endPos = blockInfo.endPos;
                    //TODO 此处应该考虑线程数目变化的情况
                } else {
                    mDownloadInfo.downloadId = i;
                }
                if (i == threadNum - 1) {
                    mDownloadInfo.endPos = mDownloadInfo.endPos > filelength ? filelength : mDownloadInfo.endPos;
                }
                LogUtils.d("update done = " + completed + ";start=" + mDownloadInfo.startPos + ";end=" + endposition + ";filelength=" + filelength
                        + ";info.downloadId=" + mDownloadInfo.downloadId);
                downBlock(mDownloadInfo, mContext, allocated, this, getTag());
            }
        } else {
            mDownloadInfo.startPos = -1;
            mDownloadInfo.endPos = -1;
            downBlock(mDownloadInfo, mContext, allocated, this, getTag());
            for (DownloadInfo blockInfo : donelist) {
                completed += blockInfo.completeSize;
            }
        }
        if (completed > 0) {
            mDownloadInfo.completeSize = 0;
            mDownloadInfo.compute(completed);
        }

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mDownloadInfo.completeSize != mDownloadInfo.fileLength) {
            notifyFail(null);
            LogUtils.i("download failed! spendTime=" + (System.currentTimeMillis() - starttime));
        } else {
            DBService.getInstance(mContext).delete(url);
            LogUtils.i("download finshed! spendTime=" + (System.currentTimeMillis() - starttime));
        }
    }

    @Override
    public void cancelTask() {
        synchronized (this) {
            this.notify();
        }
    }

    private void downBlock(final DownloadInfo info, final Context context, final int allocated, final Task task, final Object tag) {
        final long startposition = info.startPos;
        final long endposition = info.endPos;
        final String url = info.url;
        final String filepath = info.filePath;
        final int downloadId = info.downloadId;
        TaskPool.getInstance().execute(new Task() {
            private int count = 0;
            private long curPos = startposition;

            @Override
            public void onRun() {
                LogUtils.d("startposition=" + startposition + ";endposition=" + endposition);
                LinkedHashMap<String, String> header = new LinkedHashMap<>();
                if (startposition != -1 && endposition != -1) {
                    header.put("RANGE", "bytes=" + startposition + "-"
                            + endposition);
                }
                HttpResponse downloadResponse = HttpSender.instance().getInputStreamSync(url, header, null, null, tag);
                InputStream inStream = null;
                if (downloadResponse != null) {
                    inStream = downloadResponse.getInputStream();
                } else {
                    notifyDownloadFailed(null);
                    return;
                }
                RandomAccessFile accessFile = null;
                try {
                    File saveFile = new File(filepath);
                    if (startposition < 0) {
                        info.fileLength = downloadResponse.getContentLength();
                        setFileLength(info.fileDir, info.filename, info.fileLength);
                        accessFile = new RandomAccessFile(saveFile, "rwd");
                        accessFile.seek(0);
                    } else {
                        accessFile = new RandomAccessFile(saveFile, "rwd");
                        accessFile.seek(startposition);
                    }
                    byte[] buffer = new byte[allocated];
//                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while (curPos < endposition) {
                        len = inStream.read(buffer);
                        if (len == -1) {
//                            notifyDownloadFailed(null);
                            break;
                        }
                        if (curPos + len > endposition) {
                            len = (int) (endposition - curPos + 1);//获取正确读取的字节数
                        }
                        LogUtils.d("len=" + len + ";curPos=" + curPos + ";end=" + endposition + "id=" + downloadId);
                        count += len;
                        curPos += len;
                        accessFile.write(buffer, 0, len);
                        if (!info.compute(len)) {
                            //当下载任务失败以后结束此下载线程
                            DBService.getInstance(context).updataInfos(downloadId, curPos, endposition, url);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyDownloadFailed(e);
                } finally {
                    Util.closeQuietly(inStream);
                    Util.closeQuietly(accessFile);
                }
            }

            private void notifyDownloadFailed(Exception e) {
                DBService.getInstance(context).updataInfos(downloadId, curPos, endposition, url);
                task.notifyFail(e);
                synchronized (task) {
                    task.notify();
                }
            }

            @Override
            public void cancelTask() {

            }
        });
    }

    public void setFileLength(final String fileDir, final String fileName, long filelength) {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        RandomAccessFile accessFile = null;
        try {
            File downloadFile = new File(fileDir, fileName);
//            if (downloadFile.exists()) {
//                downloadFile.delete();
//            }
            accessFile = new RandomAccessFile(downloadFile, "rwd");
            accessFile.setLength(filelength);// 设置本地文件的长度和下载文件相同
            accessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(accessFile);
        }
    }
}
