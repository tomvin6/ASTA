package com.example.fieldtesttool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.R;

import java.util.List;

/**
 * Array adapter to convert array list to listView.
 */
public class ListViewAdapter extends ArrayAdapter<ListViewItem> {

    /**
     * constructor
     * @param context activity context
     * @param items list of items
     */
    public ListViewAdapter(Context context, List<ListViewItem> items) {
        super(context, R.layout.listview_item, items);
    }

    /**
     * this method is being called for each list item.
     * @param position of item
     * @param convertView view
     * @param parent parent view group.
     * @return the view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        ListViewItem item = getItem(position);
        viewHolder.ivIcon.setImageDrawable(item.icon);
        viewHolder.tvTitle.setText(item.title);
        viewHolder.tvDescription.setText(item.description);

        return convertView;
    }

    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     */
    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
    }
}