package net.akensys.reader;

import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import net.akensys.reader.model.Client;
import net.akensys.reader.model.DataLogger;
import net.akensys.reader.model.Device;
import net.akensys.reader.model.Network;
import net.akensys.reader.model.RawData;
import net.akensys.reader.model.ScanConfig;
import net.akensys.reader.model.Tour;
import net.akensys.reader.model.User;
import net.akensys.reader.util.Conversion;
import net.akensys.reader.util.ElaTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MyOty {
    private static MyOty instance;
    private static Gson gson;
    private static RequestQueue requestQueue;
    private static String token;
    private static User currentUser;
    private static List<Network> networks;
    private static Network currentNetwork;
    private static List<Tour> tours;
    private static Tour currentTour;
    private static String logo;
    private static String userLogo;
    private static String clientLogo;
    private static String networkLogo;
    private static ScanConfig scanConfig;
    private static List<Device> devices;
    private static List<ScanFilter> scanFilters;
    private static List<ScanResult> scanResults;
    private static List<RawData> rawDataList;
    private static List<RawData> magAlerts;
    private static List<RawData> movAlerts;
    private static List<RawData> pirAlerts;
    private static List<JSONObject> dataLogs;
    private static List<String> reportFiles;
    private static List<DataLogger> dataLoggers;

    private static Location currentLocation;

    private Activity homeActivity;

    private MyOty() {
    }

    public static MyOty getInstance() {
        if (instance == null) {
            instance = new MyOty();
        }
        return instance;
    }

    public Gson myGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Context context, Request<T> request) {
        getRequestQueue(context).add(request);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String value) {
        token = value;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = myGson().fromJson(myGson().toJson(user), User.class);
    }

    public Client getCurrentClient() {
        return currentUser.getClient();
    }

    public List<Network> getNetworks() {
        if (networks == null) {
            networks = new ArrayList<>();
        }
        return networks;
    }

    public void setNetworks(JSONArray list) {
        networks = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject element = list.getJSONObject(i);
                Network network = myGson().fromJson(element.getString("network"), Network.class);
                networks.add(network);
            } catch (JSONException ignored) {
            }
        }
    }

    public Network getCurrentNetwork() {
        return currentNetwork;
    }

    public void setCurrentNetwork(Network network) {
        currentNetwork = myGson().fromJson(myGson().toJson(network), Network.class);
    }

    public List<Tour> getTours() {
        if (tours == null) {
            tours = new ArrayList<>();
        }
        return tours;
    }

    public void setTours(JSONArray list) {
        tours = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject element = list.getJSONObject(i);
                Tour tour = myGson().fromJson(String.valueOf(element), Tour.class);
                tours.add(tour);
            } catch (JSONException ignored) {
            }
        }
    }

    public Tour getCurrentTour() {
        return currentTour;
    }

    public void setCurrentTour(Tour tour) {
        currentTour = myGson().fromJson(myGson().toJson(tour), Tour.class);
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String value) {
        logo = value;
    }

    public String getUserLogo() {
        return userLogo;
    }

    public void setUserLogo(String value) {
        userLogo = value;
    }

    public String getClientLogo() {
        return clientLogo;
    }

    public void setClientLogo(String value) {
        clientLogo = value;
    }

    public String getNetworkLogo() {
        return networkLogo;
    }

    public void setNetworkLogo(String value) {
        networkLogo = value;
    }

    public ScanConfig getScanConfig() {
        return scanConfig;
    }

    public void setScanConfig(ScanConfig config) {
        scanConfig = myGson().fromJson(myGson().toJson(config), ScanConfig.class);
    }

    public List<Device> getDevices() {
        if (devices == null) {
            devices = new ArrayList<>();
        }
        return devices;
    }

    public void setDevices(JSONArray list) {
        devices = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject element = list.getJSONObject(i);
                Device device = myGson().fromJson(String.valueOf(element), Device.class);
                devices.add(device);
            } catch (JSONException ignored) {
            }
        }
    }

    public void setDevices(Context context, JSONArray list) {
        devices = new ArrayList<>();
        Set<String> deviceNames = new HashSet<>();
        Set<String> deviceMACs = new HashSet<>();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject element = list.getJSONObject(i);
                Device device = myGson().fromJson(String.valueOf(element), Device.class);
                devices.add(device);
                deviceMACs.add(device.getTagId());
                deviceNames.add(device.getTagName());
                if (device.getGroupAlert() != null) {
                    PrefsHelper.setGroupAlert(context, device.getTagName(), new Gson().toJson(device.getGroupAlert()));
                    PrefsHelper.setAlertStatus(context, device.getTagName(), false);
                }
            } catch (JSONException ignored) {
            }
        }
        PrefsHelper.setDevicesCount(context, list.length());
        PrefsHelper.setDeviceNames(context, deviceNames);
        PrefsHelper.setDeviceMACs(context, deviceMACs);
    }

    public List<ScanFilter> getScanFilters() {
        if (scanFilters == null) {
            setScanFilters();
        }
        return scanFilters;
    }

    public void setScanFilters() {
        scanFilters = new ArrayList<>();
        devices = getDevices();
        for (Device device : devices) {
            scanFilters.add(new ScanFilter.Builder().setDeviceName(device.getTagName()).build());
            scanFilters.add(new ScanFilter.Builder().setDeviceName(Conversion.buildCustomName(device.getTagName())).build());
            if (Reference.SCHUCO_NETWORK_ID.equals(getCurrentNetwork().getId())) {
                scanFilters.add(new ScanFilter.Builder().setDeviceName(Conversion.buildWpName(device.getTagName())).build());
            }
            try {
                scanFilters.add(new ScanFilter.Builder().setDeviceAddress(device.getTagId().replaceAll("..", ":$0").replaceFirst(":", "")).build());
            } catch (Exception ignored) {
            }
        }
    }

    public RawData addToScanResults(ScanResult scanResult) {
        if (scanResult.getDevice() == null) {
            return null;
        }
        if (scanResult.getDevice().getName() == null) {
            return null;
        }
        if (scanResult.getDevice().getName().isEmpty()) {
            return null;
        }
        boolean found = false;
        scanResults = getScanResults();
        for (int i = 0; i < scanResults.size(); i++) {
            if (scanResult.getDevice().getName().equals(scanResults.get(i).getDevice().getName())) {
                found = true;
                scanResults.set(i, scanResult);
                break;
            }
        }
        if (!found) {
            scanResults.add(scanResult);
        }
        RawData rawData = new RawData(scanResult);
        addToRawDataList(rawData);
        return rawData;
    }

    public List<ScanResult> getScanResults() {
        if (scanResults == null) {
            scanResults = new ArrayList<>();
        }
        return scanResults;
    }

    public void resetScanResults() {
        scanResults = new ArrayList<>();
    }

    public void addToRawDataList(RawData rawData) {
        boolean found = false;
        rawDataList = getRawDataList();
        for (int i = 0; i < rawDataList.size(); i++) {
            if (rawData.getTagId().equals(rawDataList.get(i).getTagId())) {
                rawDataList.set(i, rawData);
                found = true;
                break;
            }
        }
        if (!found) {
            rawDataList.add(rawData);
        }
    }

    public List<RawData> getRawDataList() {
        if (rawDataList == null) {
            rawDataList = new ArrayList<>();
        }
        return rawDataList;
    }

    public void resetRawDataList() {
        rawDataList = new ArrayList<>();
    }

    public RawData addToMAGAlerts(ScanResult scanResult) {
        if (ElaTag.CATEGORY_ID_MAG != ElaTag.getTagCategory(scanResult.getScanRecord().getBytes())) {
            return null;
        }
        magAlerts = getMAGAlerts();
        boolean found = false;
        boolean newAlert = true;
        RawData alert =new RawData(scanResult);
        for (int i = 0; i < magAlerts.size(); i++) {
            if (alert.getTagId().equals(magAlerts.get(i).getTagId())) {
                found = true;
                if (alert.getRawData().equals(magAlerts.get(i).getRawData())) {
                    newAlert = false;
                } else {
                    magAlerts.set(i, alert);
                }
                break;
            }
        }
        if (!found) {
            magAlerts.add(alert);
        }
        if (newAlert) {
            return alert;
        } else {
            return null;
        }
    }

    public List<RawData> getMAGAlerts() {
        if (magAlerts == null) {
            magAlerts = new ArrayList<>();
        }
        return magAlerts;
    }

    public RawData addToMOVAlerts(ScanResult scanResult) {
        if (ElaTag.CATEGORY_ID_MOV != ElaTag.getTagCategory(scanResult.getScanRecord().getBytes())) {
            return null;
        }
        movAlerts = getMOVAlerts();
        boolean found = false;
        boolean newAlert = true;
        RawData alert = new RawData(scanResult);
        for (int i = 0; i < movAlerts.size(); i++) {
            if (alert.getTagId().equals(movAlerts.get(i).getTagId())) {
                found = true;
                if (alert.getRawData().equals(movAlerts.get(i).getRawData())) {
                    newAlert = false;
                } else {
                    movAlerts.set(i, alert);
                }
                break;
            }
        }
        if (!found) {
            movAlerts.add(alert);
        }
        if (newAlert) {
            return alert;
        } else {
            return null;
        }
    }

    public List<RawData> getMOVAlerts() {
        if (movAlerts == null) {
            movAlerts = new ArrayList<>();
        }
        return movAlerts;
    }

    public RawData addToPIRAlerts(ScanResult scanResult) {
        if (ElaTag.CATEGORY_ID_PIR != ElaTag.getTagCategory(scanResult.getScanRecord().getBytes())) {
            return null;
        }
        pirAlerts = getPIRAlerts();
        boolean found = false;
        boolean newAlert = true;
        RawData alert =new RawData(scanResult);
        for (int i = 0; i < pirAlerts.size(); i++) {
            if (alert.getTagId().equals(pirAlerts.get(i).getTagId())) {
                found = true;
                if (alert.getRawData().equals(pirAlerts.get(i).getRawData())) {
                    newAlert = false;
                } else {
                    pirAlerts.set(i, alert);
                }
                break;
            }
        }
        if (!found) {
            pirAlerts.add(alert);
        }
        if (newAlert) {
            return alert;
        } else {
            return null;
        }
    }

    public List<RawData> getPIRAlerts() {
        if (pirAlerts == null) {
            pirAlerts = new ArrayList<>();
        }
        return pirAlerts;
    }

    public void addToDataLogs(ScanResult scanResult, String log) {
        if (scanResult.getDevice() == null) {
            return;
        }
        if (scanResult.getDevice().getName() == null) {
            return;
        }
        if (scanResult.getDevice().getName().isEmpty()) {
            return;
        }
        String tagId = ElaTag.getTagId(scanResult);
        dataLogs = getDataLogs();
        boolean newDataLog = true;
        for (JSONObject dataLog : dataLogs) {
            try {
                if (tagId.equals(dataLog.getString("tag_id"))) {
                    newDataLog = false;
                    boolean newLog = true;
                    JSONArray logs = dataLog.getJSONArray("logs");
                    for (int i = 0; i < logs.length(); i++) {
                        if (log.equals(logs.get(i))) {
                            newLog = false;
                            break;
                        }
                    }
                    if (newLog) {
                        logs.put(log);
                    }
                    dataLog.put("logs", logs);
                    break;
                }
            } catch (JSONException ignored) {
                newDataLog = false;
            }
        }
        if (newDataLog) {
            JSONObject dataLog = new JSONObject();
            try {
                dataLog.put("tag_id", tagId);
                dataLog.put("categ_id", ElaTag.getTagCategory(scanResult.getScanRecord().getBytes()));
                dataLog.put("logs", new JSONArray().put(log));
                dataLogs.add(dataLog);
            } catch (JSONException ignored) {
            }
        }
    }

    public void addToDataLogs(ScanResult scanResult, JSONArray logs) {
        if (scanResult.getDevice() == null) {
            return;
        }
        if (scanResult.getDevice().getName() == null) {
            return;
        }
        if (scanResult.getDevice().getName().isEmpty()) {
            return;
        }
        String tagId = ElaTag.getTagId(scanResult);
        dataLogs = getDataLogs();
        boolean newDataLog = true;
        for (JSONObject dataLog : dataLogs) {
            try {
                if (tagId.equals(dataLog.getString("tag_id"))) {
                    newDataLog = false;
                    dataLog.put("logs", logs);
                }
            } catch (JSONException ignored) {
            }
        }
        if (newDataLog) {
            JSONObject dataLog = new JSONObject();
            try {
                dataLog.put("tag_id", tagId);
                dataLog.put("categ_id", ElaTag.getTagCategory(scanResult.getScanRecord().getBytes()));
                dataLog.put("logs", logs);
                dataLogs.add(dataLog);
            } catch (JSONException ignored) {
            }
        }
    }

    public JSONObject getFromDataLogs(String tagId) {
        JSONObject result = new JSONObject();
        if (tagId == null) {
            return result;
        }
        if (tagId.isEmpty()) {
            return result;
        }
        tagId = tagId.replace(":", "").toUpperCase();
        dataLogs = getDataLogs();
        for (JSONObject data : dataLogs) {
            try {
                if (tagId.equals(data.getString("tag_id"))) {
                    result = data;
                    break;
                }
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    public void removeFromDataLogs(String tagId) {
        if (tagId == null) {
            return;
        }
        if (tagId.isEmpty()) {
            return;
        }
        tagId = tagId.replace(":", "").toUpperCase();
        dataLogs = getDataLogs();
        for (int i = 0; i < dataLogs.size(); i++) {
            try {
                if (tagId.equals(dataLogs.get(i).getString("tag_id"))) {
                    dataLogs.remove(i);
                    break;
                }
            } catch (JSONException ignored) {
            }
        }
    }

    public List<JSONObject> getDataLogs() {
        if (dataLogs == null) {
            dataLogs = new ArrayList<>();
        }
        return dataLogs;
    }

    public void resetDataLogs() {
        dataLogs = new ArrayList<>();
    }

    public void addToReportFiles(String fileName) {
        if (fileName == null) {
            return;
        }
        if (fileName.isEmpty()) {
            return;
        }
        reportFiles = getReportFiles();
        boolean found = false;
        for (int i = 0; i < reportFiles.size(); i++) {
            if (fileName.equals(reportFiles.get(i))) {
                found = true;
                break;
            }
        }
        if (!found) {
            reportFiles.add(fileName);
        }
    }

    public List<String> getReportFiles() {
        if (reportFiles == null) {
            reportFiles = new ArrayList<>();
        }
        return reportFiles;
    }

    public List<DataLogger> getDataLoggers() {
        if (dataLoggers == null) {
            dataLoggers = new ArrayList<>();
        }
        return dataLoggers;
    }

    public void setDataLoggers(JSONArray list) {
        if (list.length() == 0) {
            return;
        }
        dataLoggers = getDataLoggers();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject element = list.getJSONObject(i);
                DataLogger dataLogger = myGson().fromJson(String.valueOf(element), DataLogger.class);
                addToDataLoggers(dataLogger);
            } catch (JSONException ignored) {
            }
        }
    }

    public void addToDataLoggers(DataLogger dataLogger) {
        boolean found = false;
        for (int i = 0; i < dataLoggers.size(); i++) {
            if (dataLogger.getTagId().equals(dataLoggers.get(i).getTagId())) {
                dataLoggers.set(i, dataLogger);
                found = true;
                break;
            }
        }
        if (!found) {
            dataLoggers.add(dataLogger);
        }
    }

    public void removeFromDataLoggers(String tagId) {
        if (tagId == null) {
            return;
        }
        if (tagId.isEmpty()) {
            return;
        }
        tagId = tagId.replace(":", "").toUpperCase();
        dataLoggers = getDataLoggers();
        for (int i = 0; i < dataLoggers.size(); i++) {
            if (tagId.equals(dataLoggers.get(i).getTagId())) {
                dataLoggers.remove(i);
                break;
            }
        }
    }

    public DataLogger isDataLogger(String tagId) {
        if (tagId == null) {
            return null;
        }
        if (tagId.isEmpty()) {
            return null;
        }
        tagId = tagId.replace(":", "").toUpperCase();
        dataLoggers = getDataLoggers();
        for (int i = 0; i < dataLoggers.size(); i++) {
            if (tagId.equals(dataLoggers.get(i).getTagId())) {
                return dataLoggers.get(i);
            }
        }
        return null;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        if (currentLocation == null) {
            currentLocation = new Location(location);
        } else {
            currentLocation.set(location);
        }
    }

    public Activity getHomeActivity() {
        return homeActivity;
    }

    public void setHomeActivity(Activity activity) {
        homeActivity = activity;
    }

    public void logout() {
        token = null;
        currentUser = null;
        networks = null;
        currentNetwork = null;
        tours = null;
        currentTour = null;
        logo = null;
        userLogo = null;
        clientLogo = null;
        networkLogo = null;
        scanConfig = null;
        devices = new ArrayList<>();
        scanFilters = new ArrayList<>();
        scanResults = new ArrayList<>();
        rawDataList = new ArrayList<>();
        dataLoggers = new ArrayList<>();
        dataLogs = new ArrayList<>();
        System.gc();
    }
}
