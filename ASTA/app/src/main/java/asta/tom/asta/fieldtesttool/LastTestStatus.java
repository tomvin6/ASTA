package com.example.fieldtesttool;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.data.Constants;
import com.example.R;

/**
 * activity to show summary m_sStatus screen of choosen result item.
 */
public class LastTestStatus extends Activity {
    private static final String TAG = "LastTestStatus" ;
    // initialize views:
    protected TextView m_tvMode, m_tvStatus,
            m_tvSent, m_tvReceived, m_tvUp, m_tvdn, m_tvStartTime,
            mTvElapsedTime, m_tvTaskName;

    /**
     * set test values to screen views
     * @param savedInstanceState no saved instant.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_result_status);
        String[] values = getIntent().getStringArrayExtra("values");
        // initialize fields:
        m_tvMode = (TextView) findViewById(R.id.last_mode_param);
        m_tvStatus = (TextView) findViewById(R.id.last_status_param);
        m_tvStartTime = (TextView) findViewById(R.id.start_time_param);
        m_tvTaskName = (TextView) findViewById(R.id.task_name_value);
        // find views:
        m_tvSent = (TextView) findViewById(R.id.last_sent_param);
        m_tvReceived = (TextView) findViewById(R.id.last_received_param);
        m_tvUp = (TextView) findViewById(R.id.avg_ul_param);
        m_tvdn = (TextView) findViewById(R.id.avg_dl);
        mTvElapsedTime = (TextView) findViewById(R.id.elapsed_time_param);
        // set values:
        if (values.length >= Constants.NEW_TASK_VALUES) {
            // version 2.0 and above
            String taskName = values[Constants.TASK_NAME_POS];
            if (!taskName.isEmpty()) {
                m_tvTaskName.setText(values[Constants.TASK_NAME_POS]);
            }
        } else { // version 1.0
            m_tvTaskName.setText(Constants.NO_TASK_NAME);
        }
        m_tvMode.setText(values[Constants.MODE_POS]);
        m_tvStatus.setText(values[Constants.STATUS_POS]);
        m_tvStartTime.setText(timeFormatter(values[Constants.START_TIME_POS]));
        m_tvSent.setText(values[Constants.TOTAL_SENT_POS]);
        m_tvReceived.setText(values[Constants.TOTAL_RECEIVED_POS]);
        m_tvdn.setText(values[Constants.AVG_DN_POS]);
        m_tvUp.setText(values[Constants.AVG_UP_POS]);
        mTvElapsedTime.setText(values[Constants.TOTAL_TEST_TIME_POS] + " " +
                "Sec");
    }
    /**
     * format time value to the following format: 12:34
     * @return new time string.
     */
    private String timeFormatter(String time) {
        if (!time.contains(":")) {
            return time.substring(0,2) + ":" + time.substring(2);
        } else {
            return time;
        }
    }
}
