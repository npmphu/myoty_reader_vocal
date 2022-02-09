package net.akensys.reader.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import net.akensys.reader.MyOty;
import net.akensys.reader.PrefsHelper;
import net.akensys.reader.R;
import net.akensys.reader.Reference;
import net.akensys.reader.model.ScanConfig;
import net.akensys.reader.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AKRequest {

    private static final int postDataLogsTimeout = 30000;
    private static final int postRawDataTimeout = 15000;

    public static void getDeviceList(Context context, Runnable success) {
        String networkId = PrefsHelper.getNetworkId(context);
        String clientUUID = PrefsHelper.getClientUUID(context);
        String url = Reference.BASE_URL + Reference.API_GET_DEVICE_LIST + "?network_id=" + networkId + "&client_uuid=" + clientUUID;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Log.d("getDeviceList", String.valueOf(response));
                    if (response.has("devices_list")) {
                        try {
                            JSONArray list = response.getJSONArray("devices_list");
                            MyOty.getInstance().setDevices(context, list);
                            // MyOty.getInstance().setScanFilters();
                        } catch (JSONException ignored) { }
                    }
                    new Handler().post(success);
                },
                error -> {
                    solveErrorResponse(context, error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    public static void getScanConfig(Context context, Runnable success) {
        String networkId = PrefsHelper.getNetworkId(context);
        String url = Reference.BASE_URL + Reference.API_GET_SCAN_CONFIG + "?network_id=" + networkId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    ScanConfig scanConfig = new Gson().fromJson(String.valueOf(response), ScanConfig.class);
                    MyOty.getInstance().setScanConfig(scanConfig);
                    PrefsHelper.setScanDuration(context, scanConfig.getScanDurationMobile());
                    PrefsHelper.setScanInterval(context, scanConfig.getScanIntervalMobile());
                    new Handler().post(success);
                },
                error -> {
                    solveErrorResponse(context, error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    public static void getClientLogo(Context context, String logo, Runnable success) {
        String url = Reference.BASE_URL + Reference.API_GET_CLIENT_LOGO + "/" + logo;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    MyOty.getInstance().setLogo(response);
                    MyOty.getInstance().setClientLogo(response);
                    new Handler().post(success);
                },
                error -> {
                    solveErrorResponse(context, error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    public static void getNetworkLogo(Context context, String logo, Runnable success) {
        String url = Reference.BASE_URL + Reference.API_GET_NETWORK_LOGO + "/" + logo;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    MyOty.getInstance().setLogo(response);
                    MyOty.getInstance().setNetworkLogo(response);
                    new Handler().post(success);
                },
                error -> {
                    AKRequest.solveErrorResponse(context, error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    public static void getUserLogo(Context context, String logo, Runnable success) {
        String url = Reference.BASE_URL + Reference.API_GET_USER_LOGO + "/" + logo;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    MyOty.getInstance().setLogo(response);
                    MyOty.getInstance().setUserLogo(response);
                    new Handler().post(success);
                },
                error -> {
                    AKRequest.solveErrorResponse(context, error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    public static void postRawData(Context context, JSONObject jsonRequest) {
        Log.d("postRawData", String.valueOf(jsonRequest));
        String url = Reference.BASE_URL + Reference.API_SAVE_RAW_DATA;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    Log.d("postRawDataResponse", String.valueOf(response));
                    onPostRawDataSucceeded(context, jsonRequest);
                    if (response.has("dataLoggers")) {
                        try {
                            JSONArray list = response.getJSONArray("dataLoggers");
                            MyOty.getInstance().setDataLoggers(list);
                        } catch (JSONException ignored) { }
                    }
                },
                error -> {
                     Log.d("postRawDataError", String.valueOf(error));
                    solveErrorResponse(context, error);
                    onPostRawDataFailed(context, jsonRequest);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    if (jsonString.isEmpty()) {
                        jsonString = "{\"dataLoggers\": []}";
                    } else {
                        jsonString = "{\"dataLoggers\": " + jsonString + "}";
                    }
                    JSONObject result = new JSONObject(jsonString);
                    return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(postRawDataTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    private static void onPostRawDataSucceeded(Context context, JSONObject jsonRequest) {
        try {
            String tagId = "all-";
            if (jsonRequest.getJSONArray("data").length() == 1) {
                tagId = jsonRequest.getJSONArray("data").getJSONObject(0).getString("tag_id");
            }
            String dateTime = jsonRequest.getString("date_scan").replace(" ", "-").replace(":", "-");
            String fileName = Reference.RAW_DATA_REPORT_PREFIX + tagId + dateTime;
            Utils.deleteFile(context, fileName);
            PrefsHelper.removeFromReportFiles(context, fileName);
            if (PrefsHelper.getReportFiles(context).size() == 0) {
                if (MyOty.getInstance().getHomeActivity() != null) {
                    Button btn_send_reports = MyOty.getInstance().getHomeActivity().findViewById(R.id.btn_send_reports);
                    if (btn_send_reports != null && btn_send_reports.getVisibility() == View.VISIBLE) {
                        btn_send_reports.setVisibility(View.GONE);
                    }
                }
            }
        } catch (JSONException ignored) { }
    }

    private static void onPostRawDataFailed(Context context, JSONObject jsonRequest) {
        try {
            String tagId = "all-";
            if (jsonRequest.getJSONArray("data").length() == 1) {
                tagId = jsonRequest.getJSONArray("data").getJSONObject(0).getString("tag_id");
            }
            String dateTime = jsonRequest.getString("date_scan").replace(" ", "-").replace(":", "-");
            String fileName = Reference.RAW_DATA_REPORT_PREFIX + tagId + dateTime;
            Utils.writeToFile(context, fileName, jsonRequest.toString());
            PrefsHelper.addToReportFiles(context, fileName);
            if (PrefsHelper.getReportFiles(context).size() > 0) {
                if (MyOty.getInstance().getHomeActivity() != null) {
                    Button btn_send_reports = MyOty.getInstance().getHomeActivity().findViewById(R.id.btn_send_reports);
                    if (btn_send_reports != null && btn_send_reports.getVisibility() == View.GONE) {
                        btn_send_reports.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (JSONException ignored) { }
    }

    public static void postDataLogs(Context context, JSONObject jsonRequest) {
        try {
            if (!jsonRequest.getJSONArray("data").getJSONObject(0).has("logs"))
                return;
        } catch (JSONException ignored) { }
        Log.d("postDataLogs", String.valueOf(jsonRequest));
        String url = Reference.BASE_URL + Reference.API_SAVE_DATA_LOGS;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    Log.d("postDataLogsResponse", String.valueOf(response));
                    onPostDataLogsSucceeded(context, jsonRequest);
                },
                error -> {
                    Log.d("postDataLogsError", String.valueOf(error));
                    solveErrorResponse(context, error);
                    onPostDataLogsFailed(context, jsonRequest);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    JSONObject result = null;
                    if (jsonString.length() > 0)
                        result = new JSONObject(jsonString);
                    return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(postDataLogsTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    private static void onPostDataLogsSucceeded(Context context, JSONObject jsonRequest) {
        try {
            String dateTime = jsonRequest.getString("date_scan").replace(" ", "-").replace(":", "-");
            String tagId = jsonRequest.getJSONArray("data").getJSONObject(0).getString("tag_id");
            String fileName = Reference.DATA_LOGS_REPORT_PREFIX + tagId + dateTime;
            Utils.deleteFile(context, fileName);
            PrefsHelper.removeFromReportFiles(context, fileName);
            if (PrefsHelper.getReportFiles(context).size() == 0) {
                if (MyOty.getInstance().getHomeActivity() != null) {
                    Button btn_send_reports = MyOty.getInstance().getHomeActivity().findViewById(R.id.btn_send_reports);
                    if (btn_send_reports != null && btn_send_reports.getVisibility() == View.VISIBLE) {
                        btn_send_reports.setVisibility(View.GONE);
                    }
                }
            }
        } catch (JSONException ignored) { }
    }

    private static void onPostDataLogsFailed(Context context, JSONObject jsonRequest) {
        try {
            String dateTime = jsonRequest.getString("date_scan").replace(" ", "-").replace(":", "-");
            String tagId = jsonRequest.getJSONArray("data").getJSONObject(0).getString("tag_id");
            String fileName = Reference.DATA_LOGS_REPORT_PREFIX + tagId + dateTime;
            Utils.writeToFile(context, fileName, jsonRequest.toString());
            PrefsHelper.addToReportFiles(context, fileName);
            if (PrefsHelper.getReportFiles(context).size() > 0) {
                if (MyOty.getInstance().getHomeActivity() != null) {
                    Button btn_send_reports = MyOty.getInstance().getHomeActivity().findViewById(R.id.btn_send_reports);
                    if (btn_send_reports != null && btn_send_reports.getVisibility() == View.GONE) {
                        btn_send_reports.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (JSONException ignored) { }
    }

    public static void saveLogRstDateTime(Context context, JSONObject jsonRequest) {
        Log.d("saveLogRstDateTime", String.valueOf(jsonRequest));
        String url = Reference.BASE_URL + Reference.API_SAVE_LOG_RST_DATE_TIME;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    Log.d("saveLogRstDateTimeResponse", String.valueOf(response));
                },
                error -> {
                    Log.d("saveLogRstDateTimeError", String.valueOf(error));
                    solveErrorResponse(context, error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(context));
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    JSONObject result = null;
                    if (jsonString.length() > 0)
                        result = new JSONObject(jsonString);
                    return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        MyOty.getInstance().addToRequestQueue(context, request);
    }

    public static void solveErrorResponse(Context context, VolleyError error) {
        String message;
        if (error instanceof NetworkError) {
            message = "Impossible de se connecter à Internet ... Veuillez vérifier votre connexion!";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else if (error.networkResponse != null) {
            if (error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND || error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                message = "Identification Incorrecte. Veuillez vérifier vos informations d'identification.";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        } else if (error instanceof ServerError) {
            message = "Le serveur est introuvable. Veuillez réessayer après un certain temps!";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else if (error instanceof AuthFailureError) {
            message = "Erreur d'authentification!";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else if (error instanceof ParseError) {
            message = "Erreur d'analyse! Veuillez réessayer après un certain temps!";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else if (error instanceof TimeoutError) {
            message = "Délai de connection dépassé! S'il vous plait, vérifiez votre connexion internet.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
