package net.akensys.reader;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import net.akensys.reader.model.User;
import net.akensys.reader.service.AKRequest;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acquireWakeLock();
        createNotificationChanel();
        if (isAuthenticated()) {
            onAuthenticationSuccess();
        } else {
            startLoginActivity();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy: ");
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        super.onDestroy();
    }

    @SuppressLint("WakelockTimeout")
    private void acquireWakeLock() {
        PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyOty:WakeLock");
        mWakeLock.acquire();
    }

    private void createNotificationChanel() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel myOty = new NotificationChannel(
                Reference.NOTIFICATION_CHANNEL_ID,
                Reference.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        myOty.enableVibration(true);
        myOty.setVibrationPattern(new long[] {1000, 1000, 1000, 1000, 1000, 1000});
        myOty.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beeper_sound), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        notificationManager.createNotificationChannel(myOty);
    }

    private Boolean isAuthenticated() {
        return PrefsHelper.getToken(getApplicationContext()) != null;
    }

    public void onAuthenticationSuccess() {
        fetchUserSession();
    }

    private void fetchUserSession() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        String url = Reference.BASE_URL + Reference.API_GET_SESSION;
        JsonObjectRequest userSession = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    User user = new Gson().fromJson(String.valueOf(response), User.class);
                    MyOty.getInstance().setCurrentUser(user);
                    PrefsHelper.setUserUUID(getApplicationContext(), user.getUuid());
                    PrefsHelper.setUserLogo(getApplicationContext(), user.getLogo());
                    PrefsHelper.setReaderName(getApplicationContext(), user.getNom() + " " + user.getPrenom());
                    PrefsHelper.setReaderType(getApplicationContext(), user.getReaderType());
                    PrefsHelper.setClientUUID(getApplicationContext(), user.getClient().getUuid());
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    onFetchUserSessionSuccess();
                },
                error -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    AKRequest.solveErrorResponse(getApplicationContext(), error);
                    startLoginActivity();
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
        MyOty.getInstance().addToRequestQueue(getApplicationContext(), userSession);
    }


    public void onFetchUserSessionSuccess() {
        startNetworkActivity();
    }

    private void startNetworkActivity() {
        Intent intent = new Intent(this, NetworkActivity.class);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}