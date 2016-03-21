package com.example.Task;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.example.Listeners.Logger;
import com.example.data.Constants;
import com.example.data.Paths;
import com.example.astaScreens.DisplayToast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class Validator {
    private static final String TAG = "TaskValidator";
    private Handler m_oHandler;
    private Context m_oContext;
    private String m_sResultMessage;

    public Validator(Context context, Handler handler) {
        m_oHandler = handler;
        m_oContext = context;
    }
    /**
     * validate active internet connection.
     * @return true / false if we have internet access.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) m_oContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * validate active internet connection.
     * @return true / false if we have internet access.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private boolean isConnectivityEqualTaskSettings(Task taskToValidate) {
        ConnectivityManager connManager
                = (ConnectivityManager) m_oContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()
                && taskToValidate.getNetworkType() == Constants.CELLULAR_NETWORK) {
            m_oHandler.post(new DisplayToast(m_oContext,
                    "Notice! Wifi is Connected in Network Test!!"));
            return false;
        } else if (!mWifi.isConnected() && taskToValidate.getNetworkType() == Constants.WIFI) {
            m_oHandler.post(new DisplayToast(m_oContext,
                    "Notice Wifi isn't Connected  in a Wifi Test!!"));
            return false;
        }
        return true; // test type equals connectivity
    }


    /**
     * validate device data and
     * fix it.
     * if folders doesn't exist, create them.
     * @return true
     */
    private boolean validateDeviceData(Task task) {
        File fieldTestFolder = new File(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_MAIN_FOLDER);
        /* if folder isn't exist: create folder, create server, create default task.
        then, save default task and server to this folder*/
        if (!fieldTestFolder.exists()) {
            fieldTestFolder.mkdir();
        }
        File downloadsFolder = new File(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_DOWNLOAD_FOLDER);
        File uploadFolder = new File(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_UPLOADS_FOLDER);
        if (!downloadsFolder.exists() || !uploadFolder.exists()) {
            downloadsFolder.mkdir();
            uploadFolder.mkdir();
        }
        return true;
    }

    public boolean validateByServerDetails(Server server) {
        // no server in spinner:
        if (server == null) {
            return false;
        }
        FTPClient l_oFtpClient = new FTPClient();
        try {
            //Logger.write("trying to connect to server: " + server.getServerIp(), this);
            l_oFtpClient.connect(server.getServerIp(), server.getPort());
            Log.d(TAG, "connected");
            if (!l_oFtpClient.login(server.getUserName(), server.getPassword
                    ())) {
                l_oFtpClient.disconnect();
                m_sResultMessage = "FTP Login Error";
                Logger.write("Login Error error while validating",
                 m_oContext);
                return false;
            }
            Log.d(TAG, "login");
            // user, password
            //Logger.write("logined to server", this);
            l_oFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //Logger.write("set file type", this);
            l_oFtpClient.enterLocalPassiveMode();
            //Logger.write("entered local passive m_sMode to server", this);
            Log.d(TAG, l_oFtpClient.printWorkingDirectory());
            boolean isFolderExist = l_oFtpClient.changeWorkingDirectory(
                    Paths.SERVER_MAIN_FOLDER);
            Log.d(TAG, l_oFtpClient.printWorkingDirectory());
            //dialog.setMessage("Validate server files");
            if (!isFolderExist) {
                Logger.error("couldn't change working directory", m_oContext);
                l_oFtpClient.makeDirectory(Paths.SERVER_DOWNLOADS_FOLDER);
                // create download and upload folders:
                l_oFtpClient.makeDirectory(Paths.SERVER_DOWNLOADS_FOLDER);
                l_oFtpClient.makeDirectory(Paths.SERVER_UPLOAD_FOLDER);
                m_sResultMessage = "couldn't change" +
                        " working directory";
                return false;
            }
        } catch (FTPConnectionClosedException e) {
            m_sResultMessage = "Server connection is closed! (check server)";
            Logger.write("Server connection is closed", m_oContext);
            return false;
        } catch (SocketException e) {
            Logger.write("SocketException error while validating", m_oContext);
            /*m_oHandler.post(new DisplayToast(m_oContext, "SocketException " +
                    "validation error"));*/
            m_sResultMessage = "FTP files missing (Get Application Files)";
            return false;
        } catch (IOException e) {
            m_sResultMessage = "Connection Error (try again)";
            Logger.write("IOException error while validating", m_oContext);
            return false;
        }
        Logger.write("validation completed succesfully", m_oContext);
        return true;
    }

    /**
     * validate files in server
     * @param t task details
     * @return true if task is valid, otherwise false
     */
    private boolean validateFtpData(Task t) {
        Server server = t.getServer();
        return (validateByServerDetails(server));
    }

    /**
     * validate task and it's details
     * @param taskToValidate task
     * @return m_sStatus of task
     */
    protected boolean validate(Task taskToValidate) {
        Log.d(TAG, "validate()");
        boolean l_bValidationResult = false;
        // check if connectivity settings:
        if (!isConnectivityEqualTaskSettings(taskToValidate)) {
            m_sResultMessage = Constants.NETWORK_WARNING;
            return false;
        }
        // validated internet connection:
        if (!isNetworkAvailable()) {
            Logger.error("no network connection", m_oContext);
            m_sResultMessage =  Constants.NO_NETWORK;
        } else {
            // check if device data exist and ready to be uploaded
            boolean validationStatus = validateDeviceData(taskToValidate);
            // if upload data isn't valid:
            if (!validationStatus) {
                Log.d(TAG, "Server data missing");
                m_sResultMessage =  "Server data files are missing";
            } else {
                // validate folders in ftp server:
                validationStatus = validateFtpData(taskToValidate);
                if (!validationStatus) {
                    Log.d(TAG, "FTP files missing");
                    m_sResultMessage = "Server data files are missing";
                } else {
                    int limitation = taskToValidate.getLimitationQuantity();
                    if (limitation <= 0 || limitation >= 1000) {
                        Log.d(TAG, "FTP files missing");
                        m_sResultMessage = "Illegal limitation";
                        l_bValidationResult = false;
                    }
                }

            }
            if (validationStatus) {
                Log.d(TAG, "Success");
                m_sResultMessage = "Success";
                return true;
            }
        }
        return l_bValidationResult;
    }
    public String getResult() {
        return m_sResultMessage;
    }
}