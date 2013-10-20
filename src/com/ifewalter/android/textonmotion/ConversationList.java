package com.ifewalter.android.textonmotion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;
import com.ifewalter.android.textonmotion.databaseparoles.SelectData;
import com.ifewalter.android.textonmotion.databaseparoles.UpdateData;
import com.ifewalter.android.textonmotion.location.UserLocation;
import com.ifewalter.android.textonmotion.service.SendSMS;
import com.ifewalter.android.textonmotion.speechengine.VoiceSpeechEngine;

public class ConversationList extends Activity {

	private static ListView messageListView;
	private static ListAdapter messageListViewAdapter;
	private static Cursor messageCursor;

	String[] from = {InitDatabase.RECIEPIENT, InitDatabase.MESSAGE_STATUS,
			InitDatabase.MESSAGE_CONTENT, InitDatabase.MESSAGE_DATE,
			InitDatabase.MESSAGE_TIME, InitDatabase.MESSAGE_ID};

	public static final int SETTINGS_ID = Menu.FIRST + 6;
	public static final int HELP_ID = Menu.FIRST + 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		System.gc();

		final SharedPreferences initialSettings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (initialSettings.getBoolean("eula_accepted", false) != true) {

			BufferedReader in = null;
			StringBuilder buffer = null;
			try {
				in = new BufferedReader(new InputStreamReader(getAssets().open(
						"eula.txt")));
				String line;
				buffer = new StringBuilder();
				while ((line = in.readLine()) != null)
					buffer.append(line).append('\n');

			} catch (IOException e) {
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(
					ConversationList.this);
			builder.setTitle("Terms");
			builder.setIcon(R.drawable.icon);
			builder.setMessage(buffer.toString());

			builder.setCancelable(true);
			builder.setPositiveButton("Close",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Editor editor = initialSettings.edit();
							editor.putBoolean("eula_accepted", true);
							editor.commit();

							if (initialSettings.getBoolean("tutorial_one",
									false) != true)

							{
								AlertDialog.Builder builder = new AlertDialog.Builder(
										ConversationList.this);
								builder.setTitle("Tutorials");
								builder.setIcon(R.drawable.icon);
								builder.setMessage("Learn how to use TextOnMotion?");

								builder.setCancelable(true);
								builder.setPositiveButton("Okay",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												Editor editor = initialSettings
														.edit();
												editor.putBoolean(
														"tutorial_one", true);
												editor.commit();
												Intent intent = new Intent(
														getApplicationContext(),
														About.class);
												startActivity(intent);

											}
										});

								builder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												Editor editor = initialSettings
														.edit();
												editor.putBoolean(
														"tutorial_one", true);
												editor.commit();
												Toast.makeText(
														getApplicationContext(),
														"You can view help file later, by"
																+ " pressing the menu button",
														Toast.LENGTH_LONG);

											}
										});

								AlertDialog alert = builder.create();
								alert.show();
							}

						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}

		try {

			SharedPreferences locationSettings = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			boolean getLocationSettings = locationSettings.getBoolean(
					"acquire_location_preference", true);
			if (getLocationSettings == true) {
				Intent locationIntent = new Intent(getApplicationContext(),
						UserLocation.class);
				startService(locationIntent);
			}
		} catch (Exception ex) {

		}

		setContentView(R.layout.conversation_list);

		// initComponents();

//		registerReceiver(new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context arg0, Intent arg1) {
//
//				if (arg1.getAction().equals(SendSMS.UPDATE_YOURSELF)) {
//					initComponents();
//				}
//			}
//		}, new IntentFilter(SendSMS.UPDATE_YOURSELF));

	}

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

		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		System.gc();

		try {

			SharedPreferences locationSettings = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			boolean getLocationSettings = locationSettings.getBoolean(
					"acquire_location_preference", true);
			if (getLocationSettings == true) {
				Intent locationIntent = new Intent(getApplicationContext(),
						UserLocation.class);
				startService(locationIntent);
			}
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.toString(),
					Toast.LENGTH_LONG).show();
		}

		// very risky stuff
		try {
			// setContentView(null);
			setContentView(R.layout.conversation_list);
			initComponents();
		} catch (Exception ex) {

		}
	}

	private void initComponents() {
		SelectData selectData = new SelectData();
		messageListView = null;
		messageListViewAdapter = null;
		messageCursor = null;

		try {
			// handle the list view and its contents
			messageListView = (ListView) findViewById(R.id.conversation_list_message_list_view);
			int[] to = {R.id.message_list_phone, R.id.message_list_date_time,
					R.id.message_list_message_snippet};

			try {

				messageCursor = selectData.selectAbstractThread(this);

				// SharedPreferences messageViewSettings = PreferenceManager
				// .getDefaultSharedPreferences(getApplicationContext());
				// String messageViewValue = messageViewSettings.getString(
				// "message_view_preference", "Thread");
				// if (messageViewValue.equals("Thread")) {
				// messageCursor = selectData.selectAbstractThread(this);
				// } else if (messageViewValue.equals("Activity")) {
				// messageCursor = selectData.selectAllMessages(this);
				// }
			} catch (Exception ex) {

			} finally {
				// messageCursor = selectData.selectAbstractThread(this);

			}

			messageListViewAdapter = new ConversationListCursorAdapter(
					getApplicationContext(), R.layout.conversation_list,
					messageCursor, from, to);
			messageListView.setAdapter(messageListViewAdapter);
			registerForContextMenu(messageListView);
			messageListView.setLayoutAnimation(listViewAnimation());

			messageListView.setClickable(true);
			messageListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String temp = null;
					temp = arg1.getTag().toString();

					Intent intent = new Intent(getApplicationContext(),
							MessageView.class);
					intent.putExtra(InitDatabase.RECIEPIENT, temp);
					startActivity(intent);
				}
			});

		} catch (Exception ex) {
		}
		Button composeButton = (Button) findViewById(R.id.new_message);
		composeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(getApplicationContext(),
						Compose.class);
				startActivity(intent);
			}
		});

		Button plainComposeButton = (Button) findViewById(R.id.new_plain_message);
		plainComposeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(),
						PlainCompose.class);
				startActivity(intent);

			}

		});

		final Intent intent2 = new Intent(getApplicationContext(),
				VoiceSpeechEngine.class);
		Button voiceSpeechButton = (Button) findViewById(R.id.new_voice_message);
		voiceSpeechButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				startActivity(intent2);
			}
		});

		ImageButton searchButton = (ImageButton) findViewById(R.id.message_search);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(getApplicationContext(),
						Search.class);
				startActivity(intent);

			}
		});
	}
	private LayoutAnimationController listViewAnimation() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-2.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(250);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		return controller;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		messageCursor.close();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			Intent intent = new Intent(getApplicationContext(), Search.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

}
