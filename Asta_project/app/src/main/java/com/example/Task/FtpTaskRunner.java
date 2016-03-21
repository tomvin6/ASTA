package com.example.Task;
import com.example.Listeners.Logger;
import com.example.Units.KbpsUnits;
import com.example.Units.MbsUnits;
import com.example.Units.UnitConvertor;
import com.example.data.Constants;
import com.example.data.Paths;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.List;

/**
 * class that extends TaskRunner.
 * it initialize and starts a process of download / upload test.
 * this class uses a FTPClient to intract with the server.
 * It also responsible for creating a background thread to inform
 * TaskStatusActivity on current results every second.
 */
public class FtpTaskRunner extends TaskRunner {
    private static final String FILE_SIZE = "10"; // default file size of
    private static final String SPEED_UNITS = "Kbps";
    private static final String TOTAL_UNITS = "MB";
    private static String TAG = "FtpTaskRunner";
    private static final int NONE = -1; // bytes read
    private static final int NO_BYTES = 0;
    private FTPClient m_oUploadFtpClient, m_oDownloadFtpClient;
    // task parameters:
    private long m_nUploadStartTime, m_nUploadFinishTime, m_nUploadElapsedTime;
    private long m_nDownloadStartTime, m_nDownloadFinishTime,
            m_nDownloadElapsedTime;
    private int m_nIterations;
    //private int m_nLimitationType;
    private int m_nLimitationQuantity;
    private int m_nTotalSentBytes;
    private int m_nTotalReceivedBytes;
    private int m_nRunningMode; //m_sMode: upload / down / both
    private Intent m_oRunButtonIntent;
    private int dlCurrentIteration, ulCurrentIteration;
    private boolean runningUlAndDlTask;
    private boolean isErrorAccoured;
    private UnitConvertor m_oUnitConvertor;
    private String m_sFormat;
    /**
     * constructor
     */
    public FtpTaskRunner() {
        super("ftp_task_runner");
        taskSerializer = new GeneralSerializer();
        m_oUploadFtpClient = new FTPClient();
        m_oDownloadFtpClient = new FTPClient();
    }

    /**
     * when user exit activity (after dialog) service will stop
     * automatically.
     */
    @Override
    public void onDestroy() {
        m_bNeedToCancelFTPAction = true;
        super.onDestroy();
    }


    /**
     * upload by time limitation:
     * @return how much data have been download (in MB)
     * @throws IOException
     */
    int uploadByTimeLimitation(int timeLimitation) throws Exception {
        int downloadedData = 0, downloadFilesCounter = 0;
        final String serverFileName = (FILE_SIZE + TOTAL_UNITS);
        byte[] bytesArray = new byte[Constants.ARRAY_SIZE];
        int bytesRead = NONE, bytesReadInSecond = NONE;
        long m_nRrateStartTime = 0, rateElapsedTime = 0;
        long currentTime = System.currentTimeMillis();
        long limitation = currentTime + (timeLimitation * 60000);
        Log.d(TAG, "limitation: " + limitation);
        byte [] data = new byte[10 * 1024 * 1024];
        // as long as we have not finish time limit:
        while (System.currentTimeMillis() < limitation) {
            if (!m_bNeedToCancelFTPAction) {
                // get an input + output stream for a new file:
                OutputStream outputStream = m_oUploadFtpClient.storeFileStream(serverFileName);
                InputStream iStream = new ByteArrayInputStream(data);
                while (System.currentTimeMillis() < limitation) {
                    if ((bytesRead = iStream.read(bytesArray)) == -1) {
                        currentTime = System.currentTimeMillis();
                        break;
                    }
                    if (!m_bNeedToCancelFTPAction) {
                        outputStream.write(bytesArray, 0, bytesRead);
                        m_nUploadFinishTime = System.nanoTime();
                        // calculate total bytes so far:
                        m_nTotalSentBytes += bytesRead; // size of bytes array
                        rateElapsedTime = (m_nUploadFinishTime - m_nRrateStartTime);
                        bytesReadInSecond += bytesRead;
                        // update GUI every one second:
                        if ((rateElapsedTime) >= Constants.secondInNano) {
                            // should be rateElapsedTime calculated in seconds
                            updateUploadGuiThread(1, bytesReadInSecond);
                            m_nRrateStartTime = System.nanoTime();
                            bytesReadInSecond = 0;
                        }
                    } else {
                        Log.d(TAG, "Exit from TimeLimit(upload)");
                        updateUploadGuiThread(1, NO_BYTES); // NO BYTES READ IN 1 SEC
                        break;
                    }
                }
                // do not call completePending if download havn't finished.
                // it will force download to finish before moving on.
                Log.d(TAG, "UL - Loop Ended");
                FTPReply.isPositiveIntermediate(m_oUploadFtpClient.getReplyCode()); // stuck if we call it before download finished
                Log.d(TAG, "positive reply");
                if (m_bNeedToCancelFTPAction) {
                    Logger.write("UL Job was Cancelled", getApplicationContext());
                    downloadedData = downloadFilesCounter * timeLimitation;
                    //m_nUploadFinishTime = System.nanoTime();
                    iStream.close(); // close streams
                    outputStream.close(); // close streams
                    return downloadedData;
                } else {
                    Log.d(TAG, "upload keep on running");
                    // option 2: time end up:
                    if (System.currentTimeMillis() > limitation) {
                        Logger.write("Time End up before file", getApplicationContext());
                        m_nUploadFinishTime = System.nanoTime();
                        downloadedData = downloadFilesCounter * timeLimitation;
                        iStream.close(); // close streams
                        outputStream.close(); // close streams
                        m_oUploadFtpClient.completePendingCommand();
                        return downloadedData;
                    } else {
                        // option 3: end of file befor end of time:
                        Logger.write("File end up before time", getApplicationContext());
                        downloadFilesCounter++;
                        downloadedData = downloadFilesCounter * timeLimitation;
                        iStream.close(); // close streams
                        outputStream.close(); // close streams
                    }
                }
                Log.d(TAG, "Still in process");
                m_oUploadFtpClient.completePendingCommand();
                downloadedData = downloadFilesCounter * timeLimitation;
                m_nUploadFinishTime = System.nanoTime();
            }
        }
        return downloadedData;
    }

    /**
     * start an upload task
     * @return m_sStatus
     * @throws Exception when error accoured.
     */
    @Override
    boolean upload() throws Exception {
        ulCurrentIteration = 1;
        int sentBytes = 0;
        boolean success = m_oUploadFtpClient.changeWorkingDirectory(Paths.SERVER_UPLOAD_FOLDER);
        if (!success) {
            Logger.error("Error in changing working-dir", this);
        }
        m_nTotalSentBytes = 0;
        m_nUploadStartTime = System.nanoTime();
        m_nUploadFinishTime = m_nUploadStartTime + 1;
        // while we have another iteration to do:
        while (ulCurrentIteration <= m_nIterations) {
            //Log.d(TAG, "1. ulCurrentIteration " + ulCurrentIteration);
            m_oIMessage.putExtra(Constants.ITERATION,
                    ulCurrentIteration + "/" + m_nIterations);
            sentBytes += uploadByTimeLimitation(m_nLimitationQuantity);
            // if we don't need to cancel, iteration is done:
            if (m_bNeedToCancelFTPAction) {
                break;
            } else {
                ulCurrentIteration++;
                // after iteration done - wait for upload task (when running both):
                while (runningUlAndDlTask
                        && (ulCurrentIteration <= m_nIterations)
                        && (ulCurrentIteration >
                        dlCurrentIteration)) {
                    Thread.sleep(100);
                }
                //Log.d(TAG, "after sleep");
                m_oIMessage.putExtra(Constants.SENT, (int) m_nTotalSentBytes
                        / (1024 * 1024) + Constants.TOTAL_UNITS);
                LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
                Logger.write("UL - done iteration: " + ulCurrentIteration,
                        getApplicationContext());
                Log.d(TAG, "m_bIsAdvanceByKeyPressed: " + m_bIsAdvanceByKeyPressed);
                if (m_bIsAdvanceByKeyPressed) {
                    m_oIMessage.putExtra(Constants.STATUS, Constants.PAUSE);
                    m_oIMessage.putExtra(Constants.SENT, (int) m_nTotalSentBytes
                            / (1024 * 1024) + Constants.TOTAL_UNITS);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
                    while (!m_bIsNextIterationKeyPressedUP) {
                        Log.d(TAG, "wait for key press");
                        Thread.sleep(200);
                    }
                    m_oIMessage.putExtra(Constants.STATUS, Constants.RUNNING);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
                }
                m_bIsNextIterationKeyPressedUP = false;
            }
        }
        m_nUploadElapsedTime = ((m_nUploadFinishTime - m_nUploadStartTime)
                / Constants.miliToSecondsConvertor) + 1; // calculate elapsed time in seconds:
        float avgRate = getDataByUnits
                (m_nTotalSentBytes, m_nUploadElapsedTime);
        // get a broadcast sender, and send finished message:
        Logger.success("Job done. " + ((m_nTotalSentBytes / (1024.0 * 1024.0))) + " " + Constants
                .TOTAL_UNITS + " Upload in "
                + m_nUploadElapsedTime + " seconds. Speed: " + avgRate +
                SPEED_UNITS + ")", this);
        m_oIMessage.putExtra(Constants.CURRENT_UP_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.AVG_UP_RATE, (avgRate));
        m_oIMessage.putExtra(Constants.STATUS, Constants.UP_DONE);
        String totalMbSentStr = String.format(m_sFormat + Constants.TOTAL_UNITS,
                (m_nTotalSentBytes / (1024.0 * 1024.0)));
        m_oIMessage.putExtra(Constants.SENT, totalMbSentStr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        if (sentBytes > 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * thread to update Gui with current values.
     * start a thread for calculations and update GUI.
     */
    private void updateDownloadGuiThread(final long rateTimeElapsed, final int byteReadInSecond) {
        // Received bytes calculation of current iteration
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!m_bNeedToCancelFTPAction) {
                    // calculate total bytes so far (received)
                    // calculate download avg in mb per second
                    long secondsElapsedTime = ((m_nDownloadFinishTime - m_nDownloadStartTime)
                            / Constants.miliToSecondsConvertor);
                    if (secondsElapsedTime == 0) {
                        secondsElapsedTime = 1;
                    }
                    /*
                    float bytesPerSec = m_nTotalReceivedBytes
                            / secondsElapsedTime;
                    float kbPerSec = bytesPerSec / (1024);
                    */

                    //float mbPerSec = kbPerSec / (1024);
                    m_oIMessage.putExtra(Constants.ELAPSED_TIME,
                            secondsElapsedTime);
                    // get a broadcast sender, and send m_sStatus message with rates:
                    float avgRate = getDataByUnits
                            (m_nTotalReceivedBytes, secondsElapsedTime);
                    float curRate = getDataByUnits
                            (byteReadInSecond,
                                    rateTimeElapsed);
                    m_oIMessage.putExtra(Constants.CURRENT_DN_RATE, curRate);
                    m_oIMessage.putExtra(Constants.AVG_DN_RATE, avgRate);
                    m_oIMessage.putExtra(Constants.RECEIVED,
                            String.format(m_sFormat + Constants.TOTAL_UNITS,
                                    (float)(m_nTotalReceivedBytes / (Constants
                                            .MILION)))); // m_sMode = FTP m_sMode
                    LocalBroadcastManager.getInstance(
                            getApplicationContext()).sendBroadcast(m_oIMessage);
                } else {
                    // job canceled:
                    m_oRunButtonIntent.putExtra(Constants.STATUS, false); // done
                    LocalBroadcastManager.getInstance(
                            getApplicationContext()).sendBroadcast(m_oRunButtonIntent);
                }
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();
    }
    /**
     * thread to update Gui with current values.
     * start a thread for calculations and update GUI.
     */
    private void updateUploadGuiThread(final long rateTimeElapsed, final int byteReadInSecond) {
        // Received bytes calculation of current iteration
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!m_bNeedToCancelFTPAction) {
                    // calculate total bytes so far (received)
                    /* download rate in bytes per second */
                    long secondsElapsedTime = ((m_nUploadFinishTime - m_nUploadStartTime)
                            / Constants.miliToSecondsConvertor);
                    if (secondsElapsedTime == 0) {
                        secondsElapsedTime = 1;
                    }
                    // get a broadcast sender, and send m_sStatus message:
                    m_oIMessage.putExtra(Constants.ELAPSED_TIME,
                            secondsElapsedTime);
                    float avgRate = getDataByUnits
                            (m_nTotalSentBytes, secondsElapsedTime);
                    float curRate = getDataByUnits
                            (byteReadInSecond,
                                    rateTimeElapsed);
                    m_oIMessage.putExtra(Constants.CURRENT_UP_RATE, curRate);
                    m_oIMessage.putExtra(Constants.AVG_UP_RATE, avgRate);
                    m_oIMessage.putExtra(Constants.SENT,
                            String.format(m_sFormat + TOTAL_UNITS,
                                    (float)(m_nTotalSentBytes / (Constants
                                            .MILION)))); // m_sMode = FTP m_sMode
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(m_oIMessage);
                } else {
                    // cancel job:
                    m_oRunButtonIntent.putExtra(Constants.STATUS, false); // done
                    LocalBroadcastManager.getInstance(
                            getApplicationContext()).sendBroadcast(m_oRunButtonIntent);
                }
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();
    }
    /**
     * upload by time limitation:
     * @param iterationsCounter counter for file name
     * @return how much data have been download (in MB)
     * @throws IOException
     */
    int downloadByTimeLimitation(int iterationsCounter,
                                 int fileSize) throws Exception {
        OutputStream outputStream;
        InputStream inputStream;
        long m_nRrateStartTime, rateElapsedTime = 0;
        int downloadFilesCounter = 0, downloadedData = 0;
        final String serverFileName = FILE_SIZE + TOTAL_UNITS; //time with 100Mb file!
        byte[] bytesArray = new byte[Constants.ARRAY_SIZE];
        int bytesRead = NONE, bytesReadInSecond = NONE;
        long currentTime = System.currentTimeMillis();
        long limitation = currentTime + (fileSize * 60000); // adding amount of seconds.
        Log.d(TAG, "DL - starting time limitation");
        File localFile =
                new File(Environment.getExternalStorageDirectory().getPath()
                        + Paths.INTERNAL_DOWNLOAD_FOLDER
                        + m_oCurTask.getLimitationQuantity() + TOTAL_UNITS + "(iter "
                        + iterationsCounter + ")");
        // as long as we have not finish time limit:
        while (System.currentTimeMillis() < limitation) {
            if (!m_bNeedToCancelFTPAction) {
                // get an input + output stream for a new file:
                inputStream = m_oDownloadFtpClient.retrieveFileStream(serverFileName);
                outputStream = new BufferedOutputStream(new FileOutputStream(
                        localFile));
                if (inputStream == null) {
                    Log.d(TAG, "Error! null INPUT STREAM!!");
                }
                if (outputStream == null) {
                    Log.d(TAG, "Error! null OUTPUT STREAM!!");
                }
                Log.d(TAG, "DL - initialized: in + out streams:");
                m_nRrateStartTime = System.nanoTime();// get start time
                while (System.currentTimeMillis() < limitation) {
                    // check if finished reading current file:
                    if ((bytesRead = inputStream.read(bytesArray)) == -1) {
                        Logger.write("DL - finished current file + ",
                                getApplicationContext());
                        Log.d(TAG, "DL - finished current file");
                        m_nDownloadFinishTime = System.nanoTime();
                        break;
                    }
                    // check if finished time / task canceled:
                    if (!m_bNeedToCancelFTPAction) {
                        outputStream.write(bytesArray, 0, bytesRead);
                        m_nDownloadFinishTime = System.nanoTime();
                        // calculate total bytes so far:
                        m_nTotalReceivedBytes += bytesRead; // count bytes
                        rateElapsedTime = (m_nDownloadFinishTime - m_nRrateStartTime);
                        bytesReadInSecond += bytesRead;
                        // update GUI every one second:
                        if ((rateElapsedTime) >= Constants.secondInNano) {
                            // should be rateElapsedTime calculated in seconds
                            updateDownloadGuiThread(1, bytesReadInSecond);
                            m_nRrateStartTime = System.nanoTime();
                            bytesReadInSecond = NO_BYTES;
                        }
                    } else {
                        Log.d(TAG, "Exit from time limit");
                        updateDownloadGuiThread(1, NO_BYTES); // NO BYTES READ IN 1 SEC
                        break;
                    }
                }
                // do not call completePending if download havn't finished.
                // it will force download to finish before moving on.
                Log.d(TAG, "DL - Loop Ended");
                boolean positiveIntermediate = FTPReply.isPositiveIntermediate(m_oDownloadFtpClient.getReplyCode()); // stuck if we call it before download finished
                Log.d(TAG, "positive reply? = " + positiveIntermediate);
                if (m_bNeedToCancelFTPAction) {
                    Logger.write("DL Job was Cancelled", getApplicationContext());
                    downloadedData = downloadFilesCounter * fileSize;
                    inputStream.close(); // close streams
                    outputStream.close(); // close streams
                    return downloadedData;
                } else {
                    // option 2: time end up:
                    if (System.currentTimeMillis() > limitation) {
                        Logger.write("dl - Time End up before file",
                                getApplicationContext());
                        Log.d(TAG, "Dl - Done time before file (1/2)");
                        m_nDownloadFinishTime = System.nanoTime();
                        downloadedData = downloadFilesCounter * fileSize;
                        outputStream.close(); // close streams
                        inputStream.close(); // close streams
                        // need to be after close:
                        m_oDownloadFtpClient.completePendingCommand();
                        Log.d(TAG, "Dl - Done time before file (2/2)");
                        return downloadedData;
                    } else {
                        // option 3: end of file befor end of time:
                        Logger.write("File end up before time", getApplicationContext());
                        downloadFilesCounter++;
                        downloadedData = downloadFilesCounter * fileSize;
                        inputStream.close(); // close streams
                        outputStream.close(); // close streams
                    }
                }
                m_oDownloadFtpClient.completePendingCommand();
                downloadedData = downloadFilesCounter * fileSize;
                m_nDownloadFinishTime = System.nanoTime();
            }
        }
        return downloadedData;
    }
    /**
     * initialize values.
     */
    private void initializeValues() {
        runningUlAndDlTask = false;
        m_nDownloadStartTime = 0;
        m_nDownloadFinishTime = 1;
        m_nDownloadElapsedTime = 0;
        m_nUploadStartTime = 0;
        m_nUploadFinishTime = 1;
        m_nUploadElapsedTime = 0;
        isErrorAccoured = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getString(
                Constants.UNITS, Constants.KBPS).equals(Constants.KBPS)) {
            m_oUnitConvertor = new KbpsUnits();
            m_sFormat = Constants.KBPS_FORMAT;
        } else { //   MB/s
            m_oUnitConvertor = new MbsUnits();
            m_sFormat = Constants.MBs_FORMAT;
        }
        m_nIterations = m_oCurTask.getIterations();
        m_nLimitationQuantity = m_oCurTask.getLimitationQuantity();
        Log.d(TAG, "GOT TIME FOR TASK = " + m_nLimitationQuantity);
        m_nTotalSentBytes = 0; // counting the total bytes sent
        m_nTotalReceivedBytes = 0; // counting the total bytes received
        m_bNeedToCancelFTPAction = false; // task is running.
        // get new intents for sending messages:
        m_oRunButtonIntent = new Intent(Constants.ACTION_STOP);
        m_oIMessage = new Intent(Constants.ACTION);  //can put anything in it with putExtra
        isTaskRunning = true;
        m_bNeedToCancelFTPAction = false;
    }
    /**
     * Connects client to ftp server.
     * @param client for connection
     * @throws Exception
     */
    public void connectToFtp(FTPClient client) throws Exception {
        // FTP server details:
        Server server = m_oCurTask.getServer();
        String serverIp = server.getServerIp();
        int portNumber = server.getPort();
        String user = server.getUserName();
        String password = server.getPassword();
        client.connect(serverIp, portNumber);
        client.login(user, password); // user, password
        //m_oLogger.log("Logged in");
        client.setFileType(FTP.BINARY_FILE_TYPE);
        //m_oLogger.log("Downloading");
        client.enterLocalPassiveMode();
        // logged in - success:
        Logger.write("logged in: "
                + server + " " + serverIp , this);
    }
    /**
     * dis-Connects client from ftp server.
     * @param client for connection
     * @throws Exception
     */
    public void disconnectFromFtp(FTPClient client) {
        try {
            if (client != null) {
                Logger.write("Logging out from server..", this);
                if (client.isConnected()) {
                    client.logout();
                } else {
                    Logger.write("can't Log out. client already logged out", this);
                }
                client.disconnect();
                Logger.write("Logged out!", this);
            }
        } catch (IOException e) {
            Logger.error("Error while disconnecting from server", this);
            isErrorAccoured = true;
        }
    }
    @Override
    public void runTask() {
        isErrorAccoured = false;
        Logger.write("run task", this);
        // get parameters from Task and initialize variables
        initializeValues();
        // send a message to disable "run" button.(test is started, we can't run a new one)
        m_oRunButtonIntent.putExtra(Constants.STATUS, true); // test is running.
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oRunButtonIntent);
        // send beginning message to m_sStatus screen activity:
        m_oIMessage.putExtra(Constants.STATUS, Constants.RUNNING);
        m_oIMessage.putExtra(Constants.MODE, Constants.TCP_MODE);
        m_oIMessage.putExtra(Constants.ITERATION, "1/" + m_nIterations);
        m_oIMessage.putExtra(Constants.SENT, Constants.NONE + TOTAL_UNITS); //0MB
        m_oIMessage.putExtra(Constants.RECEIVED, Constants.NONE + TOTAL_UNITS);
        m_oIMessage.putExtra(Constants.CURRENT_UP_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.CURRENT_DN_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.AVG_DN_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.AVG_UP_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.ELAPSED_TIME, Constants.LONG_NONE_VALUE);
        m_nRunningMode = m_oCurTask.getMode();
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        try {
            // if we are in upload m_sMode:
            if (m_nRunningMode == Constants.UP_MODE) {
                Logger.write("Running Upload task", this);
                runningUlAndDlTask = false;
                connectToFtp(m_oUploadFtpClient);
                upload();
                // if we are in download m_sMode:
            } else if (m_nRunningMode == Constants.DN_MODE) {
                Logger.write("Running Download task", this);
                runningUlAndDlTask = false;
                connectToFtp(m_oDownloadFtpClient);
                download();
            } else { // download and upload m_sMode: (both)
                Logger.write("Running Up + down task", this);
                connectToFtp(m_oDownloadFtpClient);
                connectToFtp(m_oUploadFtpClient);
                runningUlAndDlTask = true;
                uploadAndDownload();
            }
        } catch (SocketException e1) {
            isErrorAccoured = true;
            Logger.error("SocketException: Network error accoured while job was running", this);
            m_oIMessage.putExtra(Constants.STATUS, Constants.NETWORK_ERROR);
            LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        } catch (IOException e1) {
            isErrorAccoured = true;
            Logger.error("IOException Error accoured", this);
            m_oIMessage.putExtra(Constants.STATUS, Constants.CONNECTION_ERROR);
            LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        } catch (Exception e1) {
            isErrorAccoured = true;
            Logger.error("Exception accoured", this);
            m_oIMessage.putExtra(Constants.STATUS, Constants.ERROR);
            LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        } finally {
            if (isErrorAccoured) {
                m_oIMessage.putExtra(Constants.STATUS, Constants.NETWORK_ERROR);
                LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
            }
            // send a message to enable "run" button.(test is done, we can run new one)
            m_oRunButtonIntent.putExtra(Constants.STATUS, false); // test is done.
            LocalBroadcastManager.getInstance(this).sendBroadcast(m_oRunButtonIntent);
            isTaskRunning = false;
            disconnectFromFtpServer();
        }
        if (!isErrorAccoured) {
            // notify activity that job done:
            m_oIMessage.putExtra(Constants.STATUS, Constants.JOB_DONE);
            ///LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
            m_oIMessage.putExtra(Constants.ITERATION,
                    m_nIterations + "/" + m_nIterations);
            // update intent with current m_sStatus:
            m_oIMessage.putExtra(Constants.RECEIVED, String.format(m_sFormat + Constants.TOTAL_UNITS,
                    ((m_nTotalReceivedBytes / (1024.0 * 1024.0)))));
            m_oIMessage.putExtra(Constants.SENT, String.format(m_sFormat + Constants.TOTAL_UNITS,
                    ((m_nTotalSentBytes / (1024.0 * 1024.0)))));
            LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        }
    }
    /**
     * Disconnect client from ftp server.
     * @throws IOException
     */
    private void disconnectFromFtpServer() {
        if (m_nRunningMode == Constants.UP_MODE) {
            disconnectFromFtp(m_oUploadFtpClient);
            // if we are in download m_sMode:
        } else if (m_nRunningMode == Constants.DN_MODE) {
            disconnectFromFtp(m_oDownloadFtpClient);
        } else {
            disconnectFromFtp(m_oUploadFtpClient);
            disconnectFromFtp(m_oDownloadFtpClient);
        }

    }

    /**
     * empty implementation for FTP task
     */
    protected void stopNttTask() {
        // no NTT task
    }

    /**
     * download task
     */
    @Override
    boolean download() throws Exception {
        dlCurrentIteration = 1;
        int ReceivedBytes = 0;
        // change working directory:
        boolean success = m_oDownloadFtpClient.changeWorkingDirectory(Paths.SERVER_DOWNLOADS_FOLDER);
        if (!success) {
            Logger.error("Error in changing working-dir", this);
        }
        //while we have another iteration to do:
        m_nTotalReceivedBytes = 0;
        m_nDownloadStartTime = System.nanoTime();
        m_nDownloadFinishTime = m_nDownloadStartTime + 1;
        while (dlCurrentIteration <= m_nIterations) {
            Log.d(TAG, "DL - new iteration");
            m_oIMessage.putExtra(Constants.ITERATION, dlCurrentIteration + "/" + m_nIterations);
            ReceivedBytes += downloadByTimeLimitation(dlCurrentIteration,
                    m_nLimitationQuantity);
            Log.d(TAG, "DL - iteration " + dlCurrentIteration+ " completed");
            // if we don't need to cancel, update views and send a broadcast:
            if (m_bNeedToCancelFTPAction) {
                break;
            } else {
                /*Log.d(TAG, "DL - Mission keep on running. iteration: " +
                        dlCurrentIteration +"/" + m_nIterations);*/
                dlCurrentIteration++;
                // after iteration done - wait for upload task (when running both):
                while (runningUlAndDlTask
                        && (dlCurrentIteration <= m_nIterations)
                        && (dlCurrentIteration >
                        ulCurrentIteration)) {
                    Thread.sleep(200);
                }

                String totalMbStr = String.format(m_sFormat + Constants.TOTAL_UNITS,
                        ((m_nTotalReceivedBytes/(1024.0 * 1024.0))));
                // update intent with current m_sStatus:
                m_oIMessage.putExtra(Constants.RECEIVED, totalMbStr);
                LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
                // if checkbox: "advance on key" is checked:
                if (m_bIsAdvanceByKeyPressed) {
                    Log.d(TAG, "DL - advance by key pressed");
                    // change m_sStatus to pause:
                    m_oIMessage.putExtra(Constants.STATUS, Constants.PAUSE);
                    totalMbStr = String.format(m_sFormat + Constants.TOTAL_UNITS,
                            ((m_nTotalReceivedBytes/(1024.0 * 1024.0))));
                    m_oIMessage.putExtra(Constants.RECEIVED, totalMbStr);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
                    while (!m_bIsNextIterationKeyPressedDN) {
                        //Log.d(TAG, "DL - m_bIsNextIterationKeyPressedDN");
                        Thread.sleep(200);
                    }
                    // change m_sStatus to running:
                    m_oIMessage.putExtra(Constants.STATUS, Constants.RUNNING);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
                }
                Log.d(TAG, "m_bIsNextIterationKeyPressedDN = false");
                m_bIsNextIterationKeyPressedDN = false;
            }
        }
        m_nDownloadElapsedTime = ((m_nDownloadFinishTime - m_nDownloadStartTime)
                / Constants.miliToSecondsConvertor) + 1; // calculate elapsed time in seconds:
        float avgRate = getDataByUnits
                (m_nTotalReceivedBytes,
                        m_nDownloadElapsedTime);
        //float mbitsPerSec = (kbPerSec / (1024)) * 8;
        // get a broadcast sender, and send finished message:
        Logger.success("Job done: " + m_nTotalReceivedBytes/(1024*1024) + "MB was downloaded in "
                + m_nDownloadElapsedTime + " seconds. Speed: " + avgRate +
                SPEED_UNITS + ")" , this);
        m_oIMessage.putExtra(Constants.STATUS, Constants.DN_DONE);
        m_oIMessage.putExtra(Constants.CURRENT_DN_RATE, Constants.NONE_VALUE);
        m_oIMessage.putExtra(Constants.AVG_DN_RATE, (avgRate));
        String totalMbStr = String.format(m_sFormat + Constants.TOTAL_UNITS,
                (m_nTotalReceivedBytes/(1024.0 * 1024.0)));
        m_oIMessage.putExtra(Constants.RECEIVED, totalMbStr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(m_oIMessage);
        if (ReceivedBytes > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * method that use strategy pattern to determine
     * how to calculate values in run time.
     * defined in sharedPreference.
     * @param bytes for calculation
     * @param time for calculation.
     * @return data by format (KBps/ MBs)
     */
    private float getDataByUnits(long bytes, long time) {
        return m_oUnitConvertor.getDataByUnits(bytes, time);
    }
    /*
  * add server to list of servers.
  * for FTPTaskRunner list will contain FTP servers
  * and for NTT - NTT servers.
  */
    protected void addServer(Server newServer) {
        if (!m_oFTPServersList.contains(newServer.getServerIp())) {
            m_oFTPServersList.add(newServer);
        }
    }
    /*
     * add list of servers to current list
     * for FTPTaskRunner list will contain FTP servers
     * and for NTT - NTT servers.
     */
    protected void addServers(List<Server> serversList) {
        m_oFTPServersList.addAll(serversList);
    }
    /**
     * get servers list.
     * @return servers list.
     * List<Server> list of servers.
     */@Override
    protected List<Server> getServersList() {
        return m_oFTPServersList;
    }
    @Override
    boolean uploadAndDownload() throws Exception {
        // execute download - running on the main thread:
        //download();
        // execute upload in a different thread:
        Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    download();
                } catch (Exception e) {
                    Logger.error("Download thread error" , getApplicationContext());
                    isErrorAccoured = true;
                }
            }
        });
        // execute upload in a different thread:
        Thread upload = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    upload();
                } catch (Exception e) {
                    Logger.error("Upload thread error" , getApplicationContext());
                    isErrorAccoured = true;
                }
            }
        });
        download.start();
        upload.start();
        // if download finished before upload, wait for upload.
        try {
            download.join();
            upload.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;

    }
}
