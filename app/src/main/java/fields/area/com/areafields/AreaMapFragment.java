package fields.area.com.areafields;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Bounds;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fields.area.com.areafields.adapter.SavedMapDetails;
import fields.area.com.areafields.data.MarkOffContract;
import fields.area.com.areafields.data.MySharedPreferences;

/**
 * Created by Arjun on 07-Sep-2016 for Harvesters.
 *
 */
public class AreaMapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "AreaMap";
    private GoogleApiClient mGoogleApiClient;

    Location mCurrentLocation;
    FrameLayout frMap;
    Button startMeasure;
    FloatingActionsMenu fabMenu;
    FloatingActionButton fabDistance;
    FloatingActionButton fabArea;
    TextView textArea, textDistance, textAreaLabel;
    boolean isPolygon, isNormalMarker = false, hasMoved, isMidMarker, isGPS = false;
    Projection projection;

    static List<LatLng> latLngs;
    GoogleMap googleMap;
    LocationManager locationManager;
    PolygonOptions rectOptions;
    double distance = 0, areaFactor, distanceFactor;

    String fillColor, strokeColor, areaUnit, distanceUnit;

    Polygon polygon;
    Polyline polyline;

    PolygonOptions polygonOptions;
    PolylineOptions polylineOptions;

    ArrayList<LatLng> list;
    ImageView imageView;
    ImageButton btnArrowLeft, btnArrowRight;
    int invisibleMarkerPos = -1, invisibleMidMarkerPos = -2;
    int currentMarkerPos = 0, previousPos = 0;
    boolean resetPosition;

    Bitmap mDotHalfMarkerBitmap, mDotMarkerBitmap;

    HashMap<String, Marker> markers, midMarkers;

    Marker invisibleMarker = null, invisibleMidMarker = null;
    LatLng lastLatLng;
    LinearLayout calLayout;
    FrameLayout.LayoutParams layoutParams;

    BroadcastReceiver receiver;

    //Undo undo;
    int pxHalf;
    private SavedMapDetails details;
    ImageButton deleteMarker;

    private static final String TAG = "MAP";

    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};

    private int curMapTypeIndex = 2;

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        latLngs = new ArrayList<>();
        //undo = new Undo();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(mLocationListener);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        markers = new HashMap<>();
        midMarkers = new HashMap<>();

        list = new ArrayList<>();

        updateFactorsAndUnits();

        rectOptions = new PolygonOptions();
        rectOptions.strokeWidth(5);
        rectOptions.fillColor(Color.parseColor(fillColor));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                AreaMapFragment.this.googleMap = googleMap;
                AreaMapFragment.this.googleMap.setMapType(MAP_TYPES[curMapTypeIndex]);

                projection = googleMap.getProjection();
            }
        });

        frMap = (FrameLayout) getActivity().findViewById(R.id.fr_map);
        startMeasure = (Button) getActivity().findViewById(R.id.btnMeasure);
        fabMenu = (FloatingActionsMenu) getActivity().findViewById(R.id.fab_menu);
        fabDistance = (FloatingActionButton) getActivity().findViewById(R.id.fab_gps);
        fabArea = (FloatingActionButton) getActivity().findViewById(R.id.fab_manual);
        textDistance = (TextView) getActivity().findViewById(R.id.textDistance);
        textArea = (TextView) getActivity().findViewById(R.id.textArea);
        textAreaLabel = (TextView) getActivity().findViewById(R.id.textAreaLabel);
        deleteMarker = (ImageButton) getActivity().findViewById(R.id.btnDelete);

        deleteMarker.setVisibility(View.INVISIBLE);

        calLayout = (LinearLayout) getActivity().findViewById(R.id.calLayout);
        calLayout.setVisibility(View.INVISIBLE);

        imageView = new ImageView(getActivity());
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.marker));

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setPivotX((imageView.getLeft() + imageView.getRight()) / 2);
        imageView.setPivotY((imageView.getTop() + imageView.getBottom()) / 2);

        imageView.setLayoutParams(layoutParams);

        btnArrowLeft = (ImageButton) getActivity().findViewById(R.id.btnArrowLeft);
        btnArrowRight = (ImageButton) getActivity().findViewById(R.id.btnArrowRight);

        frMap.addView(imageView);
        imageView.setVisibility(View.INVISIBLE);

        deleteMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invisibleMarker == null) {
                    Toast.makeText(getContext(), "No Marker Selected!", Toast.LENGTH_SHORT).show();
                } else {
                    if(list.size() > 5 && isPolygon)
                        removeMidMarkers(invisibleMarkerPos);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setVisibility(View.INVISIBLE);
                        }
                    }, 10);

                    list.remove(invisibleMarkerPos == 0 ? 0 : isPolygon ? invisibleMarkerPos - 1 : invisibleMarkerPos);
                    for(Object o : markers.entrySet()) {
                        Map.Entry pair = (Map.Entry) o;
                        if(((Marker)pair.getValue()).getPosition().equals(invisibleMarker.getPosition())) {
                            markers.remove(pair.getKey()).remove();
                            break;
                        }
                    }

                    if(isPolygon) {
                        if (list.size() > 3) {
                            int pos = invisibleMarkerPos;

                            Log.d(TAG, "Size: " + list.size());
                            Marker markerMidPrev = googleMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap))
                                    .position(SphericalUtil.interpolate(list.get(pos != 0 ? pos != list.size() + 1 ? pos - 1 : 0 : list.size() - 1), list.get(pos != 0 ? pos - 2 : pos), 0.5F))
                                    .anchor(0.5F, 0.5F)
                                    .draggable(false));

                            list.add(pos != 0 ? pos - 1 : list.size(), markerMidPrev.getPosition());
                            midMarkers.put(markerMidPrev.getId() /*+ "|" + (pos != 0 ? pos - 1 : list.size())*/, markerMidPrev);
                        } else if (midMarkers.size() != 0) {
                            midMarkers.remove(midMarkers.keySet().iterator().next()).remove();
                            list.remove(1);
                            midMarkers.clear();
                        }
                    }

                    if(list.size() > 0) {
                        if(isPolygon)
                            polygon.setPoints(list);
                        else
                            polyline.setPoints(list);
                    }

                    updateComputations();

                    invisibleMarker = null;
                }
            }
        });

        btnArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invisibleMarker != null || invisibleMidMarker != null) {
                    LatLng currentPos;// = invisibleMarker != null ? invisibleMarker.getPosition() : invisibleMidMarker.getPosition();
                    currentMarkerPos = invisibleMarker != null ? invisibleMarkerPos : invisibleMidMarkerPos;

                    if(currentMarkerPos == 0) {
                        currentMarkerPos = list.size();
                        resetPosition = false;
                        previousPos = 0;
                    } else {
                        previousPos = currentMarkerPos;//(resetPosition ? currentMarkerPos - 1 : currentMarkerPos + 1);
                    }

                    currentPos = list.get(--currentMarkerPos);

                    selectNextMarker(currentPos);
                }
                
                else
                    Toast.makeText(getContext(), "No Marker Selected!", Toast.LENGTH_SHORT).show();
            }
        });

        btnArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invisibleMarker != null || invisibleMidMarker != null) {
                    LatLng currentPos;// = invisibleMarker != null ? invisibleMarker.getPosition() : invisibleMidMarker.getPosition();
                    currentMarkerPos = invisibleMarker != null ? invisibleMarkerPos : invisibleMidMarkerPos;

                    if(currentMarkerPos == list.size() - 1) {
                        currentMarkerPos = -1;
                        resetPosition = true;
                        previousPos = list.size() - 1;
                    } else {
                        previousPos = currentMarkerPos;//(resetPosition ? currentMarkerPos - 1 : currentMarkerPos + 1);
                    }

                    currentPos = list.get(++currentMarkerPos);

                    selectNextMarker(currentPos);
                }

                else
                    Toast.makeText(getContext(), "No Marker Selected!", Toast.LENGTH_SHORT).show();
            }
        });

//        undoAction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final UndoDetails undoDetails = undo.popFromStack();
//                if(undoDetails != null) {
//                    if(undoDetails.isMidMarker()) {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Marker midMarker = undoDetails.getMarker();
//                                removeMidMarkers(undoDetails.getPosition() + 1);
//                                list.set(undoDetails.getPosition(), midMarker.getPosition());
//                                midMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap));
//                                markers.remove(midMarker.getId() + "|" + undoDetails.getPosition());
//                                midMarkers.put(midMarker.getId() + "|" + undoDetails.getPosition(), midMarker);
//                                polygon.setPoints(list);
//                            }
//                        }, 100);
//                    } else {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Marker marker = undoDetails.getMarker();
//                                removeMidMarkers(undoDetails.getPosition());
//                                if(undoDetails.isAddedOnMap()) {
//                                    int pos = undoDetails.getPosition();
//                                    Log.d(TAG, "Removal Pos: " + pos);
//                                    list.remove(pos - 1);
//                                    marker.remove();
//                                    markers.remove(marker.getId() + "|" + undoDetails.getPosition());
//
//                                    if(list.size() > 3) {
//                                        Log.d(TAG, "Size: " + list.size());
//                                        Marker markerMidPrev = googleMap.addMarker(new MarkerOptions()
//                                                .icon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap))
//                                                .position(SphericalUtil.interpolate(list.get(list.size() == pos - 1 ? 0 : pos + 1), list.get(pos - 2), 0.5F))
//                                                .anchor(0.5F, 0.5F)
//                                                .draggable(false));
//
//                                        list.add(list.size() == pos + 1 ? list.size() : pos - 1, markerMidPrev.getPosition());
//                                        midMarkers.put(markerMidPrev.getId() + "|" + (list.size() == pos + 1 ? list.size() : pos - 1), markerMidPrev);
//                                    }
//
//                                    polygon.setPoints(list);
//                                } else {
//                                    list.set(undoDetails.getPosition() - 1, marker.getPosition());
//                                    addMidMarkers(undoDetails.getPosition() - 1, false);
//                                }
//                            }
//                        }, 100);
//                    }
//                }
//            }
//        });

        final Animation animation_down = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_down);
        animation_down.setStartOffset(30);

        final Animation animation_up = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_up);
        animation_up.setStartOffset(700);

        fabDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.collapse();
                fabMenu.startAnimation(animation_down);
                deleteMarker.setVisibility(View.VISIBLE);

                Intent i = new Intent(Utility.ACTION_AREA_CALCULATION_STARTED).putExtra("Title", "Distance");
                getActivity().sendBroadcast(i);

                setUpPolyOptions();
                setUpListeners(isPolygon = false);
                calLayout.startAnimation(animation_up);

                animation_up.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fabMenu.setVisibility(View.INVISIBLE);
                        calLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                textArea.setVisibility(View.GONE);
                textAreaLabel.setVisibility(View.GONE);
            }
        });

        fabArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.collapse();

                new MaterialDialog.Builder(getActivity())
                        .items(R.array.calculation_choices)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        Intent i = new Intent(Utility.ACTION_AREA_CALCULATION_STARTED).putExtra("Title", "Area");
                                        getActivity().sendBroadcast(i);

                                        fabMenu.startAnimation(animation_down);
                                        calLayout.startAnimation(animation_up);
                                        deleteMarker.setVisibility(View.VISIBLE);

                                        animation_up.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                calLayout.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });

                                        setUpPolyOptions();
                                        setUpListeners(isPolygon = true);
                                        textArea.setVisibility(View.VISIBLE);
                                        isGPS = false;
                                        break;

                                    case 1:
                                        Intent i1 = new Intent(Utility.ACTION_AREA_CALCULATION_STARTED).putExtra("Title", "Area");
                                        getActivity().sendBroadcast(i1);

                                        fabMenu.startAnimation(animation_down);
                                        calLayout.startAnimation(animation_up);

                                        animation_up.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                calLayout.setVisibility(View.VISIBLE);
                                                startMeasure.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });

                                        startMeasure.startAnimation(animation_up);
                                        textArea.setVisibility(View.VISIBLE);
                                        isPolygon = true;
                                        isGPS = true;
                                        break;
                                }
                                return true;
                            }
                        })
                        .positiveText("Choose")
                        .build()
                        .show();

//              TODO: Add image filtering and find contours using OpenCV, of the shown land to calculate its area
//                Date now = new Date();
//                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//
//                try {
//                    final String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
//
//                    googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
//                        @Override
//                        public void onSnapshotReady(Bitmap bitmap) {
//
//                            File imageFile = new File(mPath);
//
//                            FileOutputStream outputStream = null;
//                            try {
//                                outputStream = new FileOutputStream(imageFile);
//                                int quality = 100;
//                                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//                                outputStream.flush();
//                                outputStream.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            Utils.bitmapToMat(bitmap, mat);
//
//                            //convert the image to black and white
//                            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
//
//                            Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);
//                            //convert the image to black and white does (8 bit)
//                            Imgproc.Canny(mat, mat, 35, 125);
//
//                            //apply gaussian blur to smoothen lines of dots
//
//
//                            //find the contours
//                            Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//                            double maxArea = -1;
//                            MatOfPoint largestContour = new MatOfPoint();
//                            for(MatOfPoint contour : contours) {
//                                double currentArea = Imgproc.contourArea(contour);
//                                if(currentArea > maxArea) {
//                                    maxArea = currentArea;
//                                    largestContour = contour;
//                                }
//                            }
//
//                            Projection projection = googleMap.getProjection();
//
//                            Point[] points = largestContour.toArray();
//                            LatLng latLng = projection.fromScreenLocation(new android.graphics.Point((int)points[0].x, (int)points[0].y));
//
//                            googleMap.addMarker(new MarkerOptions().position(latLng).title("Hello"));
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

            }
        });

        startMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startMeasure.getText().toString().startsWith("Start")) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Toast.makeText(getActivity(), "No permission granted for accessing location..", Toast.LENGTH_SHORT).show();
                    } else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(getActivity(), "GPS Not Enabled..", Toast.LENGTH_SHORT).show();
                    } else {
                        latLngs.clear();
                        googleMap.clear();
                        distance = 0;

                        textArea.setText(R.string.str_area);
                        textDistance.setText(R.string.str_distance);

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900, 0, mLocationListener);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 900, 0, mLocationListener);

                        startMeasure.setText(R.string.str_stop_measuring);
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                } else {
                    startMeasure.setText(R.string.str_start_measuring);
                    if(!latLngs.isEmpty()) {
                        textArea.setText("Area: " + new BigDecimal(SphericalUtil.computeArea(latLngs) * 0.000247105)
                                .setScale(2, BigDecimal.ROUND_HALF_UP)
                                .doubleValue() + " acres");
                    }
                    locationManager.removeUpdates(mLocationListener);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            FrameLayout.LayoutParams layoutParams;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int event_action = event.getAction();

                int[] offset = new int[2];
                v.getLocationOnScreen(offset);

                int X = (int) (event.getX() + offset[0]);
                int Y = (int) (event.getY() - (1.0 * v.getHeight()) + offset[1]);

                Log.d(TAG, "(X, Y): " + "(" + X + ", " + Y + ")");
                layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();

                switch (event_action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        //FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                        //_xDelta = offset[0] - layoutParams.leftMargin - (2 * v.getWidth());
                        //_yDelta = offset[1] - layoutParams.topMargin - (2 * v.getHeight());

                        layoutParams.rightMargin = -2*v.getWidth();
                        layoutParams.bottomMargin = -2*v.getHeight();

                        v.setLayoutParams(layoutParams);
                        //projection = googleMap.getProjection();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if(!hasMoved) {
                            hasMoved = true;
                            if(!isMidMarker) {
                                //undo.pushOnStack(invisibleMarker, invisibleMarkerPos, false, false);
                                if(list.size() > 5 && isPolygon)
                                    removeMidMarkers(invisibleMarkerPos);
                            }
                            else {
                                invisibleMarkerPos = invisibleMidMarkerPos;
                                //undo.pushOnStack(invisibleMidMarker, invisibleMidMarkerPos, true, false);
                                if (isNormalMarker) {
                                    Log.d(TAG, "Here we are");
                                    invisibleMarkerPos++;
                                    removeMidMarkers(invisibleMarkerPos);
                                    isMidMarker = false;
                                }
                            }

                            if((list.size() < 2 || invisibleMarkerPos == 0) && isPolygon)
                                ++invisibleMarkerPos;
                        }

                        Point x_y_points = new Point(X, Y);
                        LatLng latLng = projection.fromScreenLocation(x_y_points);

                        layoutParams.leftMargin = X - (int) (0.5 * v.getWidth());
                        layoutParams.topMargin = Y;

                        Log.d(TAG+"_Margins", layoutParams.leftMargin + ", " + layoutParams.topMargin + ", " + layoutParams.rightMargin + ", " + layoutParams.bottomMargin);
                        v.setLayoutParams(layoutParams);

                        lastLatLng = latLng;

                        list.set(isMidMarker ? invisibleMarkerPos : isPolygon ? invisibleMarkerPos - 1 : invisibleMarkerPos, latLng);

                        if(isPolygon) {
                            polygon.setPoints(list);
                            polygon.setFillColor(Color.parseColor("#00000000"));
                        }

                        else
                            polyline.setPoints(list);

                        updateComputations();
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "Selected Marker Up");

                        if((list.size() < 2 || invisibleMarkerPos == 1) && isPolygon)
                            --invisibleMarkerPos;
                        onEventUpOrMarkerSelected();
                        break;

                    default:
                        break;
                }
                return true;
            }
        });

        frMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if((invisibleMarker != null || invisibleMidMarker != null)) {
                            Point point = projection.toScreenLocation(invisibleMidMarker != null ? invisibleMidMarker.getPosition() : invisibleMarker.getPosition());
                            layoutParams.leftMargin = point.x - (imageView.getWidth() / 2);
                            layoutParams.topMargin = point.y;

                            imageView.setLayoutParams(layoutParams);

                            if (new Rect(point.x - (imageView.getWidth() / 2), point.y, point.x + (imageView.getWidth() / 2), point.y + (imageView.getHeight())).contains((int) event.getX(), (int) event.getY())) {
                                imageView.setVisibility(View.VISIBLE);
                                if(invisibleMidMarker != null)
                                    invisibleMidMarker.setVisible(false);
                                else
                                    invisibleMarker.setVisible(false);
                                frMap.dispatchTouchEvent(event);

                                Log.d(TAG, "Should move");
                                return true;
                            }
                        } else {
                            if(fabMenu.isExpanded())
                                fabMenu.collapse();
                        }
                        Log.d(TAG, "AAAAAAAAAA");
                        break;


//                    case MotionEvent.ACTION_MOVE:
//                        if(invisibleMarker != null || invisibleMidMarker != null) {
//                            Point point = projection.toScreenLocation(invisibleMidMarker != null ? invisibleMidMarker.getPosition() : invisibleMarker.getPosition());
//                            layoutParams.leftMargin = point.x - (imageView.getWidth() / 2);
//                            layoutParams.topMargin = point.y;
//                            imageView.setLayoutParams(layoutParams);
//
//                            if (new Rect(point.x - (imageView.getWidth() / 2), point.y, point.x + (imageView.getWidth() / 2), point.y + (imageView.getHeight())).contains((int) event.getX(), (int) event.getY())) {
//                                imageView.setVisibility(View.VISIBLE);
//                                if(invisibleMidMarker != null)
//                                    invisibleMidMarker.setVisible(false);
//                                else
//                                    invisibleMarker.setVisible(false);
//                                frMap.dispatchTouchEvent(event);
////                                new Handler().postDelayed(new Runnable() {
////                                    @Override
////                                    public void run() {
////
////                                    }
////                                }, 2);
//
//                                Log.d(TAG, "Should move");
//                                return true;
//                            }
//                        }
//                        return false;
                }
                return false;
            }
        });
    }

    private void onEventUpOrMarkerSelected() {
        if(isPolygon)
            polygon.setFillColor(Color.parseColor(fillColor));

        if(invisibleMarker != null) {
            invisibleMarker.setPosition(lastLatLng);
            invisibleMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
            invisibleMarker.setAnchor(0.5F, 0.0F);
            invisibleMarker.setVisible(true);
        }

        else if(invisibleMidMarker != null) {
            invisibleMidMarker.setPosition(lastLatLng);
            invisibleMidMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
            invisibleMidMarker.setAnchor(0.5F, 0.0F);
            invisibleMidMarker.setVisible(true);
        }

        if(hasMoved && isPolygon) {
            if(list.size() > 2) {
                if (!isMidMarker) {
                    addMidMarkers(invisibleMarkerPos, false);
                } else {
                    addMidMarkers(invisibleMidMarkerPos, true);
                }
            }
        }

//        else if(hasMoved)
//            addMidMarkers(invisibleMidMarkerPos, true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
            }
        }, 10);

        hasMoved = false;
    }

    private void setUpPolyOptions() {

        polygonOptions = new PolygonOptions();
        polygonOptions.fillColor(Color.parseColor(fillColor));
        polygonOptions.strokeColor(Color.parseColor(strokeColor));
        polygonOptions.strokeWidth(1.5F);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor(strokeColor));
        polylineOptions.width(1.5F);
    }

    private void setUpListeners(final boolean isPolygon) {

        final int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);

        pxHalf = (int) (px/1.5f);

        mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = ContextCompat.getDrawable(getContext(), R.drawable.circle);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);

        mDotHalfMarkerBitmap = Bitmap.createScaledBitmap(mDotMarkerBitmap, (int)(px/1.5f), (int)(px/1.5f), false);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return handleOnMarkerClick(marker);
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(invisibleMarker != null) {//&& invisibleMarker.isVisible()) {
                    invisibleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap));
                    invisibleMarker.setAnchor(0.5F, 0.5F);
                    invisibleMarker.setVisible(true);

                    imageView.setVisibility(View.INVISIBLE);

                    invisibleMarker = null;
                    return;
                }

                isMidMarker = false;
                isNormalMarker = false;

                if(invisibleMidMarker != null) {//&& invisibleMidMarker.isVisible()) {
                    if(hasMoved) {
                        Log.d(TAG, "Map mid cl");
                        hasMoved = false;
                        addMidMarkers(invisibleMidMarkerPos, true);

                        invisibleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap));
                        invisibleMarker.setAnchor(0.5F, 0.5F);

                        invisibleMarker = null;

                        return;
                    }

                    invisibleMidMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap));
                    invisibleMidMarker.setAnchor(0.5F, 0.5F);
                    invisibleMidMarker.setVisible(true);

                    imageView.setVisibility(View.INVISIBLE);

                    invisibleMidMarker = null;
                    return;
                }

                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                        .position(latLng)
                        .anchor(0.5F, 0.5F)
                        .draggable(false));

                list.add(latLng);

                //undo.pushOnStack(marker, list.size() - 1, false, true);

                if(isPolygon && list.size() > 6)
                    finishAddingMarker();

                else if(isPolygon && list.size() == 3) {
                    projection = googleMap.getProjection();
                    Marker theVeryFirstMidMarker = googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap))
                            .position(SphericalUtil.interpolate(list.get(0), list.get(1), 0.5F))
                            .anchor(0.5F, 0.5F)
                            .draggable(false));

                    midMarkers.put(theVeryFirstMidMarker.getId() /*+ "|" + 1*/, theVeryFirstMidMarker);
                    list.add(1, theVeryFirstMidMarker.getPosition());

                    addMidMarkers(3, false);
                }

                if(list.size() == 1) {
                    if(isPolygon) {
                        polygonOptions.add(latLng);
                        polygon = googleMap.addPolygon(polygonOptions);
                    } else {
                        polylineOptions.add(latLng);
                        polyline = googleMap.addPolyline(polylineOptions);
                    }
                } else {
                    if(isPolygon)
                        polygon.setPoints(list);
                    else
                        polyline.setPoints(list);
                }

                markers.put(marker.getId(), marker);

                updateComputations();
            }
        });

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                projection = googleMap.getProjection();
                if(invisibleMarker != null || invisibleMidMarker != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Point point = projection.toScreenLocation(invisibleMarker != null ? invisibleMarker.getPosition() : invisibleMidMarker.getPosition());
                            layoutParams.leftMargin = point.x - (imageView.getWidth() / 2);
                            layoutParams.topMargin = point.y;

                            imageView.setLayoutParams(layoutParams);
                            imageView.invalidate();
                            Log.d(TAG, point.x + ", " + point.y);
                        }
                    }, 5);
                }
            }
        });
    }

    private void updateComputations() {
        if(isPolygon)
            textArea.setText(Html.fromHtml("" + new BigDecimal(SphericalUtil.computeArea(list) * areaFactor)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue() + " " + areaUnit));

        textDistance.setText(Html.fromHtml("" + new BigDecimal(SphericalUtil.computeLength(list) * distanceFactor)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue() + " " + distanceUnit));
    }

    private void selectNextMarker(LatLng currentPos) {
        boolean markerFound = false;
        for (Object o : midMarkers.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Marker marker = (Marker) pair.getValue();
            if(marker.getPosition().equals(currentPos)) {
                markerFound = true;
                handleOnMarkerClick(marker);
                break;
            }
        }

        if(!markerFound) {
            for (Object o : markers.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                Marker marker = (Marker) pair.getValue();
                if(marker.getPosition().equals(currentPos)) {
                    handleOnMarkerClick(marker);
                    break;
                }
            }
        }
    }

    private boolean handleOnMarkerClick(Marker marker) {
        final Marker marker_plot = marker;
        isMidMarker = false;

        if(invisibleMidMarker != null && marker.equals(invisibleMidMarker))
            return false;

        if(invisibleMarker != null && marker.equals(invisibleMarker))
            return false;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Projection projection = AreaMapFragment.this.googleMap.getProjection();
                Point point = projection.toScreenLocation(marker_plot.getPosition());

                layoutParams.leftMargin = point.x - (imageView.getWidth() / 2);
                layoutParams.topMargin = point.y;
                imageView.setLayoutParams(layoutParams);
            }
        }, 10);

        lastLatLng = marker.getPosition();

        if(invisibleMarker != null) {
            invisibleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap));
            invisibleMarker.setAnchor(0.5F, 0.5F);

            invisibleMarker.setVisible(true);

            invisibleMarker = null;
        } else if(invisibleMidMarker != null) {
            invisibleMidMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap));
            invisibleMidMarker.setAnchor(0.5F, 0.5F);

            invisibleMidMarker.setVisible(true);
            invisibleMidMarker = null;
        }

        for(Object o : midMarkers.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            if (pair.getKey().toString().split("\\|")[0].equals(marker.getId())) {
                //invisibleMidMarkerPos = Integer.parseInt(pair.getKey().toString().split("\\|")[1]);
                invisibleMidMarker = (Marker) pair.getValue();
                isNormalMarker = false;
                isMidMarker = true;
                Log.d(TAG, "Mid Marker Selected");
                break;
            }
        }

        if(invisibleMidMarker == null) {
            for (Object o : markers.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (pair.getKey().toString().split("\\|")[0].equals(marker.getId())) {
                    //invisibleMarkerPos = Integer.parseInt(pair.getKey().toString().split("\\|")[1]);
                    invisibleMarker = (Marker) pair.getValue();
                    isNormalMarker = true;
                    break;
                }
            }
        }

        setMarkerPosition();

        onEventUpOrMarkerSelected();

        return true;
    }

    private void setMarkerPosition() {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(lastLatLng)) {
                if(invisibleMarker == null) {
                    invisibleMidMarkerPos = i;
                } else {
                    invisibleMarkerPos = i;
                    Log.d(TAG, "Pos: " + invisibleMarkerPos + " size: " + list.size());
                }
                break;
            }
        }
    }

    private void finishAddingMarker() {

        LatLng endMidMarkerLatLng = list.get(list.size() - 2);

        for(Object o : midMarkers.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Marker midMarker = (Marker)pair.getValue();
            //Log.d(TAG + "_Keys", key);
            if(midMarker.getPosition().equals(endMidMarkerLatLng)) {
                Log.d(TAG, "End mid marker removed");
                if(list.size() > 9)
                    midMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

                midMarkers.remove(pair.getKey()).remove();
                break;
            }
        }

        LatLng latLng = list.remove(list.size() - 2);
        if(latLng == null) {
            Log.d(TAG, "nulll");
        }
        addMidMarkers(list.size() - 1, false);

    }

    private void addMidMarkers(int index, boolean isMid) {
        Log.d(TAG, "Index: "+index);
        if(index != -1) {

            Marker markerMidPrev = googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap))
                    .position(SphericalUtil.interpolate(list.get(isNormalMarker && index != 0 ? index - 1 : index), list.get(isNormalMarker ? index != 0 ? index - 2 : list.size() - 1 : index - 1), 0.5F))
                    .anchor(0.5F, 0.5F)
                    .draggable(false));

            Marker markerMidAfter = googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap))
                    .position(SphericalUtil.interpolate(list.get(isNormalMarker && index != 0 ? index - 1 : index), list.get(index == list.size() - 1 || index == list.size() ? 0 : (isNormalMarker && index != 0 ? index : index + 1)), 0.5F))
                    .anchor(0.5F, 0.5F)
                    .draggable(false));

            if(isMid) {
                midMarkers.remove(invisibleMidMarker.getId());
                markers.put(invisibleMidMarker.getId(), invisibleMidMarker);
            }

            list.add(isNormalMarker ? index != 0 ? index - 1 : list.size() : index, markerMidPrev.getPosition());
            list.add(isNormalMarker ? index + 1 : index + 2, markerMidAfter.getPosition());

            Log.d(TAG, isNormalMarker + " Indices: " + (index - 1) + ", " + (index + 1));
            midMarkers.put(markerMidPrev.getId() /*+ "|" + (isNormalMarker ? index != 0 ? index - 1 : list.size() - 1 : index)*/, markerMidPrev);
            midMarkers.put(markerMidAfter.getId() /*+ "|" + (isNormalMarker ? index + 1 : index + 2)*/, markerMidAfter);

            if(isMid) {
                invisibleMarker = invisibleMidMarker;
                invisibleMarkerPos = index + 1;

                isNormalMarker = true;
                invisibleMidMarker = null;
            }

            Log.d(TAG, "List Size: " + list.size());
            polygon.setPoints(list);
        }
    }

    private void removeMidMarkers(int index) {
        LatLng mid_1 = list.remove(index != 0 ? index - 1 : list.size() - 1);
        LatLng mid_2 = list.remove(index != 0 ? index : index + 1);

        boolean oneRemoved = false, midFirstRemoved = false, midSecondRemoved = false;

        Iterator iterator = midMarkers.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Marker midMarker = (Marker)pair.getValue();

            if(midMarker.getPosition().equals(mid_1) && !midFirstRemoved) {
                midMarker.remove();
                iterator.remove();

                Log.d(TAG, "Mid_1 Removed");
                midFirstRemoved = true;
                if(oneRemoved)
                    break;
                else
                    oneRemoved = true;
            }

            if(midMarker.getPosition().equals(mid_2) && !midSecondRemoved) {
                iterator.remove();

                Log.d(TAG, "Mid_2 Removed");
                midSecondRemoved = true;
                midMarker.remove();
                if(oneRemoved)
                    break;
                else
                    oneRemoved = true;
            }
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition position = CameraPosition.builder()
                    .target(latLng)
                    .zoom(18f)
                    .bearing(0.0f)
                    .tilt(0.0f)
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

            if(latLngs.size() == 3)
                googleMap.addPolygon(rectOptions);

            latLngs.add(latLng);
            rectOptions.add(latLng);

            distance += SphericalUtil.computeDistanceBetween(latLngs.get(latLngs.size() < 2 ? 0 : latLngs.size() - 2), latLng)/1000;
            textDistance.setText("Distance: " + new BigDecimal(distance).setScale(4, RoundingMode.HALF_UP).doubleValue() + " km");
            Log.d(LOG_TAG, "Location changed: " + latLng);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(Utility.DISCARD_POLY_EVENT);
        filter.addAction(Utility.SAVE_POLY_EVENT);
        filter.addAction(Utility.LOAD_POLY_EVENT);
        filter.addAction(Utility.CHANGE_MAP_HYBRID);
        filter.addAction(Utility.CHANGE_MAP_NORMAL);
        filter.addAction(Utility.CHANGE_MAP_SATELLITE);
        filter.addAction(Utility.CHANGE_MAP_TERRAIN);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Utility.DISCARD_POLY_EVENT)) {
                    clearAll();
                } else if(intent.getAction().equals(Utility.SAVE_POLY_EVENT)) {
                    Log.d(TAG, "Saving..");
                    if(isPolygon) {
                        new Utility().savePoly((getActivity()), isPolygon, isGPS ? latLngs : list, isGPS ? "GPS" : "Manual", textArea.getText().toString(), textDistance.getText().toString(), getActivity().getTitle().equals("Mark Off") ? intent.getStringExtra(Utility.POLY_NAME) : getActivity().getTitle(), intent.getIntExtra("computationId", -1));
                    } else {
                        new Utility().savePoly((getActivity()), isPolygon, list, isGPS ? "GPS" : "Manual", textDistance.getText().toString(), getActivity().getTitle().equals("Mark Off") ? intent.getStringExtra(Utility.POLY_NAME) : getActivity().getTitle(), intent.getIntExtra("computationId", -1));
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clearAll();
                        }
                    }, 900);

                } else if(intent.getAction().equals(Utility.LOAD_POLY_EVENT)) {
                    Log.d(TAG, "Loading Poly..");
                    details = intent.getParcelableExtra("ComputationObj");
                    //getActivity().setTitle(details.getName());
                    new LoadPolyOnMap(getContext()).execute(details.isArea(), details.getComputationId());

                    final Animation animation_down = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_down);
                    animation_down.setStartOffset(30);

                    final Animation animation_up = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_up);
                    animation_up.setStartOffset(700);

                    fabMenu.startAnimation(animation_down);
                    calLayout.startAnimation(animation_up);

                    animation_up.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            fabMenu.setVisibility(View.INVISIBLE);
                            calLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    deleteMarker.setVisibility(View.VISIBLE);
                } else if(intent.getAction().equals(Utility.CHANGE_MAP_HYBRID)) {
                    curMapTypeIndex = 2;
                    AreaMapFragment.this.googleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                } else if(intent.getAction().equals(Utility.CHANGE_MAP_NORMAL)) {
                    curMapTypeIndex = 1;
                    AreaMapFragment.this.googleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                } else if(intent.getAction().equals(Utility.CHANGE_MAP_SATELLITE)) {
                    curMapTypeIndex = 0;
                    AreaMapFragment.this.googleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                } else if(intent.getAction().equals(Utility.CHANGE_MAP_TERRAIN)) {
                    curMapTypeIndex = 3;
                    AreaMapFragment.this.googleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                }
            }
        };

        updateFactorsAndUnits();
        getContext().registerReceiver(receiver, filter);
    }

    private void updateFactorsAndUnits() {
        String[] areaDetails = MySharedPreferences.getDefaultAreaUnit(getContext()).split("\\|");
        String[] distanceDetails = MySharedPreferences.getDefaultDistanceUnit(getContext()).split("\\|");

        areaFactor = Double.parseDouble(areaDetails[1]);
        distanceFactor = Double.parseDouble(distanceDetails[1]);

        strokeColor = MySharedPreferences.getDefaultStrokeColor(getContext());
        fillColor = MySharedPreferences.getDefaultFillColor(getContext());

        areaUnit = areaDetails[0].contains("(") ? areaDetails[0].split("\\(")[1].replace(")","") : areaDetails[0];
        distanceUnit = distanceDetails[0].contains("(") ? distanceDetails[0].split("\\(")[1].replace(")","") : distanceDetails[0];

    }

    private void clearAll() {
        googleMap.clear();
        latLngs.clear();
        list.clear();
        setUpPolyOptions();
        googleMap.setOnMapClickListener(null);
        invisibleMarker = null;
        invisibleMidMarker = null;
        invisibleMarkerPos = -1;
        invisibleMidMarkerPos = -1;
        isNormalMarker = false;
        isMidMarker = false;
        midMarkers.clear();
        markers.clear();
        textDistance.setText(R.string.str_distance);
        textArea.setText(R.string.str_area);

        Animation animation_down = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_down);
        animation_down.setStartOffset(30);

        Animation animation_up = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_up);
        animation_up.setStartOffset(700);

        animation_up.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                calLayout.setVisibility(View.INVISIBLE);
                fabMenu.setVisibility(View.VISIBLE);
                startMeasure.setVisibility(View.INVISIBLE);

                textArea.setVisibility(View.VISIBLE);
                textAreaLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        calLayout.startAnimation(animation_down);
        fabMenu.startAnimation(animation_up);

        if(startMeasure.getVisibility() == View.VISIBLE)
            startMeasure.startAnimation(animation_down);
    }

    public void onPause() {
        super.onPause();

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        initCamera();
    }

    private void initCamera() {
        final CameraPosition position = CameraPosition.builder()
                .target(new LatLng(0, 0))
                .zoom(2f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        /* Methods Not Called */
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                AreaMapFragment.this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
            }
        });

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                AreaMapFragment.this.googleMap.setMyLocationEnabled(true);
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private LatLngBounds getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < polygonPointsList.size() ; i++)
        {
            builder.include(polygonPointsList.get(i));
        }

        return builder.build();
    }

    private class LoadPolyOnMap extends AsyncTask<Object, Void, List<LatLng>> {

        MaterialDialog dialog;
        Context context;
        int computationId;
        boolean isPolygon;

        String area, perimeter, distance, tag;

        LoadPolyOnMap(Context context) {
            this.context = context;
        }

        public void onPreExecute() {
            dialog = new Utility().createIndeterminateDialog(context, "Loading Computations..");
            googleMap.clear();
            latLngs.clear();
            list.clear();
            setUpPolyOptions();
            invisibleMarker = null;
            invisibleMidMarker = null;
            invisibleMarkerPos = -1;
            invisibleMidMarkerPos = -1;
            isNormalMarker = false;
            isMidMarker = false;
            midMarkers = new HashMap<>();
            markers = new HashMap<>();
//            midMarkers.clear();
//            markers.clear();
            dialog.show();
        }

        @Override
        protected List<LatLng> doInBackground(Object... params) {
            AreaMapFragment.this.isPolygon = (boolean) params[0];
            computationId = (int) params[1];

            Log.d(TAG+ "_bool", ""+isPolygon);

            Type listType = new TypeToken<List<MyLatLng>>() {}.getType();
            List<MyLatLng> myLatLngs;

            if(isPolygon) {
                Cursor cursor = context.getContentResolver().query(MarkOffContract.AreaComputations.CONTENT_URI, null, MarkOffContract.AreaComputations.ID + "=" + computationId, null, null);
                if(cursor != null && cursor.moveToFirst()) {
                    area = cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.AREA));
                    perimeter = cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.PERIMETER));
                    tag = cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.TAG));

                    if (tag.equals("Manual")) {
                        String poly = cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.POLY));
                        Log.d(TAG, poly);

                        myLatLngs = new Gson().fromJson(poly, listType);
                        for(MyLatLng myLatLng : myLatLngs) {
                            list.add(new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude()));
                        }

                        cursor.close();
                        return list;
                    } else {
                        myLatLngs = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.POLY)), listType);
                        for(MyLatLng myLatLng : myLatLngs) {
                            latLngs.add(new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude()));
                        }

                        cursor.close();
                        return latLngs;
                    }
                }
            } else {
                Cursor cursor = context.getContentResolver().query(MarkOffContract.DistanceComputations.CONTENT_URI, null, MarkOffContract.DistanceComputations.ID + "=" + computationId, null, null);
                if(cursor != null && cursor.moveToFirst()) {
                    distance = cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.DISTANCE));
                    tag = cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.TAG));

                    myLatLngs = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(MarkOffContract.DistanceComputations.POLY)), listType);

                    for(MyLatLng myLatLng : myLatLngs) {
                        AreaMapFragment.this.list.add(new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude()));
                    }

                    cursor.close();
                }

                return list;
            }

            return null;
        }

        protected void onPostExecute(final List<LatLng> result) {
            dialog.dismiss();
            AreaMapFragment.this.isPolygon = isPolygon;
            setUpListeners(isPolygon);

            if(result != null) {
                context.sendBroadcast(new Intent(Utility.SAVE_LOADED_POLY).putExtra("ComputationObj", details));
                if(isPolygon) {
                    getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            textAreaLabel.setVisibility(View.VISIBLE);
                            textArea.setVisibility(View.VISIBLE);

                            textArea.setText(area);
                            textDistance.setText(perimeter);

                            AreaMapFragment.this.googleMap = googleMap;
                            if(tag.equals("Manual")) {
                                Log.d("Size", ""+list.size());
                                for (int i = 0; i < list.size(); i++) {
                                    if (i % 2 == 0) {
                                        Marker marker = AreaMapFragment.this.googleMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                                                .position(list.get(i))
                                                .anchor(0.5F, 0.5F)
                                                .draggable(false));

                                        markers.put(marker.getId(), marker);
                                    } else {
                                        Marker marker = AreaMapFragment.this.googleMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromBitmap(mDotHalfMarkerBitmap))
                                                .position(list.get(i))
                                                .anchor(0.5F, 0.5F)
                                                .draggable(false));

                                        midMarkers.put(marker.getId(), marker);
                                    }
                                }
                            }

                            polygonOptions.addAll(result);
                            polygon = googleMap.addPolygon(polygonOptions);

                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getPolygonCenterPoint(list), 100));
                        }
                    });
                } else {
                    textAreaLabel.setVisibility(View.GONE);
                    textArea.setVisibility(View.GONE);

                    textDistance.setText(distance);

                    if(tag.equals("Manual")) {
                        for (LatLng latLng : list) {
                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                                    .position(latLng)
                                    .anchor(0.5F, 0.5F)
                                    .draggable(false));

                            markers.put(marker.getId(), marker);
                        }
                    }

                    polylineOptions.addAll(result);
                    polyline = googleMap.addPolyline(polylineOptions);
                }
            }
        }
    }
}