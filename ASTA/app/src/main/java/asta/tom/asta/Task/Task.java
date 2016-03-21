package com.example.Task;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.os.Environment;
import android.util.Log;

/*
 * Task holds a map with parameter names and values.
 * Task can be saved to disk. 
 */
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    // Task Parameters
    private String m_sTaskName; //task name
    private Server m_oServer;
    private int m_nProtocol; // Constants.TCP / UDP
    private int m_nMode; //Constants.DN_MODE/UP_MODE/DN_UP_MODE
    private int m_nFtpNttMode; //
    private int m_nLimitationType; //TIME_LIMITATION / DATA_LIMITATION
    private int m_nUlSession;
    private int m_nDnSession;
    private int m_nDnSpeed, m_nUlSpeed;
    private int m_niterations, m_nBufferSize;
    private int m_nLimitationQuantity;
    /**
     * set speeds in ntt task:
     * @param dnSpeed
     * @param ulSpeed
     */
    public void setSpeeds(int dnSpeed, int ulSpeed) {
        m_nDnSpeed = dnSpeed;
        m_nUlSpeed = ulSpeed;
    }
    public int getDnSpeed() {
        return m_nDnSpeed;
    }
    public int getUlSpeed() {
        return m_nUlSpeed;
    }
    /**
     * constructor
     */
    public Task(String taskName, Server server, int ftpNttMode,
            int protocol, int mode, int limitationType, int limitationQuantity,
            int iterations, int dnSessions, int ulSessions, int buffer) {
        this.m_sTaskName = taskName;
        this.m_oServer = server;
        this.m_nProtocol = protocol;
        this.m_nUlSession = ulSessions;
        this.m_nFtpNttMode = ftpNttMode;
        this.m_nDnSession = dnSessions;
        this.m_nMode = mode;
        this.m_nLimitationType = limitationType;
        m_nLimitationQuantity = limitationQuantity;
        this.m_niterations = iterations;
    }
    /**
     * getters of sessions
     * @return number of sessions.
     */
    public int getDLSession() {
        return m_nDnSession;
    }
    public int getULSession() {
        return m_nUlSession;
    }
    public int getBufferSize() {
        return m_nBufferSize;
    }
    public int getFtpNttMode() {
        return m_nFtpNttMode;
    }
    /**
     * getter of limitation quantity:
     * @return quantity.
     */
    public int getLimitationQuantity() {
        return m_nLimitationQuantity;
    }

    /**
     * @return the m_sTaskName
     */
    public String getTaskName() {
        Log.d("TaskRunnerActivity", "Task:getTaskName() = " + m_sTaskName);
        return m_sTaskName;
    }

    /**
     * @return the m_oServer
     */
    public Server getServer() {
        return m_oServer;
    }

    /**
     * @return the m_nProtocol
     */
    public int getProtocol() {
        return m_nProtocol;
    }

    /**
     * @return the m_nMode
     */
    public int getMode() {
        return m_nMode;
    }

    /**
     * @return the m_nLimitationType
     */
    public int getLimitationType() {
        return m_nLimitationType;
    }

    /**
     * @return the m_niterations
     */
    public int getIterations() {
        return m_niterations;
    }

    /*
     * returns name of Task
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return m_sTaskName;
    }

    public void setTimeLimitation(int timeLimitation) {
        this.m_nLimitationQuantity = timeLimitation;
    }
}