package com.ifewalter.android.textonmotion.speechengine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.ifewalter.android.textonmotion.About;
import com.ifewalter.android.textonmotion.Compose;
import com.ifewalter.android.textonmotion.R;
import com.ifewalter.android.textonmotion.SelectPhoneContact;
import com.ifewalter.android.textonmotion.TomSettings;
import com.ifewalter.android.textonmotion.persistence.InitDatabase;
import com.ifewalter.android.textonmotion.persistence.SelectData;
import com.ifewalter.android.textonmotion.location.UserLocation;
import com.ifewalter.android.textonmotion.sendMessage.SendSMS;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoiceSpeechEngine extends Activity
		implements
			TextToSpeech.OnInitListener {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private static int messageState = 0;
	private static String spokenMessage;
	private static boolean isttsinitialised = false;
	@SuppressWarnings("unused")
	private static boolean isTTSAvailable = false;
	@SuppressWarnings("unused")
	private static boolean isSpeechAvailable = false;

	private static final int INITIAL_MESSAGE_STATE = 0;
	private static final int ASK_DONE = 1;
	private static final int ASK_ADD_CONTACT = 2;
	private static final int ASK_SEND_NOW = 3;
	@SuppressWarnings("unused")
	private static final int ZERO_MESSAGE_STATE = 4;

	private static EditText editText;
	private static EditText contactEdit;
	private static ImageButton addContactButton, sendMessageButton;
	private TextToSpeech mTts;
	private static boolean isContactSet = false;
	private static String contactList;
	private static final int CONTACT_SELECT_REQUEST_CODE = 1001;
	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.voice_speech_engine);

		setContactFromIntent();

		AlertDialog.Builder builder2 = new AlertDialog.Builder(
				VoiceSpeechEngine.this);
		builder2.setTitle("Interactive Mode");
		builder2.setIcon(R.drawable.icon);
		builder2.setMessage("Press Start when you are ready to start composing.");

		builder2.setPositiveButton("Start",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						mTts = new TextToSpeech(VoiceSpeechEngine.this,
								VoiceSpeechEngine.this);

						AudioManager audioManager = (AudioManager) VoiceSpeechEngine.this
								.getSystemService(AUDIO_SERVICE);
						audioManager
								.setStreamVolume(
										AudioManager.STREAM_MUSIC,
										audioManager
												.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
										AudioManager.FLAG_SHOW_UI);

					}
				});

		builder2.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						finish();

					}
				});

		AlertDialog alert3 = builder2.create();
		alert3.show();

		settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (settings.getBoolean("tutorial_two", false) != true)

		{
			AlertDialog.Builder builder1 = new AlertDialog.Builder(
					VoiceSpeechEngine.this);
			builder1.setTitle("Tutorials");
			builder1.setIcon(R.drawable.icon);
			builder1.setMessage("Learn how to use the interactive mode?");

			builder1.setCancelable(true);
			builder1.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							Editor editor = settings.edit();
							editor.putBoolean("tutorial_two", true);
							editor.commit();
							Intent intent = new Intent(getApplicationContext(),
									About.class);
							startActivity(intent);

						}
					});

			AlertDialog alert1 = builder1.create();
			alert1.show();
		}

		editText = (EditText) findViewById(R.id.voice_speech_compose_box);
		contactEdit = (EditText) findViewById(R.id.voice_speech_contact_edit_text);

		addContactButton = (ImageButton) findViewById(R.id.voice_speech_engine_add_contact);
		addContactButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				addContact();
			}
		});

		Button helpButton = (Button) findViewById(R.id.voice_speech_help);
		helpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(getApplicationContext(), About.class);
				startActivity(intent);
			}
		});

		sendMessageButton = (ImageButton) findViewById(R.id.voice_speech_send_message_button);
		sendMessageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				fireSendSMSService();
			}
		});

		Button restartButton = (Button) findViewById(R.id.voice_speech_restart);
		restartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				try {
					finish();
					Intent intent = new Intent(VoiceSpeechEngine.this,
							VoiceSpeechEngine.class);
					startActivity(intent);
				} catch (Exception ex) {
				}
			}
		});

		// append signature
		appendSignature();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isttsinitialised = false;
	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		try {
			finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	// Implements TextToSpeech.OnInitListener.
	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.

		if (isttsinitialised == false) {
			if (status == TextToSpeech.SUCCESS) {
				int result = mTts.setLanguage(Locale.getDefault());

				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				} else {

					// initialise and say welcome
					speakText("Please say your message.");
					Toast.makeText(getApplicationContext(),
							"Please say your message", Toast.LENGTH_SHORT)
							.show();
					threadWait();
					startVoiceRecognitionActivity();
				}
			} else {
				// Initialization failed.
				AlertDialog.Builder builder = new AlertDialog.Builder(
						VoiceSpeechEngine.this);
				builder.setTitle("Speech Engine Error");
				builder.setIcon(R.drawable.icon);
				builder.setMessage("To use Interactive mode, your phone needs to be"
						+ " able to talk to you. Please choose to install (TTS) \"Pico "
						+ "Text-to-Speech\", in the bext window");

				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// Intent intent = new
								// Intent(android.provider.Settings.);
								// intent.setAction(Intent.);
								// startActivityForResult(new
								// Intent(android.provider.Settings.ACTION_SOUND_SETTINGS,
								// 0)
								//
								ComponentName componentToLaunch = new ComponentName(
										"com.android.settings",
										"com.android.settings.TextToSpeechSettings");
								Intent intent = new Intent();
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setComponent(componentToLaunch);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);

							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
			isttsinitialised = true;
		} else {
			isttsinitialised = true;
		}
	}
	private void threadWait() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void speakText(String sentence) {
		// Select a random hello.
		mTts.setSpeechRate((float) 0.9);
		mTts.speak(sentence, TextToSpeech.QUEUE_ADD, null);

	}

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

	private void textMessagingProcedure(int msgState) {

		switch (msgState) {
			case INITIAL_MESSAGE_STATE : {
				editText.append(spokenMessage);
				Toast.makeText(getApplicationContext(), "You have said",
						Toast.LENGTH_SHORT).show();
				speakText("You have said");
				speakText(spokenMessage);
				threadWait();
				Toast.makeText(getApplicationContext(),
						"Would you like to send now?.Say Yes or No",
						Toast.LENGTH_SHORT).show();
				speakText("Would you like to send now?");
				threadWait();
				messageState = ASK_DONE;
				startVoiceRecognitionActivity();
				break;
			}
			case ASK_DONE : {

				if (spokenMessage.contains("yes")) {

					Toast.makeText(getApplicationContext(),
							"Please add contacts", Toast.LENGTH_SHORT).show();
					speakText("Please, add contacts.");
					threadWait();
					messageState = ASK_ADD_CONTACT;
					startVoiceRecognitionActivity();
				} else if (spokenMessage.contains("no")) {

					Toast.makeText(getApplicationContext(), "Please Continue",
							Toast.LENGTH_SHORT).show();
					speakText("Please, continue.");
					threadWait();
					messageState = INITIAL_MESSAGE_STATE;
					startVoiceRecognitionActivity();
				} else {

					Toast.makeText(getApplicationContext(), "Please say again",
							Toast.LENGTH_SHORT).show();
					speakText("Please, say Again");
					threadWait();
					messageState = ASK_DONE;
					startVoiceRecognitionActivity();
				}
				break;
			}

			case ASK_ADD_CONTACT : {
				contactEdit.append(spokenMessage);
				contactList = contactEdit.getText().toString();
				isContactSet = true;
				Toast.makeText(
						getApplicationContext(),
						"Contact Added, Would you like to send now?. Say Yes or No",
						Toast.LENGTH_SHORT).show();
				speakText("Contact Added");
				speakText(spokenMessage);
				threadWait();
				speakText("Would you like to send now?");
				threadWait();
				messageState = ASK_SEND_NOW;
				startVoiceRecognitionActivity();
				break;

			}
			case ASK_SEND_NOW : {
				if (spokenMessage.contains("yes")) {

					Toast.makeText(getApplicationContext(),
							"Sending message. Thank you", Toast.LENGTH_SHORT)
							.show();
					speakText("Sending Message. Thank You");
					fireSendSMSService();
					threadWait();

					VoiceSpeechEngine.this.finish();
				} else if (spokenMessage.contains("no")) {
					speakText("Please, add another Contact.");
					threadWait();
					messageState = ASK_ADD_CONTACT;
					startVoiceRecognitionActivity();
				} else {
					speakText("Please, say Again");
					threadWait();
					messageState = ASK_SEND_NOW;
					startVoiceRecognitionActivity();
				}
				break;

			}
		}

		// say welcome

	}

	private void startVoiceRecognitionActivity() {
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			isSpeechAvailable = true;
			// recognizer present
		} else {
			// Recogniser absent

		}

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Speak clearly to your phone");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			spokenMessage = null;
			spokenMessage = matches.get(0);

			textMessagingProcedure(messageState);
		}

		else if (requestCode == CONTACT_SELECT_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			contactList = data.getAction();
			contactEdit.setText(contactList);
			isContactSet = true;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// terminate activity on back button
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			VoiceSpeechEngine.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Intent intent;
	private void addContact() {
		Toast.makeText(getApplicationContext(),
				"Previously added contacts cleared", Toast.LENGTH_SHORT).show();
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
			if (cursor == null) {
				Toast.makeText(getApplicationContext(),
						"Please configure word dictionary in settings",
						Toast.LENGTH_LONG).show();
			}
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

			try {
				settings = getSharedPreferences("contact_to_send", 0);
				Editor editor = settings.edit();
				editor.putString("contact_to_send_list", contactList);
				editor.putString("message_to_send", editText.getText()
						.toString());
				editor.commit();
			} catch (Exception ex) {
			}

			startService(intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(),
					"Error Sending Message; Please add Contacts",
					Toast.LENGTH_LONG).show();
		}
	}
	String translatedText;
	private void showTranslateOptions() {

		Translate.setHttpReferrer("http://www.ifewalter.com");
		Translate
				.setKey("ABQIAAAAXTQxEcp4wcc19ZmrpUvQlRRpq4QeGTH-XAqPe3gaCWcysLnz9xSbEr7dt8KPoEsyq_JafmNTgrDZJA");

		String[] translateLanguages = {"Afrikaans", "Albanian", "Amharic",
				"Arabic", "Benghali", "Bulgarian", "English", "French"};
		AlertDialog.Builder builder = new AlertDialog.Builder(
				VoiceSpeechEngine.this);
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

	private void switchToDrivingMode() {
		Intent intent = new Intent(getApplicationContext(), Compose.class);
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
		Toast.makeText(VoiceSpeechEngine.this, "Location", Toast.LENGTH_SHORT);
		// editText.append(shortenedUrl);
		settings = getSharedPreferences(UserLocation.SAVED_LOCATION_NAME, 0);
		shortenedUrl = settings.getString(UserLocation.SAVED_LOCATION_URL,
				"http://www.maps.google.com");
		float accuracyNotification = (settings.getFloat(
				UserLocation.ACCURACY_KEY, 0));

		Toast.makeText(
				getApplicationContext(),
				"Location added with " + String.valueOf(accuracyNotification)
						+ " Meters Accuracy", Toast.LENGTH_SHORT).show();
		editText.append(shortenedUrl);

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
		menu.add(Menu.NONE, DRIVING_MODE_ID, Menu.NONE, "Walking Mode");
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
