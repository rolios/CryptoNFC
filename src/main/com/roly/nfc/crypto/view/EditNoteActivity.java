package com.roly.nfc.crypto.view;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.view.nfc.KeyPickerDialogFragment;

@EActivity(R.layout.activity_note_editer)
@OptionsMenu(R.menu.cryptonfc_addnote_action)
public class EditNoteActivity extends Activity{

    @ViewById
	EditText title;

    @ViewById
	EditText content;

    @OptionsItem(R.id.save_note)
    public void saveNote(){
        if(title.getText().length()<1)
            Toast.makeText(this, "Invalid title!", Toast.LENGTH_LONG).show();
        else if(content.getText().length()<1)
            Toast.makeText(this, "Invalid content!", Toast.LENGTH_LONG).show();
        else{
            Intent i = new Intent(this, KeyPickerDialogFragment.class);
            startActivityForResult(i, KeyPickerDialogFragment.KEY_RETRIEVED);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {

		case KeyPickerDialogFragment.KEY_RETRIEVED:
			SecretKey key = new SecretKeySpec(data.getByteArrayExtra("key"), "DES");

			String content = this.content.getText().toString();

			try {
				content = EncryptionUtils.encrypt(key, content);
			} catch (Exception e) {
				setResult(CryptoNFCHomeActivity.NOTE_ERROR);
				finish();
			}

			ContentValues values = new ContentValues(2);
			values.put(NoteDatabase.KEY_TITLE, title.getText().toString());
			values.put(NoteDatabase.KEY_BODY, content);
			getContentResolver().insert(NoteProvider.CONTENT_URI, values);

			setResult(CryptoNFCHomeActivity.NOTE_VALIDATED);
			finish();
			break;
		case KeyPickerDialogFragment.KEY_NOT_RETRIEVED:
			Toast.makeText(this, "The tag you are using is not well formatted.", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}
}
