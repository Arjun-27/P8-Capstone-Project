package fields.area.com.areafields.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Arjun on 22-Jan-2017 for AreaFields.
 *
 */

public class MySharedPreferences {

    private static final String MEM_NAME = "Memory";
    private static final String DEFAULT_AREA_UNIT = "DefaultAreaUnit";
    private static final String DEFAULT_DISTANCE_UNIT = "DefaultDistanceUnit";
    private static final String DEFAULT_STROKE_COLOR = "DefaultStrokeColor";
    private static final String DEFAULT_FILL_COLOR = "DefaultFillColor";
    //private static final String DEFAULT_TRANSPARENCY = "DefaultTransparency";

    public static boolean saveDefaultAreaUnit(Context context, String unit) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MEM_NAME, 0).edit();
        editor.putString(DEFAULT_AREA_UNIT, unit);
        return editor.commit();
    }

    public static String getDefaultAreaUnit(Context context) {
        return context.getSharedPreferences(MEM_NAME, 0).getString(DEFAULT_AREA_UNIT, "Acres|0.000247105");
    }

    public static boolean saveDefaultDistanceUnit(Context context, String unit) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MEM_NAME, 0).edit();
        editor.putString(DEFAULT_DISTANCE_UNIT, unit);
        return editor.commit();
    }

    public static String getDefaultDistanceUnit(Context context) {
        return context.getSharedPreferences(MEM_NAME, 0).getString(DEFAULT_DISTANCE_UNIT, "KMs|0.001");
    }

    public static boolean saveDefaultStrokeColor(Context context, String color) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MEM_NAME, 0).edit();
        editor.putString(DEFAULT_STROKE_COLOR, color);
        return editor.commit();
    }

    public static String getDefaultStrokeColor(Context context) {
        return context.getSharedPreferences(MEM_NAME, 0).getString(DEFAULT_STROKE_COLOR, "#FFFFFF");
    }

    public static boolean saveDefaultFillColor(Context context, String color) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MEM_NAME, 0).edit();
        editor.putString(DEFAULT_FILL_COLOR, color);
        return editor.commit();
    }

    public static String getDefaultFillColor(Context context) {
        return context.getSharedPreferences(MEM_NAME, 0).getString(DEFAULT_FILL_COLOR, "#AA6DA5CD");
    }

//    public static boolean saveDefaultTransparencyColor(Context context, String transparency) {
//        SharedPreferences.Editor editor = context.getSharedPreferences(MEM_NAME, 0).edit();
//        editor.putString(DEFAULT_TRANSPARENCY, transparency);
//        return editor.commit();
//    }
//
//    public static String getDefaultTransparencyColor(Context context) {
//        return context.getSharedPreferences(MEM_NAME, 0).getString(DEFAULT_TRANSPARENCY, "65%|A6");
//    }
}
