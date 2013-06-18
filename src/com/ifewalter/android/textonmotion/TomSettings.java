package com.ifewalter.android.textonmotion;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class TomSettings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("TextOnMotion Settings");
		super.onCreate(savedInstanceState);

		// setPreferenceScreen(createPreferenceHierarchy());
		addPreferencesFromResource(R.xml.tom_settings);
		
	}

	// private PreferenceScreen createPreferenceHierarchy() {
	// // Root
	// PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
	// this);
	//
	// PreferenceCategory AboutHelpPrefCat = new PreferenceCategory(this);
	// AboutHelpPrefCat.setTitle("Help & About");
	// root.addPreference(AboutHelpPrefCat);
	//
	// // Help and about
	// PreferenceScreen helpAboutIntent = getPreferenceManager()
	// .createPreferenceScreen(this);
	// helpAboutIntent.setIntent(new Intent().setClass(
	// getApplicationContext(), About.class));
	// helpAboutIntent.setTitle("Help & About");
	// helpAboutIntent.setSummary("TextOnMotion guide");
	// AboutHelpPrefCat.addPreference(helpAboutIntent);
	//
	// // Preformance settgins category
	// PreferenceCategory performancePrefCat = new PreferenceCategory(this);
	// performancePrefCat.setTitle("Performance Settings");
	// root.addPreference(performancePrefCat);
	//
	// // location (new screen)
	// PreferenceScreen LocationPref = getPreferenceManager()
	// .createPreferenceScreen(this);
	// LocationPref.setKey("screen_preference");
	// LocationPref.setTitle("Location Settings");
	// LocationPref.setSummary("Change location preferences");
	// performancePrefCat.addPreference(LocationPref);
	// // location settings
	// CheckBoxPreference nextScreenCheckBoxPref = new CheckBoxPreference(this);
	// nextScreenCheckBoxPref.setKey("location_auto_acquire");
	// nextScreenCheckBoxPref.setTitle("Auto-Acquire");
	// nextScreenCheckBoxPref.setDefaultValue(true);
	// nextScreenCheckBoxPref
	// .setSummary("Acquire Location automatically on application start?");
	// LocationPref.addPreference(nextScreenCheckBoxPref);
	// // location provider
	// ListPreference listPref = new ListPreference(this);
	// listPref.setEntries(new String[]{"GPS", "Network"});
	// // listPref.setDependency("location_auto_acquire");
	// listPref.setEntryValues(new String[]{"GPS", "Network"});
	// listPref.setDialogTitle("Location Provider");
	// listPref.setDefaultValue("GPS");
	// listPref.setKey("location_provider");
	// listPref.setTitle("Location provider");
	// listPref.setSummary("How should location be acquired?");
	// LocationPref.addPreference(listPref);
	//
	// return root;
	// }

}
