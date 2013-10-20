package com.ifewalter.android.textonmotion.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InitDatabase extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "text_on_motion_database";

	public static final String _ID = "_id";
	public static final String RECIEPIENT = "receipient";
	public static final String MESSAGE_CONTENT = "message_content";
	public static final String MESSAGE_TIME = "message_time";
	public static final String MESSAGE_DATE = "message_date";
	public static final String MESSAGE_STATUS = "message_status";
	public static final String MESSAGE_TYPE = "message_type";
	public static final String MESSAGE_COUNT = "message_count";
	public static final String MESSAGE_ID = "message_id";
	public static final String DUE_TIME = "due_time";
	public static final String DUE_DATE = "due_date";

	public static final String LONG_STRING = "long_string";
	public static final String SHORT_STRING = "short_string";

	public static final String TABLE_MESSAGES = "tom_messages";
	public static final String TABLE_TASK = "tom_tasks";
	public static final String TABLE_ABSTRACT = "tom_abstract_thread";
	public static final String TABLE_SHORTHAND = "tom_shorthand";

	public static final String STATUS_SENT = "Sent";
	public static final String STATUS_DELIVERED = "Delivered";
	public static final String STATUS_FAILED = "Failed";
	public static final String STATUS_UNREAD = "Unread";
	public static final String STATUS_READ = "Read";

	public static final String TYPE_OUTBOX = "Outbox";
	public static final String TYPE_INBOX = "Inbox";

	private static String createMessageSql;
	private static String createTaskSql;
	private static String createShortHandSql;
	private static String createAbstractThreadTable;

	public InitDatabase(Context context) {

		super(context, DATABASE_NAME, null, 1);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create sql queries to create tables into the database

		createMessageSql = "CREATE TABLE IF NOT EXISTS tom_messages " + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + RECIEPIENT
				+ " TEXT," + MESSAGE_CONTENT + " TEXT," + MESSAGE_TIME
				+ " TEXT," + MESSAGE_DATE + " TEXT," + MESSAGE_STATUS
				+ " TEXT," + MESSAGE_TYPE + " TEXT)";

		createAbstractThreadTable = "CREATE TABLE IF NOT EXISTS tom_abstract_thread"
				+ "( "
				+ _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ RECIEPIENT
				+ " TEXT,"
				+ MESSAGE_CONTENT
				+ " TEXT,"
				+ MESSAGE_DATE
				+ " TEXT,"
				+ MESSAGE_ID
				+ " TEXT,"
				+ MESSAGE_TIME
				+ " TEXT,"
				+ MESSAGE_STATUS
				+ " TEXT,"
				+ MESSAGE_COUNT + " INTEGER)";

		createTaskSql = "CREATE TABLE IF NOT EXISTS tom_tasks (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + RECIEPIENT + " TEXT,"
				+ MESSAGE_CONTENT + " TEXT," + DUE_TIME + " TEXT," + DUE_DATE
				+ " TEXT)";

		createShortHandSql = "CREATE TABLE IF NOT EXISTS " + TABLE_SHORTHAND
				+ "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ LONG_STRING + " TEXT," + SHORT_STRING + " TEXT)";
		// execute Query against database
		db.execSQL(createMessageSql);
		db.execSQL(createAbstractThreadTable);
		db.execSQL(createShortHandSql);
		db.execSQL(createTaskSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ABSTRACT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHORTHAND);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
		onCreate(db);

	}
}
