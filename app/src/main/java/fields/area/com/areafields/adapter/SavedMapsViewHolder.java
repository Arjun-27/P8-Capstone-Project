package fields.area.com.areafields.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import fields.area.com.areafields.R;

/**
 * Created by Arjun on 13-Jan-2017 for AreaFields.
 */

public class SavedMapsViewHolder extends RecyclerView.ViewHolder {

    TextView textName, textAreaLabel, textPerimeterLabel, textArea, textPerimeter;

    View itemView;

    public SavedMapsViewHolder(View itemView) {
        super(itemView);

        this.itemView = itemView;

        textName = (TextView) itemView.findViewById(R.id.m_name);
        textAreaLabel = (TextView) itemView.findViewById(R.id.text_area);
        textPerimeterLabel = (TextView) itemView.findViewById(R.id.text_perimeter);

        textArea = (TextView) itemView.findViewById(R.id.text_area_container);
        textPerimeter = (TextView) itemView.findViewById(R.id.text_perimeter_container);
    }
}
