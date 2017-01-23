package fields.area.com.areafields;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import fields.area.com.areafields.adapter.SavedMapDetails;

/**
 * Created by Arjun on 07-Sep-2016 for Harvesters.
 */
public class MapAreaCalculatorActivity extends AppCompatActivity {

    Button startMeasure;
    ImageButton imageButton;
    FloatingActionsMenu fabMenu;
    LinearLayout calLayout;
    TextView textDistance, textArea;
    IntentFilter filter;
    BroadcastReceiver receiver;
    DrawerLayout drawerLayout;
    ListView drawerList;
    SavedMapDetails details;

    DrawerArrowDrawable drawerArrow;
    MaterialDialog dialog;
    ActionBarDrawerToggle drawerToggle;

    Toolbar toolbar;

    boolean shouldShowActionHome, shouldOverwrite = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_draw_poly_on_map);

        getSupportFragmentManager().beginTransaction().add(R.id.map, new AreaMapFragment()).commit();

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };

        getSupportActionBar().setHomeAsUpIndicator(drawerArrow);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 900);
            Toast.makeText(this, "No permission granted for accessing location..", Toast.LENGTH_SHORT).show();
        }
        
        startMeasure = (Button) findViewById(R.id.btnMeasure);
        fabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        imageButton = (ImageButton) findViewById(R.id.btnDelete);
        calLayout = (LinearLayout) findViewById(R.id.calLayout);
        textDistance = (TextView) findViewById(R.id.textDistance);
        textArea = (TextView) findViewById(R.id.textArea);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.list_content);

        fabMenu.setVisibility(View.INVISIBLE);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, drawerArrow, R.string.str_open_drawer, R.string.str_close_drawer) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        fabMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_translate_up));

        drawerLayout.setDrawerListener(drawerToggle);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.layout_drawer_list_item, R.id.textContent, getResources().getStringArray(R.array.drawer_content_choices)));

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        drawerList.setItemChecked(position, true);
                        drawerLayout.closeDrawer(drawerList, true);

                        if(getSupportFragmentManager().getBackStackEntryCount() >= 1) {
                            getSupportFragmentManager().popBackStack();
                        }
                        break;
                    case 1:
                        drawerList.setItemChecked(position, true);
                        drawerLayout.closeDrawer(drawerList, true);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getSupportFragmentManager().beginTransaction()
                                        .add(R.id.fragFrame, new LoadSavedMapsFragment())
                                        .addToBackStack(AreaMapFragment.class.getName())
                                        .commit();

                            }
                        }, 90);
                        break;
                    case 2:
                        drawerList.setItemChecked(position, true);
                        drawerLayout.closeDrawer(drawerList, true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MapAreaCalculatorActivity.this, SettingsActivity.class));
                            }
                        }, 90);
                        break;
                }
            }
        });
    }

    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawer(drawerList, true);
        } else if(getSupportFragmentManager().getBackStackEntryCount() >= 1) {
            getSupportFragmentManager().popBackStack();
        } else
            clearOptionsMenu();
    }

    private void clearOptionsMenu() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else if(startMeasure.getVisibility() == View.VISIBLE) {

            Animation animation_down = AnimationUtils.loadAnimation(this, R.anim.anim_translate_down);
            animation_down.setStartOffset(30);

            Animation animation_up = AnimationUtils.loadAnimation(this, R.anim.anim_translate_up);
            animation_up.setStartOffset(700);

            animation_up.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    calLayout.setVisibility(View.INVISIBLE);
                    startMeasure.setVisibility(View.GONE);
                    fabMenu.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            calLayout.startAnimation(animation_down);
            fabMenu.startAnimation(animation_up);

            startMeasure.startAnimation(animation_down);

            textDistance.setText(R.string.str_distance);
            textArea.setText(R.string.str_area);
            AreaMapFragment.latLngs.clear();

            resetToolbar();
        } else if(calLayout.getVisibility() == View.VISIBLE) {
            dialog = new MaterialDialog.Builder(this)
                    .content("Do you to discard without saving.. ?")
                    .positiveText("Discard")
                    .negativeText("Cancel")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            discardPoly();
                        }
                    })
                    .build();
            dialog.show();
        } else {
            finish();
        }
    }

    private void discardPoly() {
        resetToolbar();

        sendBroadcast(new Intent(Utility.DISCARD_POLY_EVENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(shouldShowActionHome) {
            getMenuInflater().inflate(R.menu.poly_save_menu, menu);
            menu.setGroupVisible(R.id.group_area, shouldShowActionHome);
        } else {
            getMenuInflater().inflate(R.menu.general_menu, menu);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!drawerLayout.isDrawerOpen(drawerList))
                    drawerLayout.openDrawer(drawerList);
                else
                    drawerLayout.closeDrawer(drawerList);

                return true;

            case R.id.item_hybrid:
                sendBroadcast(new Intent(Utility.CHANGE_MAP_HYBRID));
                return true;

            case R.id.item_normal:
                sendBroadcast(new Intent(Utility.CHANGE_MAP_NORMAL));
                return true;

            case R.id.item_terrain:
                sendBroadcast(new Intent(Utility.CHANGE_MAP_TERRAIN));
                return true;

            case R.id.item_satellite:
                sendBroadcast(new Intent(Utility.CHANGE_MAP_SATELLITE));
                return true;

            case R.id.item_discard:
                new MaterialDialog.Builder(this)
                        .content("Do you want to discard without saving.. ?")
                        .positiveText("Discard")
                        .negativeText("Cancel")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                discardPoly();
                            }
                        }).build()
                        .show();
                return true;

            case R.id.item_save:
                View view = LayoutInflater.from(this).inflate(R.layout.save_poly_details, null, false);

                final EditText editText = (EditText) view.findViewById(R.id.poly_name);

                final MaterialDialog mDialog = new MaterialDialog.Builder(this)
                        .customView(view, false)
                        .positiveText("Okay")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (editText.getText().toString().trim().equals("")) {
                                    editText.setError("Enter a valid identifier");
                                } else {
                                    sendBroadcast(new Intent(Utility.SAVE_POLY_EVENT).putExtra(Utility.POLY_NAME, editText.getText().toString()));
                                    dialog.dismiss();

                                    resetToolbar();
                                }
                            }
                        }).autoDismiss(false)
                        .build();

                if(!shouldOverwrite)
                    mDialog.show();
                else {
                    shouldOverwrite = false;
                    sendBroadcast(new Intent(Utility.SAVE_POLY_EVENT).putExtra(Utility.POLY_NAME, details.getName()).putExtra("computationId", details.getComputationId()));

                    resetToolbar();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(R.string.app_name);

        shouldShowActionHome = false;

        invalidateOptionsMenu();
    }

    public void onResume() {
        super.onResume();
        filter = new IntentFilter(Utility.ACTION_AREA_CALCULATION_STARTED);
        filter.addAction(Utility.SAVE_LOADED_POLY);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                shouldShowActionHome = true;

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);

                if(intent.getAction().equals(Utility.ACTION_AREA_CALCULATION_STARTED)) {
                    getSupportActionBar().setTitle(intent.getStringExtra("Title"));

                    //toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                } else if(intent.getAction().equals(Utility.SAVE_LOADED_POLY)) {
                    shouldOverwrite = true;
                    details = intent.getParcelableExtra("ComputationObj");
                    getSupportActionBar().setTitle(details.getName());
                }
                invalidateOptionsMenu();
            }
        };

        this.registerReceiver(receiver, filter);
    }

    public void onPause() {
        super.onPause();
        this.unregisterReceiver(receiver);
    }
}