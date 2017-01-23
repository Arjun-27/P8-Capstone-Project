package fields.area.com.areafields.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arjun on 30-Dec-2016 for AreaFields.
 */

public class MarkOffContract {
    static final String CONTENT_AUTHORITY = "fields.area.com.areafields";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_AREA_COMPUTATIONS = "computations_area";
    public static final String PATH_DISTANCE_COMPUTATIONS = "computations_distance";

    public static final class AreaComputations implements BaseColumns {
        public static final Uri CONTENT_URI = MarkOffContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_AREA_COMPUTATIONS).build();

        public static final String TABLE_NAME = PATH_AREA_COMPUTATIONS;
        public static final String ID = "computation_id";
        public static final String NAME = "name";
        public static final String AREA = "area";
        public static final String TAG = "tag";
        public static final String PERIMETER = "perimeter";
        public static final String POLY = "poly";

        static Uri buildAreasUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class DistanceComputations implements BaseColumns {
        public static final Uri CONTENT_URI = MarkOffContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISTANCE_COMPUTATIONS).build();

        public static final String TABLE_NAME = PATH_DISTANCE_COMPUTATIONS;
        public static final String ID = "computation_id";
        public static final String NAME = "name";
        public static final String TAG = "tag";
        public static final String DISTANCE = "distance";
        public static final String POLY = "poly";

        static Uri buildAreasUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
