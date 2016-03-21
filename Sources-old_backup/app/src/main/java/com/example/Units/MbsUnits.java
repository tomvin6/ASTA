package com.example.Units;

/**
 * class that implement UnitConvertor.
 * its method returns MB/s UNITS
 */
public class MbsUnits implements UnitConvertor {
    //public static final String FORMAT = "%.2f";
    /**
     * MB/s calculations: get MB/s
     * @param bytes to calculate
     * @param time passed
     * @return data
     */
    @Override
    public float getDataByUnits(long bytes, long time) {
        float bytesPerSecond =  (bytes / time);
        float tmpVal =(bytesPerSecond / (1024)) * 8;
        return (tmpVal / 1000);
    }
}
