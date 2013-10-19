package com.ifewalter.android.textonmotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

import com.ifewalter.android.textonmotion.persistence.InitDatabase;
import com.ifewalter.android.textonmotion.persistence.InsertData;
import com.ifewalter.android.textonmotion.notificationengine.NotificationBar;
import com.ifewalter.android.textonmotion.sendMessage.SendSMS;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class MessageIntercept extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String messageContent = "";
		String receipient = "";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

		String messageDate = dateFormat.format(cal.getTime()).toString();
		String messageTime = timeFormat.format(cal.getTime()).toString();
		if (bundle != null) {

			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				receipient = msgs[i].getOriginatingAddress().toString();
				messageContent += msgs[i].getMessageBody().toString();
			}

			try {

				InsertData.insertMessage(context, receipient,
						InitDatabase.STATUS_UNREAD, InitDatabase.TYPE_INBOX,
						messageTime, messageDate, messageContent);

				InsertData.insertAbstractThreadItem(context, receipient,
						messageContent, messageTime, messageDate);
			} catch (Exception ex) {
			}
			SharedPreferences notificationSettings = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean isNotificationEnabled = notificationSettings.getBoolean(
					"received_notification_preference", true);

			if (isNotificationEnabled == true) {

				NotificationBar notification = new NotificationBar();
				Random r = new Random();
				notification.sendNotification(
						"New Message From: " + receipient, context, context,
						r.nextInt());
			}
			context.sendBroadcast(new Intent(SendSMS.UPDATE_YOURSELF));
		}

	}
}
