package com.example.Ntt;

import org.simpleframework.xml.Element;

import java.io.Serializable;

public abstract class ScriptParametersAbstract implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //Abstract class that holds all parameters
    @Element
    private int numberOfIterations;
    @Element
    private int intervalBetweenIterations;

    private int currentIterationNumber;

    public int getNumberOfIterations()
    {
        return numberOfIterations;
    }
    public void setNumberOfIterations(int numberOfIterations)
    {
        this.numberOfIterations = numberOfIterations;
    }
    public int getIntervalBetweenIterations()
    {
        return intervalBetweenIterations;
    }
    public void setIntervalBetweenIterations(int intervalBetweenIterations)
    {
        this.intervalBetweenIterations = intervalBetweenIterations;
    }
    public int getCurrentIterationNumber()
    {
        return currentIterationNumber;
    }
    public void setCurrentIterationNumber(int currentIterationNumber)
    {
        this.currentIterationNumber = currentIterationNumber;
    }

    public String getScriptsAbbreviation()
    {
        return numberOfIterations + " it. ";
    }

    @Override
    public String toString() {
        return getScriptsAbbreviation();
    }
}
