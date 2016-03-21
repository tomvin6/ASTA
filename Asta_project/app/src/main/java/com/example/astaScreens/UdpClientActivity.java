package com.example.astaScreens;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.data.NetworkInfoRetreiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpClientActivity extends Activity implements View.OnClickListener {
    private TextView myNetworkIP, myWifiIPAddress, resultText,title, myStatus, reqTime;
    private EditText myPort, partnerPort;
    private Button runServer,stopServer;
    private EditText partnerIP;
    private boolean isTestRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTestRunning = false;
        setContentView(R.layout.activity_udp_server);
        title = (TextView) findViewById(R.id.UDP_connection_title);
        title.setText("UDP Client");
        myNetworkIP = (TextView) findViewById(R.id.server_ip_text);
        myNetworkIP.setText(new NetworkInfoRetreiver().getLocalIp());
        myWifiIPAddress = (TextView) findViewById(R.id.my_wifi_ip_text);
        myWifiIPAddress.setText(getWifiIP());
        partnerIP = (EditText) findViewById(R.id.partner_ip_text);
        partnerIP = (EditText) findViewById(R.id.partner_ip_text);
        partnerPort = (EditText) findViewById(R.id.partner_port_text);
        myPort = (EditText) findViewById(R.id.server_port_text);
        myPort.setEnabled(false);
        myPort.setText("");
        runServer = (Button) findViewById(R.id.add_server_button);
        runServer.setText("Run Test");
        runServer.setOnClickListener(this);
        resultText = (TextView) findViewById(R.id.result_text);
        reqTime = (TextView) findViewById(R.id.time_text);
        myStatus = (TextView) findViewById(R.id.my_status);
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
                        Toast.makeText(this, "Request Sent", Toast.LENGTH_SHORT).show();
                        ServerRunner runner = new ServerRunner(UdpClientActivity.this);
                        resultText.setText("Request Sent");
                        runner.execute(partnerIP.getText().toString().trim(), partnerPort.getText().toString());
                    }
                } else {
                    Toast.makeText(this, "Please enable Wifi!", Toast.LENGTH_LONG).show();
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
        private int MAX_UDP_DATAGRAM_LEN = 1500;
        private long tStart, tEnd;
        private Double elapsedSeconds;
        private Context cnt;

        ServerRunner(Context context)        {
            cnt=context;
        }

        @Override
        protected String doInBackground(String... f_url) {
            return runUdpClient(f_url[0], f_url[1]);
        }

        @Override
        protected void onPostExecute(String message) {
            String[] split = message.split(",");
            resultText.setText(split[0]);
            // calculate elapsed time:
            elapsedSeconds = (tEnd - tStart) / 1000.0;
            reqTime.setText(elapsedSeconds.toString() + " Seconds");
            myPort.setText(split[1]);
        }

        private String runUdpClient(String partnerIP, String patnerPort) {
            isTestRunning= true;
            String udpMsg = "hello UDP Server " + patnerPort;
            DatagramSocket ds = null;
            byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
            String lText = null;
            try {
                ds = new DatagramSocket();
                InetAddress serverAddress = InetAddress.getByName(partnerIP);
                DatagramPacket dp;
                dp = new DatagramPacket(udpMsg.getBytes(),
                        udpMsg.length(), serverAddress, Integer.parseInt(patnerPort));
                DatagramPacket receivePacket =
                        new DatagramPacket(lMsg, lMsg.length);
                tStart = System.currentTimeMillis();
                ds.send(dp);
                ds.receive(receivePacket);
                tEnd = System.currentTimeMillis();
                lText = new String(lMsg, 0, dp.getLength());
                return lText + "," + dp.getPort();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }
                isTestRunning = false;
            }
            return lText;
        }
    }
}
