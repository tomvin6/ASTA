package com.example.Ntt;

//import com.marvell.ntt_icon.TestRequest.DataFlowMode;
import com.marvell.ntt.TestRequest.ProtocolType;

import org.simpleframework.xml.Element;

public class NTTScriptParameters extends ScriptParametersAbstract
{

    private static final long serialVersionUID = 1L;
    @Element
    private int serverPort = 8090;
    @Element
    private String serverIp;
    @Element
    private ProtocolType protocol;		//Enum

    @Element
    private boolean download;
    @Element
    private int numberOfSessionsDL;		//number of threads
    @Element
    private int transferTimeDL; 		//in seconds
    @Element
    private int transferSizeDL;			//in bytes
    @Element
    private int sendSpeedDL;			//(UDP) KBps
    @Element
    private int packetSizeDL;			//(UDP) max = 1472, min = 8 (4(sequenceNumber) + 4(packetID)
    @Element
    private int dataChunkSizeDL;		//(TCP) max < socketSendingBuffer, data Chunk Size like packet size in UDP
    @Element
    private int socketSendBufferDL;	//(TCP) max = 65536, socket sending buffer that is filled with data chunks.

    @Element
    private boolean upload;
    @Element
    private int numberOfSessionsUL;		//number of threads
    @Element
    private int transferTimeUL; 		//in seconds
    @Element
    private int transferSizeUL;			//in bytes
    @Element
    private int sendSpeedUL;			//(UDP) KBps
    @Element
    private int packetSizeUL;			//(UDP) max = 1472, min = 8 (4(sequenceNumber) + 4(packetID))
    @Element
    private int dataChunkSizeUL;		//(TCP) max < socketSendingBuffer, data Chunk Size like packet size in UDP
    @Element
    private int socketSendBufferUL;	//(TCP) max = 65536, socket sending buffer that is filled with data chunks.

    public int getServerPort()
    {
        return serverPort;
    }
    //    public void setServerPort(int serverPort)
    //    {
    //        this.serverPort = serverPort;
    //    }
    public String getServerIp()
    {
        return serverIp;
    }
    public void setServerIp(String serverIp)
    {
        this.serverIp = serverIp;
    }
    public ProtocolType getProtocol()
    {
        return protocol;
    }
    public void setProtocol(ProtocolType protocol)
    {
        this.protocol = protocol;
    }
    public boolean isDownload()
    {
        return download;
    }
    public void setDownload(boolean download)
    {
        this.download = download;
    }
    public int getNumberOfSessionsDL()
    {
        return numberOfSessionsDL;
    }
    public void setNumberOfSessionsDL(int numberOfSessionsDL)
    {
        this.numberOfSessionsDL = numberOfSessionsDL;
    }
    public int getTransferTimeDL()
    {
        return transferTimeDL;
    }
    public void setTransferTimeDL(int transferTimeDL)
    {
        this.transferTimeDL = transferTimeDL;
    }
    public int getTransferSizeDL()
    {
        return transferSizeDL;
    }
    public void setTransferSizeDL(int transferSizeDL)
    {
        this.transferSizeDL = transferSizeDL;
    }
    public int getSendSpeedDL()
    {
        return sendSpeedDL;
    }
    public void setSendSpeedDL(int sendSpeedDL)
    {
        this.sendSpeedDL = sendSpeedDL;
    }
    public int getPacketSizeDL()
    {
        return packetSizeDL;
    }
    public void setPacketSizeDL(int packetSizeDL)
    {
        this.packetSizeDL = packetSizeDL;
    }
    public boolean isUpload()
    {
        return upload;
    }
    public void setUpload(boolean upload)
    {
        this.upload = upload;
    }
    public int getNumberOfSessionsUL()
    {
        return numberOfSessionsUL;
    }
    public void setNumberOfSessionsUL(int numberOfSessionsUL)
    {
        this.numberOfSessionsUL = numberOfSessionsUL;
    }
    public int getTransferTimeUL()
    {
        return transferTimeUL;
    }
    public void setTransferTimeUL(int transferTimeUL)
    {
        this.transferTimeUL = transferTimeUL;
    }
    public int getTransferSizeUL()
    {
        return transferSizeUL;
    }
    public void setTransferSizeUL(int transferSizeUL)
    {
        this.transferSizeUL = transferSizeUL;
    }
    public int getSendSpeedUL()
    {
        return sendSpeedUL;
    }
    public void setSendSpeedUL(int sendSpeedUL)
    {
        this.sendSpeedUL = sendSpeedUL;
    }
    public int getPacketSizeUL()
    {
        return packetSizeUL;
    }
    public void setPacketSizeUL(int packetSizeUL)
    {
        this.packetSizeUL = packetSizeUL;
    }

    public int getDataChunkSizeDL()
    {
        return dataChunkSizeDL;
    }
    public void setDataChunkSizeDL(int dataChunkSizeDL)
    {
        this.dataChunkSizeDL = dataChunkSizeDL;
    }
    public int getDataChunkSizeUL()
    {
        return dataChunkSizeUL;
    }
    public void setDataChunkSizeUL(int dataChunkSizeUL)
    {
        this.dataChunkSizeUL = dataChunkSizeUL;
    }
    public int getSocketSendBufferUL()
    {
        return socketSendBufferUL;
    }
    public void setSocketSendBufferUL(int socketSendBufferUL)
    {
        this.socketSendBufferUL = socketSendBufferUL;
    }
    public int getSocketSendBufferDL()
    {
        return socketSendBufferDL;
    }
    public void setSocketSendBufferDL(int socketSendBufferDL)
    {
        this.socketSendBufferDL = socketSendBufferDL;
    }

    @Override
    public String getScriptsAbbreviation() {

        return super.getScriptsAbbreviation() + "\nIP: " + serverIp + " " + protocol
                + (isDownload() ? " DL: " + numberOfSessionsDL + " sessions, " : "")
                + (isUpload() ? " UL: " + numberOfSessionsUL + " sessions" : "");
    }
}
