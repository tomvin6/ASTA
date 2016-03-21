package com.example.Ntt;

public interface IObservableBatch 
{
    /**
     * Get notified about changes from the child script-thread.
     * The message should direct the batch what to do / what it
     * signals (stop, pause, etc')
     */
    public void notifyChanges(ScriptMessage msg);
}
