package com.ifewalter.android.textonmotion;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ifewalter.android.textonmotion.persistence.InitDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageViewCursorAdapter extends SimpleCursorAdapter {

	int[] items;
	private static Context context;
	@SuppressWarnings("unused")
	private static int layout;
	private static Cursor cursor;

	public MessageViewCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		MessageViewCursorAdapter.context = context;
		MessageViewCursorAdapter.layout = layout;
		MessageViewCursorAdapter.cursor = cursor;
		this.items = to;

	}

	@Override
	public View getView(final int pos, View inView, ViewGroup parent) {
		View v = inView;

		if (inView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.message_view_item, null);
		}

		MessageViewCursorAdapter.cursor.moveToPosition(pos);

		TextView messageContent = (TextView) v
				.findViewById(R.id.message_view_item_message_content);
		TextView messageDateStatus = (TextView) v
				.findViewById(R.id.message_view_item_date);

		Resources res = context.getResources();

		String mesageStatus = MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_TYPE));
		if (mesageStatus.equals(InitDatabase.TYPE_INBOX)) {
			v.setBackgroundDrawable(res.getDrawable(R.drawable.you));
			messageDateStatus.setTextColor(Color.parseColor("#767676"));

		} else {
			v.setBackgroundDrawable(res.getDrawable(R.drawable.me1));
			messageDateStatus.setTextColor(Color.parseColor("#ffffff"));
		}

		String daysAgo = null;
		String savedDate = MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_DATE));

		Calendar newDate = Calendar.getInstance();
		Calendar oldDate = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String newDateVal = dateFormat.format(newDate.getTime()).toString();

		try {
			newDate.setTime(dateFormat.parse(newDateVal));
			oldDate.setTime(dateFormat.parse(savedDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long diff = newDate.getTimeInMillis() - oldDate.getTimeInMillis();

		long days = diff / (24 * 60 * 60 * 1000);

		if (days < 1) {

			daysAgo = "Today";
		} else if (days == 1) {
			daysAgo = "Yesterday";
		} else {
			daysAgo = savedDate;
		}

		String messageDateStatusTemp = MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_TIME))
				+ "; " + daysAgo;

		v.setTag(MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase._ID)));
		
		v.setTag(R.integer.resendNumberTag, MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor.getColumnIndex(InitDatabase.RECIEPIENT)));
		v.setTag(R.integer.resendMessageTag, MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor.getColumnIndex(InitDatabase.MESSAGE_CONTENT)));

		
		messageDateStatus.setText(messageDateStatusTemp);

		messageContent.setText(MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_CONTENT)));
		System.gc();
		return (v);
	}
}
