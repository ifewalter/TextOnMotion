package com.ifewalter.android.textonmotion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class SelectPhoneContact extends Activity {

	private static EditText input;
	private static ListView lv;
	private ContactListCursorAdapter adapter;
	public static final String PHONE_NUMBERS = "phone_numbers";
	public SharedPreferences phoneNumbers;
	private static String lit = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.select_phone_contact);
		// setContentView(setComponents());

		// setComponents();
		startLongAddingLocation();

	}

	final Handler addLocationHandler = new Handler();

	// Create runnable for posting
	final Runnable addLocationUpdateResults = new Runnable() {
		public void run() {
			setComponents();
		}
	};

	protected void startLongAddingLocation() {

		Thread t = new Thread() {
			public void run() {
				// mResults = doSomethingExpensive();

				// shortenedUrl = "hello world";
				addLocationHandler.post(addLocationUpdateResults);
			}
		};
		t.start();
	}

	private void setComponents() {

		lv = (ListView) findViewById(R.id.contactListView);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv.setFastScrollEnabled(true);
		lv.setFocusable(true);
		lv.setFocusableInTouchMode(true);
		lv.setSmoothScrollbarEnabled(true);
		Cursor c = getContentResolver().query(Phone.CONTENT_URI, null, null,
				null, "display_name ASC");
		startManagingCursor(c);

		String[] from = new String[]{Phone.DISPLAY_NAME, Phone.LOOKUP_KEY,
				Phone.DATA15, Phone._ID};
		int[] to = new int[]{R.id.contact_name, R.id.check1};
		adapter = new ContactListCursorAdapter(getApplicationContext(),
				R.layout.contact_list, c, from, to);

		lv.setAdapter(adapter);
		doneButton();
		manualEntryButton();
	}

	public void manualEntryButton() {
		Button manualEntryButton = (Button) findViewById(R.id.manualButton);
		manualEntryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						SelectPhoneContact.this);

				alert.setTitle("Receipient");
				alert.setMessage("Receipient Phone Number(s)");

				// Set an EditText view to get user input
				input = new EditText(SelectPhoneContact.this);
				input.setInputType(InputType.TYPE_CLASS_PHONE);
				input.setHint("Seperate multiple with wait (;)");
				alert.setView(input);

				alert.setPositiveButton("Done",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								lit = input.getText().toString();
								setResult(RESULT_OK,
										(new Intent().setAction(lit)));
								finish();
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

	}

	public void doneButton() {
		Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				ArrayList<String> list = adapter.list;

				if (list.size() > -1) {
					for (String eachListItem : list) {

						lit += eachListItem + ";";
					}
					setResult(RESULT_OK, (new Intent().setAction(lit)));
				}
				finish();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	// handle crash when back button is pressed
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setResult(RESULT_CANCELED, (new Intent().setAction(null)));
			finish();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			Intent intent = new Intent(getApplicationContext(), Search.class);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
