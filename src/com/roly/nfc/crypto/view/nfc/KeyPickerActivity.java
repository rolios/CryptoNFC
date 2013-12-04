package com.roly.nfc.crypto.view.nfc;

import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.widget.Toast;

import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.util.NfcUtils;

public class KeyPickerActivity extends Activity {

    private IntentFilter[] intentFiltersArray;
    private PendingIntent pi;
    private NfcAdapter adapter;

    public static final int KEY_NOT_RETRIEVED=-1;
    public static final int KEY_RETRIEVED=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_handler);
        adapter = NfcAdapter.getDefaultAdapter(this);
        setForegroundListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.enableForegroundDispatch(this, pi, intentFiltersArray, null);
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
                handle(intent);
            }else if(Arrays.asList(tagTechs).contains(NdefFormatable.class.getName())){
                //handle(intent);
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
            setResult(KEY_NOT_RETRIEVED);
            finish();
        }

        Intent data = new Intent();
        data.putExtra("key", key);
        setResult(KEY_RETRIEVED, data);
        finish();
    }

    protected void setForegroundListener() {
        pi = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter old_ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        old_ndef.addDataScheme("vnd.android.nfc");
        old_ndef.addDataAuthority("ext", null);
        old_ndef.addDataPath("/CryptoNFCKey",PatternMatcher.PATTERN_PREFIX);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addDataScheme("vnd.android.nfc");
        ndef.addDataAuthority("ext", null);
        ndef.addDataPath("/r0ly.fr:CryptoNFCKey",PatternMatcher.PATTERN_PREFIX);

        intentFiltersArray = new IntentFilter[] {old_ndef, ndef};
    }

}
