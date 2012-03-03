package com.roly.nfc.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.roly.nfc.crypto.nfc.KeyPicker;
import com.roly.nfc.crypto.provider.NoteDatabaseHelper;
import com.roly.nfc.crypto.provider.NoteProvider;

public class NoteEditer extends Activity{
	
	EditText title;

	EditText content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.note_editer);
		title = (EditText)findViewById(R.id.title);
		content = (EditText)findViewById(R.id.content);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case KeyPicker.KEY_RETRIEVED:
			SecretKey key = new SecretKeySpec(data.getByteArrayExtra("key"), "DES");
			//Log.d("ROLY","Key retrieved: " + KeyPicker.byteArrayToHexString(key.getEncoded()));
			DesEncrypter crypter= new DesEncrypter(key);
			ContentValues values = new ContentValues(2);
			values.put(NoteDatabaseHelper.KEY_TITLE, title.getText().toString());
			String mContent = content.getText().toString();
			mContent = crypter.encrypt(mContent);
			values.put(NoteDatabaseHelper.KEY_BODY, mContent);
			getContentResolver().insert(NoteProvider.CONTENT_URI, values);
			setResult(CryptoNFCActivity.NOTE_VALIDATED);
			finish();
			break;

		default:
			break;
		}
	}
	
	public void saveNote(View v){
		if(title.getText().length()<0)
			Toast.makeText(this, "Invalid title!", 10).show();
		else if(content.getText().length()<0)
			Toast.makeText(this, "Invalid content!", 10).show();
		else{
			Intent i = new Intent(this, KeyPicker.class);
			startActivityForResult(i, KeyPicker.KEY_RETRIEVED);
		}
			
	}
}
