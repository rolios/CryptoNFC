package com.roly.nfc.crypto.view;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;
import com.roly.nfc.crypto.util.EncryptionUtils;
import com.roly.nfc.crypto.view.nfc.KeyPickerActivity;

public class NoteListActivity extends ListActivity{

	private long mSelected;
	private AlertDialog mAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Select note to decipher");
		Cursor mCursor = getContentResolver().query(NoteProvider.CONTENT_URI, null, null, null, null);
		this.setListAdapter(new CustomAdapter(this,mCursor));

		final CharSequence[] options = {"Delete"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an action");
		builder.setItems(options, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Uri noteUri = ContentUris.withAppendedId(NoteProvider.CONTENT_URI, mSelected);
				getContentResolver().delete(noteUri, null, null);
			}
		});
		mAlert = builder.create();

		ListView list = getListView();
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long id) {
				mSelected=id;
				mAlert.show();
				return false;
			}
		});

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		this.mSelected=id;
		Intent i = new Intent(this, KeyPickerActivity.class);
		this.startActivityForResult(i, KeyPickerActivity.KEY_RETRIEVED);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case KeyPickerActivity.KEY_RETRIEVED:
			SecretKey key = new SecretKeySpec(data.getByteArrayExtra("key"), "DES");

			Uri noteUri = ContentUris.withAppendedId(NoteProvider.CONTENT_URI, mSelected);
			Cursor cursor = getContentResolver().query(noteUri, null, null, null, null);
			if(!cursor.moveToFirst()){
				Toast.makeText(this, "An error occured while retrieving data.", Toast.LENGTH_LONG).show();
				return;
			}
			String content = cursor.getString(NoteDatabase.BODY_COLUMN);
			String title = cursor.getString(NoteDatabase.TITLE_COLUMN);

			try{
				content = EncryptionUtils.decrypt(key, content);
			}catch(Exception e){
				Toast.makeText(this, "This is not the right tagkey. Can't decipher this note.", Toast.LENGTH_LONG).show();
				return;
			}

			Intent i=new Intent(this, DecipheredNoteActivity.class);
			i.putExtra("title", title);
			i.putExtra("content", content);
			this.startActivity(i);

			break;
		default:
			break;
		}
	}

	private class CustomAdapter extends CursorAdapter{
		private final LayoutInflater mInflater;

		public CustomAdapter(Context context, Cursor cursor) {
			super(context, cursor, true);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView t = (TextView) view.findViewById(R.id.title_item);
			t.setText(cursor.getString(cursor.getColumnIndex(NoteDatabase.KEY_TITLE)));

			t = (TextView) view.findViewById(R.id.content_item);
			t.setText(cursor.getString(cursor.getColumnIndex(NoteDatabase.KEY_BODY)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.note_item, parent, false);
			return view;
		}
	}
}
