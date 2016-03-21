
package com.example.astaScreens;

import com.example.R;
import com.example.Listeners.Logger;
import com.example.Task.Validator;
import com.example.data.Constants;
import com.example.data.Paths;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import androidLoggerz.AndroidLoggerz;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * main menu class. inflate a list adapter and show menu
 */
public class MainMenuActivity extends ListActivity {
    private static final String SPEED_TEST_CHOOSER = "Speed Test";
    private static final String SETTINGS = "Settings";
    private static final String LAST_RESULTS = "Results";
    private static final String APPLICATION_TITLE = "ASTA Speed Test";
    private static final String GET_APP_FILES = "Get Application Files";
    private static final String MANAGE_SERVERS = "Manage Servers";
    private static final String APP_STARTED_MESSAGE =
            "------------------- App started ------------------";
    private static final String TAG = "MainActivity";
    /**
     * entry method for application.
     * initialize special settings
     *
     * @param bundle no data inside.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // 1. pass context and data to the custom adapter
        MainListActivityAdapter adapter = new MainListActivityAdapter(this, generateData());
        // if extending Activity: 2. Get ListView from activity_main.xml
        //listView.setAdapter(adapter); if extending Activity
        // add header
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.header, lv, false);
        lv.addHeaderView(header, null, false);
        // 3. setListAdapter
        setListAdapter(adapter);
        initializeDeviceHeirarchy();
        // create and initialize logger:
        AndroidLoggerz m_oLogger = AndroidLoggerz.getInstance();
        // specify logs folder
        m_oLogger.init(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_SYSTEM_FOLDER, "debugLog.html");
        Logger.write(APP_STARTED_MESSAGE, this);
        /* clean results history: */
            Intent cleanResults = new Intent(this, OldFilesCleanerService.class);
            startService(cleanResults);

        // set version at footer:
        //d setVersionNameOnFooter(lv, inflater);
    }

    private void setVersionNameOnFooter(ListView lv, LayoutInflater inflater) {
        PackageInfo pInfo = null;
        String version = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e){
            Logger.error("package not found " + e.getMessage(), this);
        }
        if (pInfo != null)
            version = " Application version: " + pInfo.versionName;
        View footer = inflater.inflate(R.layout.footer, lv, false);
        TextView l_oVersion = (TextView) footer.findViewById(R.id.version_text);
        lv.addFooterView(footer, null, false);
        l_oVersion.setText(version);
        //d Logger.write("Application version: " + version, this);
    }
    /**
     * initialize device hierarchy files and folders
     */
    private void initializeDeviceHeirarchy() {
        boolean isFirstRunning = false;
        // main folder
        File astaFolder = new File(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_MAIN_FOLDER_NAME);
        if (!astaFolder.exists()) {
            isFirstRunning = true;
            /*Toast.makeText(this, "First Running. please " +
                    "downloading application files", Toast.LENGTH_SHORT).show();
                    */
            astaFolder.mkdir();
        }
        // results folder
        File l_oResultsDirectory = new File(Environment
                .getExternalStorageDirectory() +
                Paths.INTERNAL_RESULTS_DATA);
        if (!l_oResultsDirectory.exists()) {
            l_oResultsDirectory.mkdirs();
            Log.d(TAG, "Results folder doesn't exist");
        }
        // downloads folder
        File l_oDownloadsDirectory = new File(Environment
                .getExternalStorageDirectory() +
                Paths.INTERNAL_DOWNLOAD_FOLDER);
        if (!l_oDownloadsDirectory.exists()) {
            l_oDownloadsDirectory.mkdirs();
            Log.d(TAG, "downloads folder doesn't exist");
        }
        // if folder isn't exist, create it. else - get all files from directory.
        File l_oUploadsDirectory = new File(Environment
                .getExternalStorageDirectory() +
                Paths.INTERNAL_UPLOADS_FOLDER);
        if (!l_oUploadsDirectory.exists()) {
            isFirstRunning = true;
            l_oUploadsDirectory.mkdirs();
            Log.d(TAG, "uploads folder doesn't exist");
        }
        // servers folder:
        File l_oServersDirectory = new File(Environment
                .getExternalStorageDirectory()
                .getPath()
                + Paths.INTERNAL_MAIN_FOLDER + Paths.DEVICE_FTP_SERVERS_FOLDER);
        if (!l_oServersDirectory.exists() || (l_oServersDirectory.listFiles()
                .length <= 0)) {
            isFirstRunning = true;
            l_oServersDirectory.mkdirs();
            Log.d(TAG, "servers folder / files doesn't exist");
        }
        if (isFirstRunning) {
            openFirstRunningDialog();
        }
        ;
    }

    private void openFirstRunningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("First Time Running");
        builder.setMessage("The application recognized some files are " +
                "missing, please press on "
                 + GET_APP_FILES + " button to get the files");
        builder.setPositiveButton(GET_APP_FILES, new DialogInterface.OnClickListener
                () {
            public void onClick(DialogInterface dialog, int which) {
                new downloadDeviceFiles(MainMenuActivity.this).execute("");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * generate items for menu.
     *
     * @return menu model
     */
    private ArrayList<MenuModel> generateData() {
        ArrayList<MenuModel> models = new ArrayList<MenuModel>();
        //models.add(new MenuModel(APPLICATION_TITLE));
        models.add(new MenuModel(R.drawable.action_help, SPEED_TEST_CHOOSER));
        //d models.add(new MenuModel(R.drawable.get, GET_APP_FILES));
        models.add(new MenuModel(R.drawable.task_icon, MANAGE_SERVERS));
        models.add(new MenuModel(R.drawable.results_icon, LAST_RESULTS));
        models.add(new MenuModel(R.drawable.settings_icon, SETTINGS));
        return models;
    }

    /**
     * method that being called when menu item is clicked
     *
     * @param l        listView
     * @param v        item view
     * @param position in list
     * @param id       id of item
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        // switch items
        switch (position) {
            case 1: //speed test screen
                Intent speedTestChooser = new Intent(MainMenuActivity.this, SpeedTestTypeChooser.class);
                startActivity(speedTestChooser);
                break;
            case 2: //manage servers
                Intent manageServersActivity = new Intent(MainMenuActivity
                        .this,
                        ManageServersActivity.class);
                startActivity(manageServersActivity);
                break;
            case 3: // last results
                Intent lastResults = new Intent(MainMenuActivity.this,
                        LastResultsActivity.class);
                startActivity(lastResults);
                break;
            case 4: //Settings
                Intent settings = new Intent(MainMenuActivity.this,
                        SettingsActivity.class);
                startActivity(settings);
                break;

                /*
                Intent deviceUploadFilesRetriever = new Intent(MainActivity
                        .this,
                        DownloadDeviceFiles.class);
                startService(deviceUploadFilesRetriever);
                ?
                new downloadDeviceFiles(this).execute("");
                break;
                */

        }
    }


    /**
     * Async Task Class in order to manage "getApplicationFiles" item.
     * this class download application files in seperate process.
     * it shows a progress dialog while the download is running.
     */
    class downloadDeviceFiles extends AsyncTask<String, Integer, String> {
        ProgressDialog m_oProgressDialog;
        private boolean isErrorAccoured = false;
        private boolean isProcessRunning = false;
        private int SIZES = 193;
        private int lastFileSize = 0;
        private Context cnt;

        downloadDeviceFiles(Context context)        {
            cnt=context;
        }
        /**
         * show progress bar and initialize it
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_oProgressDialog = new ProgressDialog(cnt);
            m_oProgressDialog.setMessage("Downloading Device Files");
            m_oProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            m_oProgressDialog.setMax(100);
            m_oProgressDialog.setCancelable(true);
            m_oProgressDialog.setProgress(0);
            m_oProgressDialog.show();
        }

        /**
         * when we publish results, this method is being called
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values[0]);
            m_oProgressDialog.setProgress(values[0]);
        }

        /**
         * background task to download application files.
         * @param f_url none
         * @return m_sStatus
         */
        @Override
        protected String doInBackground(String... f_url) {
            if (Validator.isNetworkAvailable(MainMenuActivity.this) && !isProcessRunning) {
                isProcessRunning = true;
                publishProgress(5);
                int tmpProgress = 0;
                //Handler m_oHandler = new Handler(context.getMainLooper());
                FTPClient l_oFtpClient = null;
                String deviceUploadFolder = Environment.getExternalStorageDirectory().getPath()
                        + Paths.INTERNAL_UPLOADS_FOLDER;
                String deviceServersFolder = Environment.getExternalStorageDirectory()
                        .getPath()
                        + Paths.INTERNAL_MAIN_FOLDER + Paths.DEVICE_FTP_SERVERS_FOLDER;
                try {
                    l_oFtpClient = new FTPClient();
                    l_oFtpClient.connect(Constants.DEF_FTP_IP, 21);
                    //Log.d(TAG, "after connect");
                    //Log.d(LOG_TAG, "Connected. Reply: " + ftp.getReplyString());
                    l_oFtpClient.login(Constants.DEFAULT_FTP_USER, Constants.DEF_FTP_PASSWORD);
                    //Log.d(TAG, "Logged in");
                    l_oFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    //Log.d(TAG, "enter local passive");
                    l_oFtpClient.enterLocalPassiveMode();
                    l_oFtpClient.changeWorkingDirectory(Paths.SERVER_DOWNLOADS_FOLDER);
                    //Log.d(TAG, "working directory changed");
                    OutputStream outputStream = null;
                    boolean success = false;
                    try {
                        String[] fileNames = l_oFtpClient.listNames();
                        for (final String fName : fileNames) {
                            if (fName.contains("MB")) {
                                lastFileSize += Integer.parseInt(fName.substring
                                        (0, fName.length
                                                () - 2).trim());
                            } else {
                                lastFileSize += 1;
                            }
                            Log.d(TAG, "get file: " + fName + "Size: " + lastFileSize);
                            File curDownloadFile = new File(deviceUploadFolder + fName);
                            File curServerFile = new File(deviceServersFolder + "/" +
                                    fName);
                            if (!curDownloadFile.exists() && !curServerFile.exists()) {
                                if (fName.contains("MB")) {

                                    outputStream = new BufferedOutputStream(new FileOutputStream(
                                            deviceUploadFolder + "/"
                                                    + fName));
                                    //success = l_oFtpClient.retrieveFile(fName, outputStream);
                                    outputStream.flush();
                                    outputStream.close();

                                } else { //server file
                                    outputStream = new BufferedOutputStream(new FileOutputStream(
                                            deviceServersFolder + "/"
                                                    + fName));
                                    success = l_oFtpClient.retrieveFile(fName, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                }
                            } else {
                        /*
                        m_oHandler.post(new DisplayToast(this,
                                fName + " Exist"));*/
                            }
                            publishProgress((int) ((lastFileSize * 100) / SIZES));
                        }
                        // success:
                        return ("Device is ready to use");
                    } finally {
                        //lastFileSize = SIZES;
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } catch (SocketException e) {
                /*Toast.makeText(MainActivity.this, "Network Error. download " +
                        "process aborted.", Toast
                        .LENGTH_SHORT).show();*/
                    isErrorAccoured = true;
                    e.printStackTrace();
                    return ("Network Error. download process aborted.");
                } catch (IOException e) {
                /*Toast.makeText(MainActivity.this, "IO Error. download " +
                        "process aborted.", Toast
                        .LENGTH_SHORT).show();*/
                    isErrorAccoured = true;
                    e.printStackTrace();
                    return ("IO Error. download process aborted.");
                } finally {
                    if (l_oFtpClient != null) {
                        try {
                            l_oFtpClient.logout();
                            l_oFtpClient.disconnect();
                        } catch (IOException e) {
                            isErrorAccoured = true;
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Logger.error("No Network, can't download device files",
                        MainMenuActivity.this);
                publishProgress((int) (SIZES));
                isErrorAccoured = true;
                return ("No Network, Please connect and try again");
            }

        }

        /**
         * dismiss progress bar when task is finished.
         * inform the user if any error accoured.
         * @param status
         */
        @Override
        protected void onPostExecute(String status) {
            isProcessRunning = false;
            if (isErrorAccoured) {
                m_oProgressDialog.setMessage("Download was canceled");
            } else {
                m_oProgressDialog.setMessage("Device is ready");
            }
            if (m_oProgressDialog.isShowing()) {
                m_oProgressDialog.dismiss();
            }
            Toast.makeText(MainMenuActivity.this, status, Toast
                    .LENGTH_SHORT).show();
        }
    }
}
