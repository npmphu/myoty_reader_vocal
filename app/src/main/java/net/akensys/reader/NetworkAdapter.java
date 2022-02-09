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

import java.util.List;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.NetworkAdapterViewHolder>{

    private List<Network> networks;

    private final NetworkAdapterOnClickHandler mClickHandler;

    public interface NetworkAdapterOnClickHandler {
        void onClick(Network network);
    }

    public class NetworkAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final Button mNetworkListItemButton;

        public NetworkAdapterViewHolder(View view) {
            super(view);
            this.mNetworkListItemButton = (Button) view.findViewById(R.id.btn_network_list_item);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Network network = networks.get(adapterPosition);
            mClickHandler.onClick(network);
        }
    }

    public NetworkAdapter(NetworkAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public NetworkAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context ctx = viewGroup.getContext();
        int layoutIdForListItem = R.layout.network_list_item;
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new NetworkAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworkAdapterViewHolder holder, int position) {
        Network network = networks.get(position);
        holder.mNetworkListItemButton.setText(network.getLibelle());
    }

    @Override
    public int getItemCount() {
        if (networks == null) return 0;
        return networks.size();
    }

    public void setNetworks(List<Network> _networks) {
        Gson gson = new Gson();
        networks = gson.fromJson(gson.toJson(_networks), new TypeToken<List<Network>>(){}.getType());
        notifyDataSetChanged();
    }
}
