package com.roly.nfc.crypto.ui.fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.roly.nfc.crypto.R;
import com.roly.nfc.crypto.data.NoteDatabase;
import com.roly.nfc.crypto.data.NoteProvider;
import com.roly.nfc.crypto.ui.activity.NoteListActivity;

public class NoteListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] from = new String[]{NoteDatabase.KEY_TITLE, NoteDatabase.KEY_BODY};
        int[] to = {R.id.note_list_item_title, R.id.note_list_item_content};
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.note_item, null, from, to, SimpleCursorAdapter.NO_SELECTION){
            @Override
            public void setViewText(TextView v, String text) {
                if(v.getId() == R.id.note_list_item_content && text.endsWith("\n")){
                    text = text.substring(0, text.length() - 1);
                }
                super.setViewText(v, text);
            }
        };
        setListAdapter(adapter);

        ListView listView = getListView();
        listView.setDivider(null);
        int background = Color.parseColor("#BBBBBB");
        listView.setBackgroundColor(background);

        setEmptyText("You don't have any note saved for now.");
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        NoteListActivity activity = (NoteListActivity) getActivity();
        Cursor cursor = (Cursor) adapter.getItem(position);
        String title = cursor.getString(NoteDatabase.TITLE_COLUMN);
        String content = cursor.getString(NoteDatabase.BODY_COLUMN);
        activity.askTag(id, title, content);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), NoteProvider.CONTENT_URI, null, null, null, null);
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