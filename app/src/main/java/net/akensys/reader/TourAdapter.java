package net.akensys.reader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.akensys.reader.model.Network;
import net.akensys.reader.model.Tour;

import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourAdapterViewHolder> {

    private List<Tour> tours;

    private final TourAdapterOnClickHandler mClickHandler;

    public interface TourAdapterOnClickHandler {
        void onClick(Tour tour);
    }

    public class TourAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final Button mTourListItemButton;

        public TourAdapterViewHolder(View view) {
            super(view);
            this.mTourListItemButton = (Button) view.findViewById(R.id.btn_tour_list_item);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Tour tour = tours.get(adapterPosition);
            mClickHandler.onClick(tour);
        }
    }

    public TourAdapter(TourAdapter.TourAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public TourAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context ctx = viewGroup.getContext();
        int layoutIdForListItem = R.layout.tour_list_item;
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new TourAdapter.TourAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourAdapterViewHolder holder, int position) {
        Tour tour = tours.get(position);
        holder.mTourListItemButton.setText(tour.getName());
    }

    @Override
    public int getItemCount() {
        if (tours == null) return 0;
        return tours.size();
    }

    public void setTours(List<Tour> _tours) {
        Gson gson = new Gson();
        tours = gson.fromJson(gson.toJson(_tours), new TypeToken<List<Tour>>(){}.getType());
        notifyDataSetChanged();
    }

}
