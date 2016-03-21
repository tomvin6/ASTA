package com.example.data;




/*
 * project constants class
 */
public final class Constants {
    // general info:
    public static final int TCP = 1;
    public static final int UDP = 2;
    public static final int DN_MODE = 3;
    public static final int UP_MODE = 4;
    public static final String MODE_FTP_NTT = "m_sMode";
    public static final int DN_UP_MODE = 5;
    public static final int TIME_LIMITATION = 6;
    public static final int DATA_LIMITATION = 7;
    public static final int ARRAY_SIZE = 4096; // socket array (reading/writing)
    // strings for messaging:
    public static final String FTP_MODE = "FTP";
    public static final String NTT_MODE = "NTT";
    public static final String MODE = "Mode";
    public static final double MILION = 1048576.0;
    public static final float FLOAT_MILION = (float)1048576.0;
    public static final String ACTION = "testStatusAction";
    public static final String STATUS = "m_sStatus";
    public static final String ITERATION = "iteration";
    public static final String DN_DONE = "DL Done";
    public static final String UP_DONE = "UL Done";
    public static final String DOWNLOAD = "Download";
    public static final String UPLOAD = "Upload";
    public static final String JOB_DONE = "Job Done";
    public static final String JOB_CANCEL = "Job Canceled";
    public static final String JOB_FAILED = "Failed";
    public static final String RUNNING = "Running";
    public static final String HOLD = "On-Hold";
    public static final String UP_THROUGHPUT = "up";
    public static final String DN_THROUGHPUT = "dn";
    public static final String SENT = "sent";
    public static final String RECEIVED = "received";
    public static final String NONE = "0";
    public static final float NONE_VALUE = (float) 0.0;
    public static final String VALIDATION = "val";
    public static final int minuteTime = 60;
    public static final long miliToSecondsConvertor = 1000000000;
    public static final int UpdateFrequency = 1000; // frequency to update gui

    // task activity screen:
    public static final String TestAlreadyRunning = "Another test is already running";
    // graph update screens:
    public static final String TIME = "time";
    public static final String AVG_DN_RATE = "rate";
    public static final String CURRENT_DN_RATE = "cdr";
    public static final String CURRENT_UP_RATE = "cur";
    public static final String AVG_UP_RATE = "aur";
    // test m_sStatus screen:
    public static final String ACTION_STOP = "stopA";
    public static final String ITERATION_BY_KEY = "key";
    public static final String IS_PRESSED = "is_pressed";
    public static final String NEXT_ITER_BUTTON = "next_iter";
    public static final String DN_TITLE = "DL Rate";
    public static final String UP_TITLE = "UL Rate";
    public static final String ELAPSED_TIME = "time";
    public static final long LONG_NONE_VALUE = 0;
    public static final String DN_AVG_TITLE = "DL Avg";
    public static final String UP_AVG_TITLE = "UL Avg";
    public static final String TIME_TITLE = "Time (Sec)";
    public static final String DATA_TITLE = "Data (Mb)";
    public static final int NEW_TASK_VALUES = 10; // values that been added after version 1.0
    public static final String NO_TASK_NAME = "Not defined";
    public static String SPEED_UNITS = "KBps";
    public static String TOTAL_UNITS = "MB";
    public static final String KBPS_FORMAT = "%.1f";
    public static final String MBs_FORMAT = "%.2f";
    public static final String TASK_NAME = "name";
    // servers:
    public static final int FTP = 1;
    public static final int NTT = 0;

    // TASK DEFAULT VALUES:
    public static final int DEF_ITERATION = 2;
    public static final int DEF_DL_SESS = 1;
    public static final int DEF_UL_SESS = 1;
    public static final int DEF_BUFFER_SIZE = 8192;
    public static final int DEF_LIM_QUANTITY = 1;

    // FTP DEFAULT VALUES:
    public static final String DEFAULT_FTP_SERVER_NAME = "TestStation";
    public static final String DEFAULT_FTP_USER = "ts_apk";
    public static final String DEF_FTP_IP = "212.199.69.10";
    public static final int DEFAULT_FTP_PORT = 21;
    public static final String DEF_FTP_PASSWORD = "ts_apk";
    public static final String FTP_TYPE = "Ftp Server";

    // NTT DEFAULT VALUES:
    public static final String DEFAULT_NTT_SERVER_NAME = "Netvision";
    public static final String DEFAULT_NTT_USER = "ts_apk";
    public static final String DEF_NTT_IP = "62.90.226.75";
    public static final int DEFAULT_NTT_PORT = 8090;
    public static final String DEF_NTT_PASSWORD = "ts_apk";
    public static final String NTT_TYPE = "Ntt Server";
    public static final String PAUSE = "Paused";
    public static final String TCP_STR = "TCP";
    public static final String UDP_STR = "UDP";
    public static final java.lang.String SERVER = "server";
    public static final String OTHER = "Other";
    public static final String RUN_BUTTON = "runB";
    public static final String TIME_SPINNER = "Time";
    public static final String NO_NETWORK = "No network!";
    public static final String NETWORK_ERROR = "No Network Error";
    public static final String CONNECTION_ERROR = "Connection Error";
    public static final String ERROR = "Error";
    public static final int DEF_DN_SPEED = 100;
    public static final int DEF_UL_SPEED = 100;
    public static String SIZE_SPINNER = "Size";
    // preferences:
    public static int maxGraphPoints = 500; // how much points data will be saved
    public static long historyResultsSaving = 14; //default
    public static long secondInNano = 1000000000;
    public static String UNITS = "units"; // defined in preference.xml
    public static String KBPS = "1"; // defined in preference.xml
    public static String MBs = "2"; // defined in preference.xml
    // Last Results screen / show result
    public static final int MODE_POS = 0;
    public static final int STATUS_POS = 1;
    public static final int DATE_POS = 2;
    public static final int START_TIME_POS = 3;
    public static final int TOTAL_SENT_POS = 4;
    public static final int TOTAL_RECEIVED_POS = 5;
    public static final int AVG_DN_POS = 6;
    public static final int AVG_UP_POS = 7;
    public static final int TOTAL_TEST_TIME_POS = 8;
    public static final int TASK_NAME_POS = 9;
    public static final int TOTAL_VALUES = 10;
    /// validation process:
    public static final String WIFI_CONNECTED = "Wifi Connected";


    // no instantiate
    private Constants() {
        // restrict instantiation
    }

}
