package com.ifewalter.android.textonmotion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Home extends Activity {
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(initComponents());

	}

	public static ProgressDialog processDialog;

	@Override
	protected void onPause() {
		super.onDestroy();
		// The activity is about to be destroyed.
		processDialog.dismiss();
	}


	private View initComponents() {

		processDialog = new ProgressDialog(this);

		ScrollView mainContainer = new ScrollView(this);
		LinearLayout container = new LinearLayout(this);
		container.setOrientation(LinearLayout.VERTICAL);

		FrameLayout composeContainer = new FrameLayout(this);
		composeContainer.setPadding(0, 15, 0, 5);
		composeContainer.setBackgroundColor(Color.DKGRAY);
		composeContainer.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView newMessage = new TextView(this);
		newMessage.setText("New Message");
		newMessage.setTypeface(null, Typeface.BOLD);
		newMessage.setTextSize(20);
		newMessage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		composeContainer.setClickable(true);

		processDialog.setMessage("Loading...");
		composeContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				processDialog.show();
				Intent intent = new Intent(Home.this, Compose.class);
				startActivity(intent);
			}
		});
		composeContainer.addView(newMessage);

		FrameLayout allMessageContainer = new FrameLayout(this);
		allMessageContainer.setPadding(0, 15, 0, 5);
		allMessageContainer.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView allMessage = new TextView(this);
		allMessage.setText("All Messages");
		allMessage.setTypeface(null, Typeface.BOLD);
		allMessage.setTextSize(20);
		allMessage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		allMessageContainer.setClickable(true);
		allMessageContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Home.this, SentMessage.class);

				startActivity(intent);
			}
		});
		allMessageContainer.addView(allMessage);

		FrameLayout aboutContainer = new FrameLayout(this);
		aboutContainer.setPadding(0, 15, 0, 5);
		aboutContainer.setBackgroundColor(Color.DKGRAY);
		aboutContainer.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView aboutHelp = new TextView(this);
		aboutHelp.setText("Help & About");
		aboutHelp.setTypeface(null, Typeface.BOLD);
		aboutHelp.setTextSize(20);
		aboutHelp.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		aboutContainer.setClickable(true);
		aboutContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO: Re-override the launch activity
				Intent intent = new Intent(Home.this, About.class);
				startActivity(intent);
			}
		});
		aboutContainer.addView(aboutHelp);

		container.addView(composeContainer);
		container.addView(allMessageContainer);
		container.addView(aboutContainer);

		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(50);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);

		mainContainer.setLayoutAnimation(controller);

		mainContainer.addView(container);

		return mainContainer;
	}
}
