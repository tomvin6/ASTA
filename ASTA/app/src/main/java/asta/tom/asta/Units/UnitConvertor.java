package com.example.Units;

/**
 * interface for UNITS.
 * we used a strategy pattern to determine UNITS in run-time
 * KBpsUnits and MBsUnits implement this interface.
 */
public interface UnitConvertor {

    /**
     * KbpsUnits and MBsUnits implement this class
     * @param bytes to calculate
     * @param time passed
     * @return UNITS
     */
    float getDataByUnits(long bytes, long time);
}
