package com.roly.nfc.crypto.ui.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.roly.nfc.crypto.R;

public class CryptoNFCPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.cryptonfc_preferences);
	}
	
	
}
