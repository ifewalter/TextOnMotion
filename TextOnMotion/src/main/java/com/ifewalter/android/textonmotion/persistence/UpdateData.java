package com.ifewalter.android.textonmotion.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UpdateData {
	static ContentValues updateData = new ContentValues();
	protected static SQLiteDatabase db;

	private static SQLiteDatabase setDB(Context context) {
		db = (new InitDatabase(context).getWritableDatabase());
		return db;
	}

	/**
	 * Update abstract message table, Message status
	 * 
	 * @param context
	 *          
	 * @return void
	 * 
	 * */

	public static void changeMessageStatus(Context context, String newStatus,
			String receipient) {
		setDB(context);
		updateData.put(InitDatabase.MESSAGE_STATUS, newStatus);
		db.update(InitDatabase.TABLE_ABSTRACT, updateData,
				InitDatabase.RECIEPIENT, new String[]{receipient});
	}
	
	/**
	 * @deprecated
	 * @param context recipient
	 */
	public static void updateAbstractThread(Context context, String receipient) {
		setDB(context);
		updateData.clear();

		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ InitDatabase.TABLE_MESSAGES
				+ " ORDER BY _id DESC LIMIT 1 WHERE " + InitDatabase.RECIEPIENT
				+ "=?", new String[]{receipient});
		if (cursor != null) {
			updateData.put(InitDatabase.RECIEPIENT, receipient);
			updateData.put(InitDatabase.MESSAGE_CONTENT, cursor
					.getString(cursor
							.getColumnIndex(InitDatabase.MESSAGE_CONTENT)));
			updateData.put(InitDatabase.MESSAGE_DATE, cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_DATE)));
			updateData.put(InitDatabase.MESSAGE_TIME, cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_TIME)));
			updateData.put(InitDatabase.MESSAGE_STATUS, cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_STATUS)));
			updateData.put(InitDatabase.MESSAGE_COUNT, cursor.getCount());
		}
	}
}