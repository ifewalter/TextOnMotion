package com.ifewalter.android.textonmotion;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;
import com.ifewalter.android.textonmotion.databaseparoles.SelectData;

public class Search extends Activity {

	private static ImageButton searchButton;
	private static EditText searchQuery;
	private static ListView searchResults;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		initComponents();

		try {
			Intent intent = getIntent();
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				String query = intent.getStringExtra(SearchManager.QUERY);
				getSearchResults(query);
			}
		} catch (Exception ex) {
		}

	}

	private void initComponents() {

		searchQuery = (EditText) findViewById(R.id.search_query);

		searchButton = (ImageButton) findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.gc();
				getSearchResults(searchQuery.getText().toString());
			}
		});

	}

	private void getSearchResults(String query) {

		searchResults = (ListView) findViewById(R.id.search_results);

		String[] items = {InitDatabase.RECIEPIENT, InitDatabase.MESSAGE_CONTENT};
		Cursor cursor = SelectData
				.searchContent(getApplicationContext(), query);

		ListAdapter la = new SimpleCursorAdapter(this, R.layout.search_item,
				cursor, items, new int[]{R.id.search_item_receipient,
						R.id.search_item_message_content});
		searchResults.setAdapter(la);
		// cursor.close();

	}
}
