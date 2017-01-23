package fields.area.com.areafields.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Arjun on 23-Jan-2017 for AreaFields.
 */

public class AreasWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AreasWidgetDataProvider(getApplicationContext(), intent);
    }
}