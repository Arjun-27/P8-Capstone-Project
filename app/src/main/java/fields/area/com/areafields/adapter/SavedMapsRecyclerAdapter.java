package fields.area.com.areafields.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Locale;

import fields.area.com.areafields.R;
import fields.area.com.areafields.Utility;

/**
 * Created by Arjun on 14-Jan-2017 for AreaFields.
 *
 */

public class SavedMapsRecyclerAdapter extends RecyclerView.Adapter<SavedMapsViewHolder> {

    private ArrayList<SavedMapDetails> mapList;
    private Context context;

    public SavedMapsRecyclerAdapter(ArrayList<SavedMapDetails> mapList, Context context) {
        this.mapList = mapList;
        this.context = context;
    }

    @Override
    public SavedMapsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SavedMapsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_saved_maps_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SavedMapsViewHolder holder, int position) {
        final SavedMapDetails details = mapList.get(position);

        holder.textName.setText(details.getName());

        if(details.isArea()) {
            holder.textArea.setText(String.format(Locale.getDefault(), "%s", details.getArea()));
            holder.textPerimeter.setText(String.format(Locale.getDefault(), "%s", details.getPerimeter()));
        } else {
            holder.textAreaLabel.setVisibility(View.GONE);
            holder.textArea.setVisibility(View.GONE);

            holder.textPerimeterLabel.setText(R.string.str_distance_text);
            holder.textPerimeter.setText(String.format(Locale.getDefault(), "%s", details.getPerimeter()));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ID", ""+details.getComputationId());
                context.sendBroadcast(new Intent(Utility.LOAD_POLY_EVENT).putExtra("ComputationObj", details));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().remove(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragFrame)).commit();
                        ((AppCompatActivity) context).getSupportFragmentManager().popBackStack();
                    }
                }, 400);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mapList == null ? 0 : mapList.size();
    }
}
