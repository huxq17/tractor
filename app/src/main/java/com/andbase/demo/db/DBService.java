package com.andbase.demo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.andbase.demo.bean.BloackInfo;

import java.util.ArrayList;
import java.util.List;

public class DBService {
	private DBHelper dbHelper;
	private static DBService instance;

	private DBService(Context context) {
		dbHelper = new DBHelper(context);
	}

	/**
	 * 单例模式,不必每次使用都重新new
	 * 
	 * @param context
	 * @return
	 */
	public static DBService getInstance(Context context) {
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
	 * 查看数据库中是否有数据
	 */
	public boolean isHasInfors(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select count(*)  from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		Log.i("count", "count=" + count);
		cursor.close();
		return count == 0;
	}

	/**
	 * 保存下载的具体信息
	 */
	public void saveInfos(List<BloackInfo> infos) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		for (BloackInfo info : infos) {
			String sql = "insert into download_info(thread_id,start_pos,"
					+ " end_pos,compelete_size,url) values (?,?,?,?,?)";
			Object[] bindArgs = { info.getThreadId(), info.getStartPos(),
					info.getEndPos(), info.getCompeleteSize(), info.getUrl() };
			database.execSQL(sql, bindArgs);
		}
	}

	/**
	 * 得到下载具体信息
	 */
	public List<BloackInfo> getInfos(String urlstr) {
		List<BloackInfo> list = new ArrayList<BloackInfo>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select thread_id, start_pos, end_pos,compelete_size,url"
				+ " from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		while (cursor.moveToNext()) {
			BloackInfo info = new BloackInfo(cursor.getInt(0),
					cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
					cursor.getString(4));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取特定ID的线程已下载的进度
	 * 
	 * @param id
	 * @param url
	 * @return
	 */
	public synchronized int getInfoByIdAndUrl(int id, String url) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select compelete_size"
				+ " from download_info where thread_id=? and url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { id + "", url });
		if (cursor!=null&&cursor.moveToFirst()) {
			Log.i("count",
					"thread id="
							+ id
							+ "completed="
							+ cursor.getInt(0));
			return cursor.getInt(0);
		}
		return 0;
	}

	/**
	 * 更新数据库中的下载信息
	 */
	public synchronized void updataInfos(int threadId, int compeleteSize, String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		// 如果存在就更新，不存在就插入
		String sql = "replace into download_info"
				+ "(compelete_size,thread_id,url) values(?,?,?)";
		Object[] bindArgs = { compeleteSize, threadId, urlstr };
		database.execSQL(sql, bindArgs);
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
	public void delete(String url) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int count  = database.delete("download_info", "url=?", new String[] { url });
		Log.i("delete", "delete count="+count);
		database.close();
	}

	public void saveOrUpdateInfos() {

	}

	public synchronized void deleteByIdAndUrl(int id, String url) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int count = database.delete("download_info", "thread_id=? and url=?", new String[] {
				id + "", url });
		Log.i("delete", "delete id="+id+","+"count="+count);
		database.close();
	}
}
