package com.ifewalter.android.textonmotion;

import com.ifewalter.android.textonmotion.R.drawable;
import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MessageViewCursorAdapter extends SimpleCursorAdapter {

	int[] items;
	private static Context context;
	private static int layout;
	private static Cursor cursor;
	private static Drawable inboxIcon, outboxIcon;

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

		ImageView messageInboxStatusIcon = (ImageView) v
				.findViewById((R.id.message_view_status_inbox_icon));
		ImageView messageOutboxStatusIcon = (ImageView) v
				.findViewById((R.id.message_view_status_outbox_icon));

		String mesageStatus = MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_TYPE));
		if (mesageStatus.equals(InitDatabase.TYPE_INBOX)) {
			messageInboxStatusIcon.setVisibility(View.VISIBLE);
			messageContent.setGravity(Gravity.LEFT);
			messageOutboxStatusIcon.setVisibility(View.GONE);
		} else {
			messageInboxStatusIcon.setVisibility(View.GONE);
			messageContent.setGravity(Gravity.RIGHT);
			messageOutboxStatusIcon.setVisibility(View.VISIBLE);
		}

		String messageDateStatusTemp = MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_TIME))
				+ "; "
				+ MessageViewCursorAdapter.cursor
						.getString(MessageViewCursorAdapter.cursor
								.getColumnIndex(InitDatabase.MESSAGE_DATE));

		v.setTag(MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase._ID)));

		messageDateStatus.setText(messageDateStatusTemp);

		messageContent.setText(MessageViewCursorAdapter.cursor
				.getString(MessageViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_CONTENT)));

		return (v);
	}
}
