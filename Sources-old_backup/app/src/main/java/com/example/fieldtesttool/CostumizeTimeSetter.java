package com.example.fieldtesttool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.R;
import com.example.data.Constants;

/**
 * Activity for costumize time settings.
 * currently we don't use this class.
 */
public class CostumizeTimeSetter extends Activity{
    EditText m_oTime;
    Button m_bDoneButton;
    private TextView m_oTimeTiltle, m_oSubTitle;
    /**
     * initialize CostumizeTimeSetter activity.
     * @param savedInstanceState no instance saved.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_text_field);
        final String limitation = getIntent().getExtras().getString(Constants
                .MODE);
        Log.d("time", "limitation = " + limitation);
        m_oTime = (EditText) findViewById(R.id.buffer_time);
        m_bDoneButton = (Button) findViewById(R.id.done_button);
        m_oTimeTiltle = (TextView) findViewById(R.id.costumize_title);
        m_oSubTitle = (TextView) findViewById(R.id.buffer_param);
        if (limitation.equals(Constants.TIME_SPINNER)) {
            m_oTimeTiltle.setText("Set time for task");
            m_oSubTitle.setText("Time (Min)");
        } else {
            m_oTimeTiltle.setText("Set size for task");
            m_oSubTitle.setText("Size (MB)");
        }
        m_bDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneEditing();
            }
        });
    }

    /**
     * set result so that outer class will be able to get it.
     */
    public void doneEditing() {
        Intent returnIntent = new Intent();
        String number = m_oTime.getText()
                .toString().trim();
        if (!number.isEmpty()) {
            returnIntent.putExtra("result", Integer.parseInt(number));
            setResult(RESULT_OK, returnIntent);
            Log.d("CostumizeTimeSetter", "set result: " + number);
            finish();
        } else {
            setResult(RESULT_CANCELED, null);
        }
    }

}
