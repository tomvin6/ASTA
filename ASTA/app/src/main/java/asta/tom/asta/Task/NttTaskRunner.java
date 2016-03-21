package com.example.Task;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.Ntt.NTTScriptParameters;
import com.example.Ntt.NttTask;
import com.example.data.Constants;
import com.example.data.Paths;

import java.io.IOException;
import java.util.List;

import com.marvell.ntt.TestRequest.ProtocolType;

/**
 * class that extends TaskRunner.
 * it initialize and starts a process of download / upload test.
 * this class uses a NTTClient to intract with the server.
 * It also responsible for creating a background thread to inform
 * TaskStatusActivity on current results every second.
 */
public class NttTaskRunner extends TaskRunner {
    private static final String FILE_SIZE = "10"; // default file size of
    private static String TAG = "NttTaskRunner";
    private int m_nIterations;
    private NttTask script;
    private Intent m_oRunButtonIntent;
    /**
     * constructor
     */
    public NttTaskRunner() {
        super("ntt_task_runner");
        taskSerializer = new GeneralSerializer();
    }
    /*
 * add server to list of servers.
 * for FTPTaskRunner list will contain FTP servers
 * and for NTT - NTT servers.
 */
    @Override
    protected void addServer(Server newServer) {
        m_oNttServersList.add(newServer);
    }
    /*
     * add list of servers to current list
     * for FTPTaskRunner list will contain FTP servers
     * and for NTT - NTT servers.
     */
    @Override
    protected void addServers(List<Server> serversList) {
        m_oNttServersList.addAll(serversList);
    }
    /**
     * get servers list.
     * @return servers list.
     * List<Server> list of servers.
     */
    @Override
    protected List<Server> getServersList() {
        return m_oNttServersList;
    }
    /**
     * upload by quantity limitation. usfull only in FTP m_sMode.
     * @param iterationsCounter counter for file name
     * @return data size
     * @throws java.io.IOException
     */
    int uploadByDataLimitation(int iterationsCounter, int timeLimitation) throws IOException {
        return 0;
    }

    /**
     * upload by time limitation. usfull only in FTP m_sMode.
     * @return how much data have been download (in MB)
     * @throws java.io.IOException
     */
    int uploadByTimeLimitation(int timeLimitation) throws Exception {
        return 0;
    }

    /**
     * upload method. usfull only in FTP m_sMode.
     * @return true
     * @throws Exception
     */
    @Override
    boolean upload() throws Exception {
        return true;

    }

    /**
     * upload by quantity limitation:
     * @param iterationsCounter counter for file name
     * @return data size
     * @throws java.io.IOException
     */
    int downloadByDataLimitation(int iterationsCounter,
                                 int limitationSize) throws Exception {
        return 1;
    }

    /**
     * usfull only in FTP m_sMode.
     * thread to update Gui with current values.
     * start a thread for calculations and update GUI.
     * void
     */
    private void updateDownloadGuiThread(final long rateTimeElapsed, final int byteReadInSecond) {

    }
    /**
     * empty implementation for FTP task
     */
    protected void stopNttTask() {
        // no NTT task
        Log.d(TAG, "called stop..");
        script.setShouldStop(true);
    }

    /**
     * stop Ntt task
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopNttTask();
        super.onDestroy();
    }

    /**
     * usfull only in FTP m_sMode.
     * thread to update Gui with current values.
     * start a thread for calculations and update GUI.
     */
    private void updateUploadGuiThread(final long rateTimeElapsed, final int byteReadInSecond) {

    }
    /**
     * upload by time limitation. usfull only in FTP m_sMode.
     * @param iterationsCounter counter for file name
     * @return how much data have been download (in MB)
     * @throws java.io.IOException
     */
    int downloadByTimeLimitation(int iterationsCounter,
                                 int fileSize) throws Exception {
        return 0;
    }
    /**
     * initialize values.
     */
    private void initializeValues() {
        m_nIterations = m_oCurTask.getIterations();
        m_bNeedToCancelFTPAction = false; // task is running.
        // get new intents for sending messages:
        isTaskRunning = true;
    }

    /**
     * send start values and run the test.
     */
    @Override
    protected void runTask() {
        m_oRunButtonIntent = new Intent(Constants.ACTION_STOP);
        m_oIMessage = new Intent(Constants.ACTION);  //can put anything in it with putExtra
        // get parameters from Task and initialize variables
        initializeValues();
        // send a message to disable "run" button.(test is started, we can't run a new one)
        m_oRunButtonIntent.putExtra(Constants.STATUS, true); // test is running.
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oRunButtonIntent);
        // send beginning message to m_sStatus screen activity:
        m_oIMessage.putExtra(Constants.STATUS, Constants.RUNNING);
        m_oIMessage.putExtra(Constants.MODE, Constants.NTT_MODE);
        m_oIMessage.putExtra(Constants.ITERATION, "1/" + m_nIterations);
        m_oIMessage.putExtra(Constants.SENT, Constants.NONE); //0MB
        m_oIMessage.putExtra(Constants.RECEIVED, Constants.NONE);
        m_oIMessage.putExtra(Constants.CURRENT_UP_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.CURRENT_DN_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.AVG_DN_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.AVG_UP_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.ELAPSED_TIME, Constants.LONG_NONE_VALUE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        // Run NTT Task:
        initializeAndStartTask();
        // update Status Activity that job has been finished
        m_oIMessage.putExtra(Constants.MODE, Constants.NTT_MODE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
    }

    /**
     * register recievers for "iteration" buttons and "run" burron
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // register receiver for stopping the service.
        IntentFilter iFilter = new IntentFilter(Constants.ACTION_STOP);
        iFilter.addAction(Constants.ITERATION_BY_KEY);
        iFilter.addAction(Constants.NEXT_ITER_BUTTON);
    }

    /**
     * initialize and run a new ntt_icon task:
     */
    public void initializeAndStartTask() {
        // get ntt_icon object:

        script = new NttTask(getApplicationContext(), m_oIMessage);
        NTTScriptParameters params = new NTTScriptParameters();
        int limitation;
        // set m_sMode by task:
        if (m_oCurTask.getMode() == Constants.DN_MODE) {
            Log.d(TAG, "DOWNLOAD MODE");
            params.setDownload(true);
            params.setUpload(false);
        } else if (m_oCurTask.getMode() == Constants.UP_MODE) {
            Log.d(TAG, "UPLOAD MODE");
            params.setDownload(false);
            params.setUpload(true);
        } else {
            Log.d(TAG, "UPLOAD & DOWNLOAD MODE");
            params.setDownload(true);
            params.setUpload(true);
        }
        // set limitation:
        limitation = m_oCurTask.getLimitationQuantity();
        if (m_oCurTask.getLimitationType() == Constants.DATA_LIMITATION) {
            params.setTransferTimeDL(0); //seconds. IF ZERO -> SIZE limitation
            params.setTransferTimeUL(0); //seconds IF ZERO -> SIZE limitation
            params.setTransferSizeDL(1024 * limitation); //limitation
            params.setTransferSizeUL(1024 * limitation);
        } else {
            params.setTransferTimeDL(limitation * 60); //
            params.setTransferTimeUL(limitation * 60);
        }
        // set protocol:
        if (m_oCurTask.getProtocol() == Constants.TCP) {
            params.setProtocol(ProtocolType.TCP);
        } else {
            params.setProtocol(ProtocolType.UDP);
        }
        // set server ip:
        params.setServerIp(m_oCurTask.getServer().getServerIp()); //
        // ntt_icon server
        //values:
        params.setDataChunkSizeDL(4096); //TCP. like packet size in UDP
        params.setDataChunkSizeUL(4096);
        params.setNumberOfSessionsDL(m_oCurTask.getDLSession()); //  number
        // of threads
        params.setNumberOfSessionsUL(m_oCurTask.getDLSession());//  m_oCurTask.getULSession() number
        // of threads
        params.setPacketSizeDL(1472); //UDP max = 1472, min = 8
        params.setPacketSizeUL(1472); //UDP max = 1472, min = 8
        params.setSendSpeedDL(m_oCurTask.getDnSpeed()); //kbps
        params.setSendSpeedUL(m_oCurTask.getUlSpeed()); // was 100
        //SocketSendBuffer -(TCP) max = 65536, socket sending buffer that is
        // filled with data chunks.
        params.setSocketSendBufferDL(m_oCurTask.getBufferSize());
        params.setSocketSendBufferUL(m_oCurTask.getBufferSize());
        // other values:
        params.setCurrentIterationNumber(0);
        params.setIntervalBetweenIterations(0);
        params.setNumberOfIterations(m_oCurTask.getIterations());
        script.setScriptParams(params);
        script.runNttTask(0, getApplicationContext(), m_oRunButtonIntent,
                m_oIMessage);
        Log.d(TAG, "initializeAndStartTask - done");
    }

    /**
     * download task
     */
    @Override
    boolean download() throws Exception {
        return true; // not implemented in NTT
    }

    @Override
    boolean uploadAndDownload() throws Exception {
        return true; // not implemented in NTT
    }
    @Override
    protected void enabledIterationByKey(boolean m_bIsAdvanceByKeyPressed) {
        script.setIterationByKeyPressed(m_bIsAdvanceByKeyPressed);
    }
    @Override
    protected void nextIterationKeyPressed() {
        // empty for FTP class
        script.runNextIteration();
    }
}
