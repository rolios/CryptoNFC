package com.roly.nfc.crypto.ui.activity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.SecretKey;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.ui.fragment.KeyPickerDialogFragment;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.util.NfcTagUtils;
import com.roly.nfc.crypto.util.NfcUtils;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.cryptonfc_menu)
public class MainActivity extends FragmentActivity {

    private IntentFilter[] intentFiltersArray;
    private PendingIntent pi;
    private String[][] techList;
    private NfcAdapter adapter;
    private KeyPickerDialogFragment dialogFragment;

    @OptionsItem(R.id.settings)
	public void viewPreferences(){
		startActivity(new Intent(this,PreferencesActivity.class));
	}

    @Click(R.id.menu_add_note)
	public void addNote(){
		Intent i = new Intent(this,EditNoteActivity_.class);
		startActivity(i);
	}

    @Click(R.id.menu_view_notes)
	public void viewNotes(){
		Intent i = new Intent(this,NoteListActivity_.class);
		startActivity(i);
	}

    @Click(R.id.menu_get_code)
    public void getCode(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OlivierGonthier/CryptoNFC"));
        startActivity(intent);
    }

    @AfterViews
    public void init(){
        dialogFragment = new KeyPickerDialogFragment();
        setForegroundListener();
        adapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Click(R.id.menu_write_key)
	public void writeKey(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "Key Picker");
        registerNFC();
    }

    public void registerNFC(){
        adapter.enableForegroundDispatch(this, pi, intentFiltersArray, techList);
    }

    public void unregisterNFCIfNeeded() {
        if (adapter != null && adapter.isEnabled()) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialogFragment.isVisible()) {
            registerNFC();
        } else {
            unregisterNFCIfNeeded();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter.isEnabled()) {
            unregisterNFCIfNeeded();
        }
    }

    private void setForegroundListener() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean handleFormatable = preferences.getBoolean("format_ndef_formatable_tags", false);

        pi = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        intentFiltersArray = null;
        if(handleFormatable)
            techList = new String[][]{ new String[]{ NfcA.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcB.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcF.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcV.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcA.class.getName(),NdefFormatable.class.getName()},
                    new String[]{ NfcB.class.getName(),NdefFormatable.class.getName()},
                    new String[]{ NfcF.class.getName(),NdefFormatable.class.getName()},
                    new String[]{ NfcV.class.getName(),NdefFormatable.class.getName()}};
        else
            techList = new String[][]{ new String[]{ NfcA.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcB.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcF.class.getName(),Ndef.class.getName()},
                    new String[]{ NfcV.class.getName(),Ndef.class.getName()}};
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] tagTechs = tag.getTechList();
            if (Arrays.asList(tagTechs).contains(Ndef.class.getName())) {
                writeKeyOnTag(tag, false);
            } else if (Arrays.asList(tagTechs).contains(NdefFormatable.class.getName())) {
                writeKeyOnTag(tag, true);
            } else {
                Toast.makeText(this, "Tag not supported", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Background
    public void writeKeyOnTag(Tag tag, boolean formatNeeded) {
        SecretKey key;
        try {
            key = EncryptionUtils.generateKey();
        } catch (NoSuchAlgorithmException e) {
            error("An error occured while generating a key");
            return;
        }

        NdefRecord[] records;
        NdefRecord keyRecord = NfcUtils.createRecord(key.getEncoded());
        NdefRecord appRecord = NfcTagUtils.writeApplicationRecordIfPossible();
        if(appRecord != null){
            records = new NdefRecord[]{keyRecord, appRecord};
        } else {
            records = new NdefRecord[]{keyRecord};
        }

        NdefMessage message = new NdefMessage(records);

        if(formatNeeded){
            NdefFormatable formatable = NdefFormatable.get(tag);

            try {
                formatable.connect();
                formatable.format(message);
            } catch (IOException | FormatException e) {
                error("An error occured while formatting your tag");
                return;
            } finally{
                try {
                    formatable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Ndef ndef = Ndef.get(tag);
            try {
                ndef.connect();
                ndef.writeNdefMessage(message);
            } catch (IOException | FormatException e) {
                error("An error occured while writing key on tag");
                return;
            } finally{
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        success();
    }

    @UiThread
    public void success() {
        Toast.makeText(this, "A new key has been written on your tag!", Toast.LENGTH_LONG).show();
        dialogFragment.dismiss();
    }

    @UiThread
    public void error(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        dialogFragment.dismiss();
    }

}
