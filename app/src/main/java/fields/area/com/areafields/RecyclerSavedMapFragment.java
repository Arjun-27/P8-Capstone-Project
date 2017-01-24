package fields.area.com.areafields;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

public class RecyclerSavedMapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
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

        loadAreas = savedInstanceState.getBoolean("isArea");

        if(rootView == null)
            rootView = inflater.inflate(R.layout.layout_recycler, container, false);

        if(recyclerView == null) {
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_saved_maps);
            recyclerView.setLayoutManager(manager);
            getLoaderManager().initLoader(loadAreas ? 0 : 1, null, this);
        }

        getLoaderManager().restartLoader(loadAreas ? 0 : 1, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(getContext(), MarkOffContract.AreaComputations.CONTENT_URI, null, null, null, null);
            case 1:
                return new CursorLoader(getContext(), MarkOffContract.DistanceComputations.CONTENT_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new SavedMapsRecyclerAdapter(loadAdapterData(data), getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }

    private ArrayList<SavedMapDetails> loadAdapterData(Cursor cursor) {
        ArrayList<SavedMapDetails> detailList = new ArrayList<>();
        if (loadAreas) {
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
            }
        } else {
            if(cursor != null) {
                while (cursor.moveToNext()) {
                    SavedMapDetails details = new SavedMapDetails();

                    details.setIsArea(false);
                    details.setComputationId(cursor.getInt(cursor.getColumnIndex(MarkOffContract.DistanceComputations.ID)));
                    details.setName(cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.NAME)));
                    details.setPerimeter(cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.DISTANCE)));

                    detailList.add(details);
                }
            }
        }

        return detailList;
    }

}
