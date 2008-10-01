package com.zutubi.pulse.dev.xmlrpc;

import com.zutubi.pulse.core.PulseRuntimeException;

/**
 */
public class PulseXmlRpcException extends PulseRuntimeException
{
    public PulseXmlRpcException()
    {
        super();
    }

    public PulseXmlRpcException(String errorMessage)
    {
        super(errorMessage);
    }

    public PulseXmlRpcException(Throwable cause)
    {
        super(cause);
    }

    public PulseXmlRpcException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
