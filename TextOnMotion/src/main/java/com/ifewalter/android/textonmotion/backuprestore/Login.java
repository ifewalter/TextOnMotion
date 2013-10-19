package com.ifewalter.android.textonmotion.backuprestore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ifewalter.android.textonmotion.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Login extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Login to Backup Account");
		setContentView(R.layout.login);
		initComponents();
	}

	private EditText emailText;
	private EditText passwordText;
	private void initComponents() {
		// progress = new ProgressDialog(Login.this);

		Button doneButton = (Button) findViewById(R.id.login_done);
		Button cancelButton = (Button) findViewById(R.id.login_cancel);
		Button loginButton = (Button) findViewById(R.id.login_register_text);

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						Register.class);
				startActivity(intent);
				finish();
			}
		});

		emailText = (EditText) findViewById(R.id.login_email);
		passwordText = (EditText) findViewById(R.id.login_password);

		doneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// progress.setMessage("Please Wait...");
				// progress.show();

				beginRegistration();

				// progress.dismiss();
			}
		});

	}

	public void beginRegistration() {
		new registrationTask().execute();

	}

	private class registrationTask extends AsyncTask<String, Void, Boolean> {
		// can use UI thread here
		ProgressDialog progress = new ProgressDialog(Login.this);
		protected void onPreExecute() {
			progress.setMessage("Please wait...");
			progress.show();
		}
		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			try {

				HttpClient client = new DefaultHttpClient();
				// String postURL =
				// "http://192.168.43.67/tomsite/auth/login.php";
				String postURL = "http://www.ifewalter.com/textonmotion/auth/login.php";
				HttpPost post = new HttpPost(postURL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("uname", emailText.getText()
						.toString()));
				params.add(new BasicNameValuePair("pword", passwordText
						.getText().toString()));

				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,
						HTTP.UTF_8);
				post.setEntity(ent);
				HttpResponse responsePOST = client.execute(post);
				HttpEntity resEntity = responsePOST.getEntity();
				// String jsonresponse = resEntity.getContent().toString();
				if (resEntity != null) {
					// Log.i("RESPONSE", EntityUtils.toString(resEntity));
					JSONObject newJsonObject = new JSONObject(
							EntityUtils.toString(resEntity));
					String statusCode = newJsonObject.getString("status");
					String token = newJsonObject.getString("token");

					if (statusCode.equals("200")) {
						SharedPreferences settings = getSharedPreferences(
								"auth", 0);
						Editor editor = settings.edit();
						editor.putBoolean("authed", true);
						editor.putString("token", token);
						editor.commit();

						return true;

					} else {
						return false;
					}
				}
			} catch (Exception e) {
				return false;
			}
			return false;

		}

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.progress.isShowing()) {
				this.progress.dismiss();
			}
			if (success) {
				Toast.makeText(Login.this, "Login Successful",
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
}
