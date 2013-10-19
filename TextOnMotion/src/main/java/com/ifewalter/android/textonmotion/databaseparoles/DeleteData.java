package com.ifewalter.android.textonmotion.databaseparoles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DeleteData {
	static ContentValues newData = new ContentValues();
	protected static SQLiteDatabase db;

	private static SQLiteDatabase setDB(Context context) {
		db = (new InitDatabase(context).getWritableDatabase());
		return db;
	}

	public static void deleteMessage(Context context, String string) {
		setDB(context);
		db.delete(InitDatabase.TABLE_MESSAGES, "_id=" + string, null);
	}

	public static void deleteLongShort(Context context, String string) {
		setDB(context);
		db.delete(InitDatabase.TABLE_SHORTHAND, "_id=" + string, null);
	}

	public static void deleteAbstractViewItem(Context context,
			String sentMessageId) {
		setDB(context);
		db.delete(InitDatabase.TABLE_ABSTRACT, "message_id=" + sentMessageId
				+ "", null);

		int rowCount = 0;
		Cursor cursor = null;
		String messageDate = null, messageContent = null, messageTime = null;
		String messageId = null, messageStatus = null, receipient = null;

		try {
			cursor = db.rawQuery("SELECT * FROM " + InitDatabase.TABLE_MESSAGES
					+ " WHERE _id=? ORDER BY _id DESC LIMIT 1",
					new String[]{sentMessageId});
			receipient = cursor.getString(cursor
					.getColumnIndex(InitDatabase.RECIEPIENT));

		} catch (Exception ex) {

		}

		try {
			cursor = db.rawQuery("SELECT * FROM " + InitDatabase.TABLE_MESSAGES
					+ " WHERE receipient=? ORDER BY _id DESC LIMIT 1",
					new String[]{receipient});
			rowCount = cursor.getCount();
			messageDate = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_DATE));
			messageTime = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_TIME));
			messageContent = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_TIME));
			messageStatus = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_STATUS));
			messageId = cursor.getString(cursor
					.getColumnIndex(InitDatabase._ID));

		} catch (Exception ex) {
			rowCount = 0;
		}

		try {

			newData.clear();
			newData.put(InitDatabase.RECIEPIENT, receipient);
			newData.put(InitDatabase.MESSAGE_CONTENT, messageContent);
			newData.put(InitDatabase.MESSAGE_DATE, messageDate);
			newData.put(InitDatabase.MESSAGE_TIME, messageTime);
			newData.put(InitDatabase.MESSAGE_STATUS, messageStatus);
			newData.put(InitDatabase.MESSAGE_COUNT, rowCount);
			newData.put(InitDatabase.MESSAGE_ID, messageId);

			db.insert(InitDatabase.TABLE_ABSTRACT, "message_content", newData);

			db.delete(InitDatabase.TABLE_MESSAGES, "_id=" + sentMessageId, null);

			db.close();
		} catch (Exception ex) {
			rowCount = 0;
		}
	}

	public static void deleteThread(Context context, String receipient) {
		setDB(context);
		db.delete(InitDatabase.TABLE_MESSAGES, InitDatabase.RECIEPIENT + "='"
				+ receipient + "'", null);
		db.delete(InitDatabase.TABLE_ABSTRACT, InitDatabase.RECIEPIENT + "='"
				+ receipient + "'", null);

		db.close();
	}
}
