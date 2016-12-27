package com.openthos.filemanager.system;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Wang Zhixu on 12/23/16.
 */

public class SeafileSQLiteHelper extends SQLiteOpenHelper {
    public static final int VERSION =1;
    public static final String NAME="seafile";

    public SeafileSQLiteHelper(Context context, String name,
                               SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table seafileaccount ("
                + "id integer primary key autoincrement,"
                + "username text)");
        sqLiteDatabase.execSQL("create table seafilefile ("
                + "id integer primary key autoincrement,"
                + "userid integer,"
                + "libraryid text,"
                + "libraryname text,"
                + "isSync integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
