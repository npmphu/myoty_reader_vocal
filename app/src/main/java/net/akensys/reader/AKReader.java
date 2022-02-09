package net.akensys.reader;

import static android.content.Context.BATTERY_SERVICE;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import net.akensys.reader.model.DataLogger;
import net.akensys.reader.model.GroupAlert;
import net.akensys.reader.model.RawData;
import net.akensys.reader.service.AKRequest;
import net.akensys.reader.util.Conversion;
import net.akensys.reader.util.ElaTag;
import net.akensys.reader.util.TTSHelper;

import java.sql.Ref;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class AKReader {
    private static final String TAG = "AKReader";

    private static AKReader instance = null;
    private final Context context;
    private BluetoothLeScanner bleScanner = null;
    public Boolean isScanning = false;
    private List<ScanFilter> filters;
    private String startScanText;
    private Button btn_start_scan;
    private TextView tv_scan_log;

    private boolean isInvalid(@NonNull ScanResult sr) {
        if (sr.getDevice() == null) {
            return true;
        }
        if (sr.getDevice().getName() == null) {
            return true;
        }
        return sr.getDevice().getName().isEmpty();
    }

    @SuppressLint("SetTextI18n")
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult sr) {
            if (isInvalid(sr)) {
                return;
            }
            Log.d("onScanResult", sr.getDevice().getName());
            MyOty.getInstance().addToScanResults(sr);
            RawData tag = MyOty.getInstance().addToScanResults(sr);
            if (MyOty.getInstance().addToMAGAlerts(sr) != null) {
                Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(context, AKReporter.createMmrAlert(context, tag)));
                PrefsHelper.setAlertStatus(context, tag.getTagName(), false);
            }
            if (MyOty.getInstance().addToMOVAlerts(sr) != null) {
                Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(context, AKReporter.createMmrAlert(context, tag)));
                PrefsHelper.setAlertStatus(context, tag.getTagName(), false);
            }
            if (MyOty.getInstance().addToPIRAlerts(sr) != null) {
                Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(context, AKReporter.createMmrAlert(context, tag)));
                PrefsHelper.setAlertStatus(context, tag.getTagName(), false);
            }
            createNotification(sr);
            TextView tv_low_battery = null;
            if (MyOty.getInstance().getHomeActivity() != null) {
                tv_low_battery = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_low_battery);
            }
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            if (batteryLevel >= 5) { // else c'est insuffisante pour datalogger
                if (tv_low_battery != null) {
                    tv_low_battery.setVisibility(View.GONE);
                }
                DataLogger dataLogger = MyOty.getInstance().isDataLogger(sr.getDevice().getAddress());
                if (dataLogger != null) {
                    boolean connected = false;
                    if (dataLogger.getLogAction() == AKCommander.LOG_DL_ACTION) {
                        connected = AKCommander.getInstance().connectToDevice(sr, AKCommander.LOG_DL_CALLBACK, context, dataLogger);
                    }
                    else if (dataLogger.getLogAction() == AKCommander.LOG_DL_AND_RST_ACTION) {
                        connected = AKCommander.getInstance().connectToDevice(sr, AKCommander.LOG_DL_AND_RST_CALLBACK, context, dataLogger);
                    }
                    else if (dataLogger.getLogAction() == AKCommander.LOG_RST_ACTION) {
                        connected = AKCommander.getInstance().connectToDevice(sr, AKCommander.LOG_RST_CALLBACK, context, dataLogger);
                    }
                    if (connected) {
                        MyOty.getInstance().removeFromDataLoggers(sr.getDevice().getAddress());
                    }
                }
            } else {
                if (tv_low_battery != null) {
                    tv_low_battery.setText("Batterie insuffisante pour datalogger");
                    tv_low_battery.setVisibility(View.VISIBLE);
                }
            }
            if (MyOty.getInstance().getHomeActivity() != null) {
                tv_scan_log = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_scan_log);
                if (tv_scan_log != null) {
                    tv_scan_log.setText(startScanText + "\nLecture en cours ... Trouvé " + MyOty.getInstance().getScanResults().size() + "/" + PrefsHelper.getDevicesCount(context) + ".");
                }
            }
            // super.onScanResult(callbackType, sr);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            for (ScanResult sr : results) {
                if (sr.getDevice() == null) {
                    continue;
                }
                if (sr.getDevice().getName() == null) {
                    continue;
                }
                if (sr.getDevice().getName().isEmpty()) {
                    continue;
                }
                Log.d("onScanResult", sr.getDevice().getName());
                MyOty.getInstance().addToScanResults(sr);
                RawData newMAGAlert = MyOty.getInstance().addToMAGAlerts(sr);
                if (newMAGAlert != null) {
                    Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(context, AKReporter.createMmrAlert(context, newMAGAlert)));
                }
                RawData newMOVAlert = MyOty.getInstance().addToMOVAlerts(sr);
                if (newMOVAlert != null) {
                    Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(context, AKReporter.createMmrAlert(context, newMOVAlert)));
                }
                RawData newPIRAlert = MyOty.getInstance().addToPIRAlerts(sr);
                if (newPIRAlert != null) {
                    Executors.newSingleThreadExecutor().execute(() -> AKRequest.postRawData(context, AKReporter.createMmrAlert(context, newPIRAlert)));
                }
                createNotification(sr);
                TextView tv_low_battery = null;
                if (MyOty.getInstance().getHomeActivity() != null) {
                    tv_low_battery = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_low_battery);
                }
                BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
                int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                if (batteryLevel >= 5) { // else c'est insuffisante pour datalogger
                    if (tv_low_battery != null) {
                        tv_low_battery.setVisibility(View.GONE);
                    }
                    DataLogger dataLogger = MyOty.getInstance().isDataLogger(sr.getDevice().getAddress());
                    if (dataLogger != null) {
                        boolean connected = false;
                        if (dataLogger.getLogAction() == AKCommander.LOG_DL_ACTION) {
                            connected = AKCommander.getInstance().connectToDevice(sr, AKCommander.LOG_DL_CALLBACK, context, dataLogger);
                        }
                        else if (dataLogger.getLogAction() == AKCommander.LOG_DL_AND_RST_ACTION) {
                            connected = AKCommander.getInstance().connectToDevice(sr, AKCommander.LOG_DL_AND_RST_CALLBACK, context, dataLogger);
                        }
                        else if (dataLogger.getLogAction() == AKCommander.LOG_RST_ACTION) {
                            connected = AKCommander.getInstance().connectToDevice(sr, AKCommander.LOG_RST_CALLBACK, context, dataLogger);
                        }
                        if (connected) {
                            MyOty.getInstance().removeFromDataLoggers(sr.getDevice().getAddress());
                        }
                    }
                } else {
                    if (tv_low_battery != null) {
                        tv_low_battery.setText("Batterie insuffisante pour datalogger");
                        tv_low_battery.setVisibility(View.VISIBLE);
                    }
                }
                if (MyOty.getInstance().getHomeActivity() != null) {
                    tv_scan_log = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_scan_log);
                    if (tv_scan_log != null) {
                        tv_scan_log.setText(startScanText + "\nLecture en cours ... Trouvé " + MyOty.getInstance().getScanResults().size() + "/" + PrefsHelper.getDevicesCount(context) + ".");
                    }
                }
            }
            // super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };

    private void createNotification(@NonNull ScanResult sr) {
        RawData tag = MyOty.getInstance().addToScanResults(sr);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, Reference.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_myoty_round)
                        .setContentTitle("Reader")
                        .setLights(Color.WHITE, 300, 300)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (PrefsHelper.getAlertOpt(context) != Reference.ALERT_BEEPER_SOUND) {
            notificationBuilder.setSilent(true);
        }
        String gaJson = PrefsHelper.getGroupAlert(context, tag.getTagName());
        Log.d(TAG, "createNotification: " + tag.getTagName());
        Log.d(TAG, "createNotification: " + tag.getTagName() + " " + gaJson);
        Log.d(TAG, "createNotification: " + tag.getTagName() + " " + PrefsHelper.getAlertStatus(context, tag.getTagName()));
        if (gaJson != null && !PrefsHelper.getAlertStatus(context, tag.getTagName())) {
            GroupAlert groupAlert = new Gson().fromJson(gaJson, GroupAlert.class);
            PrefsHelper.setAlertStatus(context, tag.getTagName(), true);
            int AlertId = PrefsHelper.getAlertId(context);
            String AlertUid = tag.getTagName() + AlertId;
            int categId = ElaTag.getTagCategory(tag.getRawData());
            if (ElaTag.CATEGORY_ID_MAG == categId) {
                Integer[] CaS = ElaTag.getCountAndState(tag.getRawData());
                if (CaS != null && CaS[1] != groupAlert.getMax().intValue()) {
                    notificationBuilder.setContentText("Attention ! Alerte ouverture ... " + groupAlert.getLibelle());
                    notificationManager.notify("MagAlert", AlertId, notificationBuilder.build());
                    new TTSHelper(context, Locale.FRANCE, "Attention ! Alerte ouverture... " + groupAlert.getLibelle(), AlertUid);
                }
            }
            if (ElaTag.CATEGORY_ID_MOV == categId) {
                new Handler().postDelayed(() -> PrefsHelper.setAlertStatus(context, tag.getTagName(), false), 10 * 1000);
                Integer[] CaS = ElaTag.getCountAndState(tag.getRawData());
                if (CaS != null && CaS[1] != groupAlert.getMax().intValue()) {
                    notificationBuilder.setContentText("Attention ! Alerte mouvement ... " + groupAlert.getLibelle());
                    notificationManager.notify("MovAlert", AlertId, notificationBuilder.build());
                    new TTSHelper(context, Locale.FRANCE, "Attention ! Alerte mouvement ... " + groupAlert.getLibelle(), AlertUid);
                }
            }
            if (ElaTag.CATEGORY_ID_PIR == categId) {
                Integer[] CaS = ElaTag.getCountAndState(tag.getRawData());
                if (CaS != null && CaS[1] != groupAlert.getMax().intValue()) {
                    notificationBuilder.setContentText("Attention ! Alerte présence ... " + groupAlert.getLibelle());
                    notificationManager.notify("MovAlert", AlertId, notificationBuilder.build());
                    new TTSHelper(context, Locale.FRANCE, "Attention ! Alerte présence ... " + groupAlert.getLibelle(), AlertUid);
                }
            }
            if (ElaTag.CATEGORY_ID_T == categId) {
                Double t = ElaTag.getTemperature(tag.getRawData());
                if (t != null && ((groupAlert.getMin() > t) || (t > groupAlert.getMax()))) {
                    notificationBuilder.setContentText("Attention ! Alerte température... " + groupAlert.getLibelle());
                    notificationManager.notify("TemperatureAlert", AlertId, notificationBuilder.build());
                    new TTSHelper(context, Locale.FRANCE, "Attention ! Alerte température... " + groupAlert.getLibelle(), AlertUid);
                }
            }
            if (ElaTag.CATEGORY_ID_RHT == categId) {
                Double t = ElaTag.getTemperature(tag.getRawData());
                if (t != null && ((groupAlert.getMin() > t) || (t > groupAlert.getMax()))) {
                    notificationBuilder.setContentText("Attention ! Alerte température..." + groupAlert.getLibelle());
                    notificationManager.notify("RhtAlert", AlertId, notificationBuilder.build());
                    new TTSHelper(context, Locale.FRANCE, "Attention ! Alerte température... " + groupAlert.getLibelle(), AlertUid);
                }
            }
        }
    }

    public AKReader(Context ctx) {
        context = ctx;
    }

    public static AKReader getInstance(Context ctx) {
        if (instance == null)
            instance = new AKReader(ctx);
        return instance;
    }

    public void initScanner() {
        if (bleScanner == null) {
            if (BluetoothAdapter.getDefaultAdapter() != null) {
                bleScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            }
            filters = buildScanFilters();
        }
    }

    public List<ScanFilter> buildScanFilters() {
        Set<String> devicesName = PrefsHelper.getDeviceNames(context);
        Set<String> devicesMAC = PrefsHelper.getDeviceMACs(context);
        List<ScanFilter> list = new ArrayList<>();
        if (Reference.SCHUCO_NETWORK_ID.equals(PrefsHelper.getNetworkId(context))) {
            for (String name : devicesName) {
                list.add(new ScanFilter.Builder().setDeviceName(name).build());
                list.add(new ScanFilter.Builder().setDeviceName(Conversion.buildCustomName(name)).build());
                list.add(new ScanFilter.Builder().setDeviceName(Conversion.buildWpName(name)).build());
            }
        } else {
            for (String name : devicesName) {
                list.add(new ScanFilter.Builder().setDeviceName(name).build());
                list.add(new ScanFilter.Builder().setDeviceName(Conversion.buildCustomName(name)).build());
            }
        }
        for (String mac : devicesMAC) {
            try {
                list.add(new ScanFilter.Builder().setDeviceAddress(mac.replaceAll("..", ":$0").replaceFirst(":", "")).build());
            } catch (Exception ignored) { }
        }
        return list;
    }

    @SuppressLint("SetTextI18n")
    public void startScan() {
        initScanner();
        if (!isScanning) {
            MyOty.getInstance().resetScanResults();
            MyOty.getInstance().resetRawDataList();
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    //.setLegacy(false)
                    //.setUseHardwareBatchingIfSupported(false)
                    //.setReportDelay(1000)
                    .build();
            //List<ScanFilter> filters = MyOty.getInstance().getScanFilters();
            bleScanner.startScan(filters, settings, mScanCallback);
            isScanning = true;
            if (MyOty.getInstance().getHomeActivity() != null) {
                startScanText = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm").format(LocalDateTime.now()) + ": Début lecture (" + PrefsHelper.getScanDuration(context) + "s).";
                tv_scan_log = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_scan_log);
                if (tv_scan_log != null) {
                    tv_scan_log.setText(startScanText + "\nLecture en cours ... Trouvé 0/" + PrefsHelper.getDevicesCount(context) + ".");
                }
                btn_start_scan = MyOty.getInstance().getHomeActivity().findViewById(R.id.btn_start_scan);
                if (btn_start_scan != null) {
                    btn_start_scan.setEnabled(false);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void stopScan() {
        initScanner();
        if (isScanning) {
            bleScanner.stopScan(mScanCallback);
            isScanning = false;
            if (MyOty.getInstance().getHomeActivity() != null) {
                String stopScanText = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm").format(LocalDateTime.now()) + ": Lecture terminée. Trouvé " + MyOty.getInstance().getScanResults().size() + "/" + PrefsHelper.getDevicesCount(context) + ".";
                if (startScanText != null && !startScanText.isEmpty()) {
                    stopScanText = startScanText + '\n' + stopScanText;
                }
                tv_scan_log = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_scan_log);
                if (tv_scan_log != null) {
                    tv_scan_log.setText(stopScanText);
                }
                btn_start_scan = MyOty.getInstance().getHomeActivity().findViewById(R.id.btn_start_scan);
                if (btn_start_scan != null) {
                    btn_start_scan.setEnabled(true);
                }
            }
        }
    }

}
