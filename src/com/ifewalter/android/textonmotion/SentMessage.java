package com.ifewalter.android.textonmotion;

import com.ifewalter.android.textonmotion.databaseparoles.DeleteData;
import com.ifewalter.android.textonmotion.databaseparoles.InsertData;
import com.ifewalter.android.textonmotion.databaseparoles.SelectData;
import com.ifewalter.android.textonmotion.databaseparoles.UpdateData;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SentMessage extends Activity {

	public static final int DELETE_ID = Menu.FIRST + 1;
	public static final int RESEND_ID = Menu.FIRST + 2;

	private static final String MESSAGE_SENT = "Sent";
	private static final String MESSAGE_DELIVERED = "Delivered";
	private static final String MESSAGE_FAILED = "Failed (General)";
	private static final String MESSAGE_FAILED_AIRPLANE = "Failed (Airplane Mode)";
	private static final String MESSAGE_FAILED_NETWORK = "Failed (Network Problem)";
	private static final String MESSAGE_TYPE_DRAFT = "Draft";
	private static final String MESSAGE_TYPE_SENT = "Sent";
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_RECEIPIENT = "receipient";
	private static final String COLUMN_MESSAGE_STATUS = "message_status";
	private static final String COLUMN_MESSAGE_CONTENT = "message_content";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(initComponents());
	}

	private ListView lv;
	private ListAdapter la;
	private Cursor c;
	String[] items = {COLUMN_RECEIPIENT, COLUMN_MESSAGE_STATUS,
			COLUMN_MESSAGE_CONTENT};
	public ProgressDialog dialog1;

	private View initComponents() {
		SelectData selectData = new SelectData();
		lv = new ListView(this);
		LinearLayout ll = new LinearLayout(this);
		TextView tv = new TextView(this);
		ll.addView(tv);
		c = selectData.selectAllMessages(this);
		la = new SimpleCursorAdapter(this, R.layout.message_list_item, c,
				items, new int[]{R.id.message_list_phone, R.id.message_list_date_time, R.id.message_list_message_snippet});
		lv.setAdapter(la);
		registerForContextMenu(lv);

		lv.setLayoutAnimation(listViewAnimation());

		return lv;
	}

	// listView animation method
	private LayoutAnimationController listViewAnimation() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(50);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		return controller;
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
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete Message");

	}

	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
			case RESEND_ID : {

			}
			case DELETE_ID : {
				DeleteData delete = new DeleteData();
				delete.deleteMessage(SentMessage.this,
						c.getString(c.getColumnIndex("_id")));
				Toast.makeText(SentMessage.this, "Message Deleted",
						Toast.LENGTH_SHORT);
				finish();
				Intent intent = new Intent(SentMessage.this, SentMessage.class);
				startActivity(intent);
				return (true);
			}
		}

		// return false;
		return true;
	}
}
