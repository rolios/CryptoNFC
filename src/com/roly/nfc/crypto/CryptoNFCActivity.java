package com.roly.nfc.crypto;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.roly.nfc.crypto.nfc.TagWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class CryptoNFCActivity extends Activity {

	public static final int NOTE_VALIDATED=1;
	public static final int TAG_VALIDATED=2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		AdView adView = (AdView)this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
	}


	public void addNote(View v){
		Intent i = new Intent(this,NoteEditer.class);
		startActivityForResult(i, NOTE_VALIDATED);
	}


	public void viewNotes(View v){
		Intent i = new Intent(this,NoteList.class);
		startActivity(i);
	}


	public void writeTag(View v){
		Intent i = new Intent(this,TagWriter.class);
		startActivityForResult(i, NOTE_VALIDATED);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case NOTE_VALIDATED:
			Toast.makeText(this,"Note saved", 10).show();
			break;
		case TAG_VALIDATED:
			Toast.makeText(this,"Tag written", 10).show();
			break;
		default:
			break;
		}
	}
}