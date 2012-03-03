package com.roly.nfc.crypto;

import android.widget.AdapterView.OnItemLongClickListener;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.roly.nfc.crypto.nfc.KeyPicker;
import com.roly.nfc.crypto.provider.NoteDatabaseHelper;
import com.roly.nfc.crypto.provider.NoteProvider;

public class NoteList extends ListActivity{

	private long selected;
	private AlertDialog alert;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 this.setTitle("Select note for uncipher");
		 Cursor mCursor = this.managedQuery(NoteProvider.CONTENT_URI, null, null, null, null);
		 this.setListAdapter(new CustomAdapter(this,mCursor));
		 
		 final CharSequence[] options = {"Delete"};

		 AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setTitle("Select an action");
		 builder.setItems(options, new DialogInterface.OnClickListener() {
		     public void onClick(DialogInterface dialog, int item) {
		    	 Uri noteUri = ContentUris.withAppendedId(NoteProvider.CONTENT_URI, selected);
				 getContentResolver().delete(noteUri, null, null);
		     }
		 });
		 alert = builder.create();
		 
		 ListView list = getListView();
		list.setOnItemLongClickListener(new OnItemLongClickListener() {


				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View v,
						int position, long id) {
					selected=id;
					alert.show();
					return false;
				}
			});
			
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		this.selected=id;
		Intent i = new Intent(this, KeyPicker.class);
		this.startActivityForResult(i, KeyPicker.KEY_RETRIEVED);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case KeyPicker.KEY_RETRIEVED:
			SecretKey key = new SecretKeySpec(data.getByteArrayExtra("key"), "DES");
			//Log.d("ROLY","Key retrieved: " + KeyPicker.byteArrayToHexString(key.getEncoded()));
			DesEncrypter crypter= new DesEncrypter(key);
			Uri noteUri = ContentUris.withAppendedId(NoteProvider.CONTENT_URI, selected);
			Cursor mCursor = getContentResolver().query(noteUri, null, null, null, null);
			if(mCursor.moveToFirst()){
				String mContent = mCursor.getString(NoteDatabaseHelper.BODY_COLUMN);
				//Log.d("ROLY", mContent);
				try{
					mContent = crypter.decrypt(mContent);
					if(mContent==null)
						throw new NullPointerException();
					String mTitle = mCursor.getString(NoteDatabaseHelper.TITLE_COLUMN);
					Intent i=new Intent(this, UncipheredNote.class);
					i.putExtra("title", mTitle);
					i.putExtra("content", mContent);
					this.startActivity(i);
				}catch(Exception e){
					Toast.makeText(this, "Not the right key, Can't unciphered this note", 20).show();
				}

			}
			break;
		default:
			break;
		}
	}
	
	private class CustomAdapter extends CursorAdapter{
	    private final LayoutInflater mInflater;
	    private final Context context;

	    public CustomAdapter(Context context, Cursor cursor) {
	      super(context, cursor, true);
	      mInflater = LayoutInflater.from(context);
	      this.context= context;
	    }

	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	      TextView t = (TextView) view.findViewById(R.id.title_item);
	      t.setText(cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.KEY_TITLE)));

	      t = (TextView) view.findViewById(R.id.content_item);
	      t.setText(cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.KEY_BODY)));
	    }

	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	      final View view = mInflater.inflate(R.layout.note_item, parent, false);
	      
	      return view;
	    }
	  }
}
