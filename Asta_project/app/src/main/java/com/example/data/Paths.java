package com.example.data;


/**
 * Paths class.
 * contain all application paths.
 */
public final class Paths {
    // internal folders:
    public final static String INTERNAL_MAIN_FOLDER_NAME = "/ASTA";
    public final static String INTERNAL_MAIN_FOLDER =
            INTERNAL_MAIN_FOLDER_NAME +"/";
    public final static String SYSTEM_FOLDER_NAME = "System";
    public final static String INTERNAL_SYSTEM_FOLDER = INTERNAL_MAIN_FOLDER
            + SYSTEM_FOLDER_NAME + "/";

    public final static String INTERNAL_UPLOADS_FOLDER =
            INTERNAL_SYSTEM_FOLDER + "ULFiles/";
    public final static String INTERNAL_DOWNLOAD_FOLDER =
            INTERNAL_SYSTEM_FOLDER + "DLFiles/";
    public final static String INTERNAL_TASKS_FOLDER = INTERNAL_SYSTEM_FOLDER + "Tests";
    public final static String TASK_FOLDER = "Test";
    public final static String DEVICE_FTP_SERVERS_FOLDER_NAME = "Servers";
    public final static String DEVICE_FTP_SERVERS_FOLDER = "System/" + DEVICE_FTP_SERVERS_FOLDER_NAME;
    public final static String DEBUG_LOGS = INTERNAL_SYSTEM_FOLDER; // logs from logger.
    public static final String INTERNAL_RESULTS_DATA = INTERNAL_MAIN_FOLDER +
            "Results/";
    // external (FTP SERVERS) folders:
    public final static String SERVER_MAIN_FOLDER = "/ASTA/";
    public final static String SERVER_MAIN_FOLDER_VALIDATION =
            "ASTA/";
    public final static String SERVER_DOWNLOADS_FOLDER =
            SERVER_MAIN_FOLDER + "downloadForClient/";
    public final static String SERVER_UPLOAD_FOLDER =
            SERVER_MAIN_FOLDER + "uploadsFromClient/";

    // old files cleaner:
    public final static String SUMMARY_FILE_NAME = "Summary.csv";

    // debug log:
    public static final String DEBUG_LOG_FILE = "debugLog.html";
}

