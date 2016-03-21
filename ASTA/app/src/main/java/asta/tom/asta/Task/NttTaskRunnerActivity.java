package com.example.Task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.Listeners.Logger;
import com.example.R;
import com.example.data.Constants;
import com.example.fieldtesttool.CostumizeTimeSetter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * activity that derives from TaskRunnerActivity.
 * responsible for creating and initializing NTT Task
 */
public class NttTaskRunnerActivity extends TaskRunnerActivity {

    private static final String TAG = "NttTaskRunnerActivity" ;
    private EditText mUlSpeed, mDLSpeed;
    /**
     * this method in TaskRunnerActivity class will disable
     * un-relevant fields.
     * this method should be in FTP task runner class
     */
    protected void specialSettingsByTaskType() {
        // currently all fields are enabled in NTT runner activity.
        mUlSpeed = (EditText) findViewById(R.id.ul_speed_text);
        mDLSpeed = (EditText) findViewById(R.id.dl_speed_text);
        m_oBufferSize.requestFocus(); // request focus to editText.
    }

    /**
     * initialize Ntt servers:
     */
    protected void initializeServersSpinner() {
        // convert servers list to array:
        ArrayList<Server> serversList = (ArrayList<Server>) m_oNttRunner.getServersList();
        String[] serversArray = convertToArray(serversList);
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> servers_adapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item,serversArray);
        servers_adapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        m_oServersSpinner.setAdapter(servers_adapter);
        m_oServersSpinner.setSelection(m_nLastServerSelection);
    }

    /**
     * initialize values for protocol spinner (only in NTT task)
     */
    private void setProtocolSpinner() {
        // protocol initialization:
        mProtocolSpinner = (Spinner) findViewById(R.id.protocol_spinner);
        String[] protocol = { Constants.TCP_STR, Constants.UDP_STR};
        ArrayAdapter<String> protocolAdapter = new ArrayAdapter<String>(
                getApplicationContext(),R.layout.spinner_item,protocol);
        protocolAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        mProtocolSpinner.setAdapter(protocolAdapter);
        mProtocolSpinner.setSelection(0);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ntt_task_runner_activity);
        setProtocolSpinner();
        initialize(); // find view by id for all objects, and set listenners
        loadDefaultTask(); // load default task from memory to m_oCurTask
        // member.
        initializeSpinners();
        specialSettingsByTaskType();
        mProtocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int protocol = getProtocolFromSpinner();
                if (protocol == Constants.TCP) {
                    Log.d(TAG, "Tcp m_sMode");
                    mUlSpeed.setEnabled(false);
                    mDLSpeed.setEnabled(false);
                } else {
                    Log.d(TAG, "Udp m_sMode");
                    mUlSpeed.setEnabled(true);
                    mDLSpeed.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    /**
     * load parameters and initialize Runner by type
     * @param fieldTestFolder file path to home folder
     * NTT implementation.
     * this method called by loadDefaultTask
     */
    @Override
    protected void loadParametersByType(File fieldTestFolder) {
        // create general serializer with folder name of: "Task"
        // it will look for files inside "Task" folder.
        m_oNttRunner = new NttTaskRunner();
        GeneralSerializer Serializer = m_oNttRunner.getTaskSerializer();
        Server defaultServer = new Server(Constants.NTT,
                Constants.DEFAULT_NTT_SERVER_NAME, Constants.DEF_NTT_IP, 21,
                Constants.DEFAULT_NTT_USER,
                Constants.DEF_NTT_PASSWORD); // just for test
        m_oCurTask = new Task("default", defaultServer,
                Constants.NTT, Constants.TCP, Constants.DN_MODE,
                Constants.DATA_LIMITATION,
                Constants.DEF_LIM_QUANTITY, Constants.DEF_ITERATION,
                Constants.DEF_DL_SESS ,Constants.DEF_UL_SESS,
                Constants.DEF_BUFFER_SIZE);
        m_oCurTask.setSpeeds(Constants.DEF_DN_SPEED, Constants.DEF_UL_SPEED);
        // save task on local SD:
        Serializer.saveServer(fieldTestFolder, defaultServer);
        Serializer.saveTask(fieldTestFolder, m_oCurTask);
        // servers file and task exist, load default values to class members:
        m_oCurTask = Serializer.loadTaskByName(Constants.NTT,
                fieldTestFolder, "default");
        m_oServersList = Serializer.loadServersList(fieldTestFolder,
                Constants.NTT);
        if (m_oCurTask != null) {
            // add servers to FTP runner:
            m_oNttRunner.addServers(m_oServersList);
        } else {
            Toast.makeText(this, "Error, no tasks found", Toast.LENGTH_LONG).show();
            Logger.error("Error, No Tasks Found", this);
        }
    }
    /**
     * get protocol from spinner. return Constants.FTP_MODE
     * @return Download, Upload, Both
     */
    public int getProtocolFromSpinner() {
        String chosenMode = (String) mProtocolSpinner.getSelectedItem();
        if (chosenMode.equals(Constants.TCP_STR)) {
            Log.d(TAG, "Tcp m_sMode");
            return Constants.TCP;
        } else {
            Log.d(TAG, "Udp m_sMode");
            return Constants.UDP;
        }
    }
    /**
     * return Task with current values from spinners and textviews
     * @return Task
     * Task
     */
    @Override
    protected Task getTaskWithChosenValues() {
        /*
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
        String strDate = sdf.format(c.getTime());
        */
        if (m_oOtherTime.isEnabled()) {
            mTimeLimit = Integer.parseInt(m_oOtherTime.getText()
                    .toString().trim());
        } else {
            mTimeLimit = getLimitationQuantityFromSpinner();
        }
        Task task =  new Task(createTaskName(), getServerFromSpinner(),
                Constants.NTT,
                getProtocolFromSpinner(),
                getModeFromSpinner(),
                getLimitationTypeFromSpinner(),
                mTimeLimit,
                Integer.parseInt((String)m_oIteration.getSelectedItem()),
                Integer.parseInt((String)m_oULSession.getSelectedItem()),
                Integer.parseInt((String)m_oDNSession.getSelectedItem()),
                Integer.parseInt(m_oBufferSize.getText().toString()));
        Log.d(TAG, "regular task..");
        if (getProtocolFromSpinner() == Constants.UDP) {
            int dnSpeed = Integer.parseInt(mDLSpeed.getText().toString()
                    .trim());
            int ulSpeed = Integer.parseInt(mUlSpeed.getText().toString()
                    .trim());
            task.setSpeeds(dnSpeed, ulSpeed);
            Log.d(TAG, "got speed values: " + dnSpeed + " " + ulSpeed);
        }
        return task;
    }
    /**
     * stop NTT service:
     */
    protected void stopTaskService() {
        Intent testIntent = new Intent(
                NttTaskRunnerActivity.this, NttTaskRunner.class);
        stopService(testIntent);
    }
    /**
     * enable this method when pressing run button.
     */
    protected void runButtonPressed() {
        Log.d(TAG, "runButtonPressed()");
        m_oStatusScreen = new Intent(NttTaskRunnerActivity.this,
                TestStatusActivity.class);
        // define test intent for onPostExecute:
        m_iTestByMode = new Intent(
                NttTaskRunnerActivity.this, NttTaskRunner.class);
    }
    protected void initializeServerIntent() {
        m_iNewServerIntent = new Intent(
                NttTaskRunnerActivity.this, NewNttServerActivity.class);
    }
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get m_sStatus data for "run" button.
            m_bIsTestRunning = intent.getBooleanExtra(Constants.STATUS, false);
            m_oRun.setEnabled(!m_bIsTestRunning);
        }
    };
}
