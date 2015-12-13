package com.andbase.demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	// download.db-->数据库名
	public DBHelper(Context context) {
		super(context, "download.db", null, 1);
	}

	/**
	 * 在download.db数据库下创建一个download_info表存储下载信息
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table download_info(thread_id integer, "
				+ "start_pos integer, end_pos integer, compelete_size integer,url char," +
				"primary key(thread_id,url))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS download_info");
		onCreate(db);
	}

}
