package net.akensys.reader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import net.akensys.reader.model.Client;
import net.akensys.reader.model.Network;
import net.akensys.reader.model.Tour;
import net.akensys.reader.service.AKRequest;

import java.util.HashMap;
import java.util.Map;

public class TourActivity extends AppCompatActivity implements TourAdapter.TourAdapterOnClickHandler {

    private RecyclerView mTourRecyclerView;
    private TourAdapter mTourAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTourAdapter = new TourAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mTourRecyclerView = findViewById(R.id.rv_tour);
        mTourRecyclerView.setLayoutManager(layoutManager);
        mTourRecyclerView.setHasFixedSize(true);
        mTourRecyclerView.setAdapter(mTourAdapter);
        getTours();
    }

    @Override
    public void onBackPressed() {
        startPreviousActivity();
    }

    private void getTours() {
        String clientUUID = PrefsHelper.getClientUUID(getApplicationContext());
        String networkUUID = PrefsHelper.getNetworkId(getApplicationContext());
        String url = Reference.BASE_URL + Reference.API_GET_TOURS + "?network_id=" + networkUUID + "&client_uuid=" + clientUUID;
        JsonArrayRequest clientLogo = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    MyOty.getInstance().setTours(response);
                    setTours();
                },
                error -> {
                    AKRequest.solveErrorResponse(getApplicationContext(), error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(getApplicationContext()));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(getApplicationContext(), clientLogo);
    }


    private void setTours() {
        if (MyOty.getInstance().getTours().size() == 1) {
            onClick(MyOty.getInstance().getTours().get(0));
        } else {
            showTourView();
            mTourAdapter.setTours(MyOty.getInstance().getTours());
        }
    }

    private void showTourView() {
        mTourRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Tour tour) {
        MyOty.getInstance().setCurrentTour(tour);
        PrefsHelper.setTourId(getApplicationContext(), tour.getId());
        startNextActivity();
    }

    private void startNextActivity() {
        startHomeActivity();
    }

    private void startPreviousActivity() {
        startNetworkActivity();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void startNetworkActivity() {
        Intent intent = new Intent(this, NetworkActivity.class);
        startActivity(intent);
    }
}