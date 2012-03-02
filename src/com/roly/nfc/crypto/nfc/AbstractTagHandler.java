package com.roly.nfc.crypto.nfc;

import com.roly.nfc.crypto.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.os.Bundle;

public abstract class AbstractTagHandler extends Activity{

	private IntentFilter[] intentFiltersArray;
	private String[][] techList; 
	private PendingIntent pi;
	private IntentFilter ndef;
	private NfcAdapter adapter;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tag_handler);
        
        adapter = NfcAdapter.getDefaultAdapter(this);
        
        // On utilise ici le Foreground dispatch
        pi = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);     
       	ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);  
        try{
        	ndef.addDataType("*/*");
        }catch (MalformedMimeTypeException e) {
			e.printStackTrace();
		}
        
        intentFiltersArray = new IntentFilter[] {ndef};     
        techList = null;
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
    	if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
    		action(intent);

		}
    }  
 
    protected abstract void action(Intent i);
}
