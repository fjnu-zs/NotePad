package com.example.demo2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NotePadProvider extends ContentProvider {
    private static UriMatcher matcher;
    private static final int WORDS = 1;
    private static final int WORD = 2;
    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(NotePad.AUTHORITY,"notes",WORDS);
        matcher.addURI(NotePad.AUTHORITY,"notes/#",WORD);
    }
    private static final String DateBaseName = "note.db";
    private static final int version = 1;
    private static final String[] READ_NOTE_PROJECTION = new String[] {
            NotePad.Notes._ID,               // Projection position 0, the note's id
            NotePad.Notes.COLUMN_NAME_NOTE,  // Projection position 1, the note's content
            NotePad.Notes.COLUMN_NAME_TITLE, // Projection position 2, the note's title
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, //zkwwwwwwwwwwwwww
    };
    private static HashMap<String,String> notesProjectionMap;
    static {
        notesProjectionMap = new HashMap<>();
        notesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);
        notesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_TITLE);
        notesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);
        notesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, NotePad.Notes.COLUMN_NAME_CREATE_DATE);
        notesProjectionMap.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
    }
    private MyDatabaseHelper myDatabaseHelper;
    static class MyDatabaseHelper extends SQLiteOpenHelper{

        private static final String TAG = "NotePadProvider";


        MyDatabaseHelper(Context context){
            super(context,DateBaseName,null,version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            Log.w(TAG, "Upgrading database from version " + i + " to "
                    + i1 + ", which will destroy all old data");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(sqLiteDatabase);
        }
    }
    @Override
    public boolean onCreate() {
        myDatabaseHelper = new MyDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch (matcher.match(uri)){
            case WORDS:
                sqLiteQueryBuilder.setTables(NotePad.Notes.TABLE_NAME);
                sqLiteQueryBuilder.setProjectionMap(notesProjectionMap);
                break;
            case WORD:
                sqLiteQueryBuilder.setTables(NotePad.Notes.TABLE_NAME);
                sqLiteQueryBuilder.setProjectionMap(notesProjectionMap);
                sqLiteQueryBuilder.appendWhere(NotePad.Notes._ID+"="+uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("UnKnow Uri:"+uri);
        }
        String ob;
        if(TextUtils.isEmpty(sortOrder)){
            ob = NotePad.Notes.DEFAULT_SORT_ORDER;
        }else {
            ob = sortOrder;
        }
        SQLiteDatabase sqLiteDatabase = myDatabaseHelper.getReadableDatabase();
        return sqLiteQueryBuilder.query(sqLiteDatabase,projection,selection,selectionArgs,null,null,ob);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (matcher.match(uri)){
            case WORD:
                return NotePad.Notes.CONTENT_TYPE;
            case WORDS:
                return NotePad.Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("UnKnow Uri:"+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if(matcher.match(uri)!=WORDS){
            throw new IllegalArgumentException("UnKnow Uri:"+uri);
        }
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        long rowID = db.insert(NotePad.Notes.TABLE_NAME,null,contentValues);
        if(rowID>0){
            Uri reUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI,rowID);
            return reUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = -1;
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case WORDS:
                count = db.delete(NotePad.Notes.TABLE_NAME, selection, selectionArgs);
                break;

            case WORD:
                String rowID = uri.getPathSegments().get(1);
                count = db.delete(NotePad.Notes.TABLE_NAME, NotePad.Notes._ID + "=" + rowID, null);
                break;

            default:
                throw new IllegalArgumentException("Unknow URI :" + uri);
        }

        this.getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String where, @Nullable String[] whereArgs) {
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        int count = -1;
        switch (matcher.match(uri)){
            case WORDS:
                count=db.update(NotePad.Notes.TABLE_NAME,contentValues,null,null);
                break;
            case WORD:
                String rowID = uri.getPathSegments().get(1);
                count=db.update(NotePad.Notes.TABLE_NAME,contentValues, NotePad.Notes._ID+"="+rowID,null);
                break;
            default:
                throw new IllegalArgumentException("UnKnow Uri:"+uri);
        }
        this.getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
