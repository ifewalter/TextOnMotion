package com.ifewalter.android.textonmotion.backuprestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.ifewalter.android.textonmotion.Compose;
import com.ifewalter.android.textonmotion.R;
import com.ifewalter.android.textonmotion.databaseparoles.InitDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class sdRestore extends Activity {
	private Button exportDbToSdButton;
	private Button exportDbXmlToSdButton;
	private String errorMessage;

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		beginDBImportFromSd();

	}
	public void beginDBImportFromSd() {
		if (sdRestore.this.isExternalStorageAvail()) {
			new ExportDatabaseFileTask().execute();
		} else {
			Toast.makeText(
					sdRestore.this,
					"External storage is not available, unable to import data.",
					Toast.LENGTH_SHORT).show();
		}
	}
	private boolean isExternalStorageAvail() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	private class ExportDatabaseFileTask
			extends
				AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(sdRestore.this);
		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Restoring database...");
			this.dialog.show();
		}
		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			File dbFile = new File(Environment.getExternalStorageDirectory(),
					"/Android/data/com.ifewalter.android.textonmotion/backup/"
							+ InitDatabase.DATABASE_NAME + "");
			File exportDir = new File(Environment.getDataDirectory()
					+ "/data/com.ifewalter.android.textonmotion/databases");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File file = new File(exportDir, dbFile.getName());
			try {
				file.createNewFile();
				this.copyFile(dbFile, file);
				return true;
			} catch (IOException e) {
				// (MyApplication.APP_NAME, e.getMessage(), e);
				errorMessage = e.toString();
				return false;
			}
		}
		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(sdRestore.this, "Restore successful!",
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				Toast.makeText(sdRestore.this, errorMessage, Toast.LENGTH_SHORT)
						.show();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						sdRestore.this);
				builder.setTitle("Database Restore Error");
				builder.setIcon(R.drawable.icon);
				builder.setCancelable(true);
				builder.setMessage("Something went terribly wrong, and we could not "
						+ "access your memory card. Please make sure you have the right"
						+ " memory card in your phone");
				builder.setPositiveButton("Close",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();

							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
		void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}
	}

}