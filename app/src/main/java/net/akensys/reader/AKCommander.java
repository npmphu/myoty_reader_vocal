package net.akensys.reader;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.akensys.reader.model.DataLogger;
import net.akensys.reader.service.AKRequest;
import net.akensys.reader.util.CommandBuilder;
import net.akensys.reader.util.Conversion;
import net.akensys.reader.util.ElaTag;
import net.akensys.reader.util.FrameBuilder;
import net.akensys.reader.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AKCommander {
    public static final String TAG = "AKCommander";

    public static final String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String RX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String TX_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    public static final String EN12830_DEFAULT_PWD = "123456789A";

    public static final int LOG_DL_AND_RST_ACTION = 1;
    public static final int LOG_RST_ACTION = 2;
    public static final int LOG_DL_ACTION = 3;

    public static final String LOG_RST_CALLBACK = "LOG_RST_CALLBACK";
    public static final String LOG_DL_CALLBACK = "LOG_DL_CALLBACK";
    public static final String LOG_DL_AND_RST_CALLBACK = "LOG_DL_RST_CALLBACK";
    public static final String LOG_SP_INV_DL_CALLBACK = "LOG_SP_INV_DL_CALLBACK";
    public static final String LOG_SP_INV_DL_AND_RST_CALLBACK = "LOG_SP_INV_DL_RST_CALLBACK";
    public static final String LOG_INTERVAL_CALLBACK = "LOG_INTERVAL_CALLBACK";
    public static final String WRITE_DATETIME_CALLBACK = "WRITE_DATETIME_CALLBACK";


    public static final String FW_VERS_COMMAND = "FW_VERS";
    public static final String DATALOGGER_START_COMMAND = "DATALOGGER_START";
    public static final String DATALOGGER_STOP_COMMAND = "DATALOGGER_STOP";
    public static final String READ_DATA_COMMAND = "READ_DATA";
    public static final String LOG_RST_COMMAND = "LOG_RST";
    public static final String LOG_DL_COMMAND = "LOG_DL";
    public static final String LOG_SP_INV_DL_COMMAND = "LOG_SP_INV_DL";
    public static final String LOG_INTERVAL_COMMAND = "0012048910570101";
    public static final String ZERO_07_COMMAND = "007";
    public static final String ELA_EN12830 = "ELA_EN12830";

    public static final String DATALOGGER_START_SUCCESS = "DATALOGGER_START: SUCCESS";
    public static final String DATALOGGER_START_ACCESS_DENIED = "DATALOGGER_START: ACCESS DENIED";
    public static final String DATALOGGER_START_WRONG_PARAMETERS = "DATALOGGER_START: WRONG PARAMETERS";
    public static final String DATALOGGER_STOP_SUCCESS = "DATALOGGER_STOP: SUCCESS";
    public static final String DATALOGGER_STOP_ACCESS_DENIED = "DATALOGGER_STOP: ACCESS DENIED";
    public static final String DATALOGGER_STOP_LOG_NOT_STARTED = "DATALOGGER_STOP: LOG NOT STARTED!";
    public static final String READ_DATA_SUCCESS = "READ_DATA: SUCCESS";
    public static final String READ_DATA_ACCESS_DENIED = "READ_DATA: ACCESS DENIED";
    public static final String READ_DATA_LOG_NOT_STARTED = "READ_DATA: LOG NOT STARTED!";

    public static final String RESET_LOG_DATA = "RESET LOG DATA";
    public static final String DOWNLOAD_START = "DOWNLOAD_START";
    public static final String DOWNLOAD_END = "DOWNLOAD_END";
    public static final String DATA_START = "DATA_START";
    public static final String DATA_END = "DATA_END";
    public static final String END_OF_DATA = "END_OF_DATA";
    public static final String LOG_NOT_STARTED = "LOG NOT STARTED";
    public static final String LOG_MODE_DISABLED = "LOG MODE DISABLED";
    public static final String LOG_INTERVAL_HEADER = "001109341057060104";
    public static final String ZERO_07_RESPONSE = "9000017B01";
    public static final String WRONG_COMMAND = "WRONG COMMAND";
    public static final String USE_EN12830_COMMANDS = "USE EN12830 DATALOGGER COMMANDS";
    public static final String USE_REGULAR_DATALOGGER_COMMANDS = "USE REGULAR DATALOGGER COMMANDS";

    private static final DateTimeFormatter zdtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm XXXXX");
    private static final DateTimeFormatter zdtlf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss XXXXX");
    private static final int mServiceDiscoverDelay = 5000;

    private static AKCommander instance = null;
    private static Boolean isConnecting = false;
    private BluetoothGatt bluetoothGatt;
    private ScanResult mScanResult;
    private DataLogger mDataLogger;
    private Context mContext;
    private String mCallback;
    private Boolean fwEN12830;
    private ZonedDateTime mLogActDatetime;
    private ZonedDateTime mLogRstDatetime;
    private Handler mServiceDiscoverHandler;
    private Boolean mDataLoggerSentinel;
    private long logInterval;
    private List<String> frames;
    private int frameIndex;


    public static AKCommander getInstance() {
        if (null == instance)
            instance = new AKCommander();
        return instance;
    }

    public boolean connectToDevice(ScanResult scanResult, String callback, Context context, DataLogger dataLogger) {
        if (scanResult == null || callback == null || callback.isEmpty() || context == null || isConnecting)
            return false;
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).contains(scanResult.getDevice()))
            return false;
        Log.d(TAG, "connectToDevice: " + scanResult.getDevice().getName());
        mContext = context;
        mCallback = callback;
        mScanResult = scanResult;
        mDataLogger = dataLogger;
        mLogActDatetime = ZonedDateTime.now();
        mLogRstDatetime = null;
        if (mDataLogger !=null && mDataLogger.getLogRstDatetime() != null && !mDataLogger.getLogRstDatetime().isEmpty()) {
            mLogRstDatetime = getLogRstDateTime(mDataLogger.getLogRstDatetime());
            Log.d(TAG, "Datalogger Start At: " + mLogRstDatetime);
        }
        fwEN12830 = null;
        bluetoothGatt = mScanResult.getDevice().connectGatt(mContext, false, connectCallback);
        bluetoothGatt.connect();
        isConnecting = true;
        if (MyOty.getInstance().getHomeActivity() != null) {
            TextView tv_data_logger = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_data_logger);
            if (tv_data_logger != null) {
                tv_data_logger.setText(mScanResult.getDevice().getName() + " : datalogger en cours de traitement...");
                tv_data_logger.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    Runnable tv_data_logger_gone = () -> {
        if (MyOty.getInstance().getHomeActivity() != null) {
            TextView tv_data_logger = MyOty.getInstance().getHomeActivity().findViewById(R.id.tv_data_logger);
            if (tv_data_logger != null) {
                tv_data_logger.setVisibility(View.GONE);
            }
        }
    };

    public void onDisconnectDevice() {
        isConnecting = false;
        if (MyOty.getInstance().getHomeActivity() != null) {
            MyOty.getInstance().getHomeActivity().runOnUiThread(tv_data_logger_gone);
        }
    }


    public void disconnectDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        onDisconnectDevice();
    }

    public BluetoothGattCallback connectCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange: status = " + status + " ; newState = " + newState);
            if (newState == 30) {
                isConnecting = false;
            }
            else if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnecting = true;
                gatt.requestMtu(61);
                mServiceDiscoverHandler = new Handler(Looper.getMainLooper());
                mServiceDiscoverHandler.postDelayed(gatt::discoverServices, mServiceDiscoverDelay);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange: disconnected");
                onDisconnectDevice();
                if (status == 8 || status == 133) {
                    Log.d(TAG, "onConnectionStateChange: status = " + status);
                    // If smartphone lost the connection with bluetooth device then disconnect and remove its uncompleted logs
                    MyOty.getInstance().removeFromDataLogs(mScanResult.getDevice().getAddress());
                }
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            // gatt.discoverServices();
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (mServiceDiscoverHandler != null) {
                mServiceDiscoverHandler.removeCallbacksAndMessages(null);
            }
            setNotificationEnabled(gatt);
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            solveCharacteristic(gatt, characteristic);
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            writeCharacteristic(gatt);
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    private void setNotificationEnabled(BluetoothGatt gatt) {
        List<BluetoothGattService> serviceList = gatt.getServices();
        for(BluetoothGattService service : serviceList) {
            if(service.getUuid().toString().equals(SERVICE_UUID)) {
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                    if (characteristic.getUuid().toString().equals(RX_UUID)) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        break;
                    }
                }
            }
        }
    }

    private void writeCharacteristic(BluetoothGatt gatt) {
        writeToCharacteristic(gatt, FW_VERS_COMMAND);
    }

    private void writeToCharacteristic(BluetoothGatt gatt, String command) {
        Log.d(TAG, "writeToCharacteristic: " + command);
        List<BluetoothGattService> serviceList = gatt.getServices();
        for (BluetoothGattService service : serviceList) {
            if (service.getUuid().toString().equals(SERVICE_UUID)) {
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                    if (characteristic.getUuid().toString().equals(TX_UUID)) {
                        characteristic.setValue(command.getBytes(StandardCharsets.UTF_8));
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        gatt.writeCharacteristic(characteristic);
                        break;
                    }
                }
            }
        }
    }

    private void writeBytesToCharacteristic(BluetoothGatt gatt, String command) {
        Log.d(TAG, "writeBytesToCharacteristic: " + command);
        List<BluetoothGattService> serviceList = gatt.getServices();
        for (BluetoothGattService service : serviceList) {
            if (service.getUuid().toString().equals(SERVICE_UUID)) {
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                    if (characteristic.getUuid().toString().equals(TX_UUID)) {
                        characteristic.setValue(Utils.getBytes(Conversion.toHexadecimal(command)));
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        gatt.writeCharacteristic(characteristic);
                        break;
                    }
                }
            }
        }
    }

    private void solveCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (fwEN12830 == null) {
            onFwVers(gatt, characteristic);
        }
        else if (!fwEN12830) {
            switch (mCallback) {
                case LOG_RST_CALLBACK:
                    onLogRst(gatt, characteristic);
                    break;
                case LOG_DL_CALLBACK:
                    onLogDL(gatt, characteristic);
                    break;
                case LOG_DL_AND_RST_CALLBACK:
                    onLogDLRst(gatt, characteristic);
                    break;
                case LOG_SP_INV_DL_CALLBACK:
                    onLogSpInvDL(gatt, characteristic);
                    break;
                case LOG_SP_INV_DL_AND_RST_CALLBACK:
                    onLogSpInvDLRst(gatt, characteristic);
                    break;
                case WRITE_DATETIME_CALLBACK:
                    onWriteDatetime(gatt, characteristic);
                    break;
                case LOG_INTERVAL_CALLBACK:
                    onLogInterval(gatt, characteristic);
                    break;
            }
        }
        else {
            switch (mCallback) {
                case LOG_RST_CALLBACK:
                    onEn12830LogRst(gatt, characteristic);
                    break;
                case LOG_DL_CALLBACK:
                    onEn12830LogDL(gatt, characteristic);
                    break;
                case LOG_DL_AND_RST_CALLBACK:
                    onEn12830LogDLRst(gatt, characteristic);
                    break;
            }
        }
    }

    private void onFwVers(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onFwVers: " + value);
        fwEN12830 = value.toUpperCase().contains(ELA_EN12830);
        if (!fwEN12830) {
            switch (mCallback) {
                case LOG_RST_CALLBACK:
                    writeToCharacteristic(gatt, LOG_RST_COMMAND);
                    break;
                case LOG_DL_CALLBACK:
                case LOG_DL_AND_RST_CALLBACK:
                    writeToCharacteristic(gatt, LOG_DL_COMMAND);
                    break;
                case LOG_SP_INV_DL_CALLBACK:
                case LOG_SP_INV_DL_AND_RST_CALLBACK:
                    mDataLoggerSentinel = true; // We don't solve the first data logs from LOG_DL_COMMAND so a sentinel is set
                    writeToCharacteristic(gatt, LOG_DL_COMMAND);
                    break;
                case WRITE_DATETIME_CALLBACK:
                    FrameBuilder builder = new FrameBuilder();
                    String stefCustomFrame = builder.getCompleteFrame("19840526210159+01");
                    CommandBuilder commandBuilder = new CommandBuilder();
                    frames = commandBuilder.getFormattedDateCommand(stefCustomFrame, 61);
                    frameIndex = 0;
                    writeBytesToCharacteristic(gatt, frames.get(0));
                    break;
                case LOG_INTERVAL_CALLBACK:
                    writeBytesToCharacteristic(gatt, LOG_INTERVAL_COMMAND);
                    break;
            }
        }
        else {
            switch (mCallback) {
                case LOG_RST_CALLBACK:
                    writeToCharacteristic(gatt, DATALOGGER_START_COMMAND + " " + EN12830_DEFAULT_PWD + " " + zdtlf.format(ZonedDateTime.now()));
                    break;
                case LOG_DL_CALLBACK:
                case LOG_DL_AND_RST_CALLBACK:
                    writeToCharacteristic(gatt, READ_DATA_COMMAND + " " + EN12830_DEFAULT_PWD);
                    break;
            }
        }
    }

    private void onLogRst(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onLogRst: " + value);
        if (value.toUpperCase().contains(RESET_LOG_DATA)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
        }
        disconnectDevice();

    }

    private void onLogDL(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onLogDL: " + value);
        if (noneEN12830(value)) {
            solveNoneEN12830(value);
        }
        else if (value.toUpperCase().contains(END_OF_DATA)) {
            disconnectDevice();
            if (mLogRstDatetime == null) {
                calculateLogDatetime(mScanResult.getDevice());
            }
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext,AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(RESET_LOG_DATA)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(LOG_NOT_STARTED)) {
            writeToCharacteristic(gatt, LOG_RST_COMMAND);
        }
        else if (value.toUpperCase().contains(LOG_MODE_DISABLED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(WRONG_COMMAND)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_EN12830_COMMANDS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_REGULAR_DATALOGGER_COMMANDS)) {
            disconnectDevice();
        }
    }

    private void onLogDLRst(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onLogDLRst: " + value);
        if (noneEN12830(value)) {
            solveNoneEN12830(value);
        }
        else if (value.toUpperCase().contains(END_OF_DATA)) {
            writeToCharacteristic(gatt, LOG_RST_COMMAND);
            if (mLogRstDatetime == null) {
                calculateLogDatetime(mScanResult.getDevice());
            }
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext,AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(RESET_LOG_DATA)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(LOG_NOT_STARTED)) {
            writeToCharacteristic(gatt, LOG_RST_COMMAND);
        }
        else if (value.toUpperCase().contains(LOG_MODE_DISABLED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(WRONG_COMMAND)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_EN12830_COMMANDS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_REGULAR_DATALOGGER_COMMANDS)) {
            disconnectDevice();
        }
    }

    private void onLogSpInvDL(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onLogSpInvDL: " + value);
        if (noneEN12830(value)) {
            if (!mDataLoggerSentinel) {
                solveNoneEN12830(value);
            }
        }
        else if (value.toUpperCase().contains(END_OF_DATA)) {
            disconnectDevice();
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext, AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(RESET_LOG_DATA)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(DATA_START)) {
            if (mDataLoggerSentinel) {
                mDataLoggerSentinel = false;
                writeToCharacteristic(gatt, ZERO_07_COMMAND);
            }
        }
        else if (ZERO_07_RESPONSE.equals(Utils.toHexadecimalString(characteristic.getValue()))) {
            Log.d(TAG, "onLogSpInvDL: " + Utils.toHexadecimalString(characteristic.getValue()));
            String command = LOG_SP_INV_DL_COMMAND + " 00 4000";
            writeToCharacteristic(gatt, command);
        }
        else if (value.toUpperCase().contains(LOG_NOT_STARTED)) {
            writeToCharacteristic(gatt, LOG_RST_COMMAND);
        }
        else if (value.toUpperCase().contains(LOG_MODE_DISABLED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(WRONG_COMMAND)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_EN12830_COMMANDS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_REGULAR_DATALOGGER_COMMANDS)) {
            disconnectDevice();
        }
    }

    private void onLogSpInvDLRst(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onLogSpInvDLRst: " + value);
        if (noneEN12830(value)) {
            if (!mDataLoggerSentinel) {
                solveNoneEN12830(value);
            }
        }
        else if (value.toUpperCase().contains(END_OF_DATA)) {
            writeToCharacteristic(gatt, LOG_RST_COMMAND);
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext, AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(RESET_LOG_DATA)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(DATA_START)) {
            if (mDataLoggerSentinel) {
                mDataLoggerSentinel = false;
                writeToCharacteristic(gatt, ZERO_07_COMMAND);
            }
        }
        else if (ZERO_07_RESPONSE.equals(Utils.toHexadecimalString(characteristic.getValue()))) {
            // Log.d(TAG, Utils.toHexadecimalString(characteristic.getValue()));
            String command = LOG_SP_INV_DL_COMMAND + " 00 4000";
            writeToCharacteristic(gatt, command);
        }
        else if (value.toUpperCase().contains(LOG_NOT_STARTED)) {
            writeToCharacteristic(gatt, LOG_RST_COMMAND);
        }
        else if (value.toUpperCase().contains(LOG_MODE_DISABLED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(WRONG_COMMAND)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_EN12830_COMMANDS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_REGULAR_DATALOGGER_COMMANDS)) {
            disconnectDevice();
        }
    }

    private void onEn12830LogRst(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onEn12830LogRst: " + value);
        if (value.toUpperCase().contains(DATALOGGER_START_SUCCESS)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
        }
        disconnectDevice();
    }

    private void onEn12830LogDL(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onEn12830LogDL: " + value);
        if (EN12830(value)) {
            solveEN12830(value);
        }
        else if (value.toUpperCase().contains(DOWNLOAD_END)) {
            disconnectDevice();
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext,AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(DATALOGGER_START_SUCCESS)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
            disconnectDevice();
        }else if (value.toUpperCase().contains(READ_DATA_LOG_NOT_STARTED)) {
            writeToCharacteristic(gatt, DATALOGGER_START_COMMAND + " " + EN12830_DEFAULT_PWD + " " + zdtlf.format(ZonedDateTime.now()));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext,AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(READ_DATA_ACCESS_DENIED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(DATALOGGER_START_ACCESS_DENIED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(DATALOGGER_START_WRONG_PARAMETERS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(LOG_MODE_DISABLED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(WRONG_COMMAND)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_EN12830_COMMANDS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_REGULAR_DATALOGGER_COMMANDS)) {
            disconnectDevice();
        }
    }

    private void onEn12830LogDLRst(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, "onEn12830LogDLRst: " + value);
        if (EN12830(value)) {
            solveEN12830(value);
        }
        else if (value.toUpperCase().contains(DOWNLOAD_END)) {
            writeToCharacteristic(gatt, DATALOGGER_START_COMMAND + " " + EN12830_DEFAULT_PWD + " " + zdtlf.format(ZonedDateTime.now()));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext,AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(DATALOGGER_START_SUCCESS)) {
            String logRstDateTime = zdtf.format(ZonedDateTime.now().withSecond(0));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.saveLogRstDateTime(mContext, AKReporter.createLogRstDateTimeReport(mContext, mDataLogger.getTagId(), logRstDateTime)));
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(READ_DATA_LOG_NOT_STARTED)) {
            writeToCharacteristic(gatt, DATALOGGER_START_COMMAND + " " + EN12830_DEFAULT_PWD + " " + zdtlf.format(ZonedDateTime.now()));
            Executors.newSingleThreadExecutor().execute(() -> AKRequest.postDataLogs(mContext,AKReporter.createDataLogsReport(mContext, mScanResult.getDevice())));
        }
        else if (value.toUpperCase().contains(READ_DATA_ACCESS_DENIED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(DATALOGGER_START_ACCESS_DENIED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(DATALOGGER_START_WRONG_PARAMETERS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(LOG_MODE_DISABLED)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(WRONG_COMMAND)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_EN12830_COMMANDS)) {
            disconnectDevice();
        }
        else if (value.toUpperCase().contains(USE_REGULAR_DATALOGGER_COMMANDS)) {
            disconnectDevice();
        }
    }

    private void onWriteDatetime(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // String value = Utils.toHexadecimalString(characteristic.getValue());
        // Log.d(TAG, value);
        frameIndex += 1;
        if (frameIndex < frames.size()) {
            writeBytesToCharacteristic(gatt, frames.get(frameIndex));
        } else {
            disconnectDevice();
        }
    }

    private void onLogInterval(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String value = Utils.toHexadecimalString(characteristic.getValue());
        if (value.contains(LOG_INTERVAL_HEADER)) {
            String logIntervalInverse = value.substring(value.length() - 8);
            StringBuilder logIntervalHex = new StringBuilder();
            for (int i = 4; i > 0; i--) {
                logIntervalHex.append(logIntervalInverse.substring(logIntervalInverse.length() - 2));
                logIntervalInverse = logIntervalInverse.substring(0, logIntervalInverse.length() - 2);
            }
            logInterval = Long.parseLong(logIntervalHex.toString(), 16); // in ms
        }
        disconnectDevice();
    }

    public ZonedDateTime getLogRstDateTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        if (dateTime.isEmpty()) {
            return null;
        }
        dateTime = dateTime.substring(0, 16) + " " + DateTimeFormatter.ofPattern("XXXXX").format(ZonedDateTime.now());
        ZonedDateTime logRstDateTime = null;
        try { logRstDateTime = ZonedDateTime.parse(dateTime, zdtf); } catch (DateTimeParseException ignored) { }
        return logRstDateTime;
    }

    public boolean EN12830(String log) {
        Pattern pattern = Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d\\s[+,-]*\\d\\d:\\d\\d:\\s[-]*\\d*\\.\\d*");
        Matcher matcher = pattern.matcher(log);
        return matcher.find();
    }

    public void solveEN12830(String log) {
        String[] data = log.split(" ");
        String[] logDate = data[0].split("/");
        String[] logTime = data[1].split(":");
        String value = logDate[2] + "-" + logDate[1] + "-" + logDate[0] + " " + logTime[0] + ":" + logTime[1] + " " + data[2] + " " + data[3];
        Log.d(TAG, "onSolveEN12830: " + value);
        MyOty.getInstance().addToDataLogs(mScanResult, value);
    }

    public boolean noneEN12830(String log) {
        Pattern pattern = Pattern.compile("\\d*d\\d*h\\d*m\\d*s:-*\\d*");
        Matcher matcher = pattern.matcher(log);
        return matcher.find();
    }

    public void solveNoneEN12830(String log) {
        String[] data = log.split(":");
        String[] logTime = data[0].split("[dhms]");
        ZonedDateTime logDatetime = ZonedDateTime.from(mLogActDatetime);
        if (mLogRstDatetime != null) {
            logDatetime = ZonedDateTime.from(mLogRstDatetime);
            logDatetime = logDatetime.withSecond(0);
            logDatetime = logDatetime.plusDays(Integer.parseInt(logTime[0]));
            logDatetime = logDatetime.plusHours(Integer.parseInt(logTime[1]));
            logDatetime = logDatetime.plusMinutes(Integer.parseInt(logTime[2]));
            logDatetime = logDatetime.plusSeconds(Integer.parseInt(logTime[3]));
        } else {
            logDatetime = logDatetime.withSecond(0);
            logDatetime = logDatetime.minusDays(Integer.parseInt(logTime[0]));
            logDatetime = logDatetime.minusHours(Integer.parseInt(logTime[1]));
            logDatetime = logDatetime.minusMinutes(Integer.parseInt(logTime[2]));
            logDatetime = logDatetime.minusSeconds(Integer.parseInt(logTime[3]));
        }
        String value = zdtf.format(logDatetime) + ": " + (Integer.parseInt(data[1].replaceAll("[\\n\\t ]", "")) / 100.0);
        Log.d(TAG, "onSolveNoneEN12830: " + value);
        MyOty.getInstance().addToDataLogs(mScanResult, value);
    }

    private void calculateLogDatetime(BluetoothDevice device) {
        JSONObject dataLog = MyOty.getInstance().getFromDataLogs(device.getAddress());
        try {
            int categoryId = dataLog.getInt("categ_id");
            JSONArray logs = dataLog.getJSONArray("logs");
            JSONArray reverseLogs = new JSONArray();
            ZonedDateTime logDatetime = ZonedDateTime.from(mLogActDatetime);
            logDatetime = logDatetime.withSecond(0);
            logInterval = ElaTag.getDataLoggerInterval(dataLog);
            Log.d(TAG, "onCalculateLogInterval ms: " + logInterval);
            logInterval = logInterval / 1000;
            Log.d(TAG, "onCalculateLogInterval s: " + logInterval);
            int size = logs.length();
            if (size > 1) {
                if (ElaTag.CATEGORY_ID_RHT == categoryId) {
                    logDatetime = logDatetime.minusSeconds((logInterval * size / 2));
                    for (int i = 0; i < size; i += 2) {
                        logDatetime = logDatetime.plusSeconds(logInterval);
                        Log.d(TAG, "onCalculateLogDatetime origin: " + logs.getString(i));
                        String[] dataT = logs.getString(i).split(": ");
                        String logT = zdtf.format(logDatetime) + ": " + dataT[1];
                        reverseLogs.put(logT);
                        Log.d(TAG, "onCalculateLogDatetime: " + logT);
                        Log.d(TAG, "onCalculateLogDatetime origin: " + logs.getString(i + 1));
                        String[] dataH = logs.getString(i + 1).split(": ");
                        String logH = zdtf.format(logDatetime) + ": " + dataH[1];
                        reverseLogs.put(logH);
                        Log.d(TAG, "onCalculateLogDatetime: " + logH);
                    }
                }
                if (ElaTag.CATEGORY_ID_T == categoryId) {
                    logDatetime = logDatetime.minusSeconds((logInterval * size));
                    for (int i = 0; i < size; i++) {
                        logDatetime = logDatetime.plusSeconds(logInterval);
                        Log.d(TAG, "onCalculateLogDatetime origin: " + logs.getString(i));
                        String[] data = logs.getString(i).split(": ");
                        String log = zdtf.format(logDatetime) + ": " + data[1];
                        reverseLogs.put(log);
                        Log.d(TAG, "onCalculateLogDatetime: " + log);
                    }
                }
            }
            MyOty.getInstance().addToDataLogs(mScanResult, reverseLogs);
        } catch (JSONException ignored) { }
    }
}
