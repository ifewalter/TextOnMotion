package com.ifewalter.android.textonmotion;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.ifewalter.android.textonmotion.persistence.InitDatabase;
import com.ifewalter.android.textonmotion.persistence.SelectData;
import com.ifewalter.android.textonmotion.location.UserLocation;
import com.ifewalter.android.textonmotion.sendMessage.SendSMS;
import com.ifewalter.android.textonmotion.speechengine.VoiceSpeechEngine;

public class PlainCompose extends Activity {

	private static Button sendButton;
	private static Button addContactsButton;
	private static EditText editText;
	private static EditText contactEdit;
	private static TextView textLengthCounter;
	private static Intent intent;
	private static String translatedText, contactList;
	private static boolean isContactSet;
	private static final int CONTACT_SELECT_REQUEST_CODE = 1001;

	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.plain_compose);
		startInitComponents();
		setContactFromIntent();

	
	}

	final Handler initComponentsHandler = new Handler();

	// Create runnable for posting
	final Runnable initComponentsResults = new Runnable() {
		public void run() {
			initComponents();
		}
	};

	protected void startInitComponents() {

		Thread initComponenetsThread = new Thread() {
			public void run() {
				// mResults = doSomethingExpensive();

				initComponentsHandler.post(initComponentsResults);
			}
		};

		initComponenetsThread.start();
	}

	//append signature
	private void appendSignature() {
		settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		boolean appendSignature = settings.getBoolean("signature_preference",
				false);
		if (appendSignature != false) {
			String signatureValue = "\n"
					+ settings.getString("signature_value_preference", "");
			editText.append(signatureValue);
		}
	}

	private void initComponents() {

		sendButton = (Button) findViewById(R.id.plain_compose_send_message_button);
		addContactsButton = (Button) findViewById(R.id.plain_compose_add_contact);
		editText = (EditText) findViewById(R.id.plain_compose_compose_box);
		textLengthCounter = (TextView) findViewById(R.id.plain_compose_text_length);
		contactEdit = (EditText) findViewById(R.id.plain_compose_contact_edit_text);
		
		// append signature
		appendSignature();
		
		try {
			contactEdit.setText(intent.getExtras().getString(
					InitDatabase.RECIEPIENT));
		} catch (Exception ex) {
		}
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fireSendSMSService();
			}
		});

		addContactsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLongAddingContact();
			}
		});

		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

				try {
					textLengthCounter.setText(String.valueOf(arg0.length())
							+ "/" + String.valueOf((arg0.length() / 160) + 1));
				} catch (Exception ex) {
				}

			}
		});

	}

	private void switchToDrivingMode() {
		Intent intent = new Intent(getApplicationContext(),
				VoiceSpeechEngine.class);
		startActivity(intent);
		finish();
	}

	final Handler addContactHandler = new Handler();

	// Create runnable for posting
	final Runnable addContactResults = new Runnable() {
		public void run() {
			addContact();
		}
	};

	protected void startLongAddingContact() {

		Thread addContactThread = new Thread() {
			public void run() {
				shortenedUrl = "hello world";
				addContactHandler.post(addContactResults);
			}
		};
		addContactThread.start();
	}

	private static String shortenedUrl;
	final Handler addLocationHandler = new Handler();

	// Create runnable for posting
	final Runnable addLocationUpdateResults = new Runnable() {
		public void run() {
			addLocation();
		}
	};

	protected void startLongAddingLocation() {

		Thread addLocationThread = new Thread() {
			public void run() {
				// mResults = doSomethingExpensive();

				shortenedUrl = "hello world";
				addLocationHandler.post(addLocationUpdateResults);
			}
		};
		addLocationThread.start();
	}

	private void addLocation() {
		Toast.makeText(PlainCompose.this, "Location", Toast.LENGTH_SHORT);
		// editText.append(shortenedUrl);
		settings = getSharedPreferences(UserLocation.SAVED_LOCATION_NAME, 0);
		shortenedUrl = settings.getString(UserLocation.SAVED_LOCATION_URL,
				"http://maps.google.com");
		float accuracyNotification = (settings.getFloat(
				UserLocation.ACCURACY_KEY, 0));

		Toast.makeText(
				getApplicationContext(),
				"Location added with " + String.valueOf(accuracyNotification)
						+ " Meters Accuracy", Toast.LENGTH_SHORT).show();
		editText.append(shortenedUrl);

	}

	private void addContact() {
		intent = new Intent(getApplicationContext(), SelectPhoneContact.class);
		startActivityForResult(intent, CONTACT_SELECT_REQUEST_CODE);
	}
	private void shortenMessage() {

		ProgressDialog shortenProgress = new ProgressDialog(this);
		shortenProgress.setMessage("Abbreviating Message...Please wait.");
		shortenProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		shortenProgress.setCancelable(false);
		shortenProgress.show();

		try {
			Cursor cursor = SelectData
					.selectAllShortString(getApplicationContext());
			String tempString = editText.getText().toString();
			if (cursor != null) {
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false) {
					tempString = tempString
							.replace(
									cursor.getString(cursor
											.getColumnIndex(InitDatabase.LONG_STRING)),
									cursor.getString(cursor
											.getColumnIndex(InitDatabase.SHORT_STRING)));
					cursor.moveToNext();
				}
				shortenProgress.dismiss();
				editText.setText(tempString);
			}
		} catch (Exception ex) {

		}

	}

	private void setContactFromIntent() {

		try {
			intent = getIntent();
			contactList = intent.getExtras().getString(InitDatabase.RECIEPIENT);
			if (contactList != null) {
				isContactSet = true;
				contactEdit.setText(contactList);
			} else {
				isContactSet = false;
			}
		} catch (Exception ex) {

		}
		
		
	}

	private void fireSendSMSService() {
		if (!contactEdit.getText().toString().equals(null)) {
			contactList = contactEdit.getText().toString();
			isContactSet = true;
		}
		if (isContactSet == true) {
			intent = new Intent(getApplicationContext(), SendSMS.class);
			// intent.putExtra(InitDatabase.RECIEPIENT, contactList.toString());
			// intent.putExtra(InitDatabase.MESSAGE_CONTENT, editText.getText()
			// .toString());

			settings = getSharedPreferences("contact_to_send", 0);
			Editor editor = settings.edit();
			editor.putString("contact_to_send_list", contactList);
			editor.putString("message_to_send", editText.getText().toString());
			editor.commit();

			startService(intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(),
					"Error Sending Message; Please add Contacts",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CONTACT_SELECT_REQUEST_CODE : {

				if (resultCode == RESULT_CANCELED) {
				} else {
					contactList = data.getAction();

					contactEdit.setText(contactList);

					isContactSet = true;
					break;
				}
			}

			default : {
				break;
			}

		}
	}

	private void showTranslateOptions() {

		Translate.setHttpReferrer("http://www.ifewalter.com");
		Translate
				.setKey("ABQIAAAAXTQxEcp4wcc19ZmrpUvQlRRpq4QeGTH-XAqPe3gaCWcysLnz9xSbEr7dt8KPoEsyq_JafmNTgrDZJA");

		String[] translateLanguages = {"Afrikaans", "Albanian", "Amharic",
				"Arabic", "Benghali", "Bulgarian", "English", "French"};
		AlertDialog.Builder builder = new AlertDialog.Builder(PlainCompose.this);
		builder.setTitle("Translate Message");
		builder.setIcon(R.drawable.icon);
		builder.setCancelable(true);
		builder.setItems(translateLanguages,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
							case 0 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.AFRIKAANS);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}

								break;
							}

							case 1 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.ALBANIAN);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							case 2 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.AMHARIC);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							case 3 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.ARABIC);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							case 4 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.BENGALI);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							case 5 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.BULGARIAN);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							case 6 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.ENGLISH);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							case 7 : {
								try {
									translatedText = Translate.execute(editText
											.getText().toString(),
											Language.AUTO_DETECT,
											Language.FRENCH);
								} catch (Exception e) {

									translatedText = "Translation Error. Please try again.";
									// translatedText = e.toString();
								}
								break;
							}
							default : {
								break;
							}
						}

						if (!(translatedText
								.equals("Translation Error. Please try again.")))

						{
							editText.setText(translatedText);
						} else {
							Toast.makeText(getApplicationContext(),
									translatedText, Toast.LENGTH_LONG).show();
						}
						System.gc();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			Intent intent = new Intent(getApplicationContext(), Search.class);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public static final int SEND_ID = Menu.FIRST + 1;
	public static final int ADD_CONTACT_ID = Menu.FIRST + 2;
	public static final int ADD_LOCATION_ID = Menu.FIRST + 3;
	public static final int SHORTEN_ID = Menu.FIRST + 4;
	public static final int TRANSLATE_ID = Menu.FIRST + 5;
	public static final int SETTINGS_ID = Menu.FIRST + 6;
	public static final int HELP_ID = Menu.FIRST + 7;
	public static final int DRIVING_MODE_ID = Menu.FIRST + 8;
	private static final int WALKING_MODE_ID = Menu.FIRST + 9;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);
		return (super.onCreateOptionsMenu(menu));
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}
	private void populateMenu(Menu menu) {

		menu.add(Menu.NONE, SEND_ID, Menu.NONE, "Send");
		menu.add(Menu.NONE, ADD_CONTACT_ID, Menu.NONE, "Add Contact");
		menu.add(Menu.NONE, ADD_LOCATION_ID, Menu.NONE, "Add Location");
		menu.add(Menu.NONE, SHORTEN_ID, Menu.NONE, "Shorten");
	//	menu.add(Menu.NONE, TRANSLATE_ID, Menu.NONE, "Translate");
		menu.add(Menu.NONE, WALKING_MODE_ID, Menu.NONE, "Walking Mode");
		menu.add(Menu.NONE, DRIVING_MODE_ID, Menu.NONE, "Interactive Mode");
		menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, "Settings");
		menu.add(Menu.NONE, HELP_ID, Menu.NONE, "About");
	}
	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
			case SETTINGS_ID : {
				Intent intent = new Intent(getApplicationContext(),
						TomSettings.class);
				startActivity(intent);
				return (true);
			}
			case HELP_ID : {
				Intent intent = new Intent(getApplicationContext(), About.class);
				startActivity(intent);
				return (true);
			}
			case SEND_ID : {
				fireSendSMSService();
				return (true);
			}
			case ADD_CONTACT_ID : {
				addContact();
				return (true);
			}
			case ADD_LOCATION_ID : {
				startLongAddingLocation();
				return (true);
			}
			case SHORTEN_ID : {
				shortenMessage();
				return (true);
			}
			case TRANSLATE_ID : {
				showTranslateOptions();
				return (true);
			}
			case WALKING_MODE_ID : {
				Intent intent = new Intent(getApplicationContext(),
						Compose.class);
				startActivity(intent);
				return (true);
			}
			case DRIVING_MODE_ID : {
				switchToDrivingMode();
				return (true);
			}

		}
		return false;
	}

}
