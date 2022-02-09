package net.akensys.reader;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PrefsHelper {
    private static final String MYOT_APP_SETTINGS = "MYOT_APP_SETTINGS";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(MYOT_APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static String getLogin(Context context) {
        return getSharedPreferences(context).getString("LOGIN" , null);
    }

    public static void setLogin(Context context, String login) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("LOGIN" , login);
        editor.apply();
    }

    public static String getPassword(Context context) {
        return getSharedPreferences(context).getString("PASSWORD" , null);
    }

    public static void setPassword(Context context, String password) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("PASSWORD" , password);
        editor.apply();
    }

    public static String getToken(Context context) {
        return getSharedPreferences(context).getString("TOKEN" , null);
    }

    public static void setToken(Context context, String token) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("TOKEN" , token);
        editor.apply();
    }

    public static String getUserUUID(Context context) {
        return getSharedPreferences(context).getString("USER_UUID" , null);
    }

    public static void setUserUUID(Context context, String userUUID) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("USER_UUID" , userUUID);
        editor.apply();
    }

    public static String getUserLogo(Context context) {
        return getSharedPreferences(context).getString("USER_LOGO" , null);
    }

    public static void setUserLogo(Context context, String userLogo) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("USER_LOGO" , userLogo);
        editor.apply();
    }

    public static String getReaderName(Context context) {
        return getSharedPreferences(context).getString("READER_NAME" , null);
    }

    public static void setReaderName(Context context, String readerName) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("READER_NAME" , readerName);
        editor.apply();
    }

    public static Integer getReaderType(Context context) {
        return getSharedPreferences(context).getInt("READER_TYPE" , 0);
    }

    public static void setReaderType(Context context, Integer readerType) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("READER_TYPE" , readerType);
        editor.apply();
    }

    public static String getClientUUID(Context context) {
        return getSharedPreferences(context).getString("CLIENT_UUID" , null);
    }

    public static void setClientUUID(Context context, String clientUUID) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("CLIENT_UUID" , clientUUID);
        editor.apply();
    }

    public static String getNetworkId(Context context) {
        return getSharedPreferences(context).getString("NETWORK_ID" , null);
    }

    public static void setNetworkId(Context context, String networkLabel) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("NETWORK_ID" , networkLabel);
        editor.apply();
    }

    public static String getNetworkLabel(Context context) {
        return getSharedPreferences(context).getString("NETWORK_LABEL" , null);
    }

    public static void setNetworkLabel(Context context, String networkId) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("NETWORK_LABEL" , networkId);
        editor.apply();
    }

    public static String getNetworkLogo(Context context) {
        return getSharedPreferences(context).getString("NETWORK_LOGO" , null);
    }

    public static void setNetworkLogo(Context context, String networkLogo) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("NETWORK_LOGO" , networkLogo);
        editor.apply();
    }

    public static String getTourId(Context context) {
        return getSharedPreferences(context).getString("TOUR_ID" , null);
    }

    public static void setTourId(Context context, String networkId) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("TOUR_ID" , networkId);
        editor.apply();
    }

    public static Integer getScanDuration(Context context) {
        return getSharedPreferences(context).getInt("SCAN_DURATION" , 30);
    }

    public static void setScanDuration(Context context, Integer scanDuration) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("SCAN_DURATION" , scanDuration);
        editor.apply();
    }

    public static Integer getScanInterval(Context context) {
        return getSharedPreferences(context).getInt("SCAN_INTERVAL" , 5 * 60);
    }

    public static void setScanInterval(Context context, Integer scanInterval) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("SCAN_INTERVAL" , scanInterval);
        editor.apply();
    }

    public static Integer getMagAlertMng(Context context) {
        return getSharedPreferences(context).getInt("MAG_ALERT" , 0);
    }

    public static void setMagAlertMng(Context context, Integer magAlert) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("MAG_ALERT" , magAlert);
        editor.apply();
    }

    public static Integer getDevicesCount(Context context) {
        return getSharedPreferences(context).getInt("DEVICES_COUNT" , 0);
    }

    public static void setDevicesCount(Context context, Integer devicesCount) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("DEVICES_COUNT" , devicesCount);
        editor.apply();
    }

    public static Set<String> getDeviceNames(Context context) {
        return getSharedPreferences(context).getStringSet("DEVICE_NAMES" , null);
    }

    public static void setDeviceNames(Context context, Set<String> deviceNames) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putStringSet("DEVICE_NAMES" , deviceNames);
        editor.apply();
    }

    public static Set<String> getDeviceMACs(Context context) {
        return getSharedPreferences(context).getStringSet("DEVICE_MACS" , null);
    }

    public static void setDeviceMACs(Context context, Set<String> deviceMACs) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putStringSet("DEVICE_MACS" , deviceMACs);
        editor.apply();
    }

    public static Set<String> getReportFiles(Context context) {
        return getSharedPreferences(context).getStringSet("REPORT_FILES", new HashSet<>());
    }

    public static void setReportFiles(Context context, Set<String> reportFiles) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putStringSet("REPORT_FILES" , reportFiles);
        editor.apply();
    }

    public static void addToReportFiles(Context context, String fileName) {
        Set<String> reportFiles = getReportFiles(context);
        reportFiles.add(fileName);
        setReportFiles(context, reportFiles);
    }

    public static void removeFromReportFiles(Context context, String fileName) {
        Set<String> reportFiles = getReportFiles(context);
        reportFiles.remove(fileName);
        setReportFiles(context, reportFiles);
    }

    public static String getGroupAlert(Context context, String tagName) {
        String key = tagName.replaceAll("[^a-zA-Z0-9 ]", "");
        return getSharedPreferences(context).getString(key , null);
    }

    public static void setGroupAlert(Context context, String tagName, String groupAlertJson) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        String key = tagName.replaceAll("[^a-zA-Z0-9 ]", "");
        editor.putString(key , groupAlertJson);
        editor.apply();
    }

    public static boolean getAlertStatus(Context context, String tagName) {
        String key = tagName.replaceAll("[^a-zA-Z0-9 ]", "") + "_ALERTED";
        return getSharedPreferences(context).getBoolean(key , false);
    }

    public static void setAlertStatus(Context context, String tagName, boolean status) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        String key = tagName.replaceAll("[^a-zA-Z0-9 ]", "") + "_ALERTED";
        editor.putBoolean(key , status);
        editor.apply();
    }

    public static int getAlertId(Context context) {
        int currentId = getSharedPreferences(context).getInt("ALERT_ID" , 0);
        int newId = (currentId + 1) % Integer.MAX_VALUE;
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("ALERT_ID", newId);
        editor.apply();
        return currentId;
    }

    public static void setAlertOpt(Context context, int opt) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("ALERT_OPT" , opt);
        editor.apply();
    }

    public static int getAlertOpt(Context context) {
        return getSharedPreferences(context).getInt("ALERT_OPT" , Reference.ALERT_TTS);
    }

    public static void logout(Context context) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        String login = getLogin(context);
        String password = getPassword(context);
        editor.clear();
        editor.apply();
        setLogin(context, login);
        setPassword(context, password);
    }
}
