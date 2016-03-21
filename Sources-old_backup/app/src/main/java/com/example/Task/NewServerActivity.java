package com.example.Task;

import java.io.File;

import com.example.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.example.data.Constants;
import com.example.data.Paths;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity class that enable us to add a new server to servers list:
 */
public class NewServerActivity extends Activity implements OnClickListener {
    protected EditText m_oServerName, m_oServerIP,
            m_oServerPort, m_oUser, m_opassword;
    protected TextView m_oNewServerTitle;
    protected File m_oAppFolder;
    protected Button m_oAddButton;
    protected boolean mIsIPValid;
    protected Context m_oContext;
    protected int m_nType;
    protected GeneralSerializer serializator;
    protected Server m_oServer; // will be initialize after adding the server
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_server);
        initialize();
        specificInitializationByServerType();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(Constants.SERVER)) {
                Server server = (Server) getIntent().getExtras().getSerializable
                        (Constants.SERVER);
                if (server != null) {
                    m_oServerName.setText(server.getServerName());
                    m_oServerIP.setText(server.getServerIp());
                    m_oUser.setText(server.getUserName());
                    m_opassword.setText(server.getPassword());
                }
            }
        }
    }

    /**
     * configure additional values for Ftp servers
     */
    protected void specificInitializationByServerType() {
        m_oNewServerTitle.setText("Add New Ftp Server");
        m_nType = Constants.FTP;
    }

    /**
     * IP validation:
     * @param ip to validate
     * @return true for valid, false for not.
     */
    public boolean validIP (String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if(ip.endsWith(".")) {
                return false;
            }
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    /**
     * validate server details before adding it to servers list
     * validate legal IP address.
     * returns true if input is legal, else return false.
     */
    public boolean validateServerDetails() {
        // validate that server name was insert
        if (m_oServer.getServerName().isEmpty()) {
            return false;
        } else if (m_oServer.getUserName().isEmpty()) {
            return false;
        } else if (m_oServer.getPassword().isEmpty()) {
            return false;
        } else if (!mIsIPValid) {
            return false;
        }
        return true;
    }
    /*
     * find view by id for all members:
     */
    public void initialize() {
        // server
        m_oNewServerTitle = (TextView) findViewById(R.id.task_type);
        m_oServerName = (EditText) findViewById(R.id.server_name_text);
        m_oServerIP = (EditText) findViewById(R.id.server_ip_text);
        m_oServerPort = (EditText) findViewById(R.id.server_port_text);
        m_oServerPort.setEnabled(false);
        m_oUser = (EditText) findViewById(R.id.server_user_text);
        m_opassword = (EditText) findViewById(R.id.server_pass_text);
        m_oAddButton = (Button) findViewById(R.id.add_server_button);
        serializator = new GeneralSerializer();
        m_oContext = this;
        m_oAddButton.setOnClickListener(this);
        m_oServerIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                m_oServerIP.setBackgroundColor(Color.WHITE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mIsIPValid = validIP(s.toString());
            }


            @Override
            public void afterTextChanged(Editable s) {
                mIsIPValid = validIP(s.toString().trim());
                if (mIsIPValid) {
                    m_oServerIP.setTextColor(Color.BLACK);
                } else {
                    m_oServerIP.setTextColor(Color.RED);
                }
            }
        });
    }
    /**
     * iterate list of servers from internal memory.
     * return the server with corresponding name to the
     * Chosen server from the spinner.
     * @return Server
     */
    public int getServerType() {
        return Constants.FTP;
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.add_server_button:
                m_oServer = new Server (m_nType, m_oServerName.getText()
                        .toString(),
                        m_oServerIP.getText().toString(),
                        Integer.parseInt(m_oServerPort.getText().toString()),
                        m_oUser.getText().toString(),
                        m_opassword.getText().toString());
                boolean isInputLegal = validateServerDetails();
                if (isInputLegal) {
                    m_oAppFolder = new File(
                            Environment.getExternalStorageDirectory()
                                    + Paths.INTERNAL_MAIN_FOLDER);
                    new ValidationService().execute("");
                } else {
                    Toast.makeText(this, "Illegal input!", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    // Async Task Class for validation
    class ValidationService extends AsyncTask<String, String, String> {
        boolean m_IsServerResponding = false;
        ProgressDialog m_oProgressDialog;
        Handler m_oHandler;
        private Validator l_oValidator;

        /**
         * post a progress bar for validation process
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_oProgressDialog = ProgressDialog.show(NewServerActivity.this,
                    "Please wait", "Waiting for server respons");
            m_oHandler = new Handler(); // for validator
        }

        /**
         * validatate parameters in background process
         * @param f_url none
         * @return m_sStatus string
         */
        @Override
        protected String doInBackground(String... f_url) {
            l_oValidator = new Validator(m_oContext, m_oHandler);
            m_IsServerResponding = l_oValidator.validateByServerDetails
                    (m_oServer);
            if (m_IsServerResponding) {
                serializator.saveServer(m_oAppFolder, m_oServer);
                return "Success";
            }
            return "Failed";
        }


        /**
         * start task when validation finished
         * test will be run only if validation process returns success m_sStatus.
         * @param file_url
         */
        @Override
        protected void onPostExecute(String file_url) {
            m_oProgressDialog.dismiss();
            if (m_IsServerResponding) {
                Toast.makeText(m_oContext, "Server was saved", Toast
                        .LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(m_oContext, "Server was not saved. [" +
                        l_oValidator.getResult()+ "]", Toast
                        .LENGTH_LONG).show();
            }
        }
    }
}
