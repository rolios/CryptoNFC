package com.roly.nfc.crypto;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.roly.nfc.crypto.nfc.TagWriter;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

@EActivity(R.layout.main)
public class CryptoNFCActivity extends Activity {

	public static final int NOTE_VALIDATED=1;
	public static final int TAG_VALIDATED=2;
	
	@Click
	void addNote(){
		Intent i = new Intent(this,NoteEditer_.class);
		startActivityForResult(i, NOTE_VALIDATED);
	}
	
	@Click
	void viewNotes(){
		Intent i = new Intent(this,NoteList.class);
		startActivity(i);
	}
	
	@Click
	void writeTag(){
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