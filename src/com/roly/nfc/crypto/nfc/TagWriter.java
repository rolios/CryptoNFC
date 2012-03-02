package com.roly.nfc.crypto.nfc;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import com.roly.nfc.crypto.CryptoNFCActivity_;

public class TagWriter extends AbstractTagHandler{

    NdefRecord createRecord(byte[] key)
    {
    	//Log.d("ROLY", "Key Before: " + byteArrayToHexString(key));
    	char status = (char) (key.length);
    	// data correspond au futur payload
    	byte[] data = new byte[1 + key.length];
    	data[0] = (byte) status;
 
    	System.arraycopy(key, 0, data, 1, key.length);
    	// On retourne le NdefRecord avec les informations sur le contenu enregistré
    	//Log.d("ROLY", "Payload before: " + byteArrayToHexString(data));
    	//Log.d("ROLY", "length before: "+ key.length);
    	return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

//    static String byteArrayToHexString(byte[] bArray){
//        StringBuffer buffer = new StringBuffer();
//     
//        for(byte b : bArray) {
//          buffer.append(Integer.toHexString(b));
//          buffer.append(" ");
//        }
//     
//        return buffer.toString().toUpperCase();    
//      }
    
	@Override
	protected void action(Intent i) {
		// On a besoin ici d'une référence du tag détecté
		Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		try{
			SecretKey key = KeyGenerator.getInstance("DES").generateKey();
			//Log.d("ROLY", key.getEncoded().toString());
    		NdefRecord[] records = {createRecord(key.getEncoded())};
    		// On instancie un NdefMessage avec le NdefRecord que l'on vient de créer
    		NdefMessage message = new NdefMessage(records);
    		
    		// On envoie ainsi le NdefMessage vers le tag pour écrire le contenu du tag
    		Ndef ndef = Ndef.get(tag);
    		ndef.connect();
    		ndef.writeNdefMessage(message);
    		ndef.close();
    		setResult(CryptoNFCActivity_.TAG_VALIDATED);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			finish();
		}
	}
}
