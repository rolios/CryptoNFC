package com.roly.nfc.crypto.view;

import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.PatternMatcher;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.util.NfcUtils;
import com.roly.nfc.crypto.view.nfc.KeyPickerDialogFragment;

@EActivity(R.layout.activity_note_editer)
@OptionsMenu(R.menu.cryptonfc_addnote_action)
public class EditNoteActivity extends FragmentActivity{

    @ViewById
	EditText title;

    @ViewById
	EditText content;

    private KeyPickerDialogFragment dialogFragment;
    private NfcAdapter adapter;
    private IntentFilter[] intentFiltersArray;
    private PendingIntent pi;
    private String[][] techList;

    @OptionsItem(R.id.save_note)
    public void saveNote(){
        if(title.getText().length()<1)
            Toast.makeText(this, "Invalid title", Toast.LENGTH_LONG).show();
        else if(content.getText().length()<1)
            Toast.makeText(this, "Invalid content", Toast.LENGTH_LONG).show();
        else{
            FragmentManager fragmentManager = getSupportFragmentManager();
            dialogFragment.show(fragmentManager, "KeyPicker");
            registerNFC();
        }
    }

    @AfterViews
    public void init(){
        dialogFragment = new KeyPickerDialogFragment(){
            @Override
            public void onCancel(DialogInterface dialog) {
                super.onCancel(dialog);
                unregisterNFC();
            }
        };
        setForegroundListener();
        adapter = NfcAdapter.getDefaultAdapter(this);
    }

    public void registerNFC(){
        adapter.enableForegroundDispatch(this, pi, intentFiltersArray, techList);
    }

    public void unregisterNFC(){
        adapter.disableForegroundDispatch(this);
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

    protected void setForegroundListener() {
        adapter = NfcAdapter.getDefaultAdapter(this);
        pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter old_ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        old_ndef.addDataScheme("vnd.android.nfc");
        old_ndef.addDataAuthority("ext", null);
        old_ndef.addDataPath("/CryptoNFCKey", PatternMatcher.PATTERN_PREFIX);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addDataScheme("vnd.android.nfc");
        ndef.addDataAuthority("ext", null);
        ndef.addDataPath("/r0ly.fr:CryptoNFCKey",PatternMatcher.PATTERN_PREFIX);

        intentFiltersArray = new IntentFilter[] {old_ndef, ndef};
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] tagTechs = tag.getTechList();
            if(Arrays.asList(tagTechs).contains(Ndef.class.getName())){
                handle(intent);
            }else{
                Toast.makeText(this, "Tag not supported", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handle(Intent i){
        // On récupère les NdefMessage contenus dans le tag
        NdefMessage[] msgs= NfcUtils.getNdefMessages(i);
        // On récupère les NdefRecords dans chaque NdefMessage
        NdefRecord[][] records= NfcUtils.getNdefRecords(msgs);
        // Le payload d'un NdefRecord est le contenu recherché
        byte[] payload=records[0][0].getPayload();

        int keyLength = payload[0] & 0077;
        byte[] key = new byte[keyLength];
        try{
            System.arraycopy(payload, 1, key, 0, keyLength);
        }catch (ArrayIndexOutOfBoundsException e) {
            //setResult(KEY_NOT_RETRIEVED);
            finish();
        }

        Intent data = new Intent();
        data.putExtra("key", key);
        //setResult(KEY_RETRIEVED, data);
        finish();
    }

}
