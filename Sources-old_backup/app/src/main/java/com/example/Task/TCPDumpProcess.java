package com.example.Task;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * service to prompt a command on android shell.
 */
public class TCPDumpProcess {
    private static final String TAG = "TCPDumpProcess";
    private static String m_sMessage = null;

    public int onStartCommand() {
        //boolean isSuccess = executeCommandViaShell("tcpdump -s 1514 -w /storage/sdcard0/out.pcap");
        //boolean isSuccess = executeCommandViaShell("tcpdump -w /sdcard/bla");
        //J1:
        boolean isSuccess = executeCommandViaShell("data/local/tcpdump -w /sdcard/output");
        Log.d(TAG, "start TCPDump service");
        //executeCommandViaShell("ls");
        //boolean isSuccess = executeCommandViaShell("tcpdump -s 1514 -w /storage/sdcard0/out.pcap");
        if (m_sMessage.contains("no suitable device found")) {
            // device not rooted.
            Log.e(TAG, "what is wrong?");
        }
        Log.d(TAG, "execution m_sStatus: " + isSuccess);

        return Service.START_NOT_STICKY;
    }

    //@Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    public static boolean executeCommandViaShell(String sCmd) {
        Log.d(TAG, "command: " + sCmd);
        DataInputStream isErr = null, isRes = null;
        BufferedReader brErr = null, brRes = null;
        try {
            Log.d(TAG, "before running exec");
            Process su = Runtime.getRuntime().exec(sCmd);

            Log.d(TAG, "after running exec");
            isErr = new DataInputStream(su.getErrorStream());
            brErr = new BufferedReader(new InputStreamReader(isErr));
            isRes = new DataInputStream(su.getInputStream());
            brRes = new BufferedReader(new InputStreamReader(isRes));
            Log.d(TAG, "check for error m_sMessage");
            // errors:
            while ((m_sMessage = brErr.readLine()) != null) {
                Log.d(TAG, "error m_sMessage: = " + m_sMessage);
                if (m_sMessage.equalsIgnoreCase("INVALID"))
                {
                    Log.e(TAG, "INVALID m_sMessage: " + m_sMessage);
                    return false;
                }
            }
            Log.d(TAG, "error m_sMessage: = " + m_sMessage);
            Log.d(TAG, "check for success m_sMessage..");
            while ((m_sMessage = brRes.readLine()) != null) {
                Log.d(TAG, "success m_sMessage = " + m_sMessage);
                if (m_sMessage.equals("SUCCESS")) {
                    Log.d(TAG, "SUCCESS m_sMessage: " + m_sMessage);
                }
                else {
                    Log.e(TAG, "illegal m_sMessage: " + m_sMessage);
                    return false;
                }
            }
            Log.d(TAG, "final m_sMessage: "+ m_sMessage);
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "TCP Dump Exception. m_sMessage: " + m_sMessage);
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                isErr.close();
                isRes.close();
                brErr.close();
                brRes.close();
            } catch (IOException e) {
                Log.e(TAG, "TCP Dump close Exception");
                e.printStackTrace();
            }

        }
    }

}
