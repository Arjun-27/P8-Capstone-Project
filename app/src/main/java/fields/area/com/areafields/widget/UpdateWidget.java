package fields.area.com.areafields.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

import fields.area.com.areafields.R;

/**
 * Created by Arjun on 23-Jan-2017 for AreaFields.
 */

public class UpdateWidget {

    public static void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, AreasWidgetProvider.class));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.areas_list);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

        Calendar cal = Calendar.getInstance();
        String date = getFormattedNo(cal.get(Calendar.DATE)) + "/" + getFormattedNo(cal.get(Calendar.MONTH) + 1) + " " + getFormattedNo(cal.get(Calendar.HOUR_OF_DAY)) + ":" + (cal.get(Calendar.MINUTE) < 10 ? getFormattedNo(0) + getFormattedNo(cal.get(Calendar.MINUTE)) : getFormattedNo(cal.get(Calendar.MINUTE)));
        views.setTextViewText(R.id.textWidDate, date);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        Log.d("Updating", "Updated at -> " + date);
    }

    private static String getFormattedNo(int no) {
        return String.format(Locale.getDefault() , no < 10 ? "0%d" : "%d", no);
    }
}
