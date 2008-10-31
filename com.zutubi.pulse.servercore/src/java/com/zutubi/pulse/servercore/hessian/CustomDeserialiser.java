package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.JavaDeserializer;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Deserialiser that overrides the construction behaviour for troublesome
 * classes.
 */
public class CustomDeserialiser extends JavaDeserializer
{
    public CustomDeserialiser(Class cl)
    {
        super(cl);
    }

    protected Object instantiate() throws Exception
    {
        if(LogRecord.class.isAssignableFrom(getType()))
        {
            return new LogRecord(Level.OFF, null);
        }
        else if(Level.class.isAssignableFrom(getType()))
        {
            return Level.OFF;
        }

        return super.instantiate();
    }
}
