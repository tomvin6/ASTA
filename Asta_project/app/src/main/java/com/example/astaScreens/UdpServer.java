package com.example.astaScreens;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.*;

import com.example.R;
import com.example.data.Constants;
import com.example.data.NetworkInfoRetreiver;

public class UdpServer extends Activity implements View.OnClickListener {
    private TextView myNetworkIP, myWifiIPAddress,
            resultText,title, myStatus, reqTime, resultParam, reqParam;
    private EditText myPort, partnerPort;
    private Button runServer,stopServer;
    private EditText partnerIP;
    private boolean isTestRunning;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTestRunning = false;
        setContentView(R.layout.activity_udp_server);
        title = (TextView) findViewById(R.id.UDP_connection_title);
        title.setText("UDP Server");
        myNetworkIP = (TextView) findViewById(R.id.server_ip_text);
        myNetworkIP.setText(new NetworkInfoRetreiver().getLocalIp());
        myWifiIPAddress = (TextView) findViewById(R.id.my_wifi_ip_text);
        myWifiIPAddress.setText(getWifiIP());
        partnerIP = (EditText) findViewById(R.id.partner_ip_text);
        partnerIP = (EditText) findViewById(R.id.partner_ip_text);
        partnerIP.setEnabled(false);
        partnerPort = (EditText) findViewById(R.id.partner_port_text);
        partnerPort.setEnabled(false);
        partnerPort.setText("");
        myPort = (EditText) findViewById(R.id.server_port_text);
        runServer = (Button) findViewById(R.id.add_server_button);
        runServer.setText("Run Server");
        runServer.setOnClickListener(this);
        resultText = (TextView) findViewById(R.id.result_text);
        reqTime = (TextView) findViewById(R.id.time_text);
        myStatus = (TextView) findViewById(R.id.my_status);
        resultParam = (TextView) findViewById(R.id.result_param);
        resultParam.setText("Server Status:");
        resultText.setText("Not Running");
        reqParam = (TextView) findViewById(R.id.time_param);
        reqParam.setVisibility(View.INVISIBLE);
        reqTime.setVisibility(View.INVISIBLE);
    }

    public String getWifiIP() {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ipp;
    }

    private boolean isWifiConnected() {
        ConnectivityManager connManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_server_button:
                if (isWifiConnected()) {
                    if (!isTestRunning) {
                        ServerRunner runner = new ServerRunner();
                        resultText.setText("Waiting");
                        myWifiIPAddress.setText(getWifiIP());
                        runner.execute(myPort.getText().toString());
                    } else {
                        Toast.makeText(this,"Test is running, please wait..", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this,"Please enable Wifi in server before running!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.stop_server_button:

                break;
        }
    }


    /**
     * Async Task Class in order to manage "getApplicationFiles" item.
     * this class download application files in seperate process.
     * it shows a progress dialog while the download is running.
     */
    class ServerRunner extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... f_url) {
            String message = runUdpServer(Integer.parseInt(f_url[0]));
            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            String[] split = message.split(",");
            partnerIP.setText(split[1]);
            partnerPort.setText(split[2]);
            resultText.setText(split[0]);
        }

        private String runUdpServer(int listenPort) {
            isTestRunning = true;
            int port=0;
            String ip = null;
            try
            {
                DatagramSocket serverSocket = new DatagramSocket(listenPort);
                byte[] receiveData = new byte[1024];
                byte[] sendData  = new byte[1024];
                receiveData = new byte[1024];
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveData, receiveData.length);
                System.out.println("Waiting for datagram packet");
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                InetAddress IPAddress = receivePacket.getAddress();
                ip = IPAddress.toString().substring(1,IPAddress.toString().length());
                port = receivePacket.getPort();
                System.out.println ("From: " + IPAddress + ":" + port);
                System.out.println ("Message: " + sentence);
                String answerStr = "OK";
                sendData = answerStr.getBytes();
                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, IPAddress,
                                port);
                serverSocket.send(sendPacket);
                System.out.println("Sent Message: " + answerStr);
            }
            catch (SocketException ex) {
                System.out.println("UDP Port is occupied.");
                isTestRunning = false;
                System.exit(1);
            } catch (IOException e) {
                isTestRunning = false;
                e.printStackTrace();
            }
            isTestRunning = false;
            return "Sent,"+ ip + "," + Integer.toString(port);
        }
    }
}
