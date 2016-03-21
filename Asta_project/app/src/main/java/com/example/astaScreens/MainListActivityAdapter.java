package com.example.astaScreens;

import java.util.ArrayList;

import com.example.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * adapter class for converting a list to ListActivity
 */
public class MainListActivityAdapter extends ArrayAdapter<MenuModel> {
    private final Context m_oContext;
    private final ArrayList<MenuModel> m_oModelsArrayList;

    public MainListActivityAdapter(Context newContext, ArrayList<MenuModel> newModelsArrayList) {
        super(newContext, R.layout.target_item, newModelsArrayList);
        this.m_oContext = newContext;
        this.m_oModelsArrayList = newModelsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) m_oContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = null;
        if(!m_oModelsArrayList.get(position).isGroupHeader()){
            rowView = inflater.inflate(R.layout.target_item, parent, false);

            // 3. Get icon,title & counter views from the rowView
            ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
            TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
                /* if want to add a counter to list item, enable this line: 1/2
                 TextView counterView = (TextView) rowView.findViewById(R.id
                        .item_counter);
                 */

            // 4. Set the text for textView
            imgView.setImageResource(m_oModelsArrayList.get(position).getIcon());
            titleView.setText(m_oModelsArrayList.get(position).getTitle());
                /* if want to add counter to item, enable this line: 2/2
                counterView.setText(m_oModelsArrayList.get(position).getCounter
                        ());
                */
        } else {
            rowView = inflater.inflate(R.layout.group_header_item, parent, false);
            TextView titleView = (TextView) rowView.findViewById(R.id.header);
            titleView.setText(m_oModelsArrayList.get(position).getTitle());
        }

        // 5. return rowView
        return rowView;
    }
}