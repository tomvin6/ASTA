package com.example.Task;

import com.example.R;
import com.example.Listeners.CsvHandler;
import com.example.Listeners.Logger;
import com.example.data.Constants;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * TestStatusActivity is an activity than enable us to show current progress
 * to user.
 * it posts current results to screen and it also
 * update graphView model.
 */
public class TestStatusActivity extends Activity implements OnClickListener {
    private static final String TAG = "TestStatusActivity";
    private static String START_TIME; // FOR FILE NAME
    // field members:
    protected TextView m_tvMode, m_tvStatus,m_serverIp,
            m_tvSent, m_tvReceived, m_tvUp, m_tvdn, m_tvIterations;
    protected Button m_bNextIter, m_bStop;
    protected CheckBox m_bAdvanceByIter;
    protected boolean m_oStatus;
    // intents for messaging:
    Intent m_oStopServiceIntent, m_oItertationByKeyPressedIntent;
    Intent m_oNextIterButtonIntent;
    // fields to update:
    protected String m_sStatus, m_sMode, m_sIterations, sentBytes, receivedBytes, m_sTaskName;
    // graph variables:
    protected GraphView m_oGraph;
    private long m_nOldTime, m_nNewTime;
    //associates this handler with the Looper for the current thread.
    protected final Handler mHandler = new Handler();
    protected Runnable mAvgDnRunnable, mAvgUpRunnable, mCurUpRunnable,
            mCurDnRunnable, mCsvRunnable;
    protected double m_AvgDnRate, m_CurDnRate, m_AvgUpRate, m_CurUpRate;
    protected LineGraphSeries<DataPoint> m_oAvgUpSeries ,m_oAvgDownSeries,
            m_oCurDownSeries,m_oCurUpSeries;
    private static Time systemClock;
    private static long mTaskTimer;
    int mStatus, m_nUpdateFrequency, m_nCounter;
    private static CsvHandler mWriter = new CsvHandler();
    private static int mStartMin, mStartSec, mStartHour;
    Calendar mCalender;
    String mFormattedDate, mModeStr, mServerIp, m_sFormat, m_sUnits;

    /**
     * crete and initialize m_sStatus screen
     * @param savedInstanceState none
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_status);
        Bundle bundle = getIntent().getExtras();
        // get task m_sMode:
        int mode = bundle.getInt(Constants.MODE_FTP_NTT, 0);
        if (mode == Constants.FTP) {
            mModeStr = Constants.FTP_MODE;
        } else {
            mModeStr = Constants.NTT_MODE;
        }
        // change measurement units by preference:
        loadPreferenceValues();
        m_sTaskName = bundle.getString(Constants.TASK_NAME);
        Log.d("TaskRunnerActivity", "Status:m_sTaskName: " + m_sTaskName);
        // screen stays on activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initialize();
        // initialize graph:
        m_oGraph = (GraphView) findViewById(R.id.graph);
        // instantiate 4 lines
        m_oAvgDownSeries = new LineGraphSeries<DataPoint>();
        m_oAvgUpSeries = new LineGraphSeries<DataPoint>();
        m_oCurDownSeries = new LineGraphSeries<DataPoint>();
        m_oCurUpSeries = new LineGraphSeries<DataPoint>();
        //add down/up series to the graph.
        m_oGraph.addSeries(m_oAvgDownSeries);
        m_oGraph.addSeries(m_oAvgUpSeries);
        m_oGraph.addSeries(m_oCurDownSeries);
        m_oGraph.addSeries(m_oCurUpSeries);
        // set automatic bounds
        m_oGraph.getViewport().setXAxisBoundsManual(false);
        //m_oGraph.getViewport().setYAxisBoundsManual(false);
        // set manual Y bounds: need to be like that(graph bug)
        m_oGraph.getViewport().setYAxisBoundsManual(false);
        //m_oGraph.getViewport().setMinY(0);
        //m_oGraph.getViewport().setMaxY(3);
        // set series properties:
        m_oCurDownSeries.setColor(Color.BLUE);
        m_oAvgDownSeries.setColor(Color.rgb(112,147,219)); //light blue
        m_oCurUpSeries.setColor(Color.RED);
        m_oAvgUpSeries.setColor(Color.rgb(165, 42, 42)); // brown
        m_oAvgDownSeries.setTitle(Constants.DN_AVG_TITLE); //dn headline
        m_oAvgUpSeries.setTitle(Constants.UP_AVG_TITLE);     //up headline
        m_oCurDownSeries.setTitle(Constants.DN_TITLE); //dn headline
        m_oCurUpSeries.setTitle(Constants.UP_TITLE);     //up headline
        // set graph properties:
        //m_oGraph.getViewport().setMinY(0);
        m_oGraph.getLegendRenderer().setVisible(true);
        m_oGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        //m_oGraph.getLegendRenderer().setPadding(50); // headline distance from boarders
        //m_oGraph.getGridLabelRenderer().setHorizontalAxisTitle("");
        //m_oGraph.getGridLabelRenderer().setVerticalAxisTitle("");
        //m_oGraph.getGridLabelRenderer().setVerticalAxisTitleTextSize(25);
        //m_oGraph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(25); // "data (mb)" time (sec)
        m_oGraph.getGridLabelRenderer().setTextSize(20); // labels size (vertical + horizontal)
        m_oGraph.getGridLabelRenderer().setNumHorizontalLabels(9);
        m_oGraph.getGridLabelRenderer().setNumVerticalLabels(7); //set max/min
        m_oGraph.getLegendRenderer().setTextSize(25); // headline (mikra)
        /* // labels size (data / time)
        m_oGraph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(10);
        m_oGraph.getGridLabelRenderer().setVerticalAxisTitleTextSize(10);
        */
        // get task m_sMode and remove unused series
        mServerIp = bundle.getString(Constants.SERVER);
        mStatus = bundle.getInt(Constants.MODE);
    }

    /**
     * load preference values
     */
    private void loadPreferenceValues() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // load preference for units
        if (sharedPref.getString(
                Constants.UNITS, Constants.KBPS).equals(Constants.KBPS)) {
            Constants.SPEED_UNITS = "KBps";
            m_sUnits = "KBps";
            m_sFormat = Constants.KBPS_FORMAT;
        } else {    //Mb/s
            Constants.SPEED_UNITS = "Mb/s";
            m_sUnits = "Mb/s";
            m_sFormat = Constants.MBs_FORMAT;
        }
        // import preferences for points data history
        Constants.maxGraphPoints = Integer.parseInt(
                sharedPref.getString("graph_points_title", "50"));
        m_nUpdateFrequency = Integer.parseInt(
                sharedPref.getString("graph_update_freq", "1"));
    }

    /**
     * method to update m_sStatus by m_sMode.
     * if UP / DN values are illegal - fix them.
     */
    protected void updateStatusByMode() {
        // check for legal value:
        if (m_AvgDnRate >= 5000 || m_AvgDnRate < 0 ) {
            m_AvgDnRate = 0;
            Logger.write("DN- Got Nah/Infinity value", getApplicationContext());
        }
        if (m_AvgUpRate >= 5000 || m_AvgUpRate < 0 ) {
            Logger.write("UP- Got Nah/Infinity value", getApplicationContext());
            m_AvgUpRate = 0;
        }
        if (m_CurDnRate >= 5000 || m_CurDnRate < 0 ) {
            m_CurDnRate = 0;
            Logger.write("DN- Got Nah/Infinity value", getApplicationContext());
        }
        if (m_CurUpRate >= 5000 || m_CurUpRate < 0 ) {
            Logger.write("UP- Got Nah/Infinity value", getApplicationContext());
            m_CurUpRate = 0;
        }
    }
    public String[] convertSecondsToTime(){
        int hours = (int) mTaskTimer / 3600;
        int remainder = (int) mTaskTimer - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        String [] times = {String.format("%02d", hours),
                String.format("%02d", minutes),
                String.format("%02d", remainder)};
        return times;
    }
    /**
     * report m_sStatus to CSV file.
     */
    public void report() {
        String avgDnRate = String.format(m_sFormat, m_AvgDnRate);
        String curDnRate = String.format(m_sFormat, m_CurDnRate);
        String avgUpRate = String.format(m_sFormat, m_AvgUpRate);
        String curUpRate = String.format(m_sFormat, m_CurUpRate);
        systemClock.setToNow();
        // calculate relative time (seconds):
        mTaskTimer = ((systemClock.hour - mStartHour) * 60 * 60)
                + ((systemClock.minute - mStartMin) * 60)
                + (systemClock.second - mStartSec)  ;
        String [] timeValues = convertSecondsToTime();
        // get relative time:
        String relativeTime = timeValues[0] + ":"
                + timeValues[1] + ":" + timeValues[2];
        // get system time and format it:
        String systemTime =
        String.format("%02d", systemClock.hour) + ":"
                + String.format("%02d", systemClock.minute)
                + ":" + String.format("%02d", systemClock.second);
        String [] iterations = m_sIterations.split("/");
        String [] values = {systemTime, relativeTime,
                avgDnRate,curDnRate,avgUpRate,curUpRate, iterations[0], iterations[1]};
        mWriter.write(values);
    }
    /**
     * Get messages from Ftp download Task.
     */
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // check if job is running:/*
            m_sStatus = intent.getStringExtra(Constants.STATUS);
            // get all strings data from intent:
            m_sIterations = intent.getStringExtra(Constants.ITERATION);
            m_sMode = intent.getStringExtra(Constants.MODE);
            sentBytes = intent.getStringExtra(Constants.SENT);
            receivedBytes = intent.getStringExtra(Constants.RECEIVED);
            m_AvgDnRate = intent.getFloatExtra(Constants.AVG_DN_RATE, -1);
            m_CurDnRate = intent.getFloatExtra(Constants.CURRENT_DN_RATE, -1);
            m_AvgUpRate = intent.getFloatExtra(Constants.AVG_UP_RATE, -1);
            m_CurUpRate = intent.getFloatExtra(Constants.CURRENT_UP_RATE, -1);
            m_nNewTime = intent.getLongExtra(Constants.ELAPSED_TIME, -1);
            //Log.d(TAG, "dl rate: " + m_CurDnRate + "ul rate: " + m_CurUpRate);
            // set texts:
            m_tvStatus.setText(m_sStatus);
            m_tvIterations.setText(m_sIterations);
            m_tvMode.setText(m_sMode);
            updateStatusByMode();
            //if job is Running, draw lines on graph and update views:
            if (!m_sStatus.equals(Constants.JOB_DONE)) {
                if (m_sStatus.equals(Constants.RUNNING)
                        || (m_sStatus.equals(Constants.DN_DONE))
                        || (m_sStatus.equals(Constants.UP_DONE))) {
                    if (m_nOldTime != m_nNewTime) {
                        m_bNextIter.setTextColor(Color.BLACK);
                        m_oStatus = true; //true = running
                        m_bStop.setEnabled(true);
                        if (m_nCounter % m_nUpdateFrequency == 0) {
                            mHandler.post(mAvgUpRunnable);
                            mHandler.post(mAvgDnRunnable);
                        }
                        // optional - two handlers for 2 series:
                        //mHandler.post(mCurUpRunnable);
                        //mHandler.post(mCurDnRunnable);
                        mHandler.post(mCsvRunnable);
                        m_nCounter++;
                    }
                    m_nOldTime = m_nNewTime;
                } else if (m_sStatus.equals(Constants.PAUSE)) {
                    m_bNextIter.setTextColor(Color.RED);
                } else { // error
                    m_bStop.setEnabled(false);
                    m_tvStatus.setTextColor(Color.RED);
                    String[] values = {m_sMode, m_sStatus, mFormattedDate,
                            "Aborted",
                            "0", "0", "0", "0", "0", "0"};
                    mWriter.write(values); // write error to test csv file
                    // summary file
                    mWriter.finishCsvBuffer(); // write leftovers.
                    mWriter.saveTestSummary(values); // write error to
                }
            } else { // done running:
                m_bNextIter.setTextColor(Color.BLACK);
                m_tvStatus.setTextColor(getResources().getColor(
                        R.color.DarkSeaGreen));
                m_tvSent.setText(sentBytes);
                m_tvReceived.setText(receivedBytes);
                // set graph scalable when mission done:
                m_oGraph.getViewport().setScrollable(true);
                m_oStatus = false;
                m_bStop.setEnabled(false);
                m_bNextIter.setEnabled(false);
                mWriter.finishCsvBuffer(); // write leftovers.
                // write summary values to a file:
                String[] values = new String[Constants.TOTAL_VALUES];
                values[Constants.MODE_POS] = m_sMode;
                values[Constants.STATUS_POS] = "Finished";
                values[Constants.DATE_POS] = mFormattedDate;
                values[Constants.START_TIME_POS] = START_TIME;
                values[Constants.TOTAL_SENT_POS] = sentBytes;
                values[Constants.TOTAL_RECEIVED_POS] = receivedBytes;
                values[Constants.AVG_DN_POS] = String.format(m_sFormat +
                                m_sUnits,
                        m_AvgDnRate);
                values[Constants.AVG_UP_POS] = String.format(m_sFormat +
                                m_sUnits,
                        m_AvgUpRate);
                values[Constants.TOTAL_TEST_TIME_POS] = Double.toString
                        (m_nNewTime);
                values[Constants.TASK_NAME_POS] = (m_sTaskName);
                mWriter.saveTestSummary(values);
            }
        }
    };

    /**
     * post current values when onResume is being called.
     */
    @Override
    public void onResume() {
        super.onResume();
        /* Append Data parameters:
        value - the new data to append (dataPoint)
        boolean scrollToEnd - true => graphview will scroll to the end (maxX)
        maxDataCount - if max data count is reached, the oldest data value will be lost
         */
        m_AvgUpRate = 0;
        m_AvgDnRate = 0;
        m_CurUpRate = 0;
        m_CurDnRate = 0;
        mAvgUpRunnable = new Runnable() {
            @Override
            public void run() {
                m_oAvgUpSeries.appendData(
                        new DataPoint(m_nNewTime, m_AvgUpRate), false,
                        Constants.maxGraphPoints);
                String ulThp = String.format(m_sFormat + m_sUnits,
                        m_CurUpRate);
                m_tvUp.setText(ulThp);
                m_tvSent.setText(sentBytes);
                m_oCurUpSeries.appendData(
                        new DataPoint(m_nNewTime, m_CurUpRate), false,
                        Constants.maxGraphPoints);
            }
        };
        mAvgDnRunnable = new Runnable() {
            @Override
            public void run() {
                m_oAvgDownSeries.appendData(
                        new DataPoint(m_nNewTime, m_AvgDnRate), false,
                        Constants.maxGraphPoints);
                String dnThroughput = String.format(m_sFormat + m_sUnits,
                        m_CurDnRate);
                m_tvdn.setText(dnThroughput);
                m_tvReceived.setText(receivedBytes);
                m_oCurDownSeries.appendData
                        (new DataPoint(m_nNewTime, m_CurDnRate), false,
                                Constants.maxGraphPoints);
            }
        };
        mCurUpRunnable = new Runnable() {
            @Override
            public void run() {
                m_oCurUpSeries.appendData
                        (new DataPoint(m_nNewTime, m_CurUpRate), false,
                                Constants.maxGraphPoints);
            }
        };
        mCurDnRunnable = new Runnable() {
            @Override
            public void run() {
                m_oCurDownSeries.appendData(
                        new DataPoint(m_nNewTime, m_CurDnRate), false,
                        Constants.maxGraphPoints);
            }
        };
        mCsvRunnable = new Runnable() {
            @Override
            public void run() {
                report();
            }
        };
        // post handlers because of graph bug:
        mHandler.post(mAvgUpRunnable);
        mHandler.post(mAvgDnRunnable);
        Log.d(TAG, "onresume. m_sStatus = " + mStatus +" ip " + mServerIp);
        // set m_sStatus from on create initialization:
        if (mStatus == Constants.DN_MODE) {
            m_oGraph.removeSeries(m_oAvgUpSeries);
            m_oGraph.removeSeries(m_oCurUpSeries);
        } else if (mStatus == Constants.UP_MODE) {
            m_oGraph.removeSeries(m_oAvgDownSeries);
            m_oGraph.removeSeries(m_oCurDownSeries);
        }
        // set server ip:
        m_serverIp.setText(mServerIp);
    }

    /**
     * remove handlers when activity goes in pause state.
     */
    @Override
    public void onPause() {
        mHandler.removeCallbacks(mAvgUpRunnable);
        mHandler.removeCallbacks(mAvgDnRunnable);
        mHandler.removeCallbacks(mCurUpRunnable);
        mHandler.removeCallbacks(mCurDnRunnable);
        super.onPause();
    }

    /**
     * register receiver when starting activity
     */
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iFilter = new IntentFilter(Constants.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iFilter);
    }
    /**
     * unregister receivers when activity goes out of screen.
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        super.onDestroy();
    }

    /*
     * find view by id for all members:
     */
    public void initialize() {
        // server
        m_tvMode = (TextView) findViewById(R.id.mode_param);
        m_tvStatus = (TextView) findViewById(R.id.status_param);
        m_tvSent = (TextView) findViewById(R.id.sent_param);
        m_tvReceived = (TextView) findViewById(R.id.received_param1);
        m_tvUp = (TextView) findViewById(R.id.up_param);
        m_tvdn = (TextView) findViewById(R.id.dl_param);
        m_tvIterations = (TextView) findViewById(R.id.iterations_param);
        m_bNextIter = (Button) findViewById(R.id.next_iter_button);
        m_bStop = (Button) findViewById(R.id.stop_button);
        m_serverIp = (TextView) findViewById(R.id.ip_param);
        //m_bGraph = (Button) findViewById(R.id.stop_button);
        m_bAdvanceByIter = (CheckBox) findViewById(R.id.advance_on_checkbox);
        m_bNextIter.setOnClickListener(this);
        m_bStop.setOnClickListener(this);
        systemClock = new Time();
        Time startTime = new Time();
        m_AvgDnRate = 0;
        m_AvgUpRate = 0;
        m_CurDnRate = 0;
        m_CurUpRate = 0;
        m_nOldTime = 0;
        mTaskTimer = 0;
        m_nCounter = 0;
        m_bStop.setEnabled(false);
        m_oStopServiceIntent = new Intent(Constants.ACTION_STOP);
        m_oItertationByKeyPressedIntent = new Intent(Constants.ITERATION_BY_KEY);
        m_oNextIterButtonIntent = new Intent(Constants.NEXT_ITER_BUTTON);
        m_bNextIter.setEnabled(false);
        // on checked: iteration by button press listener:
        m_bAdvanceByIter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(
                    CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Logger.write("pressed", getApplicationContext());
                    m_oItertationByKeyPressedIntent.putExtra(Constants.IS_PRESSED, true);
                    LocalBroadcastManager.getInstance(
                            getApplicationContext()).sendBroadcast(m_oItertationByKeyPressedIntent);
                    m_bNextIter.setEnabled(true);
                } else {
                    //Logger.write("not pressed", getApplicationContext());
                    m_oItertationByKeyPressedIntent.putExtra(Constants.IS_PRESSED, false);
                    LocalBroadcastManager.getInstance(
                            getApplicationContext()).sendBroadcast(m_oItertationByKeyPressedIntent);
                    m_bNextIter.setEnabled(false);
                }
            }});
        // set start time:
        startTime.setToNow();
        mStartMin = startTime.minute;
        mStartSec = startTime.second;
        mStartHour = startTime.hour;
        if (mStartMin<10) {
            START_TIME = "0" + mStartMin;
        } else {
            START_TIME = "" + mStartMin;
        }
        if (mStartHour<10) {
            START_TIME = "0" + mStartHour + START_TIME;
        } else {
            START_TIME = mStartHour + "" + START_TIME;
        }
        // get date in specified format:
        mCalender = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        mFormattedDate = df.format(mCalender.getTime());
        // initialize CSV file for the test:
        mWriter.setCsvFile(mFormattedDate + "_" + START_TIME
                +"_"+ mModeStr + m_sTaskName + ".csv", m_sUnits);
    }

    /**
     * on click method to operate screen buttons.
     * @param v view of the button that have been pressed.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.next_iter_button:
                // send message: advance to next iteration
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                        m_oNextIterButtonIntent);
                break;
            case R.id.stop_button:
                m_bStop.setEnabled(false);
                // only if test is running, stop it:
                Toast.makeText(this, "Cancel Job, Please wait..",
                        Toast.LENGTH_LONG).show();
                if (m_oStatus){ //m_sStatus == true-> running
                    Logger.write("Cancelling current job", getApplicationContext());
                    // send a message to enable "run" button.(test is stopped)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            m_oStopServiceIntent);
                    m_tvStatus.setText("Abort");
                } else {
                    Logger.write("No job to cancel", getApplicationContext());
                    Toast.makeText(
                            this, "Test currently not running", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}