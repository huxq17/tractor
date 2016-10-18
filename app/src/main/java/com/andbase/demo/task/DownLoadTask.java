package com.andbase.demo.task;

import android.content.Context;

import com.andbase.demo.bean.DownloadInfo;
import com.andbase.demo.db.DBService;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.demo.utils.HttpSender;
import com.andbase.demo.utils.Utils;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.LogUtils;
import com.andbase.tractor.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okio.Segment;

/**
 * Created by huxq17 on 2015/12/16.
 */
public class DownLoadTask extends Task {
    private DownloadInfo mDownloadInfo;
    private Context mContext;
    private int mRunningThreadNum;
    private AtomicInteger mFinishedThread;

    public DownLoadTask(final DownloadInfo info, final Context context, final LoadListener listener, final Object tag) {
        super(tag, listener);
        this.mDownloadInfo = info;
        mContext = context;
        mFinishedThread = new AtomicInteger(0);
        mRunningThreadNum = 0;
        Utils.createDirIfNotExists(mDownloadInfo.fileDir);
//        List list = DBService.getInstance(mContext).getInfos(info.url);
//        if (list.size() == 0) {
//            File downloadFile = new File(info.fileDir, info.filename);
//            if (downloadFile.exists()) {
//                Utils.deleteFileSafely(downloadFile);
//            }
//        }
    }

    private void finishDownloadBlock() {
        mFinishedThread.getAndIncrement();
        if (mFinishedThread.get() == mRunningThreadNum && DownLoadTask.this.isRunning()) {
            synchronized (DownLoadTask.this) {
                DownLoadTask.this.notify();
            }
        }
    }

    @Override
    public void onRun() {
//        mFinished = false;
        notifyStart("开始下载...");
        int threadNum = mDownloadInfo.threadNum;
        if (threadNum < 1) {
            throw new RuntimeException("threadNum < 1");
        }
        //如果文件不存在就删除数据库中的缓存
        deleteCache(mDownloadInfo, mDownloadInfo.url);
        final long starttime = System.currentTimeMillis();
        int freeMemory = ((int) Runtime.getRuntime().freeMemory());// 获取应用剩余可用内存
        int allocated = freeMemory / 6 / threadNum;//给每个线程分配的内存
//        int allocated = 8092;
        long completed = 0;
        List<DownloadInfo> donelist = DBService.getInstance(mContext).getInfos(mDownloadInfo.url);
        LogUtils.d("spendTime allocated = " + allocated + "donelist.size=" + donelist.size() + ";threadNum=" + threadNum);
        threadNum = donelist.size() == 0 ? threadNum : donelist.size();
        int size = 0;
        try {
            Class segmentClass = Class.forName("okio.Segment");
            Constructor constructor = segmentClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Segment segment = (Segment) constructor.newInstance();
            Field field = segmentClass.getDeclaredField("SIZE");
            field.setAccessible(true);
            field.set(segment, allocated);
            size = field.getInt(segment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (threadNum > 1) {
            completed = startMultiThreadDownLoad(mDownloadInfo, donelist, allocated,threadNum);
        } else {
            completed = startSingleThreadDownLoad(mDownloadInfo, donelist, allocated);
        }
        synchronized (this) {
            mDownloadInfo.compute(this, completed);
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!mDownloadInfo.hasDownloadSuccess()) {
            notifyFail(null);
            LogUtils.d("download failed! " + "size=" + size + " allocated=" + allocated + " and spendTime=" + (System.currentTimeMillis() - starttime));
        } else {
            DBService.getInstance(mContext).delete(mDownloadInfo.url);
            LogUtils.d("download finshed! " + "size=" + size + " allocated=" + allocated + " and spendTime=" + (System.currentTimeMillis() - starttime));
        }
    }

    private long startMultiThreadDownLoad(DownloadInfo mDownloadInfo, List<DownloadInfo> donelist, int allocated, int threadNum) {
        long completed = 0;
        long fileLength = getFileLength(donelist, mDownloadInfo.url);
        if (fileLength <= 0) {
            notifyFail("获取下载文件信息失败");
        }
        mDownloadInfo.fileLength = fileLength;
        setFileLength(mDownloadInfo.fileDir, mDownloadInfo.filename, mDownloadInfo.fileLength);
        long block = fileLength % threadNum == 0 ? fileLength / threadNum : fileLength / threadNum + 1;

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
            } else {
                mDownloadInfo.downloadId = i;
            }
            if (mDownloadInfo.downloadId == threadNum - 1) {
                mDownloadInfo.endPos = fileLength;
            }
            if (mDownloadInfo.startPos < mDownloadInfo.endPos) {
                mRunningThreadNum++;
                LogUtils.d("startMultiThread start=" + mDownloadInfo.startPos + ";end=" + mDownloadInfo.endPos + ";filelength=" + fileLength
                        + ";info.downloadId=" + mDownloadInfo.downloadId);
                downBlock(mDownloadInfo, mContext, allocated, this, getTag());
            }
        }
        return completed;
    }

    private long startSingleThreadDownLoad(DownloadInfo mDownloadInfo, List<DownloadInfo> donelist, int allocated) {
        mDownloadInfo.downloadId = 0;
        mDownloadInfo.startPos = -1;
        mDownloadInfo.endPos = -1;
        long completed = 0;
        if (donelist.size() == 1) {
            DownloadInfo blockInfo = donelist.get(0);
            completed = blockInfo.startPos;
            mDownloadInfo.startPos = completed;
            mDownloadInfo.endPos = blockInfo.endPos;
            mDownloadInfo.fileLength = mDownloadInfo.endPos;
        }
        mRunningThreadNum++;
        LogUtils.d("startSingleThread start=" + mDownloadInfo.startPos + ";end=" + mDownloadInfo.endPos);
        downBlock(mDownloadInfo, mContext, allocated, this, getTag());
        return completed;
    }

    private void deleteCache(DownloadInfo info, String url) {
        File file = new File(info.filePath);
        if (!file.exists()) {
            DBService.getInstance(mContext).delete(url);
        }
    }

    private long getFileLength(List<DownloadInfo> donelist, String url) {
        long length = 0;
        for (DownloadInfo info : donelist) {
            if (info.endPos > length) {
                length = info.endPos;
            }
        }
        if (length <= 0) {
            HttpResponse headResponse = HttpSender.instance().headerSync(url, getTag());
            if (headResponse != null) {
                length = headResponse.getContentLength();
            }
        }
        return length;
    }


    @Override
    public void cancelTask() {
        synchronized (this) {
            this.notify();
        }
    }

    private void downBlock(final DownloadInfo info, final Context context, final int allocated, final DownLoadTask task, final Object tag) {
        final long startposition = info.startPos;
        final long endposition = info.endPos;
        final String url = info.url;
        final String filepath = info.filePath;
        final int downloadId = info.downloadId;
        TaskPool.getInstance().execute(new Task() {
            private long curPos = startposition;
            private long endPos = endposition;

            @Override
            public void onRun() {
                LinkedHashMap<String, String> header = new LinkedHashMap<>();
                if (startposition != -1 && endPos != -1) {
                    header.put("RANGE", "bytes=" + startposition + "-" + endPos);
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
                    accessFile = new RandomAccessFile(saveFile, "rwd");
                    if (startposition < 0) {
                        info.fileLength = downloadResponse.getContentLength();
                        accessFile.setLength(info.fileLength);
                        curPos = 0;
                        endPos = info.fileLength;
                        accessFile.seek(curPos);
                    } else {
                        accessFile.seek(startposition);
                    }
                    byte[] buffer = new byte[allocated];
//                    byte[] buffer = new byte[8192];
                    int len = 0;
                    while (curPos <= endPos) {
                        len = inStream.read(buffer);
                        if (len == -1) {
//                            notifyDownloadFailed(null);
                            break;
                        }
                        if (curPos + len > endPos) {
                            len = (int) (endPos - curPos + 1);//获取正确读取的字节数
                        }
                        synchronized (task) {
                            if (!info.compute(task, len)) {
                                //当下载任务失败以后结束此下载线程
                                break;
                            }
                            curPos += len;
                            accessFile.write(buffer, 0, len);
                        }
                    }
                    LogUtils.d("curPos=" + curPos + ";endPos=" + endPos);
                } catch (Exception e) {
                    e.printStackTrace();
//                    notifyDownloadFailed(e);
                } finally {
//                    LogUtils.e("block " + "id=" + downloadId + " download finished" + ";info.hasDownloadSuccess()=" + info.hasDownloadSuccess());
                    if (!info.hasDownloadSuccess()) {
                        DBService.getInstance(context).updataInfos(downloadId, curPos, endPos, url);
                    }
                    Util.closeQuietly(inStream);
                    Util.closeQuietly(accessFile);
                    task.finishDownloadBlock();
                }
            }

            private void notifyDownloadFailed(Exception e) {
                task.finishDownloadBlock();
//                task.notifyFail(e);
//                synchronized (task) {
//                    task.notify();
//                }
            }

            @Override
            public void cancelTask() {

            }
        });
    }

    public void setFileLength(String fileDir, final String fileName, long filelength) {
        Utils.createDirIfNotExists(fileDir);
        RandomAccessFile accessFile = null;
        try {
            File downloadFile = new File(fileDir, fileName);
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
