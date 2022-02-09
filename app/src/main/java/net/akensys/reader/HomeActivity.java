package net.akensys.reader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.akensys.reader.service.AKRequest;
import net.akensys.reader.service.AKWorker;
import net.akensys.reader.util.TTSHelper;
import net.akensys.reader.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Key;
import java.sql.Ref;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private Intent mAKWorker;
    private Handler mStopScanHandler;
    private Boolean autoScanMode = false;
    private Boolean getScanConfigFinished = false;
    private Boolean getDevicesFinished = false;
    private Button btn_auto_scan_mode;
    private Button btn_start_scan;
    private ActivityResultLauncher<Intent> enableBluetoothForManualScanLauncher;
    private ActivityResultLauncher<Intent> enableBluetoothForAutoScanModeLauncher;
    private ActivityResultLauncher<Intent> ignoreBatteryOptimizationLauncher;

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            event.startTracking();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            Log.d(TAG, "onKeyLongPress: ");
//            new TTSHelper(getApplicationContext(), Locale.FRANCE, "Bouton ... Salut", "Button");
//        }
//        return super.onKeyLongPress(keyCode, event);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        MyOty.getInstance().setHomeActivity(HomeActivity.this);
        enableBluetoothForManualScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        startScan();
                    }
                });
        enableBluetoothForAutoScanModeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        startAutoScanMode();
                    }
                });
        ignoreBatteryOptimizationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { });
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.home_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            initContentView();
            swipeRefreshLayout.setRefreshing(false);
        });
        initContentView();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Hello");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Hello");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Hello");
        super.onDestroy();
        stopAutoScanMode();
    }

    @Override
    public void onBackPressed() {
        if (AKReader.getInstance(getApplicationContext()).isScanning) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(HomeActivity.this)
                    .setTitle("Lecture en cours")
                    .setMessage("La déconnexion forcée peut imterrompre la lecture et les journaux de données des capteurs.")
                    .create();
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DÉCONNEXION",
                    (dialog, which) -> {
                        logout();
                        dialog.dismiss();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ANNULER",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        } else {
            logout();
        }
    }

    private void logout() {
        stopAutoScanMode();
        AKCommander.getInstance().disconnectDevice();
        MyOty.getInstance().logout();
        // PrefsHelper.logout(getApplicationContext());
        startLoginActivity();
    }

    private void getLogo() {
        String userLogo = PrefsHelper.getUserLogo(getApplicationContext());
        if (userLogo != null && !userLogo.isEmpty()) {
            Executors.newSingleThreadExecutor().execute(()->AKRequest.getUserLogo(getApplicationContext(), userLogo, setLogo));
            return;
        }
        String networkLogo = PrefsHelper.getNetworkLogo(getApplicationContext());
        if (networkLogo != null && !networkLogo.isEmpty()) {
            Executors.newSingleThreadExecutor().execute(()->AKRequest.getNetworkLogo(getApplicationContext(), networkLogo, setLogo));
            return;
        }
        String clientLogo = PrefsHelper.getClientUUID(getApplicationContext());
        if (clientLogo != null && !clientLogo.isEmpty()) {
            Executors.newSingleThreadExecutor().execute(()->AKRequest.getClientLogo(getApplicationContext(), clientLogo, setLogo));
        }
    }

    public final Runnable setLogo = () -> {
        String logo = MyOty.getInstance().getLogo();
        if (logo == null) {
            return;
        }
        if (logo.isEmpty()) {
            return;
        }
        String[] data = logo.split("base64,", 2);
        if (data.length > 1) {
            logo = data[1];
        }
        byte[] imageAsBytes = Base64.decode(logo.getBytes(), Base64.DEFAULT);
        ImageView iv_logo = findViewById(R.id.iv_logo);
        iv_logo.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
    };

    private void getScanConfig() {
        Executors.newSingleThreadExecutor().execute(() -> AKRequest.getScanConfig(getApplicationContext(), getScanConfigSuccess));
    }

    public final Runnable getScanConfigSuccess = () -> {
        getDevicesFinished = true;
        enableScanButtons();
    };

    private void getDevicesList() {
        // if (MyOty.getInstance().getCurrentNetwork().getGetdevicelist() == 1 || MyOty.getInstance().getCurrentNetwork().getDatalogger() == 1) {
        //     Executors.newSingleThreadExecutor().execute(() -> AKRequest.getDeviceList(getApplicationContext(), getDevicesListSuccess));
        // }
        Executors.newSingleThreadExecutor().execute(() -> AKRequest.getDeviceList(getApplicationContext(), getDevicesListSuccess));
    }

    public final Runnable getDevicesListSuccess = () -> {
        getScanConfigFinished = true;
        enableScanButtons();
    };

    private void enableScanButtons() {
        if (getScanConfigFinished && getDevicesFinished) {
            btn_start_scan.setEnabled(true);
            btn_auto_scan_mode.setEnabled(true);
            btn_auto_scan_mode.performClick();
        }
    }

    @SuppressLint("ResourceAsColor")
    private void initContentView() {
        TextView tv_network_name = findViewById(R.id.tv_network_name);
        tv_network_name.setText(PrefsHelper.getNetworkLabel(getApplicationContext()));
        getLogo();
        getScanConfigFinished = false;
        getScanConfig();
        getDevicesFinished = false;
        getDevicesList();
        mStopScanHandler = new Handler();
        btn_start_scan = findViewById(R.id.btn_start_scan);
        btn_start_scan.setEnabled(false);
        btn_start_scan.setOnClickListener(v -> {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                startScan();
            } else {
                requestEnableBluetoothForManualScan();
            }
        });
        btn_auto_scan_mode = findViewById(R.id.btn_auto_scan_mode);
        btn_auto_scan_mode.setEnabled(false);
        btn_auto_scan_mode.setOnClickListener(v -> {
            if (!autoScanMode) {
                if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    startAutoScanMode();
                } else {
                    requestEnableBluetoothForAutoScanMode();
                }
            } else {
                stopAutoScanMode();
            }
            btn_auto_scan_mode.invalidate();
        });
        Button btn_send_reports = findViewById(R.id.btn_send_reports);
        btn_send_reports.setOnClickListener(v -> {
            Set<String> reportFiles = PrefsHelper.getReportFiles(getApplicationContext());
            Set<String> errorFiles = new HashSet<>();
            for (String fileName : reportFiles) {
                String fileData = Utils.readFromFile(getApplicationContext(), fileName);
                if (fileData == null || fileData.isEmpty()) {
                    errorFiles.add(fileName);
                } else {
                    try {
                        JSONObject report = new JSONObject(fileData);
                        if (fileName.contains(Reference.RAW_DATA_REPORT_PREFIX)) {
                            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(getApplicationContext(), report));
                        }
                        else if (fileName.contains(Reference.DATA_LOGS_REPORT_PREFIX)) {
                            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(getApplicationContext(), report));
                        }
                    } catch (JSONException ignored) {
                        errorFiles.add(fileName);
                    }
                }
            }
            for (String fileName : errorFiles) {
                PrefsHelper.removeFromReportFiles(getApplicationContext(), fileName);
                if (PrefsHelper.getReportFiles(getApplicationContext()).size() == 0 && btn_send_reports.getVisibility() != View.GONE) {
                    btn_send_reports.setVisibility(View.GONE);
                }
            }
        });
        if (PrefsHelper.getReportFiles(getApplicationContext()).size() > 0 && btn_send_reports.getVisibility() != View.VISIBLE) {
            btn_send_reports.setVisibility(View.VISIBLE);
        } else if (PrefsHelper.getReportFiles(getApplicationContext()).size() == 0 && btn_send_reports.getVisibility() != View.GONE) {
            btn_send_reports.setVisibility(View.GONE);
        }
        Button btn_stop_and_log_out = findViewById(R.id.btn_stop_and_log_out);
        btn_stop_and_log_out.setOnClickListener(v -> {
            if (AKReader.getInstance(getApplicationContext()).isScanning) {
                AlertDialog alertDialog = new AlertDialog
                        .Builder(HomeActivity.this)
                        .setTitle("Lecture en cours")
                        .setMessage("La déconnexion forcée peut imterrompre la lecture et les journaux de données des capteurs.")
                        .create();
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DÉCONNEXION",
                        (dialog, which) -> {
                            logout();
                            dialog.dismiss();
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ANNULER",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            } else {
                logout();
            }
        });
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        RadioButton alertOpt = findViewById(R.id.radio_btn_tts);
        alertOpt.performClick();
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedOpt = findViewById(checkedId);
            if (selectedOpt.getId() == R.id.radio_btn_beeper) {
                PrefsHelper.setAlertOpt(getApplicationContext(), Reference.ALERT_BEEPER_SOUND);
            }
            if (selectedOpt.getId() == R.id.radio_btn_tts) {
                PrefsHelper.setAlertOpt(getApplicationContext(), Reference.ALERT_TTS);
            }
            if (selectedOpt.getId() == R.id.radio_btn_silence) {
                PrefsHelper.setAlertOpt(getApplicationContext(), Reference.ALERT_SILENCE);
            }
            Log.d(TAG, "onCheckedChanged: " + checkedId);
        });
    }

    private void startScan() {
        if (checkAccessFineLocation()) {
            AKReader.getInstance(getApplicationContext()).startScan();
            mStopScanHandler.postDelayed(() -> {
                AKReader.getInstance(getApplicationContext()).stopScan();
                if (MyOty.getInstance().getRawDataList().size() > 0) {
                    Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(getApplicationContext(), AKReporter.createRawDataReport(getApplicationContext())));
                }
                if (mStopScanHandler != null) {
                    mStopScanHandler.removeCallbacksAndMessages(null);
                }
            }, PrefsHelper.getScanDuration(getApplicationContext()) * 1000);
        }

    }

    private void startAutoScanMode() {
        if (checkAccessFineLocation()) {
            stopAutoScanMode();
            autoScanMode = true;
            btn_start_scan.setVisibility(View.GONE);
            btn_auto_scan_mode.setText(R.string.desactiver_mode_lecture_automatique);
            btn_auto_scan_mode.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.danger));
            mAKWorker = new Intent(getApplicationContext(), AKWorker.class);
            startService(mAKWorker);
        }
    }

    private void stopAutoScanMode() {
        AKReader.getInstance(getApplicationContext()).stopScan();
        autoScanMode = false;
        btn_auto_scan_mode.setText(R.string.activer_mode_lecture_automatique);
        btn_auto_scan_mode.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
        btn_start_scan.setVisibility(View.VISIBLE);
        if (mAKWorker != null) {
            stopService(mAKWorker);
        }
        if (AKWorker.workHandler != null) {
            AKWorker.workHandler.removeCallbacksAndMessages(null);
        }
    }


    private void requestEnableBluetoothForManualScan() {
        //BluetoothAdapter.getDefaultAdapter().enable();
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothForManualScanLauncher.launch(enableBluetoothIntent);
    }

    private void requestEnableBluetoothForAutoScanMode() {
        //BluetoothAdapter.getDefaultAdapter().enable();
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothForAutoScanModeLauncher.launch(enableBluetoothIntent);
    }

    private void requestIgnoreBatteryOptimization() {
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (!mPowerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(HomeActivity.this)
                    .setTitle("Ignorer l'optimisation de la batterie")
                    .setMessage("Cliquer sur RÉGLAGE puis IGNORER l'optimisation de la batterie pour l'exécution en arrière-plane.")
                    .create();
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "RÉGLAGE",
                    (dialog, which) -> {
                        Intent ignoreBatteryOptimizationIntent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        ignoreBatteryOptimizationLauncher.launch(ignoreBatteryOptimizationIntent);
                        dialog.dismiss();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ANNULER",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        }
    }

    private Boolean checkAccessCoarseLocation() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cette application nécessite la localisation");
            builder.setMessage("Merci d'autoriser la localisation GPS.");
            builder.setPositiveButton(R.string.autoriser, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Reference.PERMISSION_REQUEST_COARSE_LOCATION));
            builder.show();
            return false;
        }
        return true;
    }

    private Boolean checkAccessFineLocation() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cette application nécessite la localisation");
            builder.setMessage("Merci d'autoriser la localisation GPS.");
            builder.setPositiveButton(R.string.autoriser, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Reference.PERMISSION_REQUEST_FINE_LOCATION));
            builder.show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Reference.PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Log.d("PERMISSION_REQUEST_COARSE_LOCATION", "coarse location permission granted");
                    startAutoScanMode();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app requires background location");
                        builder.setMessage("Click on SETTING to go to the settings then choose ALWAYS ALLOW.");
                        builder.setPositiveButton(R.string.reglage, null);
                        builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, Reference.PERMISSION_REQUEST_BACKGROUND_LOCATION));
                        builder.show();
                    } else {
                        requestIgnoreBatteryOptimization();
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
                break;
            }
            case Reference.PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Log.d("PERMISSION_REQUEST_FINE_LOCATION", "fine location permission granted");
                    startAutoScanMode();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Cette application nécessite la localisation en arrière-plan");
                        builder.setMessage("Cliquez sur RÉGLAGE pour aller dans les paramètres puis choisir TOUJOURS AUTORISER.");
                        builder.setPositiveButton(R.string.reglage, null);
                        builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, Reference.PERMISSION_REQUEST_BACKGROUND_LOCATION));
                        builder.show();
                    } else {
                        requestIgnoreBatteryOptimization();
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Fonctionnalité limitée");
                    builder.setMessage("Étant donné que l'accès à la localisation n'a pas été accordé, cette application ne pourra pas découvrir les tags en arrière-plan.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
                break;
            }
            case Reference.PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                requestIgnoreBatteryOptimization();
            }
        }
    }

    private void startLoginActivity() {
        Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginActivity);
    }

}