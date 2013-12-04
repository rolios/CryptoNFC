package com.roly.nfc.crypto.util;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

public class NfcUtils {

    private NfcUtils(){}

    /**
     * Récupère la liste des NdefRecords contenus dans chaque NdefMessages
     */
    public static NdefRecord[][] getNdefRecords(NdefMessage[] msgs) {
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

    /**
     *  Récupère la lise des NdefMessages contenus dans le tag
     */
    public static NdefMessage[] getNdefMessages(Intent intent){
        NdefMessage[] messages;
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

    public static NdefRecord createRecord(byte[] key)
	{
		char status = (char) (key.length);
		// data correspond au futur payload
		byte[] data = new byte[1 + key.length];
		data[0] = (byte) status;

		System.arraycopy(key, 0, data, 1, key.length);
		return new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, "r0ly.fr:CryptoNFCKey".getBytes(), new byte[0], data);
	}
}
