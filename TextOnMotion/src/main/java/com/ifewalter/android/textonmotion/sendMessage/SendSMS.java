package com.ifewalter.android.textonmotion.sendMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;
import com.ifewalter.android.textonmotion.databaseparoles.InsertData;
import com.ifewalter.android.textonmotion.notificationengine.NotificationBar;

public class SendSMS extends Service {

	private static final String NETWORK_ERROR_MESSAGE = "I need signal reception to send your message. Please try when Signal Returns";
	private static final String AIRPLANE_ERROR_MESSAGE = "Please disable airplane mode, so i can send your message";
	private static final String GENERIC_ERROR_MESSAGE = "Message Sending Failed. Please Try Again";

	private static final String DELIVERED_REPORT = "Message Delivered";
	private static final String DELIVERY_FAILED_REPORT = "Message Undelivered";
	private static final String SENT_REPORT = "Message Sent";

	public static final String SENT = "TOM_SMS_SENT";
	private static final String DELIVERED = "TOM_SMS_DELIVERED";
	public static final String UPDATE_YOURSELF = "TOM_UPDATE";
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	private static NotificationBar notificationBar = new NotificationBar();
	public void sendSMS(final String phoneNumber, final String message,
			final Context sendingContext, final Context receivingContext,
			final int notificationId) {

		//final InsertData insertData = new InsertData();

		final PendingIntent sentPI = PendingIntent.getBroadcast(sendingContext,
				0, new Intent(SENT), 0);

		final PendingIntent deliveredPI = PendingIntent.getBroadcast(
				sendingContext, 0, new Intent(DELIVERED), 0);

		SmsManager sms = SmsManager.getDefault();
		// sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

		ArrayList<String> messages = sms.divideMessage(message);
		int messageCount = messages.size();
		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(
				messageCount);
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(
				messageCount);
		deliveryIntents.add(deliveredPI);
		sentIntents.add(sentPI);

		try {
			sms.sendMultipartTextMessage(phoneNumber, null, messages,
					sentIntents, deliveryIntents);
		} catch (Exception ex) {
			Random r = new Random();
			notificationBar.sendNotification("Error Sending message to: "
					+ phoneNumber, sendingContext, receivingContext,
					r.nextInt());
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		final String messageDate = dateFormat.format(cal.getTime()).toString();
		final String messageTime = timeFormat.format(cal.getTime()).toString();

		// ---when the SMS has been sent---
		sendingContext.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if (arg1.getAction().equals(SENT)) {
					switch (getResultCode()) {
						case Activity.RESULT_OK : {

							InsertData.insertMessage(sendingContext,
									phoneNumber, InitDatabase.STATUS_SENT,
									InitDatabase.TYPE_OUTBOX, messageTime,
									messageDate, message);
							InsertData.insertAbstractThreadItem(sendingContext,
									phoneNumber, message, messageTime,
									messageDate);

							// vibrator.vibrate(
							// new long[]{100, 300, 100, 300, 100}, -1);

							// alreadyInserted = true;
							try {
								SharedPreferences notificationSettings = PreferenceManager
										.getDefaultSharedPreferences(sendingContext);
								boolean isSentNotificationEnabled = notificationSettings
										.getBoolean(
												"sent_notification_preference",
												true);

								if (isSentNotificationEnabled == true) {
									notificationBar.sendNotification(
											SENT_REPORT + " : " + phoneNumber,
											sendingContext, receivingContext,
											notificationId);
								}
							} catch (Exception ex) {

							}
							sentPI.cancel();

							sendingContext.unregisterReceiver(this);
							try {
								sendingContext.removeStickyBroadcast(arg1);
							} catch (Exception ex) {
							}

							// sendBroadcast(new Intent(UPDATE_YOURSELF));
							break;
						}
							// }

						case SmsManager.RESULT_ERROR_GENERIC_FAILURE : {
							// Toast.makeText(sendingContext, "Generic failure",
							// Toast.LENGTH_LONG).show();
							InsertData.insertMessage(arg0, phoneNumber,
									InitDatabase.STATUS_FAILED,
									InitDatabase.TYPE_OUTBOX, messageTime,
									messageDate, message);
							InsertData.insertAbstractThreadItem(arg0,
									phoneNumber, message, messageTime,
									messageDate);
							// vibrator.vibrate(new long[]{100, 100, 200,
							// 700,
							// 300, 700}, -1);
							notificationBar
									.sendNotification(GENERIC_ERROR_MESSAGE
											+ " : " + phoneNumber,
											sendingContext, receivingContext,
											notificationId);
							sentPI.cancel(); 

							sendingContext.unregisterReceiver(this);
							try {
								sendingContext.removeStickyBroadcast(arg1);
							} catch (Exception ex) {
							}
							break;
						}
						case SmsManager.RESULT_ERROR_NO_SERVICE : {
							// Toast.makeText(sendingContext, "No service",
							// Toast.LENGTH_LONG).show();
							InsertData.insertMessage(arg0, phoneNumber,
									InitDatabase.STATUS_FAILED,
									InitDatabase.TYPE_OUTBOX, messageTime,
									messageDate, message);
							InsertData.insertAbstractThreadItem(arg0,
									phoneNumber, message, messageTime,
									messageDate);

							// vibrator.vibrate(new long[]{100, 100, 200,
							// 700,
							// 300, 700}, -1);
							notificationBar
									.sendNotification(NETWORK_ERROR_MESSAGE
											+ " : " + phoneNumber,
											sendingContext, receivingContext,
											notificationId);

							sentPI.cancel();

							sendingContext.unregisterReceiver(this);
							try {
								sendingContext.removeStickyBroadcast(arg1);
							} catch (Exception ex) {
							}
							break;
						}
						case SmsManager.RESULT_ERROR_NULL_PDU : {
							InsertData.insertMessage(arg0, phoneNumber,
									InitDatabase.STATUS_FAILED,
									InitDatabase.TYPE_OUTBOX, messageTime,
									messageDate, message);
							InsertData.insertAbstractThreadItem(arg0,
									phoneNumber, message, messageTime,
									messageDate);

							// vibrator.vibrate(new long[]{100, 100, 200,
							// 700,
							// 300, 700}, -1);
							notificationBar
									.sendNotification(GENERIC_ERROR_MESSAGE
											+ " : " + phoneNumber,
											sendingContext, receivingContext,
											notificationId);

							sentPI.cancel();

							sendingContext.unregisterReceiver(this);
							try {
								sendingContext.removeStickyBroadcast(arg1);
							} catch (Exception ex) {
							}
							break;
						}
						case SmsManager.RESULT_ERROR_RADIO_OFF : {
							InsertData.insertMessage(arg0, phoneNumber,
									InitDatabase.STATUS_FAILED,
									InitDatabase.TYPE_OUTBOX, messageTime,
									messageDate, message);
							InsertData.insertAbstractThreadItem(arg0,
									phoneNumber, message, messageTime,
									messageDate);

							// vibrator.vibrate(new long[]{100, 100, 200,
							// 700,
							// 300, 700}, -1);

							notificationBar.sendNotification(
									AIRPLANE_ERROR_MESSAGE + " : "
											+ phoneNumber, sendingContext,
									receivingContext, notificationId);

							sentPI.cancel();

							sendingContext.unregisterReceiver(this);
							try {
								sendingContext.removeStickyBroadcast(arg1);
							} catch (Exception ex) {
							}
							break;
						}
					}
					 arg0.sendBroadcast(new Intent(SendSMS.UPDATE_YOURSELF));
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		sendingContext.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK : {
						try {
							SharedPreferences notificationSettings = PreferenceManager
									.getDefaultSharedPreferences(sendingContext);

							if (notificationSettings.getBoolean(
									"sent_notification_preference", true) == true) {
								notificationBar.sendNotification(
										DELIVERED_REPORT + " : " + phoneNumber,
										sendingContext, receivingContext,
										notificationId);
							}
						} catch (Exception ex) {
						}
						deliveredPI.cancel();
						sendingContext.unregisterReceiver(this);
						try {
							sendingContext.removeStickyBroadcast(arg1);
						} catch (Exception ex) {
						}
						break;
					}
					case Activity.RESULT_CANCELED : {
						notificationBar.sendNotification(DELIVERY_FAILED_REPORT
								+ " : " + phoneNumber, sendingContext,
								receivingContext, notificationId);
						sendingContext.unregisterReceiver(this);
						try {
							sendingContext.removeStickyBroadcast(arg1);
						} catch (Exception ex) {
						}
						break;
					}
				}
			}
		}, new IntentFilter(DELIVERED));
		// sms.sendMultipartTextMessage(phoneNumber, null,
		// sms.divideMessage(message), sentPI, deliveredPI);
	}

	private final class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {

			SharedPreferences settings = getSharedPreferences(
					"contact_to_send", 0);

			String MessageToSend = settings.getString("message_to_send", null);
			String ContactList = settings.getString("contact_to_send_list",
					null);

			String[] newContactList = null;
			newContactList = ContactList.split(";");

			// for (String a : newContactList) {
			//
			// sendSMS.sendSMS(a, MessageToSend,
			// getApplicationContext(),
			// getApplicationContext());
			//
			// }

			SendSMS sendSMS = new SendSMS();
			int len = newContactList.length;
			String a;
			Random r = new Random();
			for (int i = 0; i < len; i++) {
				a = newContactList[i].toString();
				sendSMS.sendSMS(a, MessageToSend, getApplicationContext(),
						getApplicationContext(), r.nextInt());

			}
			
			Editor editor = settings.edit();
			editor.remove("contact_to_send_list");
			editor.remove("message_to_send");
			editor.commit();
			
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.

		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Sending Message...", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try
		{
		SharedPreferences settings = getSharedPreferences("contact_to_send", 0);
		Editor editor = settings.edit();
		editor.remove("contact_to_send_list");
		editor.remove("message_to_send");
		editor.commit();
		}
		catch(Exception ex)
		{}
		mServiceLooper.quit();
	}

}
