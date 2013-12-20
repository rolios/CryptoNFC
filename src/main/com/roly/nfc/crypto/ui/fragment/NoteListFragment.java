package com.roly.nfc.crypto.ui.fragment;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.appcompat.R;
import android.util.TypedValue;

import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;

public class NoteListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter adapter;
    private String[] projection;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        projection = new String[]{NoteDatabase.KEY_TITLE, NoteDatabase.KEY_BODY};
        int[] to = {R.id.note_list_item_title, R.id.note_list_item_content};
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.note_item, null, projection, to, SimpleCursorAdapter.NO_SELECTION);
        setListAdapter(adapter);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());

        getListView().setDividerHeight((int)px);

        setEmptyText("You don't have any note saved for now.");
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity(), NoteProvider.CONTENT_URI, null, null, null, null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        setListShown(true);
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}