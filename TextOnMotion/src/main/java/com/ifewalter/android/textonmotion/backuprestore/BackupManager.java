package com.ifewalter.android.textonmotion.backuprestore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ifewalter.android.textonmotion.R;
import com.ifewalter.android.textonmotion.persistence.InitDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class BackupManager extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Backup Manager");
		setContentView(R.layout.backup_manager);
		initComponents();
	}
	private void initComponents() {
		Button registerButton = (Button) findViewById(R.id.backup_manager_register);

		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						Register.class);
				startActivity(intent);
			}
		});

		LinearLayout signedOutContainer = (LinearLayout) findViewById(R.id.backup_manager_signed_out);
		LinearLayout signedInContainer = (LinearLayout) findViewById(R.id.backup_manager_signed_in);

		FrameLayout signedinIndicator = (FrameLayout) findViewById(R.id.backup_manager_indicator);

		signedOutContainer.setVisibility(View.GONE);
		signedInContainer.setVisibility(View.GONE);

		final SharedPreferences settings = getSharedPreferences("auth", 0);

		if (settings.getBoolean("authed", false) == true) {
			signedInContainer.setVisibility(View.VISIBLE);
			signedinIndicator.setBackgroundColor(Color.parseColor("#69FF4A"));
		} else {
			signedOutContainer.setVisibility(View.VISIBLE);
			signedinIndicator.setBackgroundColor(Color.parseColor("#4BB5FF"));
		}

		Button signOutButton = (Button) findViewById(R.id.backup_manager_sign_out_button);
		signOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor = settings.edit();
				editor.putBoolean("authed", false);
				editor.putString("token", "0");
				editor.commit();
				Intent intent = new Intent(getApplicationContext(),
						BackupManager.class);
				startActivity(intent);
				finish();
			}
		});

		Button remoteBackupButton = (Button) findViewById(R.id.backup_manager_backup_button);
		remoteBackupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				beginDBExportToSd();
			}
		});

		Button remoteRestoreButton = (Button) findViewById(R.id.backup_manager_restore_button);
		remoteRestoreButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				beginDBImport();
			}
		});

	}
	@Override
	public void onResume() {
		super.onResume();
		try {
			initComponents();
		} catch (Exception ex) {
		}
	}

	public void beginDBExportToSd() {
		if (BackupManager.this.isExternalStorageAvail()) {
			new ExportDatabaseFileTask().execute();
		} else {
			Toast.makeText(
					BackupManager.this,
					"External storage is not available, unable to export data.",
					Toast.LENGTH_SHORT).show();
		}
	}
	public void beginDBImport() {

		new ImportDatabaseFileTask().execute();
	}
	private boolean isExternalStorageAvail() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	private class ExportDatabaseFileTask
			extends
				AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				BackupManager.this);
		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Exporting database...");
			this.dialog.show();
		}
		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {
			File dbFile = new File(Environment.getDataDirectory()
					+ "/data/com.ifewalter.android.textonmotion/databases/"
					+ InitDatabase.DATABASE_NAME + "");
			File exportDir = new File(
					Environment.getExternalStorageDirectory(),
					"/Android/data/com.ifewalter.android.textonmotion/backup");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File file = new File(exportDir, dbFile.getName());
			try {
				file.createNewFile();
				this.copyFile(dbFile, file);
				SharedPreferences settings = getSharedPreferences("auth", 0);

				try {
					HttpClient client = new DefaultHttpClient();
					String postURL = "http://www.ifewalter.com/textonmotion/auth/upload.php?auth="
							+ settings.getString("token", "0");
					HttpPost post = new HttpPost(postURL);
					FileBody bin = new FileBody(file);
					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("USERFILE", bin);
					post.setEntity(reqEntity);
					HttpResponse response = client.execute(post);
					HttpEntity resEntity = response.getEntity();
					file.delete();
					if (resEntity != null) {
						// Log.i("RESPONSE",EntityUtils.toString(resEntity));
						JSONObject newJsonObject = new JSONObject(
								EntityUtils.toString(resEntity));
						String statusCode = newJsonObject.getString("status");

						if (statusCode.equals("200")) {
							return true;
						} else {
							return false;
						}

					} else {
						return false;
					}
				} catch (Exception e) {

				}

				return true;
			} catch (IOException e) {
				return false;
			}
		}
		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(BackupManager.this, "Export successful!",
						Toast.LENGTH_LONG).show();
				// finish();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						BackupManager.this);
				builder.setTitle("Database Export Error");
				builder.setIcon(R.drawable.icon);
				builder.setCancelable(true);
				builder.setMessage("Something went terribly wrong, and we could not "
						+ "access your memory card. Please make sure you have a non empty"
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

	private class ImportDatabaseFileTask
			extends
				AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				BackupManager.this);
		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Restoring database...");
			this.dialog.show();
		}
		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			try {

				HttpClient client = new DefaultHttpClient();
				// String postURL =
				// "http://192.168.43.67/tomsite/auth/register.php";
				String postURL = "http://www.ifewalter.com/textonmotion/auth/download.php";
				HttpPost post = new HttpPost(postURL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				SharedPreferences settings = getSharedPreferences("auth", 0);
				params.add(new BasicNameValuePair("auth", settings.getString(
						"token", "0")));

				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,
						HTTP.UTF_8);
				post.setEntity(ent);
				HttpResponse responsePOST = client.execute(post);
				HttpEntity resEntity = responsePOST.getEntity();
				if (resEntity != null) {
					InputStream is = resEntity.getContent();
					BufferedInputStream bis = new BufferedInputStream(is);

					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current = 0;
					while ((current = bis.read()) != -1) {
						baf.append((byte) current);
					}
					File dbFile = new File(
							Environment.getDataDirectory()
									+ "/data/com.ifewalter.android.textonmotion/databases/"
									+ InitDatabase.DATABASE_NAME + "");

					FileOutputStream fos = new FileOutputStream(dbFile);
					fos.write(baf.toByteArray());
					fos.close();
					return true;
				} else {
					return false;
				}
			} catch (Exception ex) {
				return false;
			}

		}
		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(BackupManager.this, "Restore successful!",
						Toast.LENGTH_LONG).show();
				// finish();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						BackupManager.this);
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
	}

}