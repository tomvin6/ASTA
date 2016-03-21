
package com.example.Listeners;

import android.os.Environment;
import android.util.Log;

import com.example.data.Paths;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * class that enable us to write csv files into device.
 * results folder defined in Paths class.
 */
public class CsvHandler {
    private static final int WRITING_FREQUENCY = 10;
    private static final String SUMMARY_FILE_NAME = Paths.SUMMARY_FILE_NAME;
    private static final String TAG = "CsvHandler";
    private static String m_sCsvFile, m_sFolder, m_sSummaryPath;
    private int m_oCounter;
    private File m_oSummaryFile;
    private CSVWriter m_oWriter, m_oSummaryWriter;
    private List<String[]> m_oDataBuffer;

    public CsvHandler() {
        m_sFolder = Environment.getExternalStorageDirectory()
                + Paths.INTERNAL_RESULTS_DATA;
    }

    /**
     * set and initialize a path for the csv file.
     * @param fileName
     */
    public void setCsvFile(String fileName, String units) {
        m_oDataBuffer = new ArrayList<String[]>();
        m_oCounter = 0;
        try {
            m_sCsvFile = m_sFolder + fileName;
            m_sSummaryPath = m_sFolder + SUMMARY_FILE_NAME;
            File resultsFolder = new File(m_sFolder);
            m_oSummaryFile = new File(m_sSummaryPath);
            if (!resultsFolder.exists()) {
                resultsFolder.mkdir();
            }
            if (!m_oSummaryFile.exists()) {
                m_oSummaryFile.createNewFile();
            }
            m_oSummaryWriter = new CSVWriter(new FileWriter(m_oSummaryFile, true));
            m_oWriter = new CSVWriter(new FileWriter(m_sCsvFile));
            // get header:
            String [] header = {"System Time", "Relative Time",
                    "Avg DL Rate " + units, "DL Rate " + units,
                    "Avg UL Rate " + units, "UL Rate " + units,
                    "Iteration", "Total iterations"};
            //Log.d(TAG, header[0] + header[3] + header[5]);
            write(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * write to CSV file:
     * the writing is done every WritingFrequency times
     * @param line to write
     */
    public void write(String [] line) {

        if (m_oCounter < WRITING_FREQUENCY) {
            m_oDataBuffer.add(line);
            m_oCounter++;
        } else {
            m_oCounter = 0;
            try {
                m_oWriter = new CSVWriter(new FileWriter(m_sCsvFile,true));
                // true value mean it will write on existing .csv file
                m_oWriter.writeAll(m_oDataBuffer);
                m_oWriter.close();
                m_oDataBuffer.clear(); // clear buffer
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * when cancelling a job, this method enable to write
     * what is left inside the buffer.
     */
    public void finishCsvBuffer () {
        m_oCounter = 0;
        try {
            m_oWriter = new CSVWriter(new FileWriter(m_sCsvFile,true));
            // true value mean it will write on existing .csv file
            m_oWriter.writeAll(m_oDataBuffer);
            m_oWriter.close();
            m_oDataBuffer.clear(); // clear buffer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * when we end job, write summary to a special file.
     * this file will be de-serialized when showing last results.
     * @param summary to write
     */
    public void saveTestSummary(String [] summary) {

        try {
            m_oSummaryWriter.writeNext(summary);
            m_oSummaryWriter.close();
            m_oDataBuffer.clear(); // clear buffer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load last results from special csv file called "summary.csv"
     * @return list of tests summary.
     */
    public static List<String[]> loadTestSummery() {
        Log.d(TAG, "load summary");
        List<String[]> lastResults = null;
        // validate folder exist:
        m_sFolder = Environment.getExternalStorageDirectory()
                + Paths.INTERNAL_RESULTS_DATA;
        // validate summary file exist:
        m_sSummaryPath = m_sFolder + SUMMARY_FILE_NAME;
        File summary = new File(m_sSummaryPath);
        if (!summary.exists()) {
            try {
                summary.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "could not create new summary file");
                e.printStackTrace();
            }
        }
        try {
            CSVReader reader = new CSVReader(new FileReader(m_sSummaryPath));
            lastResults = reader.readAll();
            Log.d(TAG, "done load summary");
        } catch (Exception e) {
            Log.d(TAG, "got exception");
            e.printStackTrace();
        }
        return lastResults;
    }
}
