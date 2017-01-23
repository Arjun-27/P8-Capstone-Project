package fields.area.com.areafields;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fields.area.com.areafields.adapter.SavedMapDetails;
import fields.area.com.areafields.data.MarkOffContract;

import static fields.area.com.areafields.data.MarkOffContract.AreaComputations.TAG;

/**
 * Created by Arjun on 25-Dec-2016 for AreaFields.
 */

public class Utility {
    static final String ACTION_AREA_CALCULATION_STARTED = "calculationStarted";
    static final String DISCARD_POLY_EVENT = "discardPolyEvent";
    static final String SAVE_POLY_EVENT = "savePolyEvent";
    public static final String LOAD_POLY_EVENT = "loadPolyEvent";
    static final String SAVE_LOADED_POLY = "saveLoadedPoly";
    static final String POLY_NAME = "PolyName";

    static final String CHANGE_MAP_SATELLITE = "mapSatellite";
    static final String CHANGE_MAP_NORMAL = "mapNormal";
    static final String CHANGE_MAP_HYBRID = "mapHybrid";
    static final String CHANGE_MAP_TERRAIN = "mapSatellite";

    public static final String KEY_REPLACE = "keyReplace";

    MaterialDialog createIndeterminateDialog(Context context, String message) {
        return new MaterialDialog.Builder(context)
                .content(message)
                .progress(true, 0)
                .build();
    }

    void savePoly(Context context, Object... params) {
        new SavePoly(context).execute(params);
    }

    private class SavePoly extends AsyncTask<Object, Void, Boolean> {
        List<LatLng> list;
        HashMap<Boolean, LatLng> areaMap;
        String area, perimeter, tag;
        Context context;

        boolean isAreaPoly;

        MaterialDialog dialog;

        SavePoly(Context context) {
            this.context = context;
        }

        public void onPreExecute() {
            dialog = new MaterialDialog.Builder(context)
                    .content("Saving...")
                    .progress(true, 0)
                    .build();

            //dialog.show();
            areaMap = new HashMap<>();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            isAreaPoly = (Boolean) params[0];
            list = (ArrayList<LatLng>) params[1];
            tag = (String) params[2];

            Log.d("Save Size", ""+list.size());
            ContentValues values = new ContentValues();

            List<MyLatLng> myList = new ArrayList<>();
            for(LatLng latLng : list) {
                //Log.d("LAT_LNG", latLng.toString());
                myList.add(new MyLatLng(latLng.latitude, latLng.longitude));
            }

            if(isAreaPoly) {
                area = (String) params[3];
                perimeter = (String) params[4];

                Gson gson = new Gson();
                JsonElement element = gson.toJsonTree(myList, new TypeToken<List<MyLatLng>>() {}.getType());
                String polyMap = element.getAsJsonArray().toString();

                Log.d(TAG, polyMap);
                values.put(MarkOffContract.AreaComputations.NAME, (String) params[5]);
                values.put(MarkOffContract.AreaComputations.AREA, area);
                values.put(MarkOffContract.AreaComputations.PERIMETER, perimeter);
                values.put(MarkOffContract.AreaComputations.POLY, polyMap);
                values.put(TAG, tag);
                if((int) params[6] != -1) {
                    values.put(MarkOffContract.AreaComputations.ID, (int) params[6]);
                    Log.d(TAG, "Updating");
                    values.put(Utility.KEY_REPLACE, true);
                } else {
                    values.put(Utility.KEY_REPLACE, false);
                }
                context.getContentResolver().insert(MarkOffContract.AreaComputations.CONTENT_URI, values);//, MarkOffContract.AreaComputations.ID + "=" + params[6], null);
                return true;
            } else {
                perimeter = (String) params[3];

                Gson gson = new Gson();
                JsonElement element = gson.toJsonTree(myList, new TypeToken<List<MyLatLng>>() {}.getType());
                String polyMap = element.getAsJsonArray().toString();

                values.put(MarkOffContract.DistanceComputations.NAME, (String) params[4]);
                values.put(MarkOffContract.DistanceComputations.DISTANCE, perimeter);
                values.put(MarkOffContract.DistanceComputations.POLY, polyMap);
                values.put(MarkOffContract.DistanceComputations.TAG, tag);
                if((int) params[5] != -1) {
                    values.put(Utility.KEY_REPLACE, true);
                    values.put(MarkOffContract.DistanceComputations.ID, (int) params[5]);
                } else {
                    values.put(Utility.KEY_REPLACE, false);
                }
                context.getContentResolver().insert(MarkOffContract.DistanceComputations.CONTENT_URI, values);
                return true;
            }
        }

        public void onPostExecute(Boolean result) {
            //dialog.dismiss();
            Log.d(TAG, "Updated");

            new MaterialDialog.Builder(context)
                    .content("Measurements have been saved.")
                    .positiveText("Okay")
                    .build()
                    .show();
        }
    }
}