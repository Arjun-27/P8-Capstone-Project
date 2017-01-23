package fields.area.com.areafields;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Arjun on 10-Dec-2016 for AreaFields.
 */

public class UndoDetails {
    private Marker marker;
    private int position;
    private boolean isMidMarker;
    private boolean isAddedOnMap;

    public UndoDetails(Marker marker, int position, boolean isMidMarker, boolean isAddedOnMap) {
        this.marker = marker;
        this.position = position;
        this.isMidMarker = isMidMarker;
        this.isAddedOnMap = isAddedOnMap;
    }

    public Marker getMarker() {
        return marker;
    }

    public int getPosition() {
        return position;
    }

    public boolean isMidMarker() {
        return isMidMarker;
    }

    public boolean isAddedOnMap() {
        return isAddedOnMap;
    }
}
