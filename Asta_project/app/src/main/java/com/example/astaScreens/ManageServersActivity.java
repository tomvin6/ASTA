package com.example.astaScreens;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.R;
import com.example.Task.NewNttServerActivity;
import com.example.Task.NewServerActivity;

/**
 * Activity to manage all servers.
 * enable user to delete and add a server to list of servers.
 */
public class ManageServersActivity extends ActionBarActivity implements View.OnClickListener {
    private Button m_oAddFtp, m_oAddNtt, m_oClean;
    private ManageServersFragment m_oServersFragment;

    /**
     * initialize activity.
     * @param savedInstanceState no data inside.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_servers);
        getSupportActionBar().hide();
        m_oServersFragment = new ManageServersFragment();
        m_oServersFragment.setContext(this);
        m_oAddFtp = (Button) findViewById(R.id.add_ftp);
        m_oAddNtt = (Button) findViewById(R.id.add_ntt);
        m_oClean = (Button) findViewById(R.id.clean);
        m_oAddFtp.setOnClickListener(this);
        m_oAddNtt.setOnClickListener(this);
        m_oClean.setOnClickListener(this);
        // get fragment manager
        FragmentManager fm = getFragmentManager();
        // add
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.displayList, m_oServersFragment);
        // alternatively add it with a tag
        // trx.add(R.id.your_placehodler, new YourFragment(), "detail");
        ft.commit();

    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * @param menu to inflate
     * @return true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_servers, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     * @param item that have been clicked.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * this method is called when button is clicked.
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_ftp:
                Intent newFtpServer = new Intent(this, NewServerActivity.class);
                startActivity(newFtpServer);
                break;
            case R.id.add_ntt:
                Intent newNttServer = new Intent(this, NewNttServerActivity.class);
                startActivity(newNttServer);
                break;
            case R.id.clean:
                // show deletion dialog:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Clean all servers");
                builder.setMessage("are you sure you want to clean all servers?");
                builder.setPositiveButton("Yes", new DialogInterface
                        .OnClickListener
                        () {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        m_oServersFragment.deleteAllServers();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface
                        .OnClickListener
                        () {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
    }
}
