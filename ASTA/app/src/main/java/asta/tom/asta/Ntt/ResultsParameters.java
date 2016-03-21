package com.example.Ntt;

/**
 * Represents Results DB Columns and Rows enumeration and numbering<br>
 * Used in {@link CSVResultsParser}
 * @author pavelc
 *
 */
public enum ResultsParameters {
    BatchID ("Batch ID",1),
    DeviceID ("Device ID",1),
    SIMNumber ("SIM Number",1),
    Version ("Embedded Version",1),
    Operator ("Operator",1),
    Country ("Country",1),
    TimeStarted ("Started At",1),
    TimeEnded ("Ended At",1),
    TotalDuration ("Total Duration",1),
    TotalAsserts ("Total Asserts",1),
    ScriptNumber ("#", 0),
    ScriptName ("Script Name",1),
    Passed ("Passed",2),
    Failed ("Failed",3),
    Total ("Total",4),
    ScriptAsserts ("Asserts",5),
    ScriptDuration ("Duration",6),
    Failures ("Failures",7),
    Details ("Details",8),
    ScriptIteration ("Iteration",-1);

    private final String cellText;
    private final int columnNumber;

    private ResultsParameters(String sCellText, int nColumnNumber) {
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