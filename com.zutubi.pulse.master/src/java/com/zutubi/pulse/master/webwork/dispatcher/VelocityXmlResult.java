package com.zutubi.pulse.master.webwork.dispatcher;

import com.opensymphony.webwork.dispatcher.VelocityResult;

/**
 *
 *
 */
public class VelocityXmlResult extends VelocityResult
{
    protected String getContentType(String s)
    {
        return "text/xml";
    }
}
