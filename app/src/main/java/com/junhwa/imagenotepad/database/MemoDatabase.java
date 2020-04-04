package com.junhwa.imagenotepad.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MemoDatabase {
    private static final String DB_NAME = "LINE_PRACTICE.db";
    private static final String MEMO_TABLE = "MEMO";
    private static final int DB_VERSION = 1;

    private static MemoDatabase dbManager = null;
    private SQLiteDatabase database;

    private Context context;

    public MemoDatabase(Context context) {
        this.context = context;

        database = this.context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS " + MEMO_TABLE +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "Creation DATE, " +
                        "Modification DATE, " +
                        "Title TEXT NOT NULL, " +
                        "Contents TEXT NOT NULL);");
    }

    public static MemoDatabase getInstance(Context context) {
        if (dbManager == null)
            dbManager = new MemoDatabase(context);
        return dbManager;
    }

    public static long insertMemo(Context context, ContentValues addRowValue) {
        return getInstance(context).database.insert(MEMO_TABLE, null, addRowValue);
    }

    public Cursor queryMemo(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return database.query(MEMO_TABLE, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public static long getMemoSequence(Context context) {
        Cursor cursor = getInstance(context).database.query("SQLITE_SEQUENCE", new String[]{"seq"}, "name = \'" + MEMO_TABLE + "\'",
                null, null, null, null);
        cursor.moveToFirst();
        long sequence = 0;
        try {
            sequence = cursor.getLong(0);
        } catch (CursorIndexOutOfBoundsException e) {
            sequence = 0;
        } finally {
            Log.v("Memo sequence", sequence + "");
            return sequence;
        }
    }

    public static int updateMemo(Context context, ContentValues updateRowValue, String whereClause, String[] whereArgs) {
        return getInstance(context).database.update(MEMO_TABLE, updateRowValue, whereClause, whereArgs);
    }

    public static int deleteMemo(Context context, String whereClause, String[] whereArgs) {
        return getInstance(context).database.delete(MEMO_TABLE, whereClause, whereArgs);
    }

    public static String[] loadMemo(Context context, int id) {
        Cursor cursor = getInstance(context).queryMemo(new String[] {"Title", "Contents", "Creation", "Modification"},
                "ID = " + id, null, null, null, null);
        if (cursor != null || cursor.getCount() > 0) {
            cursor.moveToFirst();
            String title = cursor.getString(0);
            String contents = cursor.getString(1);
            String creation = cursor.getString(2);
            String modification = cursor.getString(3);

            return new String[] {title, contents, creation, modification};
        }
        return null;
    }
}
