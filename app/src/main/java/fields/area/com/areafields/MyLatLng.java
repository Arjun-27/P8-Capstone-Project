package fields.area.com.areafields;

/**
 * Created by Arjun on 19-Jan-2017 for AreaFields.
 */

public class MyLatLng {

    private double latitude, longitude;

    public MyLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
