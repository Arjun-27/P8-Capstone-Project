package fields.area.com.areafields.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Arjun on 30-Dec-2016 for AreaFields.
 */

public class MarkOffDbHelper extends SQLiteOpenHelper {
    public MarkOffDbHelper(Context context) {
        super(context, "areas.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE computations_area (computation_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, area TEXT, perimeter TEXT, poly TEXT, tag TEXT, UNIQUE (computation_id) ON CONFLICT REPLACE);");
        db.execSQL("CREATE TABLE computations_distance (computation_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, distance TEXT, poly TEXT, tag TEXT, UNIQUE (computation_id) ON CONFLICT REPLACE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS computations_area;");
        db.execSQL("DROP TABLE IF EXISTS computations_perimeter;");
    }
}
