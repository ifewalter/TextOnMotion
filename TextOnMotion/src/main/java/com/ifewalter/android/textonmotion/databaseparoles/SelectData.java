package com.ifewalter.android.textonmotion.databaseparoles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SelectData {
	ContentValues newData = new ContentValues();
	protected static SQLiteDatabase db;

	private static SQLiteDatabase setDB(Context context) {
		db = (new InitDatabase(context).getWritableDatabase());
		return db;
	}

	public static Cursor selectAllMessages(Context context) {
		setDB(context);
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT * FROM " + InitDatabase.TABLE_MESSAGES
					+ " ORDER BY _id DESC", null);

		} catch (Exception ex) {
		}
		return cursor;
	}

	public static Cursor selectConversation(Context context, String Receipient) {
		setDB(context);
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT * FROM " + InitDatabase.TABLE_MESSAGES
					+ " where " + InitDatabase.RECIEPIENT
					+ "=? ORDER BY _id DESC", new String[]{Receipient});
		} catch (Exception ex) {
		}
		return cursor;
	}

	public static Cursor selectShortString(Context context, String longString) {
		setDB(context);
		Cursor cursor = null;
		try {
			cursor = db
					.rawQuery("SELECT * FROM " + InitDatabase.TABLE_SHORTHAND
							+ " where " + InitDatabase.LONG_STRING + "=?",
							new String[]{longString});

		} catch (Exception ex) {
		}
		return cursor;
	}

	public static Cursor selectAllShortString(Context context) {
		setDB(context);
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT * FROM "
					+ InitDatabase.TABLE_SHORTHAND, null);

		} catch (Exception ex) {
		}
		return cursor;
	}
	public static Cursor selectAbstractThread(Context context) {
		setDB(context);
		Cursor cursor = null;
		try {
			cursor = db
					.rawQuery(
							"SELECT * FROM tom_abstract_thread ORDER BY _id DESC",
							null);

		} catch (Exception ex) {
		}
		return cursor;
	}

	public static Cursor searchContent(Context context, String query) {
		setDB(context);
		Cursor cursor = null;

		try {
			cursor = db.rawQuery("SELECT * FROM " + InitDatabase.TABLE_MESSAGES
					+ " WHERE " + InitDatabase.MESSAGE_CONTENT + " LIKE ?",
					new String[]{"%" + query + "%"});
		} catch (Exception ex) {

		}
		return cursor;

	}
	public static Cursor selectAllTasks(Context context) {
		setDB(context);
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT * FROM tom_tasks", null);
		} catch (Exception ex) {
		}
		return cursor;
	}
}
