package com.ifewalter.android.textonmotion;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ifewalter.android.textonmotion.databaseparoles.DeleteData;
import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;
import com.ifewalter.android.textonmotion.databaseparoles.InsertData;
import com.ifewalter.android.textonmotion.databaseparoles.SelectData;

public class LongShortView extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTitle("Word Abbreviation Dictionary");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.long_short_list);
		initComponents();

	}
	private static AlertDialog newCorrectionDialog;
	private static Cursor c;
	private static String[] from = {InitDatabase.LONG_STRING,
			InitDatabase.SHORT_STRING};
	public static final int DELETE_ID = Menu.FIRST + 1;
	ListView lv;
	ListAdapter la;
	public void initComponents() {
		try {
			// SelectData selectData = new SelectData();

			@SuppressWarnings("unused")
			String[] items = {InitDatabase.LONG_STRING,
					InitDatabase.SHORT_STRING, InitDatabase._ID};
			c = SelectData.selectAllShortString(this);
			lv = (ListView) findViewById(R.id.long_short_list_list_view);

			int[] to = {R.id.long_short_text_1, R.id.long_short_text_2};

			la = new LongShortViewCursorAdapter(getApplicationContext(),
					R.layout.long_short_item, c, from, to);
			lv.setAdapter(la);

			registerForContextMenu(lv);

			Button addButton = (Button) findViewById(R.id.long_short_list_add);
			addButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					AlertDialog.Builder alert = new AlertDialog.Builder(
							LongShortView.this);

					alert.setTitle("New word");
					alert.setMessage("Define new words");

					// Set an EditText view to get user input
					final EditText longText = new EditText(LongShortView.this);
					longText.setHint("Enter long word");
					longText.setSingleLine(true);

					final EditText shortText = new EditText(LongShortView.this);
					shortText.setHint("Enter short word");
					shortText.setSingleLine(true);
					

					LinearLayout ll = new LinearLayout(LongShortView.this);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.addView(longText);
					ll.addView(shortText);

					alert.setView(ll);

					alert.setPositiveButton("Done",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String tempLongString = longText.getText()
											.toString();
									String tempShortString = shortText
											.getText().toString();
									tempLongString = tempLongString.replace(
											" ", "");
									tempShortString = tempLongString.replace(
											" ", "");
									if (tempLongString.equals("")
											|| tempShortString.equals("")) {
										final AlertDialog.Builder correctionDialog = new AlertDialog.Builder(
												LongShortView.this);
										correctionDialog
												.setMessage(
														"Both Fields must not be empty. "
																+ "Please exit if you wish to cancel intstead")
												.setTitle("invalid Entry");
										correctionDialog
												.setPositiveButton(
														"Ok",
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {
																newCorrectionDialog
																		.dismiss();

															}
														});
										newCorrectionDialog = correctionDialog
												.create();
										newCorrectionDialog.show();
									} else {
									}

									// InsertData insertData = new InsertData();
									InsertData.insertLongShort(
											getApplicationContext(), longText
													.getText().toString(),
											shortText.getText().toString());
									initComponents();
								}
							});

					alert.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Canceled.
								}
							});

					alert.show();

				}

			});

			Button syncButton = (Button) findViewById(R.id.long_short_list_sync_button);
			syncButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new SyncDatabase().execute();
				}
			});

		} catch (Exception ex) {
		}

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				selectedID = arg1.getTag().toString();

				return false;
			}
		});
	}
	private String selectedID;
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		// selectedID = v.getTag().toString();

		populateMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onContextItemSelected(item));

	}

	private void populateMenu(Menu menu) {
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete Message");

	}

	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {

			case DELETE_ID : {
				// DeleteData delete = new DeleteData();
				DeleteData.deleteLongShort(LongShortView.this, selectedID);

				try {
					Toast.makeText(LongShortView.this, "Entry Deleted",
							Toast.LENGTH_SHORT).show();
				} catch (Exception ex) {
				}
				finish();
				Intent intent = new Intent(LongShortView.this,
						LongShortView.class);
				startActivity(intent);
				return (true);
			}
		}

		// return false;
		return true;
	}

	private class SyncDatabase extends AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				LongShortView.this);
		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Synchronizing....");
			this.dialog.show();
		}
		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			try {

				// sync up
				HttpClient client = new DefaultHttpClient();
				String postURL = "http://www.ifewalter.com/textonmotion/auth/sync.php";
				HttpPost post = new HttpPost(postURL);
				SharedPreferences settings = getSharedPreferences("auth", 0);
				Cursor cursor = SelectData
						.selectAllShortString(getApplicationContext());
				if ((cursor.getCount() > 0) || (cursor != null)) {

					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						List<NameValuePair> params = new ArrayList<NameValuePair>();

						params.add(new BasicNameValuePair("auth", settings
								.getString("token", "0")));
						params.add(new BasicNameValuePair("action", "up"));
						params.add(new BasicNameValuePair(
								"long",
								cursor.getString(cursor
										.getColumnIndex(InitDatabase.LONG_STRING))));
						params.add(new BasicNameValuePair(
								"short",
								cursor.getString(cursor
										.getColumnIndex(InitDatabase.SHORT_STRING))));

						UrlEncodedFormEntity ent = new UrlEncodedFormEntity(
								params, HTTP.UTF_8);
						post.setEntity(ent);
						// HttpResponse responsePOST =
						client.execute(post);
						// HttpEntity resEntity = responsePOST.getEntity();
						System.gc();;
						cursor.moveToNext();
					}
				}

				// Down sync
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
				params2.add(new BasicNameValuePair("auth", settings.getString(
						"token", "0")));
				params2.add(new BasicNameValuePair("action", "down"));

				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params2,
						HTTP.UTF_8);
				post.setEntity(ent);
				HttpResponse responsePOST = client.execute(post);
				HttpEntity resEntity = responsePOST.getEntity();
				// String jsonresponse = resEntity.getContent().toString();
				if (resEntity != null) {
					// Log.i("RESPONSE", EntityUtils.toString(resEntity));

					JSONObject newJsonObject = new JSONObject(
							EntityUtils.toString(resEntity));

					JSONArray wordArray = newJsonObject.getJSONArray("words");

					int wordArraySize = wordArray.length();
					// InsertData insert = new InsertData();

					// iterate over json and add all stuffs to the database
					for (int i = 0; i < wordArraySize; i++) {

						JSONObject anotherJsonObject = wordArray
								.getJSONObject(i);
						String longWord = anotherJsonObject.getString("long");
						String shortWord = anotherJsonObject.getString("short");

						InsertData.insertLongShort(getApplicationContext(),
								longWord, shortWord);
					}
					return true;
				}
			} catch (Exception ex) {
				return false;
			}
			return true;

		}
		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(LongShortView.this, "Success", Toast.LENGTH_LONG)
						.show();
				Intent intent = new Intent(getApplicationContext(),
						LongShortView.class);
				startActivity(intent);
				finish();
				// finish();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LongShortView.this);
				builder.setTitle("Sync. Error");
				builder.setIcon(R.drawable.icon);
				builder.setCancelable(true);
				builder.setMessage("Something went terribly wrong. PLease chekc you internet connection");
				builder.setPositiveButton("Close",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();

							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}

		}
	}
}
