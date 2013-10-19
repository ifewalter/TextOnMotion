//package com.ifewalter.android.textonmotion;
//
//import android.app.PendingIntent;
//import android.appwidget.AppWidgetManager;
//import android.appwidget.AppWidgetProvider;
//import android.content.Context;
//import android.content.Intent;
//import android.widget.RemoteViews;
//
//import com.ifewalter.android.textonmotion.speechengine.VoiceSpeechEngine;
//
//public class ShortcutWidget extends AppWidgetProvider {
//	public static String ACTION_WIDGET_COMPOSE = "ActionTomCompose";
//	public static String ACTION_WIDGET_RECEIVER = "ActionTomReceiverWidget";
//	public static String ACTION_WIDGET_HOMEVIEW = "ActionTomHome";
//	public static String ACTION_WIDGET_DRIVING_MODE = "ActionTomDrivingMode";
//
//	@Override
//	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
//			int[] appWidgetIds) {
//		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
//				R.layout.shortcut_widget);
//
//		Intent composeIntent = new Intent(context, Compose.class);
//		composeIntent.setAction(ACTION_WIDGET_COMPOSE);
//
//		Intent homeIntent = new Intent(context, ConversationList.class);
//		homeIntent.setAction(ACTION_WIDGET_HOMEVIEW);
//
//		Intent drivingModeIntent = new Intent(context, VoiceSpeechEngine.class);
//		drivingModeIntent.setAction(ACTION_WIDGET_DRIVING_MODE);
//
//		Intent active = new Intent(context, VoiceSpeechEngine.class);
//		active.setAction(ACTION_WIDGET_RECEIVER);
//
//		PendingIntent composePendingIntent = PendingIntent.getActivity(context,
//				0, composeIntent, 0);
//
//		PendingIntent drivingModePendingIntent = PendingIntent.getActivity(
//				context, 0, drivingModeIntent, 0);
//
//		PendingIntent homePendingIntent = PendingIntent.getActivity(context, 0,
//				homeIntent, 0);
//
//		remoteViews
//				.setOnClickPendingIntent(R.id.home_button, homePendingIntent);
//		remoteViews.setOnClickPendingIntent(R.id.shortcut_button_one,
//				drivingModePendingIntent);
//		remoteViews.setOnClickPendingIntent(R.id.shortcut_button_two,
//				composePendingIntent);
//		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
//
//	}
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		final String action = intent.getAction();
//		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
//			final int appWidgetId = intent.getExtras().getInt(
//					AppWidgetManager.EXTRA_APPWIDGET_ID,
//					AppWidgetManager.INVALID_APPWIDGET_ID);
//			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
//				this.onDeleted(context, new int[]{appWidgetId});
//			}
//		} else {
//
//			super.onReceive(context, intent);
//		}
//	}
//}
