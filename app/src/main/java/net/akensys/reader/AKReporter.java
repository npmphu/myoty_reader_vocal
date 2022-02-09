package net.akensys.reader;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import net.akensys.reader.model.RawData;
import net.akensys.reader.util.ElaTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AKReporter {

    public static JSONObject createReportBody(Context context) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String dateScan = dtf.format(now);
        double latitude = 0;
        double longitude = 0;
        double altitude = 0;
        double speed = 0;
        Location lastLocation = MyOty.getInstance().getCurrentLocation();
        if (lastLocation == null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (lastLocation != null) {
            MyOty.getInstance().setCurrentLocation(lastLocation);
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
            altitude = lastLocation.getAltitude();
            speed = lastLocation.getSpeed();
        }
        JSONObject mReport = new JSONObject();
        try {
            mReport.put("date_scan", dateScan);
            mReport.put("reader", PrefsHelper.getReaderName(context.getApplicationContext()));
            mReport.put("reader_type", PrefsHelper.getReaderType(context.getApplicationContext()));
            mReport.put("client_uuid", PrefsHelper.getClientUUID(context.getApplicationContext()));
            mReport.put("network_id", PrefsHelper.getNetworkId(context.getApplicationContext()));
            mReport.put("latitude", latitude);
            mReport.put("longitude", longitude);
            mReport.put("altitude", altitude);
            mReport.put("speed", speed);

        } catch (JSONException ignored) { }
        return mReport;
    }

    public static JSONObject createRawDataReport(Context context) {
        JSONObject mRawDataReport = createReportBody(context);
        try {
            mRawDataReport.put("data", new JSONArray(new Gson().toJson(MyOty.getInstance().getRawDataList())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mRawDataReport;
    }

    public static JSONObject createMmrAlert(Context context, RawData magAlert) {
        List<RawData> data = new ArrayList<>();
        data.add(magAlert);
        JSONObject mRawDataReport = createReportBody(context);
        try {
            mRawDataReport.put("data", new JSONArray(new Gson().toJson(data)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mRawDataReport;
    }

    public static JSONObject createDataLogsReport(Context context, BluetoothDevice device) {
        JSONObject dataLog = MyOty.getInstance().getFromDataLogs(device.getAddress());
        long interval =  ElaTag.getDataLoggerInterval(dataLog) / 60000;
        JSONObject mDataLogsReport = createReportBody(context);
        try {
            mDataLogsReport.put("log_interval", interval);
            mDataLogsReport.put("data", new JSONArray().put(dataLog));
        } catch (JSONException ignored) { }
        // Once report is made, no more need to save in singleton (a report file is made after requesting to server)
        MyOty.getInstance().removeFromDataLogs(device.getAddress());
        return mDataLogsReport;
    }

    public static JSONObject createLogRstDateTimeReport(Context context, String tagId, String logRstDateTime) {
        JSONObject report = new JSONObject();
        try {
            report.put("network_id", PrefsHelper.getNetworkId(context.getApplicationContext()));
            report.put("tag_id", tagId);
            report.put("log_rst_datetime", logRstDateTime);
            report.put("log_interval", null);
        } catch (JSONException ignored) { }
        return report;
    }
}
