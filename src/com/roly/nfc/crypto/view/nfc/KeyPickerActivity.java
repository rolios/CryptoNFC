package com.roly.nfc.crypto.view.nfc;

import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.widget.Toast;

import com.roly.nfc.crypto.R;

public class KeyPickerActivity extends Activity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techList;
    private PendingIntent pi;
    private IntentFilter old_ndef2;
    private NfcAdapter adapter;
    private boolean formatNeeded=false;

    public static final int KEY_NOT_RETRIEVED=-1;
    public static final int KEY_RETRIEVED=1;

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

    protected void action(Intent i){
        // On récupère les NdefMessage contenus dans le tag
        NdefMessage[] msgs= getNdefMessages(i);
        // On récupère les NdefRecords dans chaque NdefMessage
        NdefRecord[][] records= getNdefRecords(msgs);
        // Le payload d'un NdefRecord est le contenu recherché
        byte[] payload=records[0][0].getPayload();

        int keyLength = payload[0] & 0077;
        byte[] key = new byte[keyLength];
        try{
            System.arraycopy(payload, 1, key, 0, keyLength);
        }catch (ArrayIndexOutOfBoundsException e) {
            setResult(KEY_NOT_RETRIEVED);
        }
        Intent data = new Intent();
        data.putExtra("key", key);

        setResult(KEY_RETRIEVED, data);
        finish();
    }

    /**
     * Récupère la liste des NdefRecords contenus dans chaque NdefMessages
     */
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

    /**
     *  Récupère la lise des NdefMessages contenus dans le tag
     */
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

    protected void setForegroundListener() {
        pi = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter old_ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try{
            old_ndef.addDataType("*/*");
        }catch (MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        IntentFilter old_ndef2 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        old_ndef2.addDataScheme("vnd.android.nfc");
        old_ndef2.addDataAuthority("ext", null);
        old_ndef2.addDataPath("/CryptoNFCKey",PatternMatcher.PATTERN_PREFIX);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        old_ndef2.addDataScheme("vnd.android.nfc");
        old_ndef2.addDataAuthority("ext", null);
        old_ndef2.addDataPath("/r0ly.fr:CryptoNFCKey",PatternMatcher.PATTERN_PREFIX);

        intentFiltersArray = new IntentFilter[] {old_ndef2, old_ndef, ndef};
        techList = null;
    }

}
