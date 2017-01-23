package fields.area.com.areafields.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import fields.area.com.areafields.Utility;

/**
 * Created by Arjun on 30-Dec-2016 for AreaFields.
 *
 */

public class MarkOffProvider extends ContentProvider {
    static final int AREA_COMPUTATIONS = 500;
    static final int AREA_COMPUTATIONS_WITH_ID = 501;
    static final int DISTANCE_COMPUTATIONS = 400;
    static final int DISTANCE_COMPUTATIONS_WITH_ID = 401;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MarkOffDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MarkOffContract.CONTENT_AUTHORITY, MarkOffContract.PATH_DISTANCE_COMPUTATIONS, DISTANCE_COMPUTATIONS);
        uriMatcher.addURI(MarkOffContract.CONTENT_AUTHORITY, MarkOffContract.PATH_DISTANCE_COMPUTATIONS + "/*", DISTANCE_COMPUTATIONS_WITH_ID);
        uriMatcher.addURI(MarkOffContract.CONTENT_AUTHORITY, MarkOffContract.PATH_AREA_COMPUTATIONS, AREA_COMPUTATIONS);
        uriMatcher.addURI(MarkOffContract.CONTENT_AUTHORITY, MarkOffContract.PATH_AREA_COMPUTATIONS + "/*", AREA_COMPUTATIONS_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        this.mOpenHelper = new MarkOffDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case AREA_COMPUTATIONS:
                return this.mOpenHelper.getReadableDatabase().query(MarkOffContract.AreaComputations.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case AREA_COMPUTATIONS_WITH_ID:
                return this.mOpenHelper.getReadableDatabase().query(MarkOffContract.AreaComputations.TABLE_NAME, projection, "id = ?", new String[]{String.valueOf(ContentUris.parseId(uri))}, null, null, sortOrder);
            case DISTANCE_COMPUTATIONS:
                return this.mOpenHelper.getReadableDatabase().query(MarkOffContract.DistanceComputations.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case DISTANCE_COMPUTATIONS_WITH_ID:
                return this.mOpenHelper.getReadableDatabase().query(MarkOffContract.DistanceComputations.TABLE_NAME, projection, "id = ?", new String[]{String.valueOf(ContentUris.parseId(uri))}, null, null, sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        long l;
        boolean replace = values.getAsBoolean(Utility.KEY_REPLACE);
        values.remove(Utility.KEY_REPLACE);
        switch (sUriMatcher.match(uri)) {
            case AREA_COMPUTATIONS:
                l = replace ? db.replace(MarkOffContract.AreaComputations.TABLE_NAME, null, values) : db.insert(MarkOffContract.AreaComputations.TABLE_NAME, null, values);
                if (l > 0L)
                    return MarkOffContract.AreaComputations.buildAreasUri(l);
                else
                    throw new SQLException("Failed to insert row.. " + uri);

            case DISTANCE_COMPUTATIONS:
                l = replace ? db.replace(MarkOffContract.DistanceComputations.TABLE_NAME, null, values) : db.insert(MarkOffContract.DistanceComputations.TABLE_NAME, null, values);
                if (l > 0L)
                    return MarkOffContract.DistanceComputations.buildAreasUri(l);
                else
                    throw new SQLException("Failed to insert row.. " + uri);

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
