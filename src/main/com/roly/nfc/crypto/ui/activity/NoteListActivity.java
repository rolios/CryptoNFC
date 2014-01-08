package com.roly.nfc.crypto.ui.activity;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.PatternMatcher;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.ui.fragment.KeyPickerDialogFragment;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.util.NfcUtils;

@EActivity(R.layout.activity_note_list)
public class NoteListActivity extends FragmentActivity{

    private IntentFilter[] intentFiltersArray;
    private PendingIntent pi;
    private NfcAdapter adapter;
    private KeyPickerDialogFragment dialogFragment;
    private long selectedId;
    private String selectedTitle;
    private String selectedContent;
    private String content;

    @AfterViews
    public void init(){
        dialogFragment = new KeyPickerDialogFragment();
        setForegroundListener();
        adapter = NfcAdapter.getDefaultAdapter(this);
    }

    public void askTag(long id, String title, String content){
        selectedId = id;
        selectedTitle = title;
        selectedContent = content;
        registerNFC();
        dialogFragment.show(getSupportFragmentManager(), "Key picker");
    }

    private void registerNFC(){
        adapter.enableForegroundDispatch(this, pi, intentFiltersArray, null);
    }

    private void unregisterNFCIfNeeded(){
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
            readKeyOnTag(intent);
        }
    }

    @Background
    public void readKeyOnTag(Intent i){
        NdefMessage[] msgs= NfcUtils.getNdefMessages(i);
        NdefRecord[][] records= NfcUtils.getNdefRecords(msgs);
        byte[] payload=records[0][0].getPayload();

        int keyLength = payload[0] & 0077;
        byte[] keyData = new byte[keyLength];
        try{
            System.arraycopy(payload, 1, keyData, 0, keyLength);
        }catch (ArrayIndexOutOfBoundsException e) {
            error("An error occured while reading your keyData");
            return;
        }

        SecretKey key = new SecretKeySpec(keyData, "DES");

        try{
            content = EncryptionUtils.decrypt(key, selectedContent);
        }catch(Exception e){
            error("An error occured. Are you using the right Tag?");
            return;
        }

        success();
    }

    @UiThread
    public void success() {
        dialogFragment.dismiss();
        Intent intent = new Intent(this, EditNoteActivity_.class);
        intent.putExtra("id", selectedId);
        intent.putExtra("title", selectedTitle);
        intent.putExtra("content", content);
        startActivity(intent);
    }

    @UiThread
    public void error(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        dialogFragment.dismiss();
    }

}
