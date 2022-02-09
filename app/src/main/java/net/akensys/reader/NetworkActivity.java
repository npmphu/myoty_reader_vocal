package net.akensys.reader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import net.akensys.reader.model.Network;
import net.akensys.reader.model.User;
import net.akensys.reader.service.AKRequest;

import java.util.HashMap;
import java.util.Map;

public class NetworkActivity extends AppCompatActivity implements NetworkAdapter.NetworkAdapterOnClickHandler {
    private static final String TAG = "NetworkActivity";

    private RecyclerView mNetworkRecyclerView;
    private NetworkAdapter mNetworkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkAdapter = new NetworkAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mNetworkRecyclerView = findViewById(R.id.rv_network);
        mNetworkRecyclerView.setLayoutManager(layoutManager);
        mNetworkRecyclerView.setHasFixedSize(true);
        mNetworkRecyclerView.setAdapter(mNetworkAdapter);
        getNetworks();
    }

    @Override
    public void onBackPressed() {
        logout();
    }

    private void logout() {
        MyOty.getInstance().logout();
        startLoginActivity();
    }

    private void getNetworks() {
        String url = Reference.BASE_URL + Reference.API_GET_NETWORKS + "?user_uuid=" + PrefsHelper.getUserUUID(getApplicationContext());
        JsonArrayRequest userNetworks = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    MyOty.getInstance().setNetworks(response);
                    setNetworks();
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
        MyOty.getInstance().addToRequestQueue(getApplicationContext(), userNetworks);
    }

    private void setNetworks() {
        if (MyOty.getInstance().getNetworks().size() == 1) {
            onClick(MyOty.getInstance().getNetworks().get(0));
        } else {
            showNetworkView();
            mNetworkAdapter.setNetworks(MyOty.getInstance().getNetworks());
        }
    }

    private void showNetworkView() {
        mNetworkRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Network network) {
        MyOty.getInstance().setCurrentNetwork(network);
        PrefsHelper.setNetworkId(getApplicationContext(), network.getId());
        PrefsHelper.setNetworkLabel(getApplicationContext(), network.getLibelle());
        PrefsHelper.setNetworkLogo(getApplicationContext(), network.getLogo());
        PrefsHelper.setMagAlertMng(getApplicationContext(), network.getMAGAlert());
        startNextActivity(network);
    }

    private void startNextActivity(Network network) {
        if (network.getManageTour() == 1) {
            startTourActivity();
        } else {
            startHomeActivity();
        }
    }

    private void startLoginActivity() {
        Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginActivity);
    }

    private void startTourActivity() {
        Intent intent = new Intent(this, TourActivity.class);
        startActivity(intent);
    }

    private void startHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }
}