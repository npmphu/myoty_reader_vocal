package net.akensys.reader.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import net.akensys.reader.AKReader;
import net.akensys.reader.AKReporter;
import net.akensys.reader.HomeActivity;
import net.akensys.reader.MyOty;
import net.akensys.reader.PrefsHelper;
import net.akensys.reader.R;
import net.akensys.reader.Reference;
import net.akensys.reader.util.TTSHelper;

import java.util.Locale;
import java.util.concurrent.Executors;

public class AKWorker extends Service implements LocationListener {
    protected LocationManager locationManager;

    public static Handler workHandler;
    public static int workInterval = 5 * 60 * 1000;
    public static int workDuration = 30 * 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);
        }
        workInterval = PrefsHelper.getScanInterval(getApplicationContext()) * 1000;
        workDuration = PrefsHelper.getScanDuration(getApplicationContext()) * 1000;
        workHandler = new Handler(Looper.getMainLooper());
        workHandler.post(realTimeAdvertisements);
        startMyForeground();
//        IntentFilter filter = new IntentFilter(Intent.EXTRA_KEY_EVENT);
//        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
//        registerReceiver(receiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
//        unregisterReceiver(receiver);
        stopForeground(true);
        super.onDestroy();
    }

    public void startMyForeground() {
        Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        startForeground(
                Reference.RAW_DATA_NOTIFICATION_ID,
                new NotificationCompat
                        .Builder(this, Reference.NOTIFICATION_CHANNEL_ID)
                        .setOngoing(true)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("MyOty est en cours d'exécution en arrière-plan")
                        .setSmallIcon(R.mipmap.ic_launcher_myoty_round)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setSilent(true)
                        .build());
    }

    public final Runnable realTimeAdvertisements = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            AKReader.getInstance(getApplicationContext()).stopScan();
            workHandler.postDelayed(() -> {
                AKReader.getInstance(getApplicationContext()).startScan();
                workHandler.postDelayed(() -> {
                    if (MyOty.getInstance().getRawDataList().size() > 0) {
                        Executors.newSingleThreadExecutor().execute(() ->
                                AKRequest.postRawData(getApplicationContext(), AKReporter.createRawDataReport(getApplicationContext()))
                        );
                    }
                }, workDuration);
            }, 1000);
            workHandler.postDelayed(this, workInterval - 1000);
        }
    };

    @Override
    public void onLocationChanged(@NonNull Location location) {
        MyOty.getInstance().setCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d("BroadCast", "Volume button pressed: " + new Gson().toJson(intent));
//            if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
//                Log.d("BroadCast", "Volume changed");
//                new TTSHelper(getApplicationContext(), Locale.FRANCE, "Bouton ... Bonjour", "Button");
//            }
//        }
//    };
}
