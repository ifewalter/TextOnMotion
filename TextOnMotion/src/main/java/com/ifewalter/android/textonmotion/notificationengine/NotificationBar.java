package com.ifewalter.android.textonmotion.notificationengine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import com.ifewalter.android.textonmotion.ConversationList;
import com.ifewalter.android.textonmotion.R;

public class NotificationBar {

	private NotificationManager mNotificationManager;
	private Notification notification;
	public void sendNotification(String message, Context sendingContext,
			Context receivingContext, int uniqueNotificationId) {

		// Vibrator vibrator = (Vibrator) sendingContext
		// .getSystemService(Context.VIBRATOR_SERVICE);
		//
		// vibrator.vibrate(new long[]{100, 300, 100, 300, 100}, -1);

		String title = "TextOnMotion";
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) sendingContext
				.getSystemService(ns);

		int icon = R.drawable.icon;
		CharSequence tickerText = message;
		long when = System.currentTimeMillis();

		notification = new Notification(icon, tickerText, when);
		CharSequence contentTitle = title;
		CharSequence contentText = message;
		Intent notificationIntent = new Intent(sendingContext,
				ConversationList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(
				receivingContext, 0, notificationIntent, 0);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.setLatestEventInfo(sendingContext, contentTitle,
				contentText, contentIntent);

		// mNotificationManager.notify(, notification);
	
		mNotificationManager.notify(uniqueNotificationId, notification);

	}

	public void cancelAllNotifications(Context sendingContext) {
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) sendingContext
				.getSystemService(ns);
		mNotificationManager.cancelAll();
	}

}
