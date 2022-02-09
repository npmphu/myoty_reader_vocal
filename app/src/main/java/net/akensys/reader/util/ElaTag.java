package net.akensys.reader.util;

import android.bluetooth.le.ScanResult;
import android.util.Log;

import androidx.annotation.NonNull;

import net.akensys.reader.MyOty;
import net.akensys.reader.model.Device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ElaTag {
    public static final String UUID_SERVICE_TEMPERATURE = "6E2A";
    public static final String UUID_SERVICE_HUMIDITY = "6F2A";
    public static final String UUID_SERVICE_MAG = "3F2A00";
    public static final String UUID_SERVICE_MOV = "3F2A01";
    public static final String UUID_SERVICE_PIR = "782A";
    public static final String UUID_SERVICE_MM1 = "062A";
    public static final String UUID_SERVICE_ANG = "A12A";
    public static final String UUID_SERVICE_DI = "3F2A02";
    public static final String UUID_SERVICE_AI = "582A";
    public static final String UUID_SERVICE_DO = "3F2A";
    public static final String UUID_SERVICE_BAT = "0F18";

    public static final int CATEGORY_ID_WP = 1;
    public static final int CATEGORY_ID_T = 2;
    public static final int CATEGORY_ID_H = 3;
    public static final int CATEGORY_ID_BLE = 4;
    public static final int CATEGORY_ID_MOV = 5;
    public static final int CATEGORY_ID_MAG = 6;
    public static final int CATEGORY_ID_RHT = 7;
    public static final int CATEGORY_ID_PIR = 13;

    public static int getTagCategory(byte[] bytes) {
        int mCategoryId = CATEGORY_ID_BLE;
        String payload = Utils.toHexadecimalString(bytes);
        if (payload.contains(UUID_SERVICE_TEMPERATURE) && payload.contains(UUID_SERVICE_HUMIDITY)) {
            mCategoryId = CATEGORY_ID_RHT;
        } else if (payload.contains(UUID_SERVICE_TEMPERATURE)) {
            mCategoryId = CATEGORY_ID_T;
        } else if (payload.contains(UUID_SERVICE_MAG)) {
            mCategoryId = CATEGORY_ID_MAG;
        } else if (payload.contains(UUID_SERVICE_MOV)) {
            mCategoryId = CATEGORY_ID_MOV;
        } else if (payload.contains(UUID_SERVICE_PIR)) {
            mCategoryId = CATEGORY_ID_PIR;
        } else if (payload.contains(UUID_SERVICE_MM1)) {
            mCategoryId = CATEGORY_ID_MOV;
        }
        return mCategoryId;
    }

    public static int getTagCategory(String payload) {
        int mCategoryId = CATEGORY_ID_BLE;
        if (payload.contains(UUID_SERVICE_TEMPERATURE) && payload.contains(UUID_SERVICE_HUMIDITY)) {
            mCategoryId = CATEGORY_ID_RHT;
        } else if (payload.contains(UUID_SERVICE_TEMPERATURE)) {
            mCategoryId = CATEGORY_ID_T;
        } else if (payload.contains(UUID_SERVICE_MAG)) {
            mCategoryId = CATEGORY_ID_MAG;
        } else if (payload.contains(UUID_SERVICE_MOV)) {
            mCategoryId = CATEGORY_ID_MOV;
        } else if (payload.contains(UUID_SERVICE_PIR)) {
            mCategoryId = CATEGORY_ID_PIR;
        } else if (payload.contains(UUID_SERVICE_MM1)) {
            mCategoryId = CATEGORY_ID_MOV;
        }
        return mCategoryId;
    }

    public static Double getTemperature(String payload) {
        if (payload == null) {
            return null;
        }
        if (payload.isEmpty()) {
            return null;
        }
        Double value = null;
        String T = payload.substring(16, 18) + payload.substring(14, 16);
        if (T.indexOf("F") == 0) {
            T = T.substring(1);
            value = (Integer.parseInt(T, 16) ^ 4095) * -0.01;
        } else {
            value = Integer.parseInt(T, 16) * 0.01;
        }
        return value;
    }

    public static Double getHumidity(String payload) {
        if (payload == null) {
            return null;
        }
        if (payload.isEmpty()) {
            return null;
        }
        return Integer.parseInt(payload.substring(26, 28), 16) * 0.01;
    }

    public static Integer[] getCountAndState(String payload) {
        if (payload == null) {
            return null;
        }
        if (payload.isEmpty()) {
            return null;
        }
        int value = Integer.parseInt( payload.substring(16, 18) + payload.substring(14, 16), 16);
        Log.d("ElaTag", "getCountAndState: " + value);
        String CaS = Integer.toString(value, 2);
        Log.d("ElaTag", "getCountAndState: " + CaS);
        int len = CaS.length();
        return new Integer[] {Integer.parseInt(CaS.substring(0, len - 1), 2), Integer.parseInt(CaS.substring(len - 1, len), 2)};
    }

    public static long getDataLoggerInterval(@NonNull JSONObject dataLogger) {
        long interval = 0;
        try {
            int categoryId = dataLogger.getInt("categ_id");
            JSONArray logs = dataLogger.getJSONArray("logs");
            int next = 1;
            if (CATEGORY_ID_RHT == categoryId) {
                next = 2;
            }
            if (next >= logs.length()) {
                return interval;
            }
            SimpleDateFormat dtf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date date1 = dtf.parse(logs.getString(0).split(": ")[0]);
            Date date2 = dtf.parse(logs.getString(next).split(": ")[0]);
            if (date1 != null && date2 != null) {
                interval = Math.abs((date1.getTime() - date2.getTime()));
            }
            return interval;
        } catch (JSONException | ParseException ignored) {
            return interval;
        }
    }

    public static String getTagId(@NonNull ScanResult scanResult) {
        String tagId = "";
        if (scanResult.getDevice() == null) {
            return tagId;
        }
        if (scanResult.getDevice().getAddress() == null) {
            return tagId;
        }
        tagId = scanResult.getDevice().getAddress().replace(":", "").toUpperCase();
        return tagId;
    }
}
