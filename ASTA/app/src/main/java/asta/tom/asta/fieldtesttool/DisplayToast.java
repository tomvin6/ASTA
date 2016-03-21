package com.example.fieldtesttool;

import android.content.Context;
import android.widget.Toast;

/**
 * class that enable IntentService to show Toast
 */
public class DisplayToast implements Runnable {
    private final Context m_oContext;
    String m_oText;

    /**
     * display the toast on screen
     * @param mContext our contet
     * @param text to show
     */
    public DisplayToast(Context mContext, String text){
        this.m_oContext = mContext;
        m_oText = text;
    }

    public void run(){
        Toast.makeText(m_oContext, m_oText, Toast.LENGTH_SHORT).show();
    }
}