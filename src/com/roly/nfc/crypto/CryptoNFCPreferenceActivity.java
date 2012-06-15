package com.roly.nfc.crypto;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CryptoNFCPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.cryptonfc_preferences);
	}
	
	
}
