package com.cinnamonbob.model;

import com.cinnamonbob.core.model.BuildResult;

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

    public void notify(Project project, BuildResult result)
    {

    }
}
