package com.example.Ntt;

/**
 * Represents Script's DB Columns and Rows enumeration and numbering<br>
 * Used in {@link CSVScriptParser}
 * @author pavelc
 *
 */
public enum ScriptParameters{
    ScriptName ("SCRIPT NAME",0),
    ScriptStarted ("TIME STARTED",1),
    ScriptFinished ("TIME FINISHED",2),
    IterationNumber ("#", 0),
    IterationResult ("RESULT",1),
    IterationDuration ("DURATION",2),
    IterationStarted ("TIME STARTED",3),
    IterationFinished ("TIME FINISHED",4),
    FailureName ("FAILURE NAME",5),
    IsAssert ("IS ASSERT",6),
    ResultsFolder ("RESULTS FOLDER",7),
    Ramdump ("RAMDUMP",8),
    RemotePath ("REMOTE PATH",9),
    GpsLocation ("GPS LOCATION",10),
    HHO ("HHO",11),
    Details ("DETAILS",12);

    private final String cellText;
    private final int columnNumber;

    private ScriptParameters(String sCellText, int nColumnNumber)
    {
        cellText = sCellText;
        columnNumber = nColumnNumber;
    }

    public boolean equalsText(String otherName){
        return (otherName == null)? false : cellText.equals(otherName);
    }

    public boolean equalsNumber(int otherNumber){
        return otherNumber == columnNumber;
    }

    @Override
    public String toString(){
        return cellText;
    }

    public int toNumber(){
        return columnNumber;
    }
}
