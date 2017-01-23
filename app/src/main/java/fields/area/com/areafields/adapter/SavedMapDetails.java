package fields.area.com.areafields.adapter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arjun on 14-Jan-2017 for AreaFields.
 */

public class SavedMapDetails implements Parcelable{

    private String area;
    private String perimeter;

    private String name;
    private boolean isArea;

    private int computationId;

    public SavedMapDetails() {

    }

    private SavedMapDetails(Parcel in) {
        area = in.readString();
        perimeter = in.readString();
        name = in.readString();
        isArea = in.readByte() != 0;
        computationId = in.readInt();
    }

    public static final Creator<SavedMapDetails> CREATOR = new Creator<SavedMapDetails>() {
        @Override
        public SavedMapDetails createFromParcel(Parcel in) {
            return new SavedMapDetails(in);
        }

        @Override
        public SavedMapDetails[] newArray(int size) {
            return new SavedMapDetails[size];
        }
    };

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPerimeter() {
        return perimeter;
    }

    public void setPerimeter(String perimeter) {
        this.perimeter = perimeter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArea() {
        return isArea;
    }

    public void setIsArea(boolean area) {
        isArea = area;
    }

    public int getComputationId() {
        return computationId;
    }

    public void setComputationId(int computationId) {
        this.computationId = computationId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(area);
        dest.writeString(perimeter);
        dest.writeString(name);
        dest.writeByte((byte) (isArea ? 1 : 0));
        dest.writeInt(computationId);
    }
}
