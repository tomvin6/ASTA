package com.example.Task;

import com.example.data.Constants;

import java.io.Serializable;

/**
 * model class that contains server details
 */
public class Server implements Serializable {

    private static final long serialVersionUID = 1L;
    private String m_sServerName;
    private String m_sServerIp;
    private int m_nPort, m_sServerType;
    private String m_sUserName;
    private String m_sPassword;

    /**
     * constructor
     * @param serverType type (ntt / ftp)
     * @param serverName string name
     * @param ip server ip
     * @param port server port
     * @param uName user name
     * @param pass password
     */
    public Server(int serverType, String serverName, String ip, int port,
                  String uName, String pass) {
        m_sServerName = serverName;
        m_sServerType = serverType;
        m_sServerIp = ip;
        m_nPort = port;
        m_sUserName = uName;
        m_sPassword = pass;
    }
    /**
     * @return the m_sServerType
     */
    public int getServerType() {
        return m_sServerType;
    }
    /**
     * @return the m_sServerIp
     */
    public String getServerIp() {
        return m_sServerIp;
    }
    /**
     * @return the m_nPort
     */
    public int getPort() {
        return m_nPort;
    }
    /**
     * @return the m_sUserName
     */
    public String getUserName() {
        return m_sUserName;
    }
    /**
     * @return the m_sPassword
     */
    public String getPassword() {
        return m_sPassword;
    }
    /**
     * get server name
     * @return name
     */
    public String getServerName() {
        // TODO Auto-generated method stub
        return this.m_sServerName;
    }
    @Override
    public String toString() {
        return this.m_sServerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server server = (Server) o;
        return m_sServerIp.equals(server.m_sServerIp);
    }

    @Override
    public int hashCode() {
        return m_sServerIp.hashCode();
    }
}
