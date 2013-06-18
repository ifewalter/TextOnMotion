package com.ifewalter.android.textonmotion.databaseparoles;

import android.content.Context;
import android.database.Cursor;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.MatchType;

public class CheckPhoneNumberMatch {

	/**
	 * <b>searchForMatch</b> - Compares number in database and number to be
	 * entered for match. Useful in grouping threads
	 * 
	 * @param context
	 * @param cursor
	 * @param input
	 * @return void
	 */
	public static String searchForMatch(Context context, Cursor cursor,
			String input) {

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String tempString = null;
		if (cursor != null && !cursor.isLast()) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {

				tempString = cursor.getString(cursor
						.getColumnIndex(InitDatabase.RECIEPIENT));
				if (MatchType.NSN_MATCH.equals(phoneUtil.isNumberMatch(
						tempString, input))) {
					// Toast.makeText(context, "True",
					// Toast.LENGTH_SHORT).show();
					return tempString;
				}
				cursor.moveToNext();

			}
		}
		return input;
	}
}
