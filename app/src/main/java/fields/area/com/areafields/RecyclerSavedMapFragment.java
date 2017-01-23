package fields.area.com.areafields;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import fields.area.com.areafields.adapter.SavedMapDetails;
import fields.area.com.areafields.adapter.SavedMapsRecyclerAdapter;
import fields.area.com.areafields.data.MarkOffContract;

/**
 * Created by Arjun on 16-Jan-2017 for AreaFields.
 */

public class RecyclerSavedMapFragment extends Fragment {
    RecyclerView recyclerView;

    LinearLayoutManager manager;
    SavedMapsRecyclerAdapter adapter;

    View rootView;

    boolean loadAreas = false;

    public RecyclerSavedMapFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = new LinearLayoutManager(getContext());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        savedInstanceState = this.getArguments();

        if(rootView == null)
            rootView = inflater.inflate(R.layout.layout_recycler, container, false);

        if(recyclerView == null) {
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_saved_maps);
            recyclerView.setLayoutManager(manager);
        }

        loadAreas = savedInstanceState.getBoolean("isArea");

        new LoadSavedDetails().execute();
        return rootView;
    }

    private class LoadSavedDetails extends AsyncTask<Void, Void, ArrayList<SavedMapDetails>> {

        MaterialDialog dialog;

        public void onPreExecute() {
            dialog = new Utility().createIndeterminateDialog(getContext(), loadAreas ? "Loading Saved Areas.." : "Loading Saved Distances..");
            dialog.show();
        }

        @Override
        protected ArrayList<SavedMapDetails> doInBackground(Void... params) {
            ArrayList<SavedMapDetails> detailList = new ArrayList<>();

            if (loadAreas) {
                Cursor cursor = getContext().getContentResolver().query(MarkOffContract.AreaComputations.CONTENT_URI, null, null, null, null);
                if(cursor != null) {
                    while (cursor.moveToNext()) {
                        SavedMapDetails details = new SavedMapDetails();

                        details.setIsArea(true);
                        details.setComputationId(cursor.getInt(cursor.getColumnIndex(MarkOffContract.AreaComputations.ID)));
                        details.setName(cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.NAME)));
                        details.setArea(cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.AREA)));
                        details.setPerimeter(cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.PERIMETER)));

                        detailList.add(details);
                    }
                    cursor.close();
                }
            } else {
                Cursor cursor = getContext().getContentResolver().query(MarkOffContract.DistanceComputations.CONTENT_URI, null, null, null, null);
                if(cursor != null) {
                    while (cursor.moveToNext()) {
                        SavedMapDetails details = new SavedMapDetails();

                        details.setIsArea(false);
                        details.setComputationId(cursor.getInt(cursor.getColumnIndex(MarkOffContract.DistanceComputations.ID)));
                        details.setName(cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.NAME)));
                        details.setPerimeter(cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.DISTANCE)));

                        detailList.add(details);
                    }
                    cursor.close();
                }
            }

            return detailList;
        }

        public void onPostExecute(ArrayList<SavedMapDetails> result) {
            dialog.dismiss();

            if(result != null) {
                adapter = new SavedMapsRecyclerAdapter(result, getActivity());
                recyclerView.setAdapter(adapter);

            } else {
                Snackbar.make(recyclerView, loadAreas ? "No Saved Areas.." : "No Saved Distances..", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

}
