package com.example.astaScreens;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.data.Constants;
import com.example.data.Paths;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * class that enable us to clean application files.
 * we use a sharedPreference object to get application "history"
 * value. this value define the file we need to delete.
 * every file that have been changed before the specified time
 * will be deleted.
 *
 */
public class OldFilesCleanerService extends IntentService  {
    private static final String TAG = "OldFilesCleanerService";
    File m_oResultsDirectory; // dir home folder
    File m_oDownloadsDirectory; // dir home folder
    /*
     * default Constructor should be empty
     */
    public OldFilesCleanerService() {
        super("deleteOldFiles");
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Cleaned Device Results ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        // import preferences for history saving:
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Constants.historyResultsSaving = Integer.parseInt(
                sharedPref.getString("test_history_title", "7"));
        Log.d(TAG, "deleting old files");
        // get time
        Calendar time = Calendar.getInstance();
        long purgeTime = System.currentTimeMillis()
                - (Constants.historyResultsSaving * 24 * 60 * 60 * 1000);
        // clean files
        cleanDownloadsFolder(purgeTime);
        cleanResultsFolder(purgeTime);
    }

    /**
     * method to clean device results folder
     * @param purgeTime clean all files that have been
     * changed before this time.
     */
    public void cleanResultsFolder(long purgeTime) {
        // if folder isn't exist, create it. else - get all files from directory.
        m_oResultsDirectory = new File(Environment.getExternalStorageDirectory() +
                Paths.INTERNAL_RESULTS_DATA);
        int newFilesCounter = 0;
        if (!m_oResultsDirectory.exists()) {
            //m_oResultsDirectory.mkdirs();
            Log.d(TAG, "Results folder doesn't exist");
        } else {
            ArrayList<File> resultsFiles = getAllFilesInDir(m_oResultsDirectory);
            if (resultsFiles != null) {
                for (File file : resultsFiles) {
                    //I store the required attributes here and delete them
                    if (file.lastModified() < purgeTime) {
                        // if it's not the summary file: delete it:
                        if (!file.getName().toString().startsWith(Paths
                                .SUMMARY_FILE_NAME)) {
                            file.delete();
                        }
                    } else {
                        newFilesCounter++;
                    }
                }
            }
        }
        // delete summary file and make a new one:

        try {
            String mFolder = Environment.getExternalStorageDirectory()
                    + Paths.INTERNAL_RESULTS_DATA;
            File fromFile = new File(mFolder + Paths.SUMMARY_FILE_NAME);
            if (fromFile.exists()) {
                File tmpFile = new File(mFolder + "tmp.csv");
                CSVReader l_oReader = new CSVReader(new FileReader(fromFile));
                String[] l_sLine = null;
                CSVWriter l_oSummaryWriter = new CSVWriter(
                        new FileWriter(tmpFile, true));
                int counter = 0;
                do {
                    l_sLine = l_oReader.readNext();
                    l_oSummaryWriter.writeNext(l_sLine);
                    counter++;
                } while (l_sLine != null && counter < newFilesCounter);
                // delete tmp file
                l_oSummaryWriter.close();
                fromFile.delete();
                tmpFile.renameTo(new File(mFolder + Paths.SUMMARY_FILE_NAME));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to clean device downloads folder
     * @param purgeTime clean all files that have been
     * changed before this time.
     */
    public void cleanDownloadsFolder(long purgeTime) {
        m_oDownloadsDirectory = new File(Environment
                .getExternalStorageDirectory() +
                Paths.INTERNAL_DOWNLOAD_FOLDER);
        if (!m_oDownloadsDirectory.exists()) {
            //m_oResultsDirectory.mkdirs();
            Log.d(TAG, "download folder doesn't exist");
        } else {
            ArrayList<File> downloadsFiles = getAllFilesInDir(m_oDownloadsDirectory);
            if (downloadsFiles != null) {
                for (File file : downloadsFiles) {
                    //I store the required attributes here and delete them
                    if (file.lastModified() < purgeTime) {
                        file.delete();
                    }
                }
            }
        }
    }
    // get all files in a dir:
    public static ArrayList<File> getAllFilesInDir(File dir) {
        if (dir == null) {
            return null;
        }
        // create an array of files:
        ArrayList<File> files = new ArrayList<File>();
        Stack<File> dirlist = new Stack<File>();
        dirlist.clear();
        dirlist.push(dir);
        // insert all files in sub folders to a list.
        while (!dirlist.isEmpty()) {
            File dirCurrent = dirlist.pop();
            File[] fileList = dirCurrent.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory())
                    dirlist.push(aFileList);
                else
                    files.add(aFileList);
            }
        }
        return files;
    }
}
