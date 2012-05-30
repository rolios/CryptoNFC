package com.roly.nfc.crypto.nfc;

import com.roly.nfc.crypto.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;

public abstract class AbstractTagHandler extends Activity{

	protected IntentFilter[] intentFiltersArray;
	protected String[][] techList; 
	protected PendingIntent pi;
	protected IntentFilter old_ndef2;
	protected NfcAdapter adapter;
	
	
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
    		action(intent);
		}
    }  
 
    protected abstract void action(Intent i);
    protected abstract void setForegroundListener();
}
