package com.andbase.demo.db;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.andbase.demo.bean.DownloadInfo;
import com.andbase.tractor.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class DBService {
    private DBHelper dbHelper;
    private static DBService instance;
    private static final String DOWNLOAD_TABLE = "download_info";

    private DBService(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * @param context
     * @return
     */
    public static DBService getInstance(Context context) {
        if (context == null) {
            throw new RuntimeException("context == null");
        }
        if (!(context instanceof Application)) {
            context = context.getApplicationContext();
        }
        if (instance == null) {
            synchronized (DBService.class) {
                if (instance == null) {
                    instance = new DBService(context);
                    return instance;
                }
            }
        }
        return instance;
    }

    /**
     * 得到下载具体信息
     */
    public synchronized List<DownloadInfo> getInfos(String urlstr) {
        List<DownloadInfo> list = new ArrayList<DownloadInfo>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select thread_id,startposition,endposition,url from "+DOWNLOAD_TABLE+" where url=?";
        Cursor cursor = database.rawQuery(sql, new String[]{urlstr});
        while (cursor.moveToNext()) {
            DownloadInfo info = new DownloadInfo(cursor.getInt(0), cursor.getInt(1), cursor.getLong(2),
                    cursor.getString(3));
            list.add(info);
        }
        cursor.close();
        database.close();
        return list;
    }


    /**
     * 更新数据库中的下载信息
     */
    public synchronized void updataInfos(int threadId, long startposition, long endposition, String url) {
        long totalCount = getCount(DOWNLOAD_TABLE);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // 如果存在就更新，不存在就插入
        String sql = "replace into " + DOWNLOAD_TABLE + "(thread_id,startposition,endposition,url) values(?,?,?,?)";
        Object[] bindArgs = {threadId, startposition, endposition, url};
        database.execSQL(sql, bindArgs);
        database.close();
        LogUtils.i("updataInfos total=" + totalCount + ";threadid=" + threadId + ";startposition=" + startposition + ";endposition=" + endposition);
    }

    /**
     * 关闭数据库
     */
    public void closeDb() {
        dbHelper.close();
    }

    /**
     * 下载完成后删除数据库中的数据
     */
    public synchronized void delete(String url) {
        long totalCount = getCount(DOWNLOAD_TABLE);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = database.delete(DOWNLOAD_TABLE, "url=?", new String[]{url});
        Log.i("delete", "delete total=" + totalCount + ";delete count=" + count + ";url=" + url);
        database.close();
    }

    private long getCount(String table) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select count(*)from " + table, null);
        cursor.moveToFirst();
        long result = cursor.getLong(0);
        cursor.close();
        database.close();
        return result;
    }

    public void saveOrUpdateInfos() {

    }

    public synchronized void deleteByIdAndUrl(int id, String url) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = database.delete(DOWNLOAD_TABLE, "thread_id=? and url=?", new String[]{
                id + "", url});
        Log.i("delete", "delete id=" + id + "," + "count=" + count);
        database.close();
    }
}
