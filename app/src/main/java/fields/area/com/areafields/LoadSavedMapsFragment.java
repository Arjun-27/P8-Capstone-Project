package fields.area.com.areafields;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import fields.area.com.areafields.adapter.SavedMapDetails;
import fields.area.com.areafields.adapter.SavedMapsRecyclerAdapter;
import fields.area.com.areafields.data.MarkOffContract;

/**
 * Created by Arjun on 13-Jan-2017 for AreaFields.
 */

public class LoadSavedMapsFragment extends Fragment {

    FragmentTabHost tabHost;
    Bundle bundle_area, bundle_distance;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle_area = new Bundle();
        bundle_area.putBoolean("isArea", true);

        bundle_distance = new Bundle();
        bundle_distance.putBoolean("isArea", false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_load_saved_maps, container, false);

//        toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        tabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        tabHost.setup(getContext(), getChildFragmentManager(), android.R.id.tabcontent);
        tabHost.getTabWidget().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
//        FragmentTabHost.TabSpec spec = tabHost.newTabSpec("Areas");
//        spec.setContent(R.id.recycler_view_group);
//        spec.setIndicator("Areas");
//        tabHost.addTab(spec);

        tabHost.addTab(tabHost.newTabSpec("Areas").setIndicator("Areas"), RecyclerSavedMapFragment.class, bundle_area);
        tabHost.addTab(tabHost.newTabSpec("Distances").setIndicator("Distances"), RecyclerSavedMapFragment.class, bundle_distance);

//        spec = tabHost.newTabSpec("Distances");
//        spec.setContent(R.id.recycler_view_group);
//        spec.setIndicator("Distances");
//        tabHost.addTab(spec);

//        tabHost.setOnTabChangedListener(new FragmentTabHost.OnTabChangeListener() {
//            @Override
//            public void onTabChanged(String tabId) {
//                loadAreas = !loadAreas;
//                new LoadSavedDetails().execute();
//            }
//        });

        return rootView;
    }
}
