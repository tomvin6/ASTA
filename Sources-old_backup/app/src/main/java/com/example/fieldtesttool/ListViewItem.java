package com.example.fieldtesttool;

import android.graphics.drawable.Drawable;

/**
 * ListViewItem is a class that contain item details
 */
public class ListViewItem {
    public final Drawable icon;       // the drawable for the ListView item ImageView
    public final String title;        // the text for the ListView item title
    public final String description;  // the text for the ListView item description


    public ListViewItem(Drawable tmpIcon, String title, String description) {
        this.icon = tmpIcon;
        this.title = title;
        this.description = description;
    }
}