package com.example.fieldtesttool;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.R;
import com.example.Task.GeneralSerializer;
import com.example.Task.NewNttServerActivity;
import com.example.Task.NewServerActivity;
import com.example.Task.Server;
import com.example.data.Constants;
import com.example.data.Paths;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage Servers Fragment.
 * enable user to add / remove / clean server files.
 */
public class ManageServersFragment extends ListFragment {
    private static final String TAG = "ManageServersFragment";
    private List<ListViewItem> mItems;        // ListView items list
    private File m_oInternalAppPath;
    private GeneralSerializer m_oSerializer;
    private Resources m_oResources;
    private Context m_oContext;
    private List<Server> m_oFtpServers, m_oNttServers;
    /**
     * after building this object, developer need to set context
     * of container activity.
     * @param context of container activity.
     */
    public void setContext(Context context) {
        m_oContext = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_oInternalAppPath = new File(
                Environment.getExternalStorageDirectory()
                        + Paths.INTERNAL_MAIN_FOLDER_NAME);
        m_oSerializer = new GeneralSerializer();
        mItems = new ArrayList<ListViewItem>();
        m_oResources = getResources();
    }

    /**
     * will be called from Manage activity in order to delete all servers.
     */
    public void deleteAllServers() {
        m_oSerializer.deleteAllServers(m_oInternalAppPath);
        mItems.clear();
        setListAdapter(new ListViewAdapter(getActivity(), mItems));
    }
    /**
     * load servers from device files.
     * the servers folder is defined in Paths class.
     */
    private void loadServers() {
        m_oFtpServers = m_oSerializer.loadServersList
                (m_oInternalAppPath, Constants.FTP);
        m_oNttServers = m_oSerializer.loadServersList
                (m_oInternalAppPath, Constants.NTT);
        // add ftp servers:
        for (Server s: m_oFtpServers) {
            mItems.add(new ListViewItem(m_oResources.getDrawable(R.drawable.task_icon),
                    s.getServerName(), s.getServerIp()));
        }
        // add ntt servers:
        for (Server s: m_oNttServers) {
            mItems.add(new ListViewItem(m_oResources.getDrawable(R.drawable.task_icon),
                    s.getServerName(), s.getServerIp()));
        }
        // initialize and set the list adapter
        setListAdapter(new ListViewAdapter(getActivity(), mItems));
    }

    /**
     * clear items and load servers.
     */
    @Override
    public void onResume() {
        super.onResume();
        mItems.clear();
        loadServers();
    }

    /**
     * for each view, set devider to null.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // remove the dividers from the ListView of the ListFragment
        getListView().setDivider(null);
    }

    /**
     * when server item is being clicked, open a deletion activity.
     * if user confirm deletion, the server will be deleted from device storage.
     * @param l list view of servers
     * @param v a view
     * @param position of server item
     * @param id of item
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // retrieve theListView item
        final ListViewItem item = mItems.get(position);
        // show deletion dialog:
        AlertDialog.Builder builder = new AlertDialog.Builder(m_oContext);
        builder.setTitle("Server options");
        builder.setMessage("What would you like to do?");
        builder.setPositiveButton("Delete server", new DialogInterface
                .OnClickListener
                () {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                m_oSerializer.deleteServer(m_oInternalAppPath, item.description);
                Toast.makeText(getActivity(), "Delete server", Toast.LENGTH_SHORT)
                        .show();
                mItems.clear();
                loadServers();
            }
        });

        builder.setNegativeButton("Edit server", new DialogInterface
                .OnClickListener
                () {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                for (Server server : m_oFtpServers) {
                    if (server.getServerIp().equals(item.description)) {
                        Intent editServer = new Intent(m_oContext,
                                NewServerActivity.class);
                        editServer.putExtra(Constants.SERVER, server);
                        startActivity(editServer);
                    }
                }
                for (Server server : m_oNttServers) {
                    if (server.getServerIp().equals(item.description)) {
                        Intent editServer = new Intent(m_oContext,
                                NewNttServerActivity.class);
                        editServer.putExtra(Constants.SERVER, server);
                        startActivity(editServer);
                    }
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}