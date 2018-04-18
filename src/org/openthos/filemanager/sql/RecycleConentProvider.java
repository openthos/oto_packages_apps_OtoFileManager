package org.openthos.filemanager.sql;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by root on 3/1/17.
 */

public class RecycleConentProvider extends ContentProvider {
    private RecycleDataBaseHelper mDataBaseHelper;
    private ContentResolver mContentResolver;
    private static final String TABLE_NAME = "recycle";
    private static final int CONTACT = 1;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI("org.openthos.filemanager", "recycle", CONTACT);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDataBaseHelper = new RecycleDataBaseHelper(context, "recycle.db", null, CONTACT);
        mContentResolver = context.getContentResolver();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                                                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        if (uriMatcher.match(uri) == CONTACT) {
            SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();
            cursor = db.query(TABLE_NAME, projection, selection,
                                                              selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(mContentResolver, uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri u = null;
        if (uriMatcher.match(uri) == CONTACT) {
            SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
            long id = db.insert(TABLE_NAME, "_id", values);
            u = ContentUris.withAppendedId(uri, id);
            mContentResolver.notifyChange(u, null);
        }
        return u;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int id = 0;
        if (uriMatcher.match(uri) == CONTACT) {
            SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
            id = db.delete(TABLE_NAME, selection, selectionArgs);
            mContentResolver.notifyChange(uri, null);
        }
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int id = 0;
        if (uriMatcher.match(uri) == CONTACT) {
            SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
            id = db.update(TABLE_NAME, values, selection, selectionArgs);
        }
        return id;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    private class RecycleDataBaseHelper extends SQLiteOpenHelper {

        public RecycleDataBaseHelper(Context context, String name,
                                          SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table recycle(id integer primary key autoincrement,"
                    + " source text not null, filename text not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }
}
