package com.example.Task;

import com.example.data.Constants;

/**
 * activity for creating a new NTT server.
 * derives from newServerActivity.
 */
public class NewNttServerActivity extends NewServerActivity {
    // disable un-relevant fields:
    protected void specificInitializationByServerType() {
        m_oNewServerTitle.setText("Add New Ntt Server");
        m_oUser.setText("None");
        m_opassword.setText("None");
        m_oServerPort.setText("8090");
        m_oUser.setEnabled(false);
        m_opassword.setEnabled(false);
        m_nType = Constants.NTT;
    }
    /**
     * server type getter
     */
    public int getServerType() {
        return Constants.NTT;
    }
}
