package com.example.Task;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import com.example.data.Constants;
import com.example.data.Paths;

/**
 * serialize assignments.
 * task & job implement Assignments.
 *
 */
public class GeneralSerializer {
    private static final String TAG = "GeneralSerializer";
    private String m_sAppSystemFolderPath;
    private static final String FTP = "Ftp_";
    private static final String NTT = "Ntt_";
    /**
     * constructor.
     */
    public GeneralSerializer() {
        m_sAppSystemFolderPath = Environment.getExternalStorageDirectory().getPath()
                + Paths.INTERNAL_SYSTEM_FOLDER;
    }
    /**
     * getter of Tasks folder name
     * @return Tasks folder name
     */
    public String getTaskFolderName() {
        return Paths.TASK_FOLDER;
    }
    /**
     * getter of Servers folder name
     * @return Servers folder name
     */
    public String getServersFolderName() {
        return Paths.SYSTEM_FOLDER_NAME;
    }

    /**
     * save Task to file.
     * need to be done by another thread. (not main thread)
     * in successful save - return true, otherwise return false.
     * @param appFolder application folder
     * return file path.
     */
    public void saveTask(File appFolder, Task fileToSave) {
        // make directory for the XML object:
        File l_oDirectory = new File(m_sAppSystemFolderPath + File.separator + Paths.TASK_FOLDER);
        if (!l_oDirectory.exists()) {
            l_oDirectory.mkdirs();
        }
        // save object to the created folder in this format: task_id_name.xml
        File l_oTaskFile = new File(
                l_oDirectory.getPath() + File.separator + fileToSave.getTaskName() + ".xml");

        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(l_oTaskFile)); //Select where you wish to save the file...
            oos.writeObject(fileToSave); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * load Task from file.
     * need to be done by another thread. (not main thread)
     * in successful load - return Task object, when file not found - return null.
     * when encounter error while reading - throws exception.
     * @param appFolder application folder
     */
    public Task loadTaskByName(int taskType, File appFolder, String name) {
        Task l_oTask;
        File l_oDirectory = new File(
                m_sAppSystemFolderPath + File.separator + Paths.TASK_FOLDER);
        if (!l_oDirectory.exists()) {
            l_oDirectory.mkdirs();
        }
        if (taskType == Constants.FTP) {
            name = FTP + name;

        } else { //NTT Task
            name = NTT + name;

        }
        // File xmlFile = new File(getFilesDir().getPath() + "/Person.xml");
        File l_oTaskFile = new File(
                l_oDirectory.getPath() + File.separator + name + ".xml");
        if (!l_oTaskFile.exists()) {
            Server defaultServer = new Server(taskType,
                    Constants.DEFAULT_FTP_SERVER_NAME , Constants.DEF_FTP_IP,
                    Constants.DEFAULT_FTP_PORT,
                    Constants.DEFAULT_FTP_USER,
                    Constants.DEF_FTP_PASSWORD); // just for test
            l_oTask = new Task(name, defaultServer,
                    Constants.FTP, Constants.TCP, Constants.DN_MODE,
                    Constants.DATA_LIMITATION,
                    Constants.DEF_LIM_QUANTITY, Constants
                    .DEF_ITERATION, Constants.WIFI);
            this.saveTask(new File(m_sAppSystemFolderPath), l_oTask);
            return l_oTask;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(l_oTaskFile));
            l_oTask = (Task) ois.readObject();
            return l_oTask;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * load Servers list from folder.
     * if folder doesn't exist, create it and throw (No server found) exception
     * if folder exist, get all of the files inside this folder and
     * insert each one of them to a list of servers.
     */
    public List<Server> loadServersList(File appFolder, int serversType) {
        List<Server> l_oServers;
        File l_oServersFolder = new File(m_sAppSystemFolderPath
                + Paths.DEVICE_FTP_SERVERS_FOLDER_NAME);
        Log.d(TAG, "l_oServersFolder = " + l_oServersFolder);
        l_oServers = new ArrayList<Server>(); // create an array of servers:
        // if folder doesn't exist, create it:
        if (!l_oServersFolder.exists()) {
            Log.d(TAG, "folder isn't exist");
            l_oServersFolder.mkdirs();
        }
        try {
            // get list of files inside servers folder:
            File[] serversFileList = l_oServersFolder.listFiles();

            for (File serverFile : serversFileList) {
                if (!serverFile.exists()) {
                    Log.e(TAG, "file not exist!!!!!: " + serverFile);
                }
                FileInputStream fin = new FileInputStream(serverFile);
                ObjectInputStream ois = new ObjectInputStream(fin);
                Server tmpServer = (Server) ois.readObject();
                ois.close();
                fin.close();
                Log.d(TAG, "got  object = " + tmpServer.getServerName());
                Log.d(TAG, "serverFile was read and cast: " + serverFile);
                // add servers by type (ftp / ntt_icon)
                if (tmpServer.getServerType() == serversType) {
                    Log.d(TAG, "got " + serversType + " server: " + tmpServer);
                    l_oServers.add(tmpServer);
                } else {
                    Log.d(TAG, "not this type: " + tmpServer);
                }
            }
            return l_oServers;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
            e.printStackTrace();
        } catch (OptionalDataException e) {
            Log.e(TAG, "OptionalDataException");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            Log.e(TAG, "StreamCorruptedException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
        }
        return l_oServers;
    }
    /**
     * delete Servers list from folder.
     * if folder doesn't exist, create it and throw (No server found) exception
     * if folder exist, get all of the files inside this folder and
     * insert each one of them to a list of servers.
     */
    public void deleteAllServers(File appFolder) {
        List<Server> l_oServers;
        File l_oServersFolder = new File(m_sAppSystemFolderPath
                + Paths.DEVICE_FTP_SERVERS_FOLDER_NAME);
        // if folder doesn't exist, create it:
        if (!l_oServersFolder.exists()) {
            l_oServersFolder.mkdirs();
        }
        try {
            l_oServers = new ArrayList<Server>(); // create an array of servers:
            // get list of files inside servers folder:
            File[] serversFileList = l_oServersFolder.listFiles();
            // insert every server in folder to list of servers:
            for (File serverFile : serversFileList) {
                serverFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * delete server by name
     */
    public void deleteServer(File appFolder, String serverIp) {
        List<Server> l_oServers;
        File l_oServersFolder = new File(m_sAppSystemFolderPath
                + Paths.DEVICE_FTP_SERVERS_FOLDER_NAME);
        // if folder doesn't exist, create it:
        if (!l_oServersFolder.exists()) {
            l_oServersFolder.mkdirs();
        }
        try {
            l_oServers = new ArrayList<Server>(); // create an array of servers:
            // get list of files inside servers folder:
            File[] serversFileList = l_oServersFolder.listFiles();
            // insert every server in folder to list of servers:
            for (File serverFile : serversFileList) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serverFile));
                Server tmpServer = (Server) ois.readObject();
                if (tmpServer.getServerIp().trim().equals(serverIp.trim())) {
                    serverFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * save server to servers folder.
     * server folder defined with the following name (m_sServersFolderName).
     * throws exception if fails to save.
     */
    public void saveServer(File appFolder, Server server) {
        // make directory for the XML object:
        File l_oDirectory = new File(m_sAppSystemFolderPath
                + Paths.DEVICE_FTP_SERVERS_FOLDER_NAME);
        if (!l_oDirectory.exists()) {
            l_oDirectory.mkdirs();
        }
        // add type to server in device:
        String type;
        if (server.getServerType() == Constants.FTP) {
            type = FTP;
        } else {
            type = NTT;
        }
        // save object to the created folder in this format: task_id_name.xml
        File l_oServerFile = new File(
                l_oDirectory.getPath() + File.separator
                        + type
                        + server.getServerIp() + ".xml");
        if (l_oServerFile.exists()) {
            l_oServerFile.delete();
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(l_oServerFile)); //Select where you wish to save the file...
            oos.writeObject(server); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}