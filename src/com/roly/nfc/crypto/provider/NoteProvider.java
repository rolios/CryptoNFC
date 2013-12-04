
package com.roly.nfc.crypto.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class NoteProvider extends ContentProvider{

	public static final String AUTHORITY = "com.roly.nfc.crypto.noteprovider";
	private static final String INSCRIPTION_MIME_TYPE = "vnd.android.cursor.item/vnd.roly.note";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/notes");
	
	private SQLiteDatabase notesDB;

	// Create the constants used to differentiate between the different URI
	// requests.
	private static final int ALL_NOTES = 1;
	private static final int NOTE_ID = 2;

	private static final UriMatcher uriMatcher;

	// Allocate the UriMatcher object, where a URI ending in 'notes' will
	// correspond to a request for all notes, and 'notes' with a trailing
	// '/[rowID]' will represent a single note row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "notes", ALL_NOTES);
		uriMatcher.addURI(AUTHORITY, "notes/#", NOTE_ID);
	}
	
	@Override
	public boolean onCreate() {
		NoteDatabaseHelper helper = new NoteDatabaseHelper(getContext());
		notesDB = helper.getWritableDatabase();
		return notesDB != null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(NoteDatabaseHelper.DATABASE_TABLE);

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case NOTE_ID:
			qb.appendWhere(NoteDatabaseHelper.KEY_ID + "=" + uri.getPathSegments().get(1));
			break;
		case ALL_NOTES:
			break;
		default:
			break;
		}

		// Apply the query to the underlying database.
		Cursor c = qb.query(notesDB, projection, selection, selectionArgs,
				null, null, sortOrder);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
        ContentResolver contentResolver = getContext().getContentResolver();
        c.setNotificationUri(contentResolver, uri);

		// Return a cursor to the query result.
		return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Insert the new row, will return the row number if
		// successful.
		long rowID = notesDB.insert(NoteDatabaseHelper.DATABASE_TABLE, "note", values);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            ContentResolver contentResolver = getContext().getContentResolver();
            contentResolver.notifyChange(newUri, null);
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}	
		
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		int count;
		switch (uriMatcher.match(uri)) {
		case ALL_NOTES:
			count = notesDB.delete(NoteDatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
			break;

		case NOTE_ID:
			String segment = uri.getPathSegments().get(1);
			StringBuilder whereClause = new StringBuilder(NoteDatabaseHelper.KEY_ID).append("=")
					.append(segment);
			if (!TextUtils.isEmpty(selection)) {
				whereClause.append(" AND (").append(selection).append(")");
			}

			count = notesDB.delete(NoteDatabaseHelper.DATABASE_TABLE, whereClause.toString(),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.notifyChange(uri, null);
		return count;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case ALL_NOTES:
			count = notesDB.update(NoteDatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
			break;

		case NOTE_ID:
			String segment = uri.getPathSegments().get(1);
			StringBuilder whereClause = new StringBuilder(NoteDatabaseHelper.KEY_ID).append("=")
					.append(segment);
			if (!TextUtils.isEmpty(selection)) {
				whereClause.append(" AND (").append(selection).append(")");
			}
			count = notesDB.update(NoteDatabaseHelper.DATABASE_TABLE, values,
					whereClause.toString(), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ALL_NOTES:
			return "vnd.android.cursor.dir/vnd.roly.notes";
		case NOTE_ID:
			return INSCRIPTION_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
}
