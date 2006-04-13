/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

/**
 *
 */
public class YahooContactPoint extends ContactPoint
{
    public void setYahooId(String id)
    {
        setUid(id);
    }

    public String getYahooId()
    {
        return getUid();
    }

    public void notify(BuildResult result)
    {

    }
}
