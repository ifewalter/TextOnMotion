package com.ifewalter.android.textonmotion.databaseparoles;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InsertData {
	static ContentValues newData = new ContentValues();
	protected static SQLiteDatabase db;
	public static final String DATABASE_NAME = "text_on_motion_database";

	private static SQLiteDatabase setDB(Context context) {
		db = (new InitDatabase(context).getWritableDatabase());
		return db;
	}

	public static void insertMessage(Context context, String messageReceipient,
			String messageStatus, String messageType, String messageTime,
			String messageDate, String messageContent) {
		// Initialise database
		setDB(context);

		String newReceipient = CheckPhoneNumberMatch.searchForMatch(context, db
				.rawQuery(("SELECT * FROM " + InitDatabase.TABLE_MESSAGES),
						null), messageReceipient);
		// create dataset
		newData.clear();
		newData.put(InitDatabase.RECIEPIENT, newReceipient);
		newData.put(InitDatabase.MESSAGE_STATUS, messageStatus);
		newData.put(InitDatabase.MESSAGE_TYPE, messageType);
		newData.put(InitDatabase.MESSAGE_TIME, messageTime);
		newData.put(InitDatabase.MESSAGE_DATE, messageDate);
		newData.put(InitDatabase.MESSAGE_CONTENT, messageContent);

		// insert to database
		db.insert(InitDatabase.TABLE_MESSAGES, InitDatabase.MESSAGE_TYPE,
				newData);
		db.close();

		insertAbstractThreadItem(context, newReceipient, messageContent,
				messageTime, messageDate);

	}
	public static void insertTask(Context context, String phoneNumber,
			String messageContent, String dueTime, String dueDate) {

		setDB(context);

		newData.clear();
		newData.put(InitDatabase.RECIEPIENT, phoneNumber);
		newData.put(InitDatabase.MESSAGE_CONTENT, messageContent);
		newData.put(InitDatabase.DUE_TIME, dueTime);
		newData.put(InitDatabase.DUE_DATE, dueDate);

		// insert into database
		db.insert("tom_tasks", InitDatabase.DUE_DATE, newData);

		db.close();
	}

	public static void insertAbstractThreadItem(Context context,
			String receipient, String messageContent, String tempTime,
			String tempDate) {
		setDB(context);

		try {
			db.delete(InitDatabase.TABLE_ABSTRACT, "receipient='" + receipient
					+ "'", null);
		} catch (Exception ex) {
		}

		int rowCount = 0;
		Cursor cursor = null;
		@SuppressWarnings("unused")
		String messageDate = null, messageTime = null, messageStatus = null, messageId = null;

		try {
			cursor = db
					.rawQuery(
							"SELECT * FROM tom_messages WHERE receipient=? ORDER BY _id DESC",
							new String[]{receipient});
			rowCount = cursor.getCount();
			messageDate = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_DATE));
			messageTime = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_TIME));
			messageStatus = cursor.getString(cursor
					.getColumnIndex(InitDatabase.MESSAGE_STATUS));
			messageId = cursor.getString(cursor
					.getColumnIndex(InitDatabase._ID));

		} catch (Exception ex) {

		}
		try {

			newData.clear();
			newData.put(InitDatabase.RECIEPIENT, receipient);
			newData.put(InitDatabase.MESSAGE_CONTENT, messageContent);
			newData.put(InitDatabase.MESSAGE_DATE, tempDate);
			newData.put(InitDatabase.MESSAGE_TIME, tempTime);
			newData.put(InitDatabase.MESSAGE_STATUS, messageStatus);
			newData.put(InitDatabase.MESSAGE_COUNT, rowCount);
			newData.put(InitDatabase.MESSAGE_ID, messageId);

			db.insert(InitDatabase.TABLE_ABSTRACT, "message_content", newData);
			db.delete(InitDatabase.TABLE_ABSTRACT, InitDatabase.MESSAGE_COUNT + " = " + 0, null);
			db.close();
		} catch (Exception ex) {
			rowCount = 0;
		}
	}

	public static void insertLongShort(Context context, String longString,
			String shortString) {
		setDB(context);

		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ InitDatabase.TABLE_SHORTHAND + " where "
				+ InitDatabase.LONG_STRING + " =?", new String[]{longString});
		try {
			if ((cursor.getCount() < 1) || cursor == null) {

				newData.clear();
				newData.put(InitDatabase.LONG_STRING, longString);
				newData.put(InitDatabase.SHORT_STRING, shortString);

				// insert into database
				db.insert(InitDatabase.TABLE_SHORTHAND,
						InitDatabase.SHORT_STRING, newData);
				cursor.close();
				db.close();
			}
		} catch (Exception ex) {
			newData.clear();
			newData.put(InitDatabase.LONG_STRING, longString);
			newData.put(InitDatabase.SHORT_STRING, shortString);

			// insert into database
			db.insert(InitDatabase.TABLE_SHORTHAND, InitDatabase.SHORT_STRING,
					newData);
			// cursor.close();
			db.close();
		} finally {

		}

	}
}
