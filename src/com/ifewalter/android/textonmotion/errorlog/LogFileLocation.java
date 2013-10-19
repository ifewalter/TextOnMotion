package com.ifewalter.android.textonmotion.errorlog;

import android.os.Environment;

public class LogFileLocation {
	private static boolean isExternalStorageAvailableAndWritable = false;
	public static final int PHONE_MEMORY = 0;
	public static final int MEDIA_STORAGE = 1;

	private static boolean checkStorageMedia() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			isExternalStorageAvailableAndWritable = true;
		} else {
			isExternalStorageAvailableAndWritable = false;
		}

		return isExternalStorageAvailableAndWritable;
	}

	public static int getAvailableLocation() {
		boolean state = checkStorageMedia();

		if (state == true) {
			return MEDIA_STORAGE;
		} else {
			return PHONE_MEMORY;
		}

	}
}
