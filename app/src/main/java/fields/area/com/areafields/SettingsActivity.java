package fields.area.com.areafields;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import fields.area.com.areafields.data.MySharedPreferences;

/**
 * Created by Arjun on 25-Dec-2016 for AreaFields.
 *
 */

public class SettingsActivity extends AppCompatActivity {

    TextView areaUnits, distanceUnits, strokeColor, fillColor;
    String currentAreaFactor, currentDistanceFactor;

    View strokeColorView, fillColorView;

    ShapeDrawable strokeBackground, fillBackground;

    String[] unitAreaFactors, unitDistanceFactors;

    Spanned[] unitsArea, unitsDistance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);

        areaUnits = (TextView) findViewById(R.id.area_units);
        distanceUnits = (TextView) findViewById(R.id.distance_units);

        strokeColor = (TextView) findViewById(R.id.stroke_units);
        fillColor = (TextView) findViewById(R.id.fill_units);

        strokeColorView = findViewById(R.id.color_stroke);
        fillColorView = findViewById(R.id.color_fill);

        setTitle(R.string.str_settings);

        final String[] listAreaFactors = getResources().getStringArray(R.array.area_units_factor);
        final String[] listDistanceFactors = getResources().getStringArray(R.array.distance_units_factor);

        strokeBackground = new ShapeDrawable();
        fillBackground = new ShapeDrawable();

        areaUnits.setText(Html.fromHtml(MySharedPreferences.getDefaultAreaUnit(this).split("\\|")[0]));
        distanceUnits.setText(Html.fromHtml(MySharedPreferences.getDefaultDistanceUnit(this).split("\\|")[0]));
        strokeColor.setText(MySharedPreferences.getDefaultStrokeColor(this));
        fillColor.setText(MySharedPreferences.getDefaultFillColor(this));

        unitAreaFactors = getResources().getStringArray(R.array.area_units_list);
        unitDistanceFactors = getResources().getStringArray(R.array.distance_units_list);

        unitsArea = new Spanned[7];
        unitsDistance = new Spanned[7];

        for(int i = 0; i < unitAreaFactors.length; i++) {
            unitsArea[i] = Html.fromHtml(unitAreaFactors[i]);
        }

        for(int i = 0; i < unitDistanceFactors.length; i++) {
            unitsDistance[i] = Html.fromHtml(unitDistanceFactors[i]);
        }

        float[] radii = new float[8];
        radii[0] = getResources().getDimension(R.dimen.radius);
        radii[1] = radii[0];

        radii[2] = radii[0];
        radii[3] = radii[0];

        radii[4] = radii[0];
        radii[5] = radii[0];

        radii[6] = radii[0];
        radii[7] = radii[0];

        strokeBackground.setShape(new RoundRectShape(radii, null, null));
        strokeBackground.getPaint().setColor(Color.parseColor(MySharedPreferences.getDefaultStrokeColor(this)));

        fillBackground.setShape(new RoundRectShape(radii, null, null));
        fillBackground.getPaint().setColor(Color.parseColor(MySharedPreferences.getDefaultFillColor(this)));

        strokeColorView.setBackground(strokeBackground);
        fillColorView.setBackground(fillBackground);

        areaUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(SettingsActivity.this)
                        .items(unitsArea)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                currentAreaFactor = listAreaFactors[which];
                                Log.d("SharedPrefs", currentAreaFactor);
                                areaUnits.setText(unitsArea[which]);

                                MySharedPreferences.saveDefaultAreaUnit(SettingsActivity.this, unitAreaFactors[which] + "|" + currentAreaFactor);

                                return true;
                            }
                        }).positiveText("Choose")
                        .build()
                        .show();
            }
        });

        distanceUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(SettingsActivity.this)
                        .items(unitsDistance)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                currentDistanceFactor = listDistanceFactors[which];
                                distanceUnits.setText(text.toString());

                                MySharedPreferences.saveDefaultAreaUnit(SettingsActivity.this, unitDistanceFactors[which] + "|" + currentDistanceFactor);

                                return true;
                            }
                        }).positiveText("Choose")
                        .build()
                        .show();
            }
        });

        strokeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(SettingsActivity.this)
                        .setTitle("Choose Stroke Color")
                        .initialColor(Color.parseColor(MySharedPreferences.getDefaultStrokeColor(SettingsActivity.this)))
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {

                            }
                        }).setPositiveButton("Okay", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                String currentDefaultColor = "#" + Integer.toHexString(selectedColor).toUpperCase();

                                strokeBackground.getPaint().setColor(Color.parseColor(currentDefaultColor));
                                strokeBackground.invalidateSelf();
                                strokeColorView.setBackground(strokeBackground);

                                MySharedPreferences.saveDefaultStrokeColor(SettingsActivity.this, currentDefaultColor);

                                strokeColor.setText(currentDefaultColor);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        }).build()
                        .show();
            }
        });

        fillColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(SettingsActivity.this)
                        .setTitle("Choose Fill Color")
                        .initialColor(Color.parseColor(MySharedPreferences.getDefaultFillColor(SettingsActivity.this)))
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {

                            }
                        }).setPositiveButton("Okay", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                String currentDefaultColor = "#" + Integer.toHexString(selectedColor).toUpperCase();

                                fillBackground.getPaint().setColor(Color.parseColor(currentDefaultColor));
                                fillBackground.invalidateSelf();
                                fillColorView.setBackground(fillBackground);

                                MySharedPreferences.saveDefaultFillColor(SettingsActivity.this, currentDefaultColor);

                                fillColor.setText(currentDefaultColor);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        }).build()
                        .show();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }
}