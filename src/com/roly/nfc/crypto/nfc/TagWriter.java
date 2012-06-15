package com.roly.nfc.crypto.nfc;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.roly.nfc.crypto.CryptoNFCActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;


public class TagWriter extends AbstractTagHandler{

    NdefRecord createRecord(byte[] key)
    {
    	char status = (char) (key.length);
    	// data correspond au futur payload
    	byte[] data = new byte[1 + key.length];
    	data[0] = (byte) status;
 
    	System.arraycopy(key, 0, data, 1, key.length);
    	return new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, "r0ly.fr:CryptoNFCKey".getBytes(), new byte[0], data);
    }
    
	@Override
	protected void action(Intent i) {
		// On a besoin ici d'une référence du tag détecté
		Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		try{
			SecretKey key = KeyGenerator.getInstance("DES").generateKey();

    		NdefRecord[] records = new NdefRecord[2];
    		records[0] = createRecord(key.getEncoded());
    		if(Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT)
    			records[1] = NdefRecord.createApplicationRecord("com.roly.nfc.crypto");
    		// On instancie un NdefMessage avec le NdefRecord que l'on vient de créer
    		NdefMessage message = new NdefMessage(records);
    		
    		if(super.isFormatNeeded()){
    			NdefFormatable formatable = NdefFormatable.get(tag);
    			formatable.connect();
    			formatable.format(message);
    			formatable.close();
    		}else{
    			// On envoie ainsi le NdefMessage vers le tag pour écrire le contenu du tag
        		Ndef ndef = Ndef.get(tag);
        		ndef.connect();
        		ndef.writeNdefMessage(message);
        		ndef.close();
    		}
    		setResult(CryptoNFCActivity.TAG_VALIDATED);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			finish();
		}
	}
	
	@Override
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
