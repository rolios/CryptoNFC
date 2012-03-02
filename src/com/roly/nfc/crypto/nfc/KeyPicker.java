package com.roly.nfc.crypto.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;


public class KeyPicker extends AbstractTagHandler{

	public static final int KEY_RETRIEVED=1;
	
	@Override
	protected void action(Intent i) {
		// On récupère les NdefMessage contenus dans le tag
		NdefMessage[] msgs= getNdefMessages(i);
		// On récupère les NdefRecords dans chaque NdefMessage
		NdefRecord[][] records= getNdefRecords(msgs);
		// Le payload d'un NdefRecord est le contenu recherché
		byte[] payload=records[0][0].getPayload(); 
		//Log.d("ROLY", "Payload after:" + byteArrayToHexString(payload));

		int keyLength = payload[0] & 0077;
		//Log.d("ROLY", "Length after:"+ keyLength);
		byte[] key = new byte[keyLength];
		System.arraycopy(payload, 1, key, 0, keyLength);
		//Log.d("ROLY", "Key after:" + byteArrayToHexString(key));
		
		Intent data = new Intent();
		data.putExtra("key", key);

		setResult(KEY_RETRIEVED, data);
		finish();
		
	}
	
//    public static String byteArrayToHexString(byte[] bArray){
//        StringBuffer buffer = new StringBuffer();
//     
//        for(byte b : bArray) {
//          buffer.append(Integer.toHexString(b));
//          buffer.append(" ");
//        }
//     
//        return buffer.toString().toUpperCase();    
//      }
    
//
//	Récupère la liste des NdefRecords contenus dans chaque NdefMessages
//	
	public NdefRecord[][] getNdefRecords(NdefMessage[] msgs) {
		NdefRecord[][] records=null;
		if(msgs!=null){
			records = new NdefRecord[msgs.length][];
			for(int i=0; i<msgs.length; i++){
				records[i]= new NdefRecord[msgs[i].getRecords().length];
				records[i]= msgs[i].getRecords();
			}
		}
		return records;
	}
	
//
//	Récupère la lise des NdefMessages contenus dans le tag
//
	public NdefMessage[] getNdefMessages(Intent intent){
		NdefMessage[] messages=null;
		// On récupère les objets
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs != null) {
			messages = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				// On cast les les objets en NdefMessage
				messages[i] = (NdefMessage) rawMsgs[i];
			}
		} else {
        // Type du tag inconnu
            byte[] empty = new byte[] {};
            // On crée un faux NdefMessage qui a contenu vide
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
            NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
            messages = new NdefMessage[] {msg};
        }
		
		return messages;
	}
    
}
