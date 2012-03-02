package com.roly.nfc.crypto.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteDatabaseHelper extends SQLiteOpenHelper {
	
	// column names
	public static final String KEY_ID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	
	// column indexes
	public static final int ID_COLUMN = 0;
	public static final int TITLE_COLUMN = 1;
	public static final int BODY_COLUMN = 2;

	protected static final String DATABASE_NAME = "cryptonfc.db";
	protected static final String DATABASE_TABLE = "notes";
	protected static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = String.format("create table %s (%s integer primary key autoincrement, %s text not null, %s text not null);",
			DATABASE_TABLE, KEY_ID, KEY_TITLE, KEY_BODY);;
	
	public NoteDatabaseHelper(Context context) {
		super(context,DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("CryptoNFC", "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS notes");
		onCreate(db);
	}

}
