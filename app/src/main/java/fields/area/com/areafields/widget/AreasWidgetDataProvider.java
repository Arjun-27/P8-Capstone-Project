package fields.area.com.areafields.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import fields.area.com.areafields.R;
import fields.area.com.areafields.data.MarkOffContract;

/**
 * Created by Arjun on 23-Jan-2017 for AreaFields.
 */

public class AreasWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Cursor cursor = null;
    private boolean doUpdate;
    private int mAppWidgetId;

    public AreasWidgetDataProvider(Context context, Intent intent) {
        this.context = context;

        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        Log.d("Create", "In Provider");
    }

    @Override
    public void onDataSetChanged() {
        initCursor();
    }

    @Override
    public void onDestroy() {
        if(cursor != null && !cursor.isClosed())
            cursor.close();
    }

    @Override
    public int getCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.layout_widget_list_item);
        if(cursor != null) {
            cursor.moveToPosition(position);

            Log.d("INFO", cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.NAME)));

            view.setTextViewText(R.id.txt_name, cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.NAME)));
            view.setTextViewText(R.id.area_measurement, cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.AREA)));
            view.setTextViewText(R.id.distance_measurement, cursor.getString(cursor.getColumnIndex(MarkOffContract.AreaComputations.PERIMETER)));
        }

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    private void initCursor() {
        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        cursor = context.getContentResolver().query(MarkOffContract.AreaComputations.CONTENT_URI, null, null, null, null);

        assert cursor != null;
        Log.d("COUNT", "" + cursor.getCount());
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}