package com.ifewalter.android.textonmotion;

import com.ifewalter.android.textonmotion.R.layout;
import com.ifewalter.android.textonmotion.databaseparoles.DeleteData;
import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;
import com.ifewalter.android.textonmotion.databaseparoles.InsertData;
import com.ifewalter.android.textonmotion.databaseparoles.SelectData;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
			SelectData selectData = new SelectData();

			String[] items = {InitDatabase.LONG_STRING,
					InitDatabase.SHORT_STRING, InitDatabase._ID};
			c = selectData.selectAllShortString(this);
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
					// TODO Auto-generated method stub

					AlertDialog.Builder alert = new AlertDialog.Builder(
							LongShortView.this);

					alert.setTitle("New word");
					alert.setMessage("Define new words");

					// Set an EditText view to get user input
					final EditText longText = new EditText(LongShortView.this);
					longText.setHint("Enter long word");

					final EditText shortText = new EditText(LongShortView.this);
					shortText.setHint("Enter short word");

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

									InsertData insertData = new InsertData();
									insertData.insertLongShort(
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

		} catch (Exception ex) {
		}

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

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
				DeleteData delete = new DeleteData();
				delete.deleteLongShort(LongShortView.this, selectedID);

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
}
