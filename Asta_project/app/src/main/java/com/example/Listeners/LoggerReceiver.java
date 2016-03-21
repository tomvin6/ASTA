package com.example.Listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidLoggerz.AndroidLoggerz;
/**
 * How To Use Logger ?
 * need to create the logger outside this class for the first time
 * then, declare a receiver inside the manifest, for example:
  <receiver android:name=".Listeners.LoggerReceiver" >
     <intent-filter>
        <action android:name="com.example.LOGBACK_INTENT" />
     </intent-filter>
  </receiver>
 * than - send messages (broadcasts) in the following way:
 * example: (look in Logger class)
    Intent intent = new Intent();
    intent.putExtra("message", "this is just a test");
    intent.setAction("com.example.LOGBACK_INTENT");
    sendBroadcast(intent);
 */
public class LoggerReceiver extends BroadcastReceiver {
    static AndroidLoggerz logger = AndroidLoggerz.getInstance();
    /**
     * get the message from the intent and write it 
     * with the logger to a file
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        CharSequence intentData = intent.getCharSequenceExtra("data");
        logger.log(intentData.toString());
    }
    
}
