package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;

/**
 * A contact point defines a means of contacting a user.
 */
public interface ContactPoint
{
    /**
     * Returns the name of this contact point.
     * 
     * @return the name of this contact point.
     */
    public String getName();

    public String getUid();

    public void notify(BuildResult result);
}

