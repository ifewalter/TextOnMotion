package com.ifewalter.android.textonmotion;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ContactListCursorAdapter extends SimpleCursorAdapter {

	int[] items;
	private Context context;
	private int layout;
	private Cursor cursor;
	public static final String PHONE_NUMBERS = "phone_numbers";
	public SharedPreferences phoneNumbers;
	public ArrayList<String> list = new ArrayList<String>();
	public ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

	public ContactListCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		this.context = context;
		this.layout = layout;
		this.cursor = cursor;
		this.items = to;
		phoneNumbers = context.getSharedPreferences(PHONE_NUMBERS, 0);

		for (int i = 0; i < this.getCount(); i++) {
			itemChecked.add(i, false);
		}
	}

	@Override
	public View getView(final int pos, View inView, ViewGroup parent) {
		View v = inView;

		if (inView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.contact_list, null);
		}
		this.cursor.moveToPosition(pos);
		String phoneNumberTag = this.cursor.getString(this.cursor
				.getColumnIndex(Phone.NUMBER));
		final CheckBox cBox = (CheckBox) v.findViewById(R.id.check1);
		TextView contactName = (TextView) v.findViewById(R.id.contact_name);
		TextView contactNumber = (TextView) v.findViewById(R.id.contact_number);
		ImageView contactBadge = (ImageView) v.findViewById(R.id.contact_badge);

		String tempName = this.cursor.getString(this.cursor
				.getColumnIndex(Phone.DISPLAY_NAME));
		String tempNumber = this.cursor.getString(this.cursor
				.getColumnIndex(Phone.NUMBER));

		contactName.setText(tempName);

		contactNumber.setText(tempNumber);

		try {
			ContentResolver cr = context.getContentResolver();
			Cursor cursor2 = this.cursor;
			String contactId = cursor2.getString(cursor2
					.getColumnIndex(Phone.CONTACT_ID));
			Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
					Long.parseLong(contactId));

			Uri photo = Uri.withAppendedPath(uri,
					Contacts.Photo.CONTENT_DIRECTORY);

			contactBadge.setImageURI(photo);

		} catch (Exception ex) {
		}

		cBox.setTag(tempNumber);

		cBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CheckBox checkBox = (CheckBox) v.findViewById(R.id.check1);
				if (checkBox.isChecked()) {
					itemChecked.set(pos, true);
					list.add(cBox.getTag().toString());
				} else if (!checkBox.isChecked()) {
					itemChecked.set(pos, false);
					list.remove(cBox.getTag().toString());
				}
			}
		});
		cBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				CheckBox checkBox1 = (CheckBox) arg0;
				if (checkBox1.isChecked()) {
					itemChecked.set(pos, true);
				} else if (!checkBox1.isChecked()) {
					itemChecked.set(pos, false);
				}
			}
		});
		cBox.setChecked(itemChecked.get(pos));
		return (v);
	}

}
