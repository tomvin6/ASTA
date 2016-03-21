package com.example.Ntt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.Task.Validator;
import com.example.Units.KbpsUnits;
import com.example.Units.MbsUnits;
import com.example.Units.UnitConvertor;
import com.example.data.Constants;
import com.marvell.ntt.BasicManager;
import com.marvell.ntt.Client;
import com.marvell.ntt.IReportable;
import com.marvell.ntt.ManagerReport;
import com.marvell.ntt.ResultData;
import com.marvell.ntt.TestRequest;
import com.marvell.ntt.logger.ILogable;
import com.marvell.ntt.logger.NttLogger;
import com.marvell.ntt.tcp.ResultDataTCP;
import com.marvell.ntt.udp.ResultDataUDP;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Ntt task class.
 */
public class NttTask implements ILogable, IReportable {
    private static final int CUR_SPEED_FREQUENCY = 2;
    protected ScriptParametersAbstract m_oScriptParams;
    private NTTScriptParameters params = null;
    private float lastULBytes, lastDLBytes;
    private boolean m_bShouldStop = false;
    private boolean mGoOnNextIteration = false;
    private final static String TAG = "NttTask";
    private Client uploadClient = null;
    private Client downloadClient = null;
    private ResultData ulData = null;
    private int lastDLPercentage = 0, totalIterations;
    private int lastULPercentage = 0, curIteration;
    private ResultData dlData = null;
    private long mTotalElapsedTime = 0;
    private Intent messageSender;
    private Context m_oContext; // for broadcast messaging.
    private String failureReason = "", mode;
    private long mSentBytes, mReceivedBytes;
    private final String f_sScriptName = this.getClass().getSimpleName();
    String m_oTaskStatus;
    float curDLAvg, curULAvg;
    boolean iterationByKeyPressed = false, mShouldPause = false, m_bTaskSucceeded = false;
    private long mTotalIterationSentMb = 0,mTotalIterationReceivedMb = 0;
    private UnitConvertor m_oUnitConvertor;

    // initialize values inside constructor
    public NttTask(Context otherClassContext, Intent sender) {
        mTotalElapsedTime = 0; // initialize time for task
        messageSender = sender;
        m_oContext = otherClassContext;
        mSentBytes = 0;
        mReceivedBytes = 0;
        m_oTaskStatus = Constants.RUNNING;
        mode = Constants.NTT_MODE;
        curIteration = 0;
        mSentBytes = 0;
        mReceivedBytes = 0;
        curDLAvg = 0;
        lastULBytes = 0;
        curULAvg = 0;
        lastDLBytes = 0;
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(m_oContext);
        if (sharedPref.getString(
                Constants.UNITS, Constants.KBPS).equals(Constants.KBPS)) {
            m_oUnitConvertor = new KbpsUnits();
        } else { //   MB/s
            m_oUnitConvertor = new MbsUnits();
        }
    }
    // getter:
    public ScriptParametersAbstract getScriptParams() {
        return m_oScriptParams;
    }
    //setter:
    public void setScriptParams(ScriptParametersAbstract scriptParams) {
        this.m_oScriptParams = scriptParams;

    }

    public boolean shouldStop() {
        return m_bShouldStop;
    }
    //update in batch and do stuff in batch
    public void setShouldStop(boolean m_bShouldStop) {
        this.m_bShouldStop = m_bShouldStop;
    }
    protected void waitIntervalBetweenIterations(int interval) {
        /* IF WE WANT INTERVAL BETWEEN ITERATIONS
        enable this:
        try {
            Log.d(TAG, "||Waiting interval: " + interval + "sec.");
            Thread.sleep(interval * 1000);
            Log.d(TAG, "||Waiting interval ended.");
        } catch (InterruptedException e) {
            Log.d(TAG, e.toString());
        }
        */
    }

    /**
     * when one of the clients end before other (exp: down before up)
     * this method ensure that he will wait for the other client.
     * @return
     */
    private boolean waitForClients()
    {
        //Log.d(TAG, "waitForClients()");
        while ((uploadClient!=null && uploadClient.isRunning())
                ||(downloadClient!=null && downloadClient.isRunning()))
        {
            /*Log.d(TAG, "uploadClient ==" + uploadClient
            + " downloadClient==" + downloadClient);*/
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        //Log.d(TAG, "done waiting..");
        boolean retVal = ((uploadClient==null
                || uploadClient.getStatus()== BasicManager.ManagerStatus.FINISHED)
                &&(downloadClient==null
                || downloadClient.getStatus()== BasicManager.ManagerStatus.FINISHED));
        if(!retVal){
            failureReason = "uploadClient = " + uploadClient+ " ";
            if(uploadClient != null && !uploadClient.getErrorMessage().equals("")){
                failureReason = "UL: " + uploadClient.getErrorMessage();
            }
            if(downloadClient != null && !downloadClient.getErrorMessage().equals("")){
                failureReason += ", DL: " + downloadClient.getErrorMessage();
            }
        }
        return retVal;
    }
    protected void printIterationSummaryToLog()
    {
        /*
        String[] l_arrSummary = getIterationSummary();
        boolean noSummary = (l_arrSummary.length == 1 && l_arrSummary[0].equals(""));
*/
        boolean noSummary = false;
        Log.d(TAG, "#######");
        if (noSummary)
            Log.d(TAG, "#### Iteration summary: " + "No summary available");
        else
        {
            /*
            for (String str : l_arrSummary) {
                Log.d(TAG, str);
            }
            */
        }
        Log.d(TAG, "#######");
    }
    protected void performPostIterationActions(int interval) {
        //addSummaryToBatchResultFile();
        waitIntervalBetweenIterations(interval);
        //printIterationSummaryToLog();
    }
    protected String getScriptName(){
        return f_sScriptName;
    }
    /**
     * Init phase of script iteration
     * @param iterationNumber
     * @return boolean whether script should be continued
     */
    protected boolean initScriptIteration(int iterationNumber) {
        Log.d(TAG, "initScriptIteration()");
        // handle pc retriever
        boolean isShouldStop = shouldStop();
        if(isShouldStop)
            return !isShouldStop;

        m_oScriptParams.setCurrentIterationNumber(iterationNumber);
        /*
        RATHandler.getInstance().startRATCalculation();
        setWifiConnectivity(false);
        m_sScriptResultDetails = "";
        //m_sIterationResultDetails = "";
        */
        String loggerText = "||" + (isShouldStop ? "Script will be STOPPED" : getScriptName()
                + ". Starting iteration " + (iterationNumber + 1));
        Log.e(TAG, loggerText);
        return !isShouldStop;
    }
    private int calculatePercentage(ManagerReport report)
    {
        Log.d(TAG, "calculatePercentage()");
        int percentage = 0;
        ResultData data = report.getData();
        if (data != null)
        {
            if (report.getTransferTime() > 0)
            {
                percentage = (int) Math.round((data.endTime - data.startTime) / 10.0 / report
                        .getTransferTime());
            }
            //Log.d(TAG, "DATA != NULL");
            if (report.getProtocolType()== TestRequest.ProtocolType.UDP)
            {
                Log.d(TAG, "UDP");
                ResultDataUDP udpData = (ResultDataUDP)data;
                long bytes = (udpData.receivedPackets + udpData.lostPackets) * report.getPacketSize();
                percentage = (int) Math.round(bytes * 100.0 / report.getTransferSize());

                if (report.getDataFlowMode()== TestRequest.DataFlowMode.DL)
                {
                    //Log.d(TAG, "DOWNLOAD");
                    lastDLBytes = mReceivedBytes;
                    mReceivedBytes = udpData.receivedPackets * report.getPacketSize();
                }
                if (report.getDataFlowMode()== TestRequest
                        .DataFlowMode.UL){
                    Log.d(TAG, "UPLOAD");
                    lastULBytes = mSentBytes;
                    mSentBytes = udpData.receivedPackets * report.getPacketSize();

                }


            } else //TCP
            {
                Log.d(TAG, "TCP");
                ResultDataTCP tcpData = (ResultDataTCP)data;
                if (report.getDataFlowMode()== TestRequest.DataFlowMode.DL)
                {
                    lastDLBytes = mReceivedBytes;
                    mReceivedBytes = tcpData.receivedBytes;
                    percentage = (int) Math.round(
                            tcpData.receivedBytes * 100.0 / report.getTransferSize());
                }
                else// Upload
                {
                    lastULBytes = mSentBytes;
                    mSentBytes = tcpData.sentBytes;
                    percentage = (int) Math.round(
                            tcpData.sentBytes * 100.0 / report.getTransferSize());
                }
            }
            if (report.getTransferTime() > 0)
            {
                percentage = (int) Math.round((data.endTime - data.startTime) / 10.0 / report
                        .getTransferTime());


            }
            /*
            else //file size based calculations
            {
                if (report.getProtocolType() == TestRequest.ProtocolType.UDP)
                {
                    ResultDataUDP udpData = (ResultDataUDP)data;
                    long bytes = (udpData.receivedPackets + udpData
                            .lostPackets) * report.getPacketSize();
                    mReceivedBytes = (udpData
                            .receivedPackets);
                    percentage = (int) Math.round(bytes * 100.0 / report.getTransferSize());
                }
                else //TCP
                {
                    ResultDataTCP tcpData = (ResultDataTCP)data;
                    if (report.getDataFlowMode()== TestRequest.DataFlowMode.DL)
                    {
                        mReceivedBytes = tcpData.receivedBytes;
                        percentage = (int) Math.round(
                                tcpData.receivedBytes * 100.0 / report.getTransferSize());
                    }
                    else// Upload
                    {
                        mSentBytes = tcpData.sentBytes;
                        percentage = (int) Math.round(
                                tcpData.sentBytes * 100.0 / report.getTransferSize());
                    }
                }

            }
            */
        }
        return percentage;
    }
    @Override
    public void reportToUser(ManagerReport report)
    {
        Log.d(TAG,"reportToUser()");
        ResultData mReportedData = report.getData();
        int curPercentage;
        if (report.getStatus() == BasicManager.ManagerStatus.IN_PROGRESS && (mReportedData != null))
        {
            curPercentage = calculatePercentage(report);
            if (report.getDataFlowMode()== TestRequest.DataFlowMode.DL)
            {
                Log.d(TAG, "reportToUser_download");
                if ((lastDLPercentage/10) < (curPercentage/10))
                {
                    lastDLPercentage = curPercentage;
                    Log.d(TAG, "DL: "+ lastDLPercentage+"%");
                }
                dlData = mReportedData;
            }
            else //upload
            {
                Log.d(TAG, "reportToUser_upload.. lastULPercentage = " + lastULPercentage
                + " curPercentage = " + curPercentage);
                if ((lastULPercentage/10) < (curPercentage/10))
                {
                    lastULPercentage = curPercentage;
                    Log.d(TAG, "UL: " + lastULPercentage + "%");
                }
                ulData = mReportedData;
            }
        }
        if (report.getStatus() == BasicManager.ManagerStatus.FINISHED)
        {
            m_bTaskSucceeded = true;
        }
        if (report.getStatus() == BasicManager.ManagerStatus.ABORTED)
        {
            m_bTaskSucceeded = false;
            Log.d(TAG, "job done becouse of error");
            if (!Validator.isNetworkAvailable(m_oContext)) {
                m_oTaskStatus = Constants.NETWORK_ERROR;
                messageSender.putExtra(Constants.STATUS, Constants.NETWORK_ERROR);
            } else { // ABORTED
                m_oTaskStatus = Constants.JOB_CANCEL;
                messageSender.putExtra(Constants.STATUS, Constants.JOB_CANCEL);
            }
            LocalBroadcastManager.getInstance(m_oContext).sendBroadcast
                    (messageSender);
        }
        if (report.getStatus() == BasicManager.ManagerStatus.FAILED)
        {
            m_bTaskSucceeded = false;
            Log.d(TAG, "Job failed");
            //failed.
            m_oTaskStatus = Constants.JOB_FAILED;
            messageSender.putExtra(Constants.STATUS, Constants.JOB_FAILED);
            LocalBroadcastManager.getInstance(m_oContext).sendBroadcast
                    (messageSender);
        }
        Log.d(TAG, "done reportToUser");
    }

    /**
     * calculate speed:
     * @param totalBytes got / sent
     * @return speed
     */
    private float getSpeed(float totalBytes) {
        // calculate speeds:
        return m_oUnitConvertor.getDataByUnits((long)totalBytes,
                mTotalElapsedTime);
        /*
        float bytesPerSec = (totalBytes
                / ((mTotalElapsedTime)));
        // download rate in kilobytes per second
        float kbPerSec = bytesPerSec / (1024);
        return kbPerSec;
        */
    }

    /**
     * method to send broadcast with current values to
     * taskStatusActivity.
     */
    private void sendData() {
        mTotalElapsedTime++;
        float ulAVGSpeed = getSpeed(mSentBytes + mTotalIterationSentMb);
        float dlAVGSpeed = getSpeed(mReceivedBytes + mTotalIterationReceivedMb);
        // calculate & update current speed every "speed_freq" time:
        if (mTotalElapsedTime % CUR_SPEED_FREQUENCY == 0) {
            // default time for ntt is 1 second
            float tmpUnRate = m_oUnitConvertor.getDataByUnits(
                    (long) (mSentBytes - lastULBytes), 1);
            float tmpDLRate = m_oUnitConvertor.getDataByUnits(
                    (long) (mReceivedBytes - lastDLBytes), 1);
            /*
            float tmpUnRate =(float) ((mSentBytes - lastULBytes)
                    / 1024);
            float tmpDLRate = (float)
                    ((mReceivedBytes - lastDLBytes)
                            / 1024);
            */
            messageSender.putExtra(Constants.CURRENT_DN_RATE, tmpDLRate);
            messageSender.putExtra(Constants.CURRENT_UP_RATE, tmpUnRate);
        }
        // put values in broadcast:
        messageSender.putExtra(Constants.STATUS, m_oTaskStatus);
        messageSender.putExtra(Constants.NTT_MODE, mode);
        messageSender.putExtra(Constants.ITERATION,
                (curIteration+1) + "/" + totalIterations);
        messageSender.putExtra(Constants.ELAPSED_TIME,
                (mTotalElapsedTime));
        // m_sMode = FTP m_sMode
        messageSender.putExtra(Constants.AVG_UP_RATE, ulAVGSpeed);
        messageSender.putExtra(Constants.AVG_DN_RATE, dlAVGSpeed);
        // round values:
        float mSentMb = (float)Math.round(
                ((mSentBytes + mTotalIterationSentMb)
                        /(Constants.MILION)) *100) / 100;
        float mReceivedMb = (float)Math.round(((mReceivedBytes + mTotalIterationReceivedMb) /
                (Constants.MILION)) * 100) / 100;
        messageSender.putExtra(Constants.SENT,
                mSentMb + "MB");
        messageSender.putExtra(Constants.RECEIVED,
                mReceivedMb + "MB"); // m_sMode = FTP
        LocalBroadcastManager.getInstance(m_oContext).sendBroadcast
                (messageSender);
    }

    /**
     * initialize a background thread for updates
     */
    private void updateInBackground() {
        Thread t = new Thread() {
            @Override
            public void run() {
                sendDataWithTimer();
            }
        };
        t.start();
    }
    /**
     * Timer method to send updates every one second:
     */
    private void sendDataWithTimer() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (m_bShouldStop) {
                    Log.d(TAG, "Should stop. Cancel update timer");
                    timer.cancel();
                    // stop clients:
                    if (uploadClient != null) {
                        uploadClient.stop();
                    }
                    if (downloadClient != null) {
                        downloadClient.stop();
                    }
                    return;
                } else {
                    if (!mShouldPause) {
                        sendData();
                    } else {
                        messageSender.putExtra(Constants.STATUS, Constants.PAUSE);
                        LocalBroadcastManager.getInstance(m_oContext).sendBroadcast
                                (messageSender);
                    }
                }
            }
        }, 0, 1000);//Update text every second
    }
    private void writeAllParametersToLog() {
        Log.d(TAG, "mSentBytes " + mSentBytes);
        Log.d(TAG, "mReceivedBytes " + mReceivedBytes);
        Log.d(TAG, "totalIterations " + totalIterations);
        Log.d(TAG, "m_oTaskStatus " + m_oTaskStatus);
        Log.d(TAG, "m_sMode " + mode);
        Log.d(TAG, "curIteration " + curIteration);
        Log.d(TAG, "curDLAvg " + curDLAvg);
        Log.d(TAG, "lastULBytes " + lastULBytes);
        Log.d(TAG, "curULAvg " + curULAvg);
        Log.d(TAG, "lastDLBytes " + lastDLBytes);
        Log.d(TAG, "params.getNumberOfSessionsUL() " + params.getNumberOfSessionsUL());
        Log.d(TAG, "params.getSocketSendBufferUL() " + params.getSocketSendBufferUL());
        Log.d(TAG, "params.getServerIp() " + params.getServerIp());
        Log.d(TAG, "params.getSocketSendBufferDL() " + params
                .getSocketSendBufferDL());
    }
    /**
     * Run ntt_icon task with chosen parameters
     * @param startIterationNumber
     */
    public void runNttTask(int startIterationNumber,
                           Context m_oContext, Intent runButtonSender,
                           Intent messageSender) {
        Log.d(TAG, "runNttTask()");
        NttLogger nttLogger = NttLogger.getInstance();
        nttLogger.addPrinter(this);
        params = ((NTTScriptParameters) this
                .getScriptParams());
        totalIterations = params.getNumberOfIterations();
        writeAllParametersToLog();
        boolean alreadyUpdate = false;
        for (curIteration = startIterationNumber;
             curIteration < params.getNumberOfIterations();
             curIteration++)
        {
            mShouldPause = false;
            if(!initScriptIteration(curIteration)) {
                Log.d(TAG, "NEED TO STOP!");
                break;
            }
            if (params.isUpload())
            {
                Log.d(TAG, "upload task");
                //create client.
                uploadClient  = new Client(new TestRequest(params
                        .getProtocol(),
                        TestRequest.DataFlowMode.UL, params.getNumberOfSessionsUL(),
                        params.getSendSpeedUL()*1024, params.getPacketSizeUL(),
                        params.getTransferTimeUL(), params.getTransferSizeUL()*1024,
                        params.getDataChunkSizeUL(), params.getSocketSendBufferUL()),
                        params.getServerIp(), params.getServerPort(), this);
                //start client
                uploadClient.start();
                //downloadThread.start();
                //listOfThreads.add(downloadThread);
            }
            if (params.isDownload())
            {
                Log.d(TAG, "download task");
                downloadClient  = new Client(new TestRequest(params.getProtocol(),
                        TestRequest.DataFlowMode.DL, params.getNumberOfSessionsDL(),
                        params.getSendSpeedDL()*1024, params.getPacketSizeDL(),
                        params.getTransferTimeDL(), params.getTransferSizeDL()*1024,
                        params.getDataChunkSizeDL(), params.getSocketSendBufferDL()),
                        params.getServerIp(), params.getServerPort(), this);
                //start client
                downloadClient.start();
                //uploadThread.start();
                //listOfThreads.add(uploadThread);
            }
            // background thread to count time and update m_oTaskStatus
            if (!alreadyUpdate) {
                alreadyUpdate = true;
                updateInBackground();
            }
            boolean isScriptPassed = waitForClients();
            mShouldPause = true;
            waitForNextIterationKeyPressIfEnabled();
            if (isScriptPassed)
            {
                Log.d(TAG, "script passed");
                if (params.getProtocol()== TestRequest.ProtocolType.TCP)
                {
                    ResultDataTCP ulTCPData;
                    if (ulData!=null) {
                        ulTCPData = (ResultDataTCP) ulData;
                        Log.d(TAG, "ulData!=null");
                    }
                    else {
                        ulTCPData = new ResultDataTCP();
                        Log.d(TAG, "ulData==null");
                    }

                    ResultDataTCP dlTCPData;
                    if (dlData!=null) {
                        Log.d(TAG, "dlData!=null");
                        dlTCPData = (ResultDataTCP) dlData;
                    } else {
                        Log.d(TAG, "else -> dlData==null");
                        dlTCPData = new ResultDataTCP();
                    }
                    handleResults(true,ulTCPData.sentBytes/1024
                            +";" +(ulTCPData.endTime-ulTCPData.startTime)/1000
                            +";" +dlTCPData.receivedBytes/1024
                            +";"+(dlTCPData.endTime-dlTCPData.startTime)/1000);
                }
                else
                {
                    Log.d(TAG, "UDP script passed");
                    ResultDataUDP ulUDPData;
                    if (ulData!=null)
                        ulUDPData = (ResultDataUDP)ulData;
                    else ulUDPData = new ResultDataUDP();
                    ResultDataUDP dlUDPData;
                    if (dlData!=null)
                        dlUDPData = (ResultDataUDP)dlData;
                    else dlUDPData = new ResultDataUDP();
                    handleResults(true,ulUDPData.sentPackets+";"
                            +ulUDPData.receivedPackets+";"
                            +ulUDPData.lostPackets+";"
                            +ulUDPData.outOfOrderPackets+";"
                            +ulUDPData.duplicatePackets+";"
                            +(ulUDPData.endTime-ulUDPData.startTime)/1000+";"
                            +dlUDPData.sentPackets+";"
                            +dlUDPData.receivedPackets+";"
                            +dlUDPData.lostPackets+";"
                            +dlUDPData.outOfOrderPackets+";"
                            +dlUDPData.duplicatePackets+";"
                            +(dlUDPData.endTime-dlUDPData.startTime)/1000);
                }
            }
            else
            {
                Log.e(TAG, "script failed");
                m_oTaskStatus = Constants.NETWORK_ERROR;
                if (uploadClient!=null)
                    uploadClient.stop();
                if (downloadClient!=null)
                    downloadClient.stop();
                handleResults(false, failureReason);
            }
            mTotalIterationSentMb += mSentBytes;
            mTotalIterationReceivedMb += mReceivedBytes;
            mSentBytes = 0;
            mReceivedBytes = 0;
            // performPostIterationActions(params.getIntervalBetweenIterations());
        }
        //addSummaryToBatchResultFile();
        if (m_bTaskSucceeded) {
            m_oTaskStatus = Constants.JOB_DONE;
            messageSender.putExtra(Constants.STATUS, Constants.JOB_DONE);
            LocalBroadcastManager.getInstance(m_oContext).sendBroadcast
                    (messageSender);
        }
        m_bShouldStop = true;
        nttLogger.dispose();
    }

    private void waitForNextIterationKeyPressIfEnabled() {
        if (iterationByKeyPressed) {
            while (mGoOnNextIteration != true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mGoOnNextIteration = false;
        }
    }

    private void handleResults(boolean b, String s) {
        Log.d(TAG, "handle results: " + s);
    }

    @Override
    public void printToLog(long l, String s, String s2) {

    }

    /**
     * set iteration by key pressed.
     * @param isKeyPressed
     */
    public void setIterationByKeyPressed(boolean isKeyPressed) {
        iterationByKeyPressed = isKeyPressed;
    }
    /**
     * set iteration by key pressed.
     */
    public void runNextIteration() {
        mGoOnNextIteration = true;
    }

}
