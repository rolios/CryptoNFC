package com.roly.nfc.crypto.nfc;

import java.util.Arrays;

import com.roly.nfc.crypto.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.widget.Toast;

public abstract class AbstractTagHandler extends Activity{

	protected IntentFilter[] intentFiltersArray;
	protected String[][] techList; 
	protected PendingIntent pi;
	protected IntentFilter old_ndef2;
	protected NfcAdapter adapter;
	private boolean formatNeeded=false;
	
    public boolean isFormatNeeded() {
		return formatNeeded;
	}

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
 
    protected abstract void action(Intent i);
    protected abstract void setForegroundListener();
}
