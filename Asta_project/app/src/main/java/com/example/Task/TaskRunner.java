package com.example.Task;

import com.example.Listeners.Logger;
import com.example.data.Constants;
import com.example.data.Paths;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskRunner is a base class for FTP / NTT task runners.
 * it starts a background process for download/ upload task.
 */
public abstract class TaskRunner extends IntentService {
    private static final String TAG = "TaskRunner";
    protected Task m_oCurTask;
    protected ArrayList<Server> m_oFTPServersList;
    protected ArrayList<Server> m_oNttServersList;
    protected GeneralSerializer taskSerializer;
    protected Intent m_oIMessage; //intent for m_sStatus message
    // variable for stopping the process:
    protected static volatile boolean isTaskRunning = false;
    // path for downloading the data to, and uploading the data from.
    protected String m_oLocalDataFolder;
    protected static volatile boolean m_bNeedToCancelFTPAction;
    protected static boolean m_bIsAdvanceByKeyPressed = false;
    protected static boolean m_bIsNextIterationKeyPressedDN = false;
    protected static boolean m_bIsNextIterationKeyPressedUP = false;

    /*
     * Constructor:
     */
    public TaskRunner(String serviceName) {
        super(serviceName);
        // m_oCurTask = task;
        m_oFTPServersList = new ArrayList<Server>();
        m_oNttServersList = new ArrayList<Server>();
        //String that represent the path to the local data folder
        m_oLocalDataFolder
        = new String(Environment.getExternalStorageDirectory().getPath()
                + Paths.INTERNAL_TASKS_FOLDER);
    }
    /**
     * serializer getter
     * @return serializer of FTP/NTT
     * GeneralSerializer
     */
    public GeneralSerializer getTaskSerializer() {
        return taskSerializer;
    }
    // abstract methods that will be implemented inside derived classes:
    abstract List<Server> getServersList();
    abstract void addServers(List<Server> serversList);
    abstract void addServer(Server newServer);
    abstract boolean upload() throws Exception;
    abstract boolean download() throws Exception;
    abstract void runTask();
    abstract boolean uploadAndDownload() throws Exception;


    /**
     * register receiver for  "run" and "next iteration" button.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // register receiver for stopping the service.
        IntentFilter iFilter = new IntentFilter(Constants.ACTION_STOP);
        iFilter.addAction(Constants.ITERATION_BY_KEY);
        iFilter.addAction(Constants.NEXT_ITER_BUTTON);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iFilter);
    }
    /**
     * Desc: stop the process.
     */
    @Override
    public void onDestroy() {
       Log.d(TAG, "TaskRunner-onDestroy started ");
        // un-register receiver (was in ftp task runner)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        // enable run button:
        Intent isJobRunningIntent = new Intent(Constants.ACTION_STOP);  //can put anything in it with putExtra
        isJobRunningIntent.putExtra(Constants.STATUS, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(isJobRunningIntent);
        super.onDestroy();
    }
    private String getTaskDetails() {
        return "Name: " + m_oCurTask.getTaskName()
                + "; ftp?ntt: " + m_oCurTask.getFtpNttMode()
                + "; iter: " + m_oCurTask.getIterations()
                + "; limit quan: " + m_oCurTask.getLimitationQuantity()
                + "; m_sMode: " + m_oCurTask.getMode()
                + "; server: " + m_oCurTask.getServer()
                + "; protocol: " + m_oCurTask.getProtocol();
    }
    /*
     * open a new process for handling the download / upload task.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        m_oCurTask = (Task) intent.getSerializableExtra("task");
        Log.d(TAG, "OnHandleIntent. " + getTaskDetails());
        runTask(); // Test m_sMode will be switched inside
        stopSelf(); // calls onDestroy().
    }

    /*
     * Run task from TaskRunnerActivity
     * intent is the created intent - (TaskRunnerActivity.this, TaskRunner.class)
     */
    public void run(Task taskToRun, Intent intent) {
        m_oCurTask = taskToRun;
    }
    // will be implemented inside derived classes: FTP NTT Runners
    protected abstract void stopNttTask();
    /**
     * Receive a messages for stopping the service
     */
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Logger.write("Action: " + action, getApplicationContext());
            if (action.equals(Constants.ACTION_STOP)) {
                boolean need = !intent.getBooleanExtra(Constants.STATUS, false);
                /*Logger.write("got: " + need + " isTaskRunning " + isTaskRunning,
                getApplicationContext());*/
                if (need && isTaskRunning) {
                    isTaskRunning = false;
                    Logger.write("FtpTaskRunner:onNotice - Stop service", getApplicationContext());
                    m_bNeedToCancelFTPAction = true;
                    stopNttTask();
                }
                // checkbox: iteration by key pressed intent.
            } else if (action.equals(
                    Constants.ITERATION_BY_KEY)) {
                m_bIsAdvanceByKeyPressed = intent.getBooleanExtra(
                        Constants.IS_PRESSED, false);
                Logger.write("isPressed: " + m_bIsAdvanceByKeyPressed, getApplicationContext());
                enabledIterationByKey(m_bIsAdvanceByKeyPressed);
            } else if(action.equals(
                    Constants.NEXT_ITER_BUTTON)) { // button: "next iteration" - pressed
                m_bIsNextIterationKeyPressedDN = true;
                m_bIsNextIterationKeyPressedUP = true;
                nextIterationKeyPressed();
            }
        }
    };

    protected void enabledIterationByKey(boolean m_bIsAdvanceByKeyPressed) {
        // empty for FTP class.
    }

    protected void nextIterationKeyPressed() {
        // empty for FTP class
    }
}
