package com.roly.nfc.crypto.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.roly.nfc.crypto.R;

public class DecipheredNoteActivity extends Activity {

	TextView title_details;
	TextView content_details;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_details);
		title_details=(TextView) findViewById(R.id.title_details);
		content_details=(TextView) findViewById(R.id.content_details);
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			title_details.setText(extras.getString("title"));
			content_details.setText(extras.getString("content"));
		}else 
			finish();
	}
	
}
