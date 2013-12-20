package com.roly.nfc.crypto.view;

import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EActivity;
import com.roly.nfc.crypto.R;

@EActivity(R.layout.activity_note_list)
public class NoteListActivity extends FragmentActivity{

/*
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setTitle("Select note to decipher");
*//*        adapter = new LoanListItemAdapter(getActivity(), null);
        setListAdapter(adapter);*//*
        setEmptyText("No loans for the moment!\n Click on the \u2295 button to add one.");
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);

        Cursor mCursor = getContentResolver().query(NoteProvider.CONTENT_URI, null, null, null, null);
		setListAdapter(new CustomAdapter(this, mCursor));

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
		Intent i = new Intent(this, KeyPickerDialogFragment.class);
		this.startActivityForResult(i, KeyPickerDialogFragment.KEY_RETRIEVED);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case KeyPickerDialogFragment.KEY_RETRIEVED:
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }*/


    /*    protected void setForegroundListener() {
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
    }*/
/*
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
    }*/
/*
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
	}*/
}
