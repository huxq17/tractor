package com.andbase.demo;

import android.content.Context;
import android.text.TextUtils;

import com.andbase.demo.bean.BloackInfo;
import com.andbase.demo.bean.DownloadInfo;
import com.andbase.demo.db.DBService;
import com.andbase.demo.http.HttpBase;
import com.andbase.demo.http.OKHttp;
import com.andbase.demo.http.request.HttpMethod;
import com.andbase.demo.http.request.HttpRequest;
import com.andbase.demo.http.response.HttpResponse;
import com.andbase.demo.http.response.ResponseType;
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
 * Created by huxq17 on 2015/11/16.
 */
public class HttpSender {
    static HttpBase mHttpBase = new OKHttp();

    private static class InstanceHolder {
        private static HttpSender instance = new HttpSender();
    }

    public static HttpSender instance() {
        return InstanceHolder.instance;
    }


    public void post(final String url,
                     LinkedHashMap<String, String> headers, final String params,
                     final LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setStringParams(params);
        //如无特殊需求，这步可以省去，但如果传的参数是json则应该设置contentType为application/json
        builder.contentType("application/x-www-form-urlencoded").charSet("utf-8");
        //网络请求返回的默认类型就是string,如果是String类型就不用设置，如果下载文件和加载图片需要用到InputStream，则设置为ResponseType.InputStream
//        builder.setResponseType(ResponseType.InputStream);
        HttpRequest request = builder.build();
        mHttpBase.post(request, listener, tag);
    }

    public void post(final String url,
                     LinkedHashMap<String, String> headers, LinkedHashMap<String, Object> params,
                     LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setParams(params);
        HttpRequest request = builder.build();
        mHttpBase.post(request, listener, tag);
    }

    public void get(String url, LinkedHashMap<String, String> headers, String params,
                    final LoadListener listener, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url);
        addHeaders(builder, headers);
        builder.setResponseType(ResponseType.String);
        mHttpBase.get(builder.build(), listener, tag);
    }

    public HttpResponse getInputStreamSync(String url, LinkedHashMap<String, String> headers, String params, Object... tag) {
        if (!TextUtils.isEmpty(params)) {
            url = url + "?" + params;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).synchron().setResponseType(ResponseType.InputStream);
        addHeaders(builder, headers);
        return mHttpBase.get(builder.build(), null, tag);
    }

    public void header(String url, LoadListener listener, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).method(HttpMethod.HEAD);
        mHttpBase.request(builder.build(), listener, tag);
    }

    /**
     * 同步请求应当在非ui线程中调用
     *
     * @param url
     * @param tag
     */
    public HttpResponse headerSync(String url, Object... tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).synchron().method(HttpMethod.HEAD);
        HttpResponse response = mHttpBase.request(builder.build(), null, tag);
        return response;
    }

    public void upload(String url, LinkedHashMap<String, String> headers, File[] files, LinkedHashMap<String, Object> params, LoadListener listener, Object tag) {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).setParams(params);
        addHeaders(builder, headers);
        for (File file : files) {
            builder.addFile(file);
        }
        mHttpBase.post(builder.build(), listener, tag);
    }

    public void upload(String url, LinkedHashMap<String, String> headers, File[] files, LoadListener listener, Object tag) {
        upload(url, headers, files, null, listener, tag);
    }


    /**
     * 下载文件，支持多线程下载
     *
     * @param info     下载信息，url,文件保存路径等
     * @param listener
     * @param tag
     */
    public void download(final DownloadInfo info, final Context context, final LoadListener listener, final Object tag) {
        TaskPool.getInstance().execute(new Task(tag, listener) {
            @Override
            public void onRun() {
                notifyStart("开始下载...");
                String url = info.url;
                int threadNum = info.threadNum;
                if (threadNum < 1) {
                    throw new RuntimeException("threadNum<1");
                }
                long block = -1;
                final long starttime = System.currentTimeMillis();
                int freeMemory = ((int) Runtime.getRuntime().freeMemory());// 获取应用剩余可用内存
                int allocated = freeMemory / 6 / threadNum;//给每个线程分配的内存
                LogUtils.d("spendTime allocated = " + allocated);
                info.setTask(this);
                int completed = 0;
                List<BloackInfo> donelist = DBService.getInstance(context).getInfos(url);
                if (threadNum > 1) {
                    HttpResponse headResponse = headerSync(url, tag);
                    if (headResponse == null) {
                        notifyFail("获取下载文件信息失败");
                        return;
                    }
                    long filelength = headResponse.getContentLength();
                    info.fileLength = filelength;
                    if (filelength < 0) {
                        notifyFail("获取下载文件信息失败");
                    }
                    block = filelength % threadNum == 0 ? filelength / threadNum
                            : filelength / threadNum + 1;
                    setFileLength(info.fileDir, info.filename, info.fileLength);
                    for (int i = 0; i < threadNum; i++) {
                        final long startposition = i * block;
                        final long endposition = (i + 1) * block - 1;
                        info.startPos = startposition;
                        if (i < donelist.size()) {
                            BloackInfo bloackInfo = donelist.get(i);
                            int done = bloackInfo.getCompeleteSize();
                            info.downloadId = bloackInfo.getThreadId();
                            info.startPos += done;
                            completed += done;
                            info.completeSize = done;
                            //TODO 此处应该考虑线程数目变化的情况
                        } else {
                            info.downloadId = i;
                        }
                        LogUtils.i("update done = " + info.completeSize + ";start=" + startposition + ";end=" + endposition + ";filelength=" + filelength
                                + ";donelist.size=" + donelist.size());
                        info.endPos = endposition;
                        downBlock(info, context, allocated, this, tag);
                    }
                } else {
                    info.startPos = -1;
                    info.endPos = -1;
                    downBlock(info, context, allocated, this, tag);
                    for (BloackInfo blockInfo : donelist) {
                        completed += blockInfo.getCompeleteSize();
                    }
                }
                if (completed > 0) {
                    info.completeSize = 0;
                    info.compute(completed);
                }

                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (info.completeSize != info.fileLength) {
                    notifyFail(null);
                    LogUtils.i("download failed! spendTime=" + (System.currentTimeMillis() - starttime));
                } else {
                    DBService.getInstance(context).delete(url);
                    LogUtils.i("download finshed! spendTime=" + (System.currentTimeMillis() - starttime));
                }
            }

            @Override
            public void cancelTask() {
                synchronized (this) {
                    this.notify();
                }
            }
        });
    }

    private void downBlock(final DownloadInfo info, final Context context, final int allocated, final Task task, final Object tag) {
        final long startposition = info.startPos;
        final long endposition = info.endPos;
        final String url = info.url;
        final String filepath = info.filePath;
        final int downloadId = info.downloadId;
        final long completeSize = info.completeSize;
        TaskPool.getInstance().execute(new Task() {
            private int count = 0;

            @Override
            public void onRun() {
                LogUtils.d("startposition=" + startposition + ";endposition=" + endposition);
                LinkedHashMap<String, String> header = new LinkedHashMap<>();
                if (startposition != -1 && endposition != -1) {
                    header.put("RANGE", "bytes=" + startposition + "-"
                            + endposition);
                }
                HttpResponse downloadResponse = getInputStreamSync(url, header, null, null, tag);
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
                    while ((len = inStream.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, len);
                        // 实时更新进度
                        LogUtils.d("len=" + len);
                        count += len;
                        if (!info.compute(len)) {
                            //当下载任务失败以后结束此下载线程
                            LogUtils.i("停止下载 update start=" + startposition + ";end=" + endposition + ";count=" + count);
                            DBService.getInstance(context).updataInfos(downloadId, completeSize + count, url);
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
                DBService.getInstance(context).updataInfos(downloadId, completeSize + count, url);
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

    private void addHeaders(HttpRequest.Builder builder, LinkedHashMap<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            builder.setHeader(headers);
        }
    }
}
