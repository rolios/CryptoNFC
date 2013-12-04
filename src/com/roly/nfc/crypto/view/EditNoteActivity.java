package com.roly.nfc.crypto.view;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;
import com.roly.nfc.crypto.utils.Encryption;
import com.roly.nfc.crypto.view.nfc.KeyPickerActivity;

public class EditNoteActivity extends Activity{
	
	private EditText mTitle;
	private EditText mContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.note_editer);
		mTitle = (EditText)findViewById(R.id.title);
		mContent = (EditText)findViewById(R.id.content);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		
		case KeyPickerActivity.KEY_RETRIEVED:
			SecretKey key = new SecretKeySpec(data.getByteArrayExtra("key"), "DES");
			
			String content = mContent.getText().toString();
			
			try {
				content = Encryption.encrypt(key, content);
			} catch (Exception e) {
				setResult(CryptoNFCHomeActivity.NOTE_ERROR);
				finish();
			}
			
			ContentValues values = new ContentValues(2);
			values.put(NoteDatabase.KEY_TITLE, mTitle.getText().toString());
			values.put(NoteDatabase.KEY_BODY, content);
			getContentResolver().insert(NoteProvider.CONTENT_URI, values);
			
			setResult(CryptoNFCHomeActivity.NOTE_VALIDATED);
			finish();
			break;
		case KeyPickerActivity.KEY_NOT_RETRIEVED:
			Toast.makeText(this, "The tag you are using is not well formatted.", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}
	
	public void saveNote(View v){
		if(mTitle.getText().length()<1)
			Toast.makeText(this, "Invalid title!", Toast.LENGTH_LONG).show();
		else if(mContent.getText().length()<1)
			Toast.makeText(this, "Invalid content!", Toast.LENGTH_LONG).show();
		else{
			Intent i = new Intent(this, KeyPickerActivity.class);
			startActivityForResult(i, KeyPickerActivity.KEY_RETRIEVED);
		}	
	}
}
