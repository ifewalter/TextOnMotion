package com.ifewalter.android.textonmotion;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;

public class ConversationListCursorAdapter extends SimpleCursorAdapter {

	int[] items;
	private static Context context;
	private static int layout;
	private static Cursor cursor;

	public ConversationListCursorAdapter(Context context, int layout,
			Cursor cursor, String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		ConversationListCursorAdapter.context = context;
		ConversationListCursorAdapter.layout = layout;
		ConversationListCursorAdapter.cursor = cursor;
		this.items = to;
	}

	@Override
	public View getView(final int pos, View inView, ViewGroup parent) {
		View v = inView;

		if (inView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.message_list_item, null);
		}

		ConversationListCursorAdapter.cursor.moveToPosition(pos);

		ImageView contactBadge = (ImageView) v
				.findViewById(R.id.message_list_contact_badge);
		TextView contactNumber = (TextView) v
				.findViewById(R.id.message_list_phone);
		TextView messageSnippet = (TextView) v
				.findViewById(R.id.message_list_message_snippet);
		TextView messageStatus = (TextView) v
				.findViewById(R.id.message_list_item_date);

		String phoneNumberTemp = ConversationListCursorAdapter.cursor
				.getString(ConversationListCursorAdapter.cursor
						.getColumnIndex(InitDatabase.RECIEPIENT));

		v.setTag(phoneNumberTemp);

		String messageDateStatusTemp = ConversationListCursorAdapter.cursor
				.getString(ConversationListCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_TIME))
				+ "; "
				+ ConversationListCursorAdapter.cursor
						.getString(ConversationListCursorAdapter.cursor
								.getColumnIndex(InitDatabase.MESSAGE_DATE));

		String messageSnippetTemp = ConversationListCursorAdapter.cursor
				.getString(ConversationListCursorAdapter.cursor
						.getColumnIndex(InitDatabase.MESSAGE_CONTENT));
		messageSnippet.setText(messageSnippetTemp);
		try {
			messageSnippet.setText(messageSnippetTemp.substring(0, 30));
		} catch (Exception ex) {

		}
		// messageSnippet.setText(messageSnippetTemp);
		messageStatus.setText(messageDateStatusTemp);
		String contactName = null;

		try {
			ContentResolver localContentResolver = context.getContentResolver();
			Cursor contactLookupCursor = localContentResolver.query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
							Uri.encode(phoneNumberTemp)), new String[]{
							PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_ID,
							PhoneLookup._ID}, null, null, null);

			while (contactLookupCursor.moveToNext()) {
				contactName = contactLookupCursor.getString(contactLookupCursor
						.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
				String contactId = contactLookupCursor
						.getString(contactLookupCursor
								.getColumnIndexOrThrow(PhoneLookup._ID));

				Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
						Long.parseLong(contactId));

				Uri photo = Uri.withAppendedPath(uri,
						Contacts.Photo.CONTENT_DIRECTORY);

				contactBadge.setImageURI(photo);

				Resources res = context.getResources();
				Drawable d = res.getDrawable(R.drawable.contact);
				if (contactId == null) {
					contactBadge.setImageDrawable(d);
				}
			}
		} catch (Exception ex) {
		}

		if (contactName == null) {
			contactNumber
					.setText(phoneNumberTemp
							+ " ("
							+ ConversationListCursorAdapter.cursor
									.getString(ConversationListCursorAdapter.cursor
											.getColumnIndex(InitDatabase.MESSAGE_COUNT))
							+ ")");

			// v.setTag(0, phoneNumberTemp);
		} else {
			contactNumber
					.setText(contactName
							+ " ("
							+ ConversationListCursorAdapter.cursor
									.getString(ConversationListCursorAdapter.cursor
											.getColumnIndex(InitDatabase.MESSAGE_COUNT))
							+ ")");
			// v.setTag(1, contactName);
		}

		return (v);
	}

}
