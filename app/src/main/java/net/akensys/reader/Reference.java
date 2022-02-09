package net.akensys.reader;

public class Reference {
    public static final String NOTIFICATION_CHANNEL_ID = "MYOTY_NOTIFICATION";
    public static final String NOTIFICATION_CHANNEL_NAME = "MYOTY_NOTIFICATION";
    public static final int RAW_DATA_NOTIFICATION_ID = 1;
    public static final int ALERT_BEEPER_SOUND = 1;
    public static final int ALERT_TTS = 2;
    public static final int ALERT_SILENCE = 3;


    public static final String RAW_DATA_REPORT_PREFIX = "raw-data-";
    public static final String MAG_ALERT_REPORT_PREFIX = "mag-alert-";
    public static final String DATA_LOGS_REPORT_PREFIX = "data-logs-";


    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    public static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 3;

    public static final String BASE_URL = "https://api.myoty.com/";
    public static final String API_LOGIN_CONNECT = "api/login_connect";
    public static final String API_GET_SESSION = "api/session";
    public static final String API_GET_USER_LOGO = "api/userLogo";
    public static final String API_GET_CLIENT_LOGO = "api/clientLogo";
    public static final String API_GET_NETWORK_LOGO = "api/networkLogo";
    public static final String API_GET_NETWORKS = "api/listeNetworksUser";
    public static final String API_GET_SCAN_CONFIG = "api/getScanConfig";
    public static final String API_GET_DEVICE_LIST = "api/getDeviceList";
    public static final String API_GET_TOURS = "api/getListeTour";
    public static final String API_SAVE_RAW_DATA = "api/saveInventoryFromMobile";
    public static final String API_SAVE_DATA_LOGS = "api/saveDatalog";
    public static final String API_SAVE_LOG_RST_DATE_TIME = "api/saveLogRstDateTime";

    public static final String SCHUCO_NETWORK_ID = "2019101";
}
