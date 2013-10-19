package com.ifewalter.android.textonmotion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class About extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		BufferedReader in = null;
		StringBuilder buffer = null;
		try {
			in = new BufferedReader(new InputStreamReader(getAssets().open(
					"local_help.html")));
			String line;
			buffer = new StringBuilder();
			while ((line = in.readLine()) != null)
				buffer.append(line).append('\n');

		} catch (IOException e) {
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		WebView wv;

		wv = (WebView) findViewById(R.id.about_webview);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setBlockNetworkImage(false);
		wv.getSettings().setLoadsImagesAutomatically(true);
		wv.getSettings().setRenderPriority(RenderPriority.HIGH);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView wv, int progress) {
				About.this.setProgress(progress);
			}
		});
		// wv.loadData(buffer.toString(), mimeType, encoding);
		wv.loadUrl("file:///android_asset/local_help.html");

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			Intent intent = new Intent(getApplicationContext(), Search.class);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public static final int EXIT_ID = Menu.FIRST + 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onContextItemSelected(item));

	}

	private void populateMenu(Menu menu) {
		menu.add(Menu.NONE, EXIT_ID, Menu.NONE, "Close Help");

	}

	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
			case EXIT_ID : {

				finish();

				return (true);
			}
		}

		return true;
	}
}
