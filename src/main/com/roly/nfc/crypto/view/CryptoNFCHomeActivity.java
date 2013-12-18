package com.roly.nfc.crypto.view;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.SecretKey;

import android.app.PendingIntent;
import android.content.DialogInterface;
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

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.util.NfcTagUtils;
import com.roly.nfc.crypto.util.NfcUtils;
import com.roly.nfc.crypto.view.nfc.KeyPickerDialogFragment;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.cryptonfc_menu)
public class CryptoNFCHomeActivity extends FragmentActivity {

	public static final int TAG_ERROR=-2;
	public static final int NOTE_ERROR=-1;
	public static final int NOTE_VALIDATED=1;
	public static final int TAG_VALIDATED=2;

    private IntentFilter[] intentFiltersArray;
    private PendingIntent pi;
    private String[][] techList;
    private NfcAdapter adapter;
    private KeyPickerDialogFragment dialogFragment;

    @OptionsItem(R.id.settings)
	public void viewPreferences(){
		startActivity(new Intent(this,CryptoNFCPreferenceActivity.class));
	}

    @Click(R.id.menu_add_note)
	public void addNote(){
		Intent i = new Intent(this,EditNoteActivity_.class);
		startActivityForResult(i, NOTE_VALIDATED);
	}

    @Click(R.id.menu_view_notes)
	public void viewNotes(){
		Intent i = new Intent(this,NoteListActivity.class);
		startActivity(i);
	}

    @Click(R.id.menu_get_code)
    public void getCode(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OlivierGonthier/"));
        startActivity(intent);
    }

    @Click(R.id.menu_write_key)
	public void writeKey(){
        if(dialogFragment == null){

            dialogFragment = new KeyPickerDialogFragment(){
                @Override
                public void onCancel(DialogInterface dialog) {
                    super.onCancel(dialog);
                    unregisterNFC();
                }
            };

            setForegroundListener();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "Key Picker");
        registerNFC();
    }

    public void registerNFC(){
        adapter.enableForegroundDispatch(this, pi, intentFiltersArray, techList);
    }

    public void unregisterNFC(){
        adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialogFragment!=null && dialogFragment.isVisible()) {
            registerNFC();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter!=null && adapter.isEnabled()) {
            unregisterNFC();
        }
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case NOTE_VALIDATED:
			Toast.makeText(this,"Note saved", Toast.LENGTH_SHORT).show();
			break;
		case TAG_VALIDATED:
			Toast.makeText(this,"Tag written", Toast.LENGTH_SHORT).show();
			break;
		case TAG_ERROR:
			Toast.makeText(this, "An error occured while writing key on tag", Toast.LENGTH_SHORT).show();
			break;
		case NOTE_ERROR:
			Toast.makeText(this, "An error occured while saving note", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}

    private void setForegroundListener() {
        adapter = NfcAdapter.getDefaultAdapter(this);
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

    private void writeKeyOnTag(Tag tag, boolean formatNeeded) {
        SecretKey key;
        try {
            key = EncryptionUtils.generateKey();
        } catch (NoSuchAlgorithmException e) {
            //finishOnError(e);
            return;
        }

        NdefRecord[] records = new NdefRecord[2];
        records[0] = NfcUtils.createRecord(key.getEncoded());
        NfcTagUtils.writeApplicationRecordIfPossible(records[1]);

        NdefMessage message = new NdefMessage(records);

        if(formatNeeded){
            NdefFormatable formatable = NdefFormatable.get(tag);

            try {
                formatable.connect();
                formatable.format(message);
            } catch (IOException | FormatException e) {
                //finishOnError(e);
                return;
            } finally{
                try {
                    formatable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            // On envoie ainsi le NdefMessage vers le tag pour écrire le contenu du tag
            Ndef ndef = Ndef.get(tag);
            try {
                ndef.connect();
                ndef.writeNdefMessage(message);
            } catch (IOException | FormatException e) {
                //finishOnError(e);
            } finally{
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //setResult(CryptoNFCHomeActivity.TAG_VALIDATED);
        //finish();
    }


}
