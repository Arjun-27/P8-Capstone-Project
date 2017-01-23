package fields.area.com.areafields.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import fields.area.com.areafields.R;

/**
 * Created by Arjun on 23-Jan-2017 for AreaFields.
 */

public class AreasWidgetProvider extends AppWidgetProvider {
    public static String WIDGET_BUTTON = "fields.area.com.areafields.WIDGET_BUTTON";

    public void onReceive(Context context, Intent intent) {
        if(WIDGET_BUTTON.equals(intent.getAction())) {
            updateWidget(context);
        }

        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            for(int appWidgetId : appWidgetIds) {
                Intent intent = new Intent(context, AreasWidgetService.class);

                // Add the app widget ID to the intent extras.
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
                rv.setRemoteAdapter(R.id.areas_list, intent);
                rv.setEmptyView(R.id.areas_list, R.id.textEmpty_w);

                if(appWidgetId == R.xml.areas_widget_provider) {
                    updateWidget(context);
                }

                Intent intent1 = new Intent(WIDGET_BUTTON);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.button, pendingIntent);

                Log.d("Updating", "Updated at " + System.currentTimeMillis());
                appWidgetManager.updateAppWidget(appWidgetId, rv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void updateWidget(Context context) {
        UpdateWidget.updateWidget(context);
        Toast.makeText(context, R.string.str_updating_widget, Toast.LENGTH_SHORT).show();
    }
}
