package com.roly.nfc.crypto.ui.activity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.util.NfcUtils;
import com.roly.nfc.crypto.ui.fragment.KeyPickerDialogFragment;

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

    private String contentValue;
    private String titleValue;

    @OptionsItem(R.id.save_note)
    public void saveNote(){
        if(title.getText().length()<1)
            Toast.makeText(this, "Invalid title", Toast.LENGTH_LONG).show();
        else if(content.getText().length()<1)
            Toast.makeText(this, "Invalid content", Toast.LENGTH_LONG).show();
        else{
            contentValue = content.getText().toString();
            titleValue = title.getText().toString();

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
        if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(messages.length == 0){
                error("The tag you are using doesn't contains any inforations");
                return;
            }
            extractKey(messages);
        }
    }

    @Background
    public void extractKey(Parcelable[] messages){
        NdefMessage ndef = (NdefMessage) messages[0];
        NdefRecord[] records= NfcUtils.getNdefRecords(ndef);
        byte[] payload=records[0].getPayload();

        int keyLength = payload[0] & 0077;
        byte[] keyData = new byte[keyLength];
        try{
            System.arraycopy(payload, 1, keyData, 0, keyLength);
        }catch (ArrayIndexOutOfBoundsException e) {
            error("An error occured while reading key on your tag");
            return;
        }

        SecretKey key = new SecretKeySpec(keyData, "DES");
        String encrypted;
        try {
            encrypted = EncryptionUtils.encrypt(key, contentValue);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            error("An error occured while encrypting text content");
            return;
        }

        ContentValues values = new ContentValues(2);
        values.put(NoteDatabase.KEY_TITLE, titleValue);
        values.put(NoteDatabase.KEY_BODY, encrypted);
        Uri insert = getContentResolver().insert(NoteProvider.CONTENT_URI, values);
        if(insert != null){
            success(encrypted);
        } else {
            error("An error occured while saving note");
        }
    }

    @UiThread
    public void success(String encrypted) {
        Toast.makeText(this, "The note has been encrypted and saved.", Toast.LENGTH_LONG).show();
        dialogFragment.dismiss();
        content.setText(encrypted);
    }

    @UiThread
    public void error(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        dialogFragment.dismiss();
    }

}
