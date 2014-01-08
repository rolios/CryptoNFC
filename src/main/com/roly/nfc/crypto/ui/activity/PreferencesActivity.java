package com.roly.nfc.crypto.ui.activity;

import android.os.Bundle;

import com.roly.nfc.crypto.R;

public class PreferencesActivity extends android.preference.PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.cryptonfc_preferences);
	}
	
	
}
