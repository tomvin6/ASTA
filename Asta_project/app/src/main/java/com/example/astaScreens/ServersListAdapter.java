package com.example.astaScreens;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.R;
import com.example.Task.Server;

import java.util.List;

/**
 * list adapter to convert array of servers to listView.
 */
public class ServersListAdapter extends ArrayAdapter {
    private final Context m_oContext;

    /**
     * constructor
     * @param context of host activity
     * @param values servers list.
     */
    public ServersListAdapter(Context context, List<Server> values) {
        super(context, android.R.layout.simple_list_item_1, values);
        this.m_oContext = context;
    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder{
        TextView titleText;
    }
    /*
    * @param position
    * @param convertView
    * @param parent
    * @return view of item
    */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Server item = (Server)getItem(position);
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) m_oContext
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
                viewToUse = mInflater.inflate(R.layout.server_item, null);
            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(R.id
                    .firstLine);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        holder.titleText.setText(item.getServerName());
        return viewToUse;
    }

}