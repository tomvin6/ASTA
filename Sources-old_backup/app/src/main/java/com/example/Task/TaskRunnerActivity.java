package com.example.Task;

import com.example.Listeners.Logger;
import com.example.R;
import com.example.data.Constants;
import com.example.data.Paths;
import com.example.fieldtesttool.CostumizeTimeSetter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidLoggerz.AndroidLoggerz;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class TaskRunnerActivity extends Activity implements OnClickListener {
    private static final String TAG = "TaskRunnerActivity";
    protected FtpTaskRunner m_oFtpkRunner;
    protected NttTaskRunner m_oNttRunner;
    protected Task m_oCurTask;
    protected AndroidLoggerz m_oLogger;
    protected LocalBroadcastManager m_oBroadcaster;
    protected boolean m_bIsTestRunning, mValidationStatus, gotValidationStatus;
    protected int mTimeLimit, mOtherPosition;
    //members:
    protected TaskRunner m_oTaskRunner;
    List<Server> m_oServersList;
    protected Handler m_oHandler;
    protected Context m_oContext;
    protected Intent m_oStatusScreen;
    protected Bundle m_oTaskDetailsBundle;
    // GUI objects:
    protected Spinner m_oServersSpinner, m_oModeSpinner, m_oLimitSpinner,
            m_oULSession, m_oDNSession,m_oIteration,m_oLimitationQuantity,
            mProtocolSpinner;
    protected Button m_oNewServer, m_oRun, m_bStatus;
    protected CheckBox m_oTCPDump;
    protected EditText m_oBufferSize, m_oOtherTime, m_oTaskName;
    protected TextView m_oServerParams, m_oModeParams, m_oLimitParams, m_oLimitType,
            m_oULSessionParams, m_oDNSessionParams, m_oIterationsParams, m_oBuffer,
            m_otvQuanType, m_otvOtherTitle;
    protected Intent m_iTestByMode, m_iNewServerIntent;
    private Validator m_oValidator;
    protected int m_nLastServerSelection;

    abstract protected void loadParametersByType(File fieldTestFolder);
    abstract protected void initializeServersSpinner();
    abstract protected Task getTaskWithChosenValues();
    abstract protected void stopTaskService();
    abstract protected void specialSettingsByTaskType();
    abstract protected void runButtonPressed();
    protected abstract void initializeServerIntent();

    protected void runTestIfNoTestRunning() {
        Log.d(TAG, "runTestIfNoTestRunning()");
        // if test isn't running, run a new test and show m_sStatus screen:
        if (!m_bIsTestRunning) {
            m_bIsTestRunning = true;
            m_oRun.setEnabled(false);
            runButtonPressed();
            m_oTaskDetailsBundle = new Bundle();
            m_oTaskDetailsBundle.putString(Constants.TASK_NAME, m_oCurTask.getTaskName());
            // wait for validation process to end:
            // if validation returned true, run test:
            RunTestIfItsValid();
        } else {
            Toast.makeText(this,
                    Constants.TestAlreadyRunning, Toast.LENGTH_SHORT).show();
        }
    }
    /*
     * find view by id for all members:
     */
    public void initialize() {
        m_oHandler = new Handler(); // for validator
        m_oContext = this;
        m_oTaskName = (EditText) findViewById(R.id.name_text);
        // server
        m_oServerParams = (TextView) findViewById(R.id.server_param);
        m_oServersSpinner = (Spinner) findViewById(R.id.server_spinner);
        m_oNewServer = (Button) findViewById(R.id.new_server);
        // m_sMode - up / dn
        m_oModeSpinner = (Spinner) findViewById(R.id.mode_spinner);
        m_oModeParams = (TextView) findViewById(R.id.mode_param);
        // limit
        m_oLimitParams = (TextView) findViewById(R.id.limit_param);
        m_oLimitSpinner = (Spinner) findViewById(R.id.limit_spinner);
        m_oLimitationQuantity = (Spinner) findViewById(R.id.limit_insert_text);
        //m_oLimitType = (TextView) findViewById(R.id.limit_end);
        // ul / dn sessions
        m_oULSessionParams = (TextView) findViewById(R.id.ul_session_param);
        m_oDNSessionParams = (TextView) findViewById(R.id.dl_session_param);
        m_oULSession = (Spinner) findViewById(R.id.ul_session_spinner);
        m_oDNSession = (Spinner) findViewById(R.id.dl_session_spinner);
        // Buffer size:
        m_oBuffer = (TextView) findViewById(R.id.buffer_param);
        m_oBufferSize = (EditText) findViewById(R.id.buffer_text);
        mValidationStatus = true; // before validation process.
        // m_sIterations:
        m_oIterationsParams = (TextView) findViewById(R.id.iterations_text);
        m_oIteration = (Spinner) findViewById(R.id.iterations_spinner);
        m_otvQuanType = (TextView) findViewById(R.id.type_text);
        m_nLastServerSelection = 0;
        m_oOtherTime = (EditText) findViewById(R.id.costumize_time_text);
        m_otvOtherTitle = (TextView) findViewById(R.id.other_param);
        m_oLimitationQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int quantity = getLimitationQuantityFromSpinner();
                if (quantity == mOtherPosition) {
                    m_oOtherTime.setEnabled(true);
                    mTimeLimit = Integer.parseInt(m_oOtherTime.getText()
                            .toString().trim());
                    /*
                    Intent i = new Intent(TaskRunnerActivity.this, CostumizeTimeSetter.class);
                    String chosenMode = m_oLimitSpinner.getSelectedItem().toString();
                    if (chosenMode == Constants.TIME_SPINNER) {
                        i.putExtra(Constants.MODE, Constants.TIME_SPINNER);
                    } else {
                        i.putExtra(Constants.MODE, Constants.SIZE_SPINNER);
                    }
                    startActivityForResult(i, 1);
                    // mTimeLimit will be updated
                    */
                } else {
                    m_oOtherTime.setEnabled(false);
                    mTimeLimit = quantity;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        m_oLimitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int limitationType = getLimitationTypeFromSpinner();
                Log.d(TAG, "Limitation type = " + limitationType);
                if (limitationType == Constants.DATA_LIMITATION) {
                    m_otvQuanType.setText("MB");
                    m_otvOtherTitle.setText("Other Size");
                } else {
                    m_otvQuanType.setText("Min");
                    m_otvOtherTitle.setText("Other Time");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //tcp dump
        //m_oTCPDump = (CheckBox) findViewById(R.id.tcp_dump);
        // run button
        m_oRun = (Button) findViewById(R.id.run_button);
        m_bStatus = (Button) findViewById(R.id.status_button);
        m_bIsTestRunning = false;
        // set listeners on buttons:
        m_oRun.setOnClickListener(this);
        m_oNewServer.setOnClickListener(this);
        m_bStatus.setOnClickListener(this);
    }

    /*
     * find view by id for all members:
     */
    protected void loadDefaultTask() {
        // get path to internal device main folder:
        File fieldTestFolder = new File(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_MAIN_FOLDER_NAME);
        // create and initialize logger:
        AndroidLoggerz m_oLogger = AndroidLoggerz.getInstance();
        // specify logs folder
        m_oLogger.init(
                Environment.getExternalStorageDirectory()
                        + Paths.DEBUG_LOGS, Paths.DEBUG_LOG_FILE);
        loadParametersByType(fieldTestFolder);
    }

    /*
     * find view by id for all members:
     */
    public void initializeSpinners() {
        //        initializeServersSpinner(); moved to onResume
        // Array of Modes names acting as a data pump
        String[] mode = { "Upload", "Download", "Upload & Download"};
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item,mode);
        modeAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        m_oModeSpinner.setAdapter(modeAdapter);
        m_oModeSpinner.setSelection(0);

        // Array of Limits names acting as a data pump
        String[] limits = { Constants.TIME_SPINNER, Constants.SIZE_SPINNER};
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> limitsAdapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item ,limits);
        limitsAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        m_oLimitSpinner.setAdapter(limitsAdapter);
        m_oLimitSpinner.setSelection(0);

        // Array of Session options:
        String[] ulSessions = { "1", "2", "3", "4","5", "6",
                "7", "8","9", "10", "11", "12"};
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> sessionsAdapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item ,ulSessions);
        sessionsAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner (dn and up)
        m_oULSession.setAdapter(sessionsAdapter);
        m_oDNSession.setAdapter(sessionsAdapter);
        m_oDNSession.setSelection(0);
        m_oULSession.setSelection(0);

        // Iterations options:
        String[] iterations = { "1", "2", "3", "4","5", "6",
                "7", "8","9", "10", "11", "12"};
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> iterationsAdapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item ,iterations);
        iterationsAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        m_oIteration.setAdapter(iterationsAdapter);
        m_oIteration.setSelection(0);

        // Array of limitation options:
        String[] limitations = { "1", "2", "5", "10", "20", "60", "100",
                Constants.OTHER};
        mOtherPosition = limitations.length;
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> limitationAdapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item ,limitations);
        limitationAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        m_oLimitationQuantity.setAdapter(limitationAdapter);
        m_oLimitationQuantity.setSelection(0);
    }
    /**
     * convert arrayList to Array for arrayAdapter.
     * @param list Array to convert
     * @return array[]
     * Server[] - returned array.
     */
    protected String[] convertToArray(ArrayList<Server> list) {
        if (list != null) {
            int numberOfServers = list.size();
            String[] serverList = new String[numberOfServers];
            for (int i = 0; i < numberOfServers; i++) {
                serverList[i] = list.get(i).getServerName();
            }
            return serverList;
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftp_task_runner_activity);
        initialize(); // find view by id for all objects.
        loadDefaultTask(); // load defualt task from
        // memory to
        // m_oCurTask member.
        initializeSpinners();
        specialSettingsByTaskType();

    }


    /**
     * register broadcast receiver for enabling / disabling "run" button.
     */
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iFilter = new IntentFilter(Constants.ACTION_STOP);
        iFilter.addAction(Constants.VALIDATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iFilter);
    }


    /**
     * if test is running, set "run" button to be disabled.
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        m_oRun.setEnabled(!m_bIsTestRunning);
        initializeServersSpinner();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    // save and restore screen m_sStatus:
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putBoolean(Constants.RUN_BUTTON, m_oRun.isEnabled());
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        m_oRun.setEnabled(savedInstanceState.getBoolean(Constants.RUN_BUTTON));
    }
    /**
     * un-register broadcast receiver.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        super.onDestroy();
    }
    /**
     * iterate list of servers from internal memory.
     * return the server with corresponding name to the
     * Chosen server from the spinner.
     * @return Server
     * Server
     */
    public Server getServerFromSpinner() {
        //Log.d(TAG, "getServerFromSpinner()");
        Server server = null;
        String chosenServer = (String) m_oServersSpinner.getSelectedItem();
        for (int i = 0; i < m_oServersList.size(); i++) {
            server = m_oServersList.get(i);
            if (server.getServerName().equals(chosenServer)) {
                m_nLastServerSelection = i;
                break;
            }
        }
        if (server != null) {
            Log.d(TAG, "return new server: " + server.getServerName());
        } else {
            Log.d(TAG, "No server found (null)");
        }
        return server;
    }
    /**
     * get m_sMode from spinner. return Constants.FTP_MODE
     * @return Download, Upload, Both
     */
    public int getModeFromSpinner() {
        String chosenMode = (String) m_oModeSpinner.getSelectedItem();
        if (chosenMode.equals("Download")) {
            Log.d(TAG, "download m_sMode chosen");
            return Constants.DN_MODE;
        } else if(chosenMode.equals("Upload")) {
            Log.d(TAG, "upload m_sMode chosen");
            return Constants.UP_MODE;
        } else {
            Log.d(TAG, "both m_sMode chosen");
            return Constants.DN_UP_MODE;
        }
    }
    /**
     * get limitation type from spinner. return Constants.LIMITATION
     * @return Time, Data
     */
    public int getLimitationTypeFromSpinner() {
        String chosenMode = m_oLimitSpinner.getSelectedItem().toString();
        Log.d(TAG, "limitation type: " + chosenMode);
        if (chosenMode.equals(Constants.TIME_SPINNER)) {
            return Constants.TIME_LIMITATION;
        } else { // Data
            return Constants.DATA_LIMITATION;
        }
    }
    /**
     * get limitation type from spinner. return Constants.LIMITATION
     * @return Time, Data
     */
    public int getProtocolFromSpinner() {
        String chosenMode = (String) m_oLimitSpinner.getSelectedItem();
        if (chosenMode.equals(Constants.TIME_SPINNER)) {
            return Constants.TIME_LIMITATION;
        } else { // Data
            return Constants.DATA_LIMITATION;
        }
    }
    /**
     * get limitation type from spinner. return Constants.LIMITATION
     * @return Time, Data
     */
    /*
    public int getUpSessionFromSpinner() {
        String chosenMode = (String) m_oULSession.getSelectedItem();
        if (chosenMode.equals("Time")) {
            return Constants.TIME_LIMITATION;
        } else { // Data
            return Constants.DATA_LIMITATION;
        }
    }
     */
    /**
     * get limitation quantity from spinner. legal values: {1,2,5,10,20,50,100}
     * @return quantity.
     */
    public int getLimitationQuantityFromSpinner() {
        String quantity = m_oLimitationQuantity.getSelectedItem().toString();
        Log.d(TAG, "Quantity: " + quantity);
        if (quantity.equals(Constants.OTHER)) {
            return mOtherPosition;
        } else {
            return Integer.parseInt(quantity);
        }
    }

    /**
     * get result from time chooser activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                mTimeLimit = data.getIntExtra("result", 3);
                Log.d(TAG, "got time: " + mTimeLimit);
            } if (resultCode == RESULT_CANCELED) {
                //no result
            }
        }
    }//onActivityResult

    protected String createTaskName() {
        /*
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yy_HH:mm:ss a");
        String strDate = sdf.format(c.getTime());
        */
        String optionalName = m_oTaskName.getText().toString();
        Log.d(TAG, "task name: "+ optionalName);
        if (optionalName.contains(":")) {
            optionalName.replace(":","-");
        }
        if (optionalName.contains("/")) {
            optionalName.replace("/","-");
        }
        if (optionalName.isEmpty() || optionalName.equals("null")) {
            return "";
        } else {
            return (optionalName);
        }
    }

    protected void RunTestIfItsValid() {
        Log.d(TAG, "RunTestIfItsValid()");
        m_oCurTask = getTaskWithChosenValues();
        // background process: validate device, server folders and files.
        // on POST validation, if everything is legal -> run task
        new ValidationService().execute("");
    }

    /**
     * when clicking run button, it begins running in FTP / Ntt m_sMode.
     * @param v
     */
    @Override
    public void onClick(View v) {
        // on action click:
        switch(v.getId()) {
            case R.id.run_button:
                runTestIfNoTestRunning();
                //runTcpDumpIfEnabled();
                break;
            case R.id.status_button:
                if (m_bIsTestRunning) {
                    // show m_sStatus screen of current running test: (defined in manifest)
                    m_oStatusScreen = new Intent(TaskRunnerActivity.this,
                            TestStatusActivity.class);
                    m_oTaskDetailsBundle.putInt(Constants.MODE, m_oCurTask.getMode());
                    m_oTaskDetailsBundle.putInt(Constants.MODE_FTP_NTT, m_oCurTask.getFtpNttMode());
                    m_oTaskDetailsBundle.putString(Constants.SERVER, m_oCurTask.getServer().getServerIp());
                    m_oTaskDetailsBundle.putString(Constants.TASK_NAME, m_oCurTask.getTaskName());
                    m_oStatusScreen.putExtras(m_oTaskDetailsBundle);
                    startActivity(m_oStatusScreen);
                }
                break;
            case R.id.new_server:
                initializeServerIntent();
                startActivity(m_iNewServerIntent);
                break;

        }
    }

    private void runTcpDumpIfEnabled() {
        if (m_oTCPDump.isEnabled() == true) {
            Logger.write("TCPDump Enabled", this);
            // use this to start and trigger a service
            Intent tcpCommand = new Intent(TaskRunnerActivity.this, TCPDumpProcess.class);
            ///potentially add data to the intent
            //tcpCommand.putExtra("KEY1", "Value to be used by the service");
            //startService(tcpCommand);
        }
    }


    // receive broadcasts:
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == Constants.ACTION_STOP) {
                // get m_sStatus data for "run" button.
                m_bIsTestRunning = intent.getBooleanExtra(Constants.STATUS, false);
                m_oRun.setEnabled(!m_bIsTestRunning);
            }
        }
    };


    /**
     * show message if user want to exist a running task
     */
    @Override
    public void onBackPressed() {
        if (m_bIsTestRunning) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Test is currently running");
            builder.setMessage("Notice! Task will be canceled. Are you sure " +
                    "you want to exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    stopTaskService();
                    Log.d(TAG, "service was stopped");
                    finish();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            finish();
        }
    }

    // Async Task Class
    class ValidationService extends AsyncTask<String, String, String> {
        boolean m_bValidationStatus = false;
        ProgressDialog m_oProgressDialog;
        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_oProgressDialog = ProgressDialog.show(TaskRunnerActivity.this,
                    "Please wait", "Validating Task...");
        }

        // validatate parameters:
        @Override
        protected String doInBackground(String... f_url) {
            Log.d(TAG, "validation proccess");
            m_oValidator = new Validator(m_oContext, m_oHandler);
            m_bValidationStatus = m_oValidator.validate(m_oCurTask);
            if (m_bValidationStatus) {
                // put task values and run task:
                // at this point: mCurTask has required values.
                m_oTaskDetailsBundle.putInt(Constants.MODE, m_oCurTask.getMode());
                m_oTaskDetailsBundle.putInt(Constants.MODE_FTP_NTT, m_oCurTask.getFtpNttMode());
                m_oTaskDetailsBundle.putString(Constants.SERVER, m_oCurTask.getServer().getServerIp());
                m_oTaskDetailsBundle.putString(Constants.TASK_NAME, m_oCurTask.getTaskName());
                m_iTestByMode.putExtras(m_oTaskDetailsBundle);
                m_oStatusScreen.putExtras(m_oTaskDetailsBundle);
                m_iTestByMode.putExtra("task", m_oCurTask);
            }
            return m_oValidator.getResult();
        }

        // start task when validation finished
        @Override
        protected void onPostExecute(String file_url) {
            m_oProgressDialog.dismiss();
            if (m_bValidationStatus) {
                startService(m_iTestByMode);
                // define m_sStatus screen (in derived classes - runButtonPressed)
                //m_oStatusScreen.putExtras(m_oTaskDetailsBundle);
                startActivity(m_oStatusScreen);
            } else {
                Log.d(TAG, "Validation Failed: " + m_oValidator.getResult());
                Toast.makeText(m_oContext, "Task Not Valid! "
                        + m_oValidator.getResult(), Toast.LENGTH_SHORT).show();
                m_bIsTestRunning = false;
                m_oRun.setEnabled(true);
            }
        }
    }
}
