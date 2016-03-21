package com.example.astaScreens;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.example.Listeners.Logger;
import com.example.R;
import com.example.Task.TcpTaskRunnerActivity;

import java.util.ArrayList;

public class SpeedTestTypeChooser extends ListActivity {

    private static final java.lang.String TCP_TEST = "TCP Test";
    private static final java.lang.String UDP_TEST = "UDP Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. pass context and data to the custom adapter
        MainListActivityAdapter adapter = new MainListActivityAdapter(this, generateData());
        // add header
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.header, lv, false);
        lv.addHeaderView(header, null, false);
        Logger.write("SpeedTestTypeChooser", this);
        setListAdapter(adapter);
    }

    /**
     * generate items for menu.
     *
     * @return menu model
     */
    private ArrayList<MenuModel> generateData() {
        ArrayList<MenuModel> models = new ArrayList<MenuModel>();
        models.add(new MenuModel(R.drawable.action_help, TCP_TEST));
        models.add(new MenuModel(R.drawable.action_help, UDP_TEST));
        return models;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // switch items
        switch (position) {
            case 1: //new Task
                Intent tcpTask = new Intent(SpeedTestTypeChooser.this,
                        TcpTaskRunnerActivity.class);
                startActivity(tcpTask);
                break;
            case 2: //New job
                Intent udpTask = new Intent(SpeedTestTypeChooser.this,
                        UDPServerClientChooser.class);
                startActivity(udpTask);
                break;
        }
    }
}
