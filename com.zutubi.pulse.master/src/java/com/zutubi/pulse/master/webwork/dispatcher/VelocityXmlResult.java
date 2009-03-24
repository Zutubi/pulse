package com.zutubi.pulse.master.webwork.dispatcher;

import com.opensymphony.webwork.dispatcher.VelocityResult;

/**
 * An extension of the default velocity result that overrides the content type,
 * fixing it to text/xml.
 *
 * This result should be used by velocity templates producing xml.
 */
public class VelocityXmlResult extends VelocityResult
{
    protected String getContentType(String s)
    {
        return "text/xml";
    }
}
