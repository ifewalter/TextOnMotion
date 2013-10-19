package com.ifewalter.android.textonmotion;

import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LongShortViewCursorAdapter extends SimpleCursorAdapter {

	int[] items;
	private static Context context;
	private static int layout;
	private static Cursor cursor;

	public LongShortViewCursorAdapter(Context context, int layout,
			Cursor cursor, String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		LongShortViewCursorAdapter.context = context;
		LongShortViewCursorAdapter.layout = layout;
		LongShortViewCursorAdapter.cursor = cursor;
		this.items = to;
	}

	@Override
	public View getView(final int pos, View inView, ViewGroup parent) {
		View v = inView;

		if (inView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.long_short_item, null);
		}

		LongShortViewCursorAdapter.cursor.moveToPosition(pos);

		TextView LongWordView = (TextView) v
				.findViewById(R.id.long_short_text_1);
		TextView shortWordView = (TextView) v
				.findViewById(R.id.long_short_text_2);

		String viewId = LongShortViewCursorAdapter.cursor
				.getString(LongShortViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase._ID));

		String longWord = LongShortViewCursorAdapter.cursor
				.getString(LongShortViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.LONG_STRING));
		String shortWord = LongShortViewCursorAdapter.cursor
				.getString(LongShortViewCursorAdapter.cursor
						.getColumnIndex(InitDatabase.SHORT_STRING));

		v.setTag(viewId);
		// v.setId(viewId);

		LongWordView.setText(longWord);
		shortWordView.setText(shortWord);

		return (v);
	}
}
