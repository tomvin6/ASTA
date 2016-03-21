package com.example.fieldtesttool;

import android.app.ListActivity;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.Listeners.CsvHandler;
import com.example.R;
import com.example.data.Constants;

/**
 * Class that display a list of last results.
 * when you click on an item, you will get result details.
 */
public class LastResultsActivity extends ListActivity {
    private static final String TAG = "LastResults";
    private List<String[]> m_oTestsList;
    private TextView m_tvText;
    private ArrayList<MenuModel> m_oListValues;

    /**
     * on create inflate list items.
     * if there is no "result" item, display an "list empty" message.
     * @param savedInstanceState for recreation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_results);
        m_tvText = (TextView) findViewById(R.id.mainText);
        convertResultFilesToList();
        MainListActivityAdapter adapter = new MainListActivityAdapter(this, m_oListValues);
        setListAdapter(adapter);
        Log.d(TAG, "List has been initialized");
    }

    /**
     * method is called when an item of the list is clicked
     * @param list xml view
     * @param view item view
     * @param position items position
     * @param id of item.
     */
    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        String[] testValues = m_oTestsList.get(position);
        //String selectedItem = (String) getListAdapter().getItem(position);
        Intent statusActivity = new Intent(LastResultsActivity.this,
                LastTestStatus.class);
        statusActivity.putExtra("values", testValues);
        startActivity(statusActivity);
    }

    /**
     * scan results folder and for every result file,
     * insert a result item to results list.
     */
    public void convertResultFilesToList() {
        m_oTestsList = CsvHandler.loadTestSummery();
        m_oListValues = new ArrayList<MenuModel>();
        for (String [] s: m_oTestsList) {
            if (s.length >= Constants.NEW_TASK_VALUES) { // new result with task name
                m_oListValues.add(new MenuModel(R.drawable.last_results_item_icon,
                        s[Constants.MODE_POS] + " "
                                + s[Constants.DATE_POS] + " "
                                + timeFormatter(s[Constants.START_TIME_POS]) + " "
                                + s[Constants.TASK_NAME_POS]));
            } else { // values before 2.0 version (without task name)
                m_oListValues.add(new MenuModel(R.drawable.last_results_item_icon,
                        s[Constants.MODE_POS] + " "
                                + s[Constants.DATE_POS] + " "
                                + timeFormatter(s[Constants.START_TIME_POS])));
            }
        }
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
