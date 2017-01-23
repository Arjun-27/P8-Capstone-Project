package fields.area.com.areafields;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Stack;

/**
 * Created by Arjun on 23-Dec-2016 for AreaFields.
 */

public class Undo {
    private static final String TAG = "StackTag";
    private Stack<UndoDetails> undoDetailsStack;

    public Undo() {
        undoDetailsStack = new Stack<>();
    }

    public void pushOnStack(Marker marker, int position, boolean isMidMarker, boolean isAddedOnMap) {
        if(marker != null || position != -1) {
            undoDetailsStack.push(new UndoDetails(marker, position, isMidMarker, isAddedOnMap));
            Log.d(TAG, "Pushed To Stack...");
        } else
            Log.d(TAG, "Incorrect Values...");
    }

    public UndoDetails popFromStack() {
        return undoDetailsStack.size() > 0 ? undoDetailsStack.pop() : null;
    }
}
