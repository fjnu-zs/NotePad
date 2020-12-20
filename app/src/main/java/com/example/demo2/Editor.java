package com.example.demo2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Editor extends AppCompatActivity {
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    private int mState;
    private EditText title;
    private EditText note;
    private String localTitle = null;
    private String localNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editor);
        title = findViewById(R.id.editorTitle);
        note = findViewById(R.id.editorNote);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
        } else if (Intent.ACTION_INSERT.equals(action)) {
            Log.e("editor", "insert");
            mState = STATE_INSERT;
        } else {
            Log.e("Editor", "Unknown Uri:" + intent.getData());
            finish();
        }
        if (mState == STATE_EDIT) {
            Cursor cursor = this.getContentResolver().query(intent.getData(), null, null, null);
            while (cursor.moveToNext()) {
                title.setText(cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE)));
                localTitle = title.getText().toString();
                note.setText(cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE)));
                localNote = note.getText().toString();
            }
            cursor.close();
        }
    }

    public void onClick(View view) {
        int count = -1;
        long modify = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotePad.Notes.COLUMN_NAME_TITLE, title.getText().toString());
        contentValues.put(NotePad.Notes.COLUMN_NAME_NOTE, note.getText().toString());
        contentValues.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, modify);
        Uri uri = null;
        if (mState == STATE_INSERT) {
            if (localTitle == null || localNote == null) {
                contentValues.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, modify);
                uri = this.getContentResolver().insert(NotePad.CONTENT_URI, contentValues);
                Log.e("editor", "uri:" + uri);
            }
        } else if (mState == STATE_EDIT) {
            if (!(localTitle.equals(title.getText().toString())) || !(localNote.equals(note.getText().toString()))) {
                count = this.getContentResolver().update(getIntent().getData(), contentValues, null, null);
            }
        }
        if (count > 0 || uri != null) {
            int result;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            String id;
            if (count > 0) {
                result = 1;
                id = getIntent().getData().getPathSegments().get(1);
            } else {
                result = 2;
                id = uri.getPathSegments().get(1);
            }
            intent.putExtra(NotePad.Notes._ID, id);
            intent.putExtra(NotePad.Notes.COLUMN_NAME_TITLE, title.getText().toString());
            intent.putExtra(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, modify + "");
            intent.putExtra(NotePad.Notes.COLUMN_NAME_NOTE, note.getText().toString());
            setResult(result, intent);
            finish();
        }
    }
}
