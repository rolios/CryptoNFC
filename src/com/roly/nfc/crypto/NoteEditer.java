package com.roly.nfc.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.roly.nfc.crypto.nfc.KeyPicker;
import com.roly.nfc.crypto.provider.NoteDatabaseHelper;
import com.roly.nfc.crypto.provider.NoteProvider;

@EActivity(R.layout.note_editer)
public class NoteEditer extends Activity{
	
	@ViewById
	EditText title;
	
	@ViewById
	EditText content;
	
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
			setResult(CryptoNFCActivity_.NOTE_VALIDATED);
			finish();
			break;

		default:
			break;
		}
	}
	
	@Click
	void saveNote(){
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
