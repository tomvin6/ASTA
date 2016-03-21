package com.example.Listeners;

import android.content.Context;
import android.content.Intent;

/**
 * class that envelop write method inside the logger.
 * enable user to write into log file with a simple method
 */
public final class Logger {

    /**
     * public static method to enable logger access.
     * @param message to write
     * @param activityContext of host activity.
     */
    public static void write(String message, Context activityContext) {
        Intent intent = new Intent();
        intent.putExtra("data","<br><b>" + message + "</b><br>");
        intent.setAction("com.example.LOGBACK_INTENT");
        activityContext.sendBroadcast(intent);
        //logger.log("<br><b>" + message + "</b><br>");
    }
    /**
     * public static method to enable logger access.
     * message will be shown in red (in html file)
     * @param message to write
     * @param activityContext of host activity.
     */
    public static void error(String message, Context activityContext) {
        Intent intent = new Intent();
        intent.putExtra("data", "<br><b><font color="+"red"+">" + message + "</b></font><br>");
        intent.setAction("com.example.LOGBACK_INTENT");
        activityContext.sendBroadcast(intent); //next line added
        //logger.log("<br><b><font color="+"red"+">" + message + "</b></font><br>");
    }
    /**
     * public static method to enable logger access.
     * message will be shown in green (in html file)
     * @param message to write
     * @param activityContext of host activity.
     */
    public static void success(String message, Context activityContext) {
        Intent intent = new Intent();
        intent.putExtra("data", "<br><b><font color="+"green"+">" + message + "</b></font><br>");
        intent.setAction("com.example.LOGBACK_INTENT");
        activityContext.sendBroadcast(intent);
        //logger.log("<br><b><font color="+"green"+">" + message + "</b></font><br>");
    }
    /**
     * public static method to enable logger access.
     * @param message to write
     * @param activityContext of host activity.
     */
    public static void data(String message, Context activityContext) {
        Intent intent = new Intent();
        intent.putExtra("data", message + "\n");
        intent.setAction("com.example.LOGBACK_INTENT");
        activityContext.sendBroadcast(intent);
        //logger.log("<br><b><font color="+"green"+">" + message + "</b></font><br>");
    }


}
