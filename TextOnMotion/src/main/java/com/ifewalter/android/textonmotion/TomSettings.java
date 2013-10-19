package com.ifewalter.android.textonmotion;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TomSettings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("TextOnMotion Settings");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tom_settings);

	}
}
