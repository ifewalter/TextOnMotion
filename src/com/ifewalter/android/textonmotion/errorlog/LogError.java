package com.ifewalter.android.textonmotion.errorlog;

import java.io.File;
import java.io.FileOutputStream;
import android.content.Context;
import java.io.FileWriter;
import java.io.BufferedWriter;
import android.os.Environment;

public class LogError {
	private static final String MEDIA_STORAGE_LOCATION = "/sdcard/Android/data/com.ifewalter.android.textonmotion/anonusagelog/log.tom";
	private static final String PHONE_STORAGE_LOCATION = "log.tom";
	private static boolean isExternalStorageAvailableAndWritable = false;
	public static final int PHONE_MEMORY = 0;
	public static final int MEDIA_STORAGE = 1;

	private void writeLog(Context context, String logDetails) {

		if (LogFileLocation.getAvailableLocation() == LogFileLocation.MEDIA_STORAGE) {
			try {
				FileWriter fileWriter = new FileWriter(MEDIA_STORAGE_LOCATION,
						true);
				BufferedWriter buf = new BufferedWriter(fileWriter);
				buf.write(logDetails);
			} catch (Exception ex) {

			}
		} else {
			String filePath = context.getFilesDir().getAbsolutePath();
			File file = new File(filePath, PHONE_STORAGE_LOCATION);
		}
	}

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
