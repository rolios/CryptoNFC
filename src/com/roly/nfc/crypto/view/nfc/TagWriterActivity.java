package com.roly.nfc.crypto.view.nfc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.view.CryptoNFCHomeActivity;

public class TagWriterActivity extends Activity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techList;
    private PendingIntent pi;
    private IntentFilter old_ndef2;
    private NfcAdapter adapter;
    private boolean formatNeeded=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tag_handler);

        adapter = NfcAdapter.getDefaultAdapter(this);

        setForegroundListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.enableForegroundDispatch(this, pi, intentFiltersArray, techList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] tagTechs = tag.getTechList();
            if(Arrays.asList(tagTechs).contains(Ndef.class.getName())){
                formatNeeded=false;
                action(intent);
            }else if(Arrays.asList(tagTechs).contains(NdefFormatable.class.getName())){
                formatNeeded=true;
                action(intent);
            }else{
                Toast.makeText(this, "Tag not supported", Toast.LENGTH_LONG).show();
            }
        }
    }


    NdefRecord createRecord(byte[] key)
	{
		char status = (char) (key.length);
		// data correspond au futur payload
		byte[] data = new byte[1 + key.length];
		data[0] = (byte) status;

		System.arraycopy(key, 0, data, 1, key.length);
		return new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, "r0ly.fr:CryptoNFCKey".getBytes(), new byte[0], data);
	}

	protected void action(Intent i) {
		// On a besoin ici d'une référence du tag détecté
		Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		SecretKey key;
		try {
			key = KeyGenerator.getInstance("DES").generateKey();
		} catch (NoSuchAlgorithmException e) {
			finishOnError(e);
			return;
		}

		NdefRecord[] records = new NdefRecord[2];
		records[0] = createRecord(key.getEncoded());
		if(Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT)
			records[1] = NdefRecord.createApplicationRecord("com.roly.nfc.crypto");

		// On instancie un NdefMessage avec le NdefRecord que l'on vient de créer
		NdefMessage message = new NdefMessage(records);

		if(formatNeeded){
			NdefFormatable formatable = NdefFormatable.get(tag);

			try {
				formatable.connect();
				formatable.format(message);
			} catch (IOException e) {
				finishOnError(e);
				return;
			} catch (FormatException e) {
				finishOnError(e);
				return;
			}finally{
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
			} catch (IOException e) {
				finishOnError(e);
				return;
			} catch (FormatException e) {
				finishOnError(e);
				return;
			}finally{
				try {
					ndef.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		setResult(CryptoNFCHomeActivity.TAG_VALIDATED);
		finish();
	}

	public void finishOnError(Exception e){
		setResult(CryptoNFCHomeActivity.TAG_ERROR);
		finish();
	}

	protected void setForegroundListener() {
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
}
