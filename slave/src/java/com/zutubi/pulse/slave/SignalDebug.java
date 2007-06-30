package com.zutubi.pulse.slave;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 */
public class SignalDebug implements SignalHandler
{
    public SignalDebug()
    {
        Signal signal = new Signal();
    }

    public void handle(Signal signal)
    {
        throw new RuntimeException("Method not yet implemented.");
    }
}
