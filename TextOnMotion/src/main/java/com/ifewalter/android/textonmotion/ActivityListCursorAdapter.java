package com.ifewalter.android.textonmotion;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ifewalter.android.textonmotion.persistence.InitDatabase;

public class ActivityListCursorAdapter extends SimpleCursorAdapter {

	int[] items;
	private static Context context;
	@SuppressWarnings("unused")
	private static int layout;
	private static Cursor cursor;

	public ActivityListCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		ActivityListCursorAdapter.context = context;
		ActivityListCursorAdapter.layout = layout;
		ActivityListCursorAdapter.cursor = cursor;
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

		ActivityListCursorAdapter.cursor.moveToPosition(pos);

		TextView messageContent = (TextView) v
				.findViewById(R.id.message_view_item_message_content);
		TextView messageDateStatus = (TextView) v
				.findViewById(R.id.message_view_item_date);

		String messageDateStatusTemp = ActivityListCursorAdapter.cursor
				.getString(ActivityListCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_STATUS))
				+ "; "
				+ ActivityListCursorAdapter.cursor
						.getString(ActivityListCursorAdapter.cursor
								.getColumnIndex(InitDatabase.MESSAGE_TIME))
				+ "; "
				+ ActivityListCursorAdapter.cursor
						.getString(ActivityListCursorAdapter.cursor
								.getColumnIndex(InitDatabase.MESSAGE_DATE));

		v.setTag(ActivityListCursorAdapter.cursor
				.getString(ActivityListCursorAdapter.cursor
						.getColumnIndex(InitDatabase._ID)));
		messageDateStatus.setText(messageDateStatusTemp);

		messageContent.setText(ActivityListCursorAdapter.cursor
				.getString(ActivityListCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_CONTENT)));

		return (v);
	}
}
