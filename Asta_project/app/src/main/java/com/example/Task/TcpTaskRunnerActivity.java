package com.example.Task;

import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.Listeners.Logger;
import com.example.R;
import com.example.data.Constants;

import java.io.File;
import java.util.ArrayList;

/**
 * activity that derives from TaskRunnerActivity.
 * responsible for creating and initializing FTP Task
 */
public class TcpTaskRunnerActivity extends TaskRunnerActivity {
    private static final String TAG = "FtpTaskRunnerActivity";

    /**
     * load parameters and initialize Runner by type
     * @param fieldTestFolder file path to home folder
     * FTP implementation.
     * this method called by loadDefaultTask
     */
    protected void loadParametersByType(File fieldTestFolder) {
        // create general serializer with folder name of: "Task"
        // it will look for files inside "Task" folder.
        m_oFtpkRunner = new FtpTaskRunner();
        GeneralSerializer Serializer = m_oFtpkRunner.getTaskSerializer();
        Server defaultServer = new Server(Constants.FTP,
                Constants.DEFAULT_FTP_USER,
                Constants.DEF_FTP_IP,
                Constants.DEFAULT_FTP_PORT,
                Constants.DEFAULT_FTP_USER,
                Constants.DEF_FTP_PASSWORD);
        // servers file and task exist, load default values to class members:
        m_oCurTask = Serializer.loadTaskByName(Constants.FTP,
                fieldTestFolder,
                "default");
        m_oServersList = Serializer.loadServersList(fieldTestFolder,
                Constants.FTP);
        // if didn't found any server on the device:
        if (m_oServersList.size() == 0) {
            m_oServersList.add(defaultServer);
        }
        if (m_oCurTask != null) {
            // add servers to FTP runner:
            m_oFtpkRunner.addServers(m_oServersList);
        } else {
            Toast.makeText(this, "Error, no task found", Toast.LENGTH_LONG).show();
            Logger.error("Error, No Task Fount", this);
        }
    }
    /**
     * initialize FTP servers / Ntt servers:
     */
    protected void initializeServersSpinner() {
        // convert servers list to array:
        ArrayList<Server> serversList = (ArrayList<Server>) m_oFtpkRunner.getServersList();
        String[] serversArray = convertToArray(serversList);
        // Declaring an Adapter and initializing it to the data pump
        ArrayAdapter<String> servers_adapter = new ArrayAdapter<String>(
                getApplicationContext(), R.layout.spinner_item,serversArray);
        servers_adapter.setDropDownViewResource(R.layout.spinner_item);
        // Setting Adapter to the Spinner
        m_oServersSpinner.setAdapter(servers_adapter);
        m_oServersSpinner.setSelection(m_nLastServerSelection);
    }

    /**
     * return Task with current values from spinners and textviews
     * @return Task with field values.
     */
    protected Task getTaskWithChosenValues() {
        if (m_oOtherTime.isEnabled()) {
            mTimeLimit = Integer.parseInt(m_oOtherTime.getText()
                    .toString().trim());
        } else {
            mTimeLimit = getLimitationQuantityFromSpinner();
        }
        Task task =  new Task(createTaskName(), getServerFromSpinner(),
                Constants.FTP,
                Constants.TCP,
                getModeFromSpinner(),
                getLimitationTypeFromSpinner(),
                mTimeLimit, // in case we got other earlier.
                Integer.parseInt((String)m_oIteration.getSelectedItem()),
                getNetworkTypeFromSpinner()
        );
        return task;
    }
    /**
     * stop FTP service:
     */
    protected void stopTaskService() {
        Intent testIntent = new Intent(
                TcpTaskRunnerActivity.this, FtpTaskRunner.class);
        stopService(testIntent);
    }
    /**
     * this method in TaskRunnerActivity class will disable
     * un-relevant fields.
     * this method should be in FTP task runner class
     */
    protected void specialSettingsByTaskType() {

    }
    // initialize server intent:
    protected void initializeServerIntent() {
        m_iNewServerIntent = new Intent(
                TcpTaskRunnerActivity.this, NewServerActivity.class);
    }
    /**
     * enable this method when pressing run button.
     * FTP implementation
     */
    protected void runButtonPressed() {
        m_oStatusScreen = new Intent(TcpTaskRunnerActivity.this,
                TestStatusActivity.class);
        m_iTestByMode = new Intent(
                TcpTaskRunnerActivity.this, FtpTaskRunner.class);
    }
}
