package com.ifewalter.android.textonmotion;

import com.ifewalter.android.textonmotion.databaseparoles.DeleteData;
import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;
import com.ifewalter.android.textonmotion.databaseparoles.SelectData;
import com.ifewalter.android.textonmotion.speechengine.VoiceSpeechEngine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MessageView extends Activity {

	public static final int DELETE_ID = Menu.FIRST + 1;
	public static final int RESEND_ID = Menu.FIRST + 2;
	public static final int FORWARD_ID = Menu.FIRST + 3;
	private static String phoneNumber;
	private static Intent intent, intent2;
	private static ListView messageView;
	private static ListAdapter messageViewAdapter;
	private static Cursor messageCursor;
	private static String[] from = {InitDatabase.MESSAGE_STATUS,
			InitDatabase.MESSAGE_CONTENT};
	private static ProgressDialog deleteProcess;
	private static AlertDialog deleteConfirmation, deleteConfirmation1;
	private static final String DELETE_CONFIRMATION_MESSAGE = "Are you sure you want to delete entire conversation thread";
	private static final String DELETE_ABORTED = "Your conversation thread is safe";

	private static final String[] items = {"Regular Mode", "Walking Mode",
			"Interactive Mode"};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_view);

		initComponents();
	}
	private String SelectedID;

	private void initComponents() {
		SelectData selectData = new SelectData();
		// handle the list view and its contents
		intent = getIntent();
		phoneNumber = intent.getExtras().getString(InitDatabase.RECIEPIENT);

		this.setTitle(phoneNumber);

		messageView = (ListView) findViewById(R.id.message_view_list_view);
		int[] to = {R.id.message_view_item_date,
				R.id.message_view_item_message_content};
		messageCursor = selectData.selectConversation(this, phoneNumber);
		messageViewAdapter = new MessageViewCursorAdapter(
				getApplicationContext(), R.layout.message_view_item,
				messageCursor, from, to);
		messageView.setAdapter(messageViewAdapter);
		messageView.setLayoutAnimation(listViewAnimation());

		messageView.setClickable(true);

		// registerForContextMenu(messageView);
		messageView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				SelectedID = arg1.getTag().toString();
				return false;
			}
		});

		Button callButton = (Button) findViewById(R.id.message_view_call);
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				try {
					Uri uri = Uri.fromParts("tel", phoneNumber, null);

					Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Toast.makeText(getApplicationContext(), "Dialing...",
							Toast.LENGTH_SHORT).show();
					startActivity(callIntent);
				} catch (Exception ex) {
					Toast.makeText(getApplicationContext(),
							"Contact Can not be dialed at this time",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		Button replyButton = (Button) findViewById(R.id.message_view_reply);
		replyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				showMenuDialog();

			}
		});

		ImageButton deleteButton = (ImageButton) findViewById(R.id.message_view_delete);
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				initDeleteConfirmation();
			}
		});

		ImageButton searchButton = (ImageButton) findViewById(R.id.message_view_search);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(getApplicationContext(),
						Search.class);
				startActivity(intent);

			}
		});
	}

	private void showMenuDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageView.this);
		builder.setTitle("Message Options");
		builder.setIcon(R.drawable.icon);
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0 : {
						Intent intent = new Intent(getApplicationContext(),
								PlainCompose.class);
						intent.putExtra(InitDatabase.RECIEPIENT,
								MessageView.phoneNumber);
						startActivity(intent);
						break;
					}
					case 1 : {
						Intent intent = new Intent(getApplicationContext(),
								Compose.class);
						intent.putExtra(InitDatabase.RECIEPIENT,
								MessageView.phoneNumber);
						startActivity(intent);
						break;
					}
					case 2 : {
						Intent intent = new Intent(getApplicationContext(),
								VoiceSpeechEngine.class);
						intent.putExtra(InitDatabase.RECIEPIENT,
								MessageView.phoneNumber);
						startActivity(intent);
						break;
					}
				}
			}
		});

		AlertDialog menuAlert = builder.create();
		menuAlert.show();
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

	private void initDeleteConfirmation() {
		final DeleteData deleteData = new DeleteData();
		deleteProcess = new ProgressDialog(MessageView.this);
		deleteProcess
				.setMessage("Your wish is my command. \n Deleting Message...");
		deleteProcess.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(DELETE_CONFIRMATION_MESSAGE)
				.setCancelable(false)
				.setPositiveButton("Yes, Please",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								deleteConfirmation.dismiss();
								deleteProcess.show();
								deleteData.deleteThread(MessageView.this,
										phoneNumber);

								deleteProcess.dismiss();
								finish();
							}
						})
				.setNegativeButton("Nope",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Toast.makeText(MessageView.this,
										DELETE_ABORTED, Toast.LENGTH_SHORT)
										.show();
								deleteConfirmation.dismiss();
							}
						});
		deleteConfirmation = builder.create();
		deleteConfirmation.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onContextItemSelected(item));

	}

	private void populateMenu(Menu menu) {
		menu.add(Menu.NONE, RESEND_ID, Menu.NONE, "Resend Message");
		menu.add(Menu.NONE, FORWARD_ID, Menu.NONE, "Forward Message");
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete Message");

	}

	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
			case RESEND_ID : {

				return (true);
			}
			case DELETE_ID : {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(
						"Are you sure you want to delete this message?")
						.setCancelable(false)
						.setIcon(R.drawable.icon)
						.setPositiveButton("Yes, Please",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										deleteConfirmation1.dismiss();
										DeleteData delete = new DeleteData();
										delete.deleteAbstractViewItem(
												MessageView.this, SelectedID);
										Toast.makeText(MessageView.this,
												"Message Deleted",
												Toast.LENGTH_SHORT).show();
										// MessageView.this.initComponents();
										finish();
										Intent intent = new Intent(
												MessageView.this,
												MessageView.class);
										startActivity(intent);
									}
								})
						.setNegativeButton("Nope",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Toast.makeText(MessageView.this,
												DELETE_ABORTED,
												Toast.LENGTH_SHORT).show();
										deleteConfirmation1.dismiss();
									}
								});
				deleteConfirmation1 = builder.create();
				deleteConfirmation1.show();

				return (true);
			}
		}

		// return false;
		return true;
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
