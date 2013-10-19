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
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.ifewalter.android.textonmotion.persistence.InitDatabase;
import com.ifewalter.android.textonmotion.persistence.SelectData;
import com.ifewalter.android.textonmotion.location.UserLocation;
import com.ifewalter.android.textonmotion.sendMessage.SendSMS;
import com.ifewalter.android.textonmotion.speechengine.VoiceSpeechEngine;

public class Compose extends Activity {

	private Button buttonClick;
	private static EditText editText;
	private TextView textCharacterCounter;
	public ProgressDialog dialog1;
	private CameraPreview mPreview;
	private CameraPreview270 mPreview270;
	private CameraPreview360 mPreview360;
	private CameraPreview180 mPreview180;
	private FrameLayout cameraHolder;

	private static final int CONTACT_SELECT_REQUEST_CODE = 1000;
	private static final int KEYBOARD_SELECT = 1002;
	private static final int ACTION_SEND = 0;
	private static final int ACTION_CONTACT = 1;
	private static final int ACTION_LOCATION = 2;
	// private static final int ACTION_SCHEDULE = 3;
	private static final int ACTION_SHORTEN = 3;
	private static final int ACTION_TRANSLATE = 4;
	private static final int ACTION_SWITCH_VOICE = 5;
	private static final int ACTION_HELP = 6;

	private TextView contactCounter;

	private static final String[] items = {"Send", "Add Contact",
			"Add Location", "Shorten Message", "Translate", "Interactive Mode",
			"Help"};

	public static String RECEIPIENT_LIST_PREFERENCES = "Receipients";
	private static String translatedText;
	private static boolean isContactSet = false;
	private static String contactList = null;

	private static Intent intent;
	private SharedPreferences settings;

	private void setContactFromIntent() {

		try {
			intent = getIntent();
			contactList = intent.getExtras().getString(InitDatabase.RECIEPIENT);
			if (contactList != null) {
				isContactSet = true;
			} else {
				isContactSet = false;
			}
		} catch (Exception ex) {

		}
	}

	private void appendSignature() {
		settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		boolean appendSignature = settings.getBoolean("signature_preference",
				false);
		if (appendSignature != false) {
			String signatureValue = "\n" + settings.getString(
					"signature_value_preference", "");
			editText.append(signatureValue);
		}
	}

	private void startKeyboardInstallTutorial() {

		settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		boolean tutorialDone = settings.getBoolean(
				"transperent_keyboard_tutorial", false);
		if (tutorialDone == false) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Compose.this);
			builder.setTitle("Translucent Keyboard");
			builder.setIcon(R.drawable.icon);
			builder.setMessage("Text on motion provides an even better keyboard than you have,"
					+ " with improved transperency, among other features. Try it now?");

			builder.setCancelable(true);
			builder.setPositiveButton("Yes, Please",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(
									android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);

							startActivityForResult(intent, KEYBOARD_SELECT);
							Toast.makeText(Compose.this,
									"Simply Enable \"TextOnMotion\" KeyBoard",
									Toast.LENGTH_LONG).show();

						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		settings = getSharedPreferences("contact_to_send", 0);
		@SuppressWarnings("unused")
		String tempCont = settings.getString("contact_to_send_list", "nothing");
		Editor editor = settings.edit();
		editor.remove("contact_to_send_list");
		editor.remove("message_to_send");
		editor.commit();

		setCameraOrientation();

		setContactFromIntent();

		startKeyboardInstallTutorial();

		contactCounter = (TextView) findViewById(R.id.compose_contact_added);

		textCharacterCounter = (TextView) findViewById(R.id.compose_text_length);

		editText = (EditText) findViewById(R.id.editText);
		
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
					textCharacterCounter.setText(String.valueOf(arg0.length())
							+ "/" + String.valueOf((arg0.length() / 160) + 1));
				} catch (Exception ex) {
				}

			}
		});

		buttonClick = (Button) findViewById(R.id.sendButton);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startshowMenuDialog();
			}
		});
		//append signature to the end of the message
		appendSignature();

	}
	final Handler showMenuDialogHandler = new Handler();

	// Create runnable for posting
	final Runnable showMenuDialogUpdateResults = new Runnable() {
		public void run() {
			showMenuDialog();
		}
	};

	protected void startshowMenuDialog() {

		Translate.setHttpReferrer("http://www.ifewalter.com");
		Translate
				.setKey("ABQIAAAAXTQxEcp4wcc19ZmrpUvQlRRpq4QeGTH-XAqPe3gaCWcysLnz9xSbEr7dt8KPoEsyq_JafmNTgrDZJA");

		Thread showMenuDialogThread = new Thread() {
			public void run() {
				// mResults = doSomethingExpensive();

				showMenuDialogHandler.post(showMenuDialogUpdateResults);
			}
		};
		showMenuDialogThread.start();
	}

	private void switchToDrivingMode() {
		Intent intent = new Intent(getApplicationContext(),
				VoiceSpeechEngine.class);
		startActivity(intent);
		finish();
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
		Toast.makeText(Compose.this, "Location", Toast.LENGTH_SHORT);
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
		Toast.makeText(getApplicationContext(), "Previous Contacts cleared",
				Toast.LENGTH_SHORT).show();
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

	private void showMenuDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Compose.this);
		builder.setTitle("Message Options");
		builder.setIcon(R.drawable.icon);
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case ACTION_SEND : {
						fireSendSMSService();
						break;
					}
					case ACTION_CONTACT : {
						addContact();
						break;
					}
					case ACTION_LOCATION : {
						startLongAddingLocation();
						break;
					}
					case ACTION_SHORTEN : {
						shortenMessage();
						break;
					}
					case ACTION_HELP : {
						showHelp();
						break;
					}
					case ACTION_SWITCH_VOICE : {
						switchToDrivingMode();
						break;
					}
					case ACTION_TRANSLATE : {

						showTranslateOptions();
						break;
					}
					default : {
						break;
					}
				}
				System.gc();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}
	private void showHelp() {
		intent = new Intent(getApplicationContext(), About.class);
		startActivity(intent);
	}

	private void fireSendSMSService() {
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

	private void setCameraOrientation() {
		mPreview = new CameraPreview(this);
		mPreview270 = new CameraPreview270(this);
		mPreview360 = new CameraPreview360(this);
		mPreview180 = new CameraPreview180(this);

		cameraHolder = ((FrameLayout) findViewById(R.id.preview));

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		int orientation = display.getRotation();

		switch (orientation) {

			// When phone straight in protrait
			case Surface.ROTATION_0 : {
				cameraHolder.removeAllViews();
				cameraHolder.addView(mPreview);
				break;
			}
				// not sure/probably upside down
			case Surface.ROTATION_180 : {
				cameraHolder.removeAllViews();
				cameraHolder.addView(mPreview270);
				break;
			}
				// when phone on right side

			case Surface.ROTATION_270 : {
				cameraHolder.removeAllViews();
				cameraHolder.addView(mPreview180);
				break;

			}
				// when phone on left side
			case Surface.ROTATION_90 : {
				cameraHolder.removeAllViews();
				cameraHolder.addView(mPreview360);
				break;
			}

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CONTACT_SELECT_REQUEST_CODE : {

				if (resultCode == RESULT_CANCELED) {
				} else {
					contactList = data.getAction();
					String[] contactListCounter = contactList.split(";");
					contactCounter.setText(String
							.valueOf(contactListCounter.length));

					isContactSet = true;
					break;
				}
			}
			case KEYBOARD_SELECT : {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						Compose.this);

				builder.setTitle("Translucent Keyboard (Final Step)");
				builder.setIcon(R.drawable.icon);
				builder.setMessage("If you chose \"TextOnMotion\" and agreed to the terms,"
						+ " now tap the settings icon on your keyboard.\n "
						+ "Or touch and hold the compose box above, then select \"Input Method\"."
						+ "\n Choose \"TextOnMotion\". \n Press the return key on your phone to close this.");

				settings = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				AlertDialog alert = builder.create();
				if (settings.getBoolean("transperent_keyboard_tutorial", false) == false) {
					alert.show();
				}

				Editor editor = settings.edit();
				editor.putBoolean("transperent_keyboard_tutorial", true);
				editor.commit();
				builder.setCancelable(true);
				break;
			}
			default : {
				break;
			}

		}
	}

	private void showTranslateOptions() {

		String[] translateLanguages = {"Afrikaans", "Albanian", "Amharic",
				"Arabic", "Benghali", "Bulgarian", "English", "French"};
		AlertDialog.Builder builder = new AlertDialog.Builder(Compose.this);
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

						editText.setText(translatedText);
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
		//menu.add(Menu.NONE, TRANSLATE_ID, Menu.NONE, "Translate");
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
			case DRIVING_MODE_ID : {
				switchToDrivingMode();
				return (true);
			}

		}
		return false;
	}
}