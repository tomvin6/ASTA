package com.example.astaScreens;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.example.Listeners.Logger;
import com.example.R;

import java.util.ArrayList;

public class UDPServerClientChooser extends ListActivity {

    private static final java.lang.String UDP_SERVER = "UDP Server";
    private static final java.lang.String UDP_CLIENT = "UDP Client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainListActivityAdapter adapter = new MainListActivityAdapter(this, generateData());
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.header, lv, false);
        lv.addHeaderView(header, null, false);
        setListAdapter(adapter);
        Logger.write("udp_settings", this);
    }

    /**
     * generate items for menu.
     *
     * @return menu model
     */
    private ArrayList<MenuModel> generateData() {
        ArrayList<MenuModel> models = new ArrayList<MenuModel>();
        models.add(new MenuModel(R.drawable.action_help, UDP_SERVER));
        models.add(new MenuModel(R.drawable.action_help, UDP_CLIENT));
        return models;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // switch items
        switch (position) {
            case 1: //new Task
                    Intent udpServer = new Intent(UDPServerClientChooser.this,
                        UdpServer.class);
                startActivity(udpServer);
                break;
            case 2: //New job
                Intent udpClient = new Intent(UDPServerClientChooser.this,
                        UdpClientActivity.class);
                startActivity(udpClient);
                break;
        }
    }
}
