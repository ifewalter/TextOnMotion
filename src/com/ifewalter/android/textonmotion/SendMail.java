package com.ifewalter.android.textonmotion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SendMail extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"ife@ifewalter.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "TextOnMotion Feedback");
		i.putExtra(
				Intent.EXTRA_TEXT,
				"Please give feedback. If you're reporting, please tell us what you were doing when you noticed the bug");
		startActivity(i);
		
		try {
			startActivity(Intent.createChooser(i, "Send Email using"));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getApplicationContext(),
					"There are no email clients installed.", Toast.LENGTH_SHORT)
					.show();
		}
		finish();
	}

}
