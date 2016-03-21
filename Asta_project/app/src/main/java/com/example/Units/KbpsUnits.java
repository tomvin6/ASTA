package com.example.Units;

/**
 * class that implement UnitConvertor.
 * its method returns Kbps UNITS
 */
public class KbpsUnits implements UnitConvertor {
    //public static final String FORMAT = "%.1f";
    /**
     * KBps calculations: get Kbps
     * @param bytes to calculate
     * @param time passed
     * @return data
     */
    @Override
    public float getDataByUnits(long bytes, long time) {
        float bytesPerSecond =  (bytes / time);
        return  (bytesPerSecond / (1024));
    }
}
