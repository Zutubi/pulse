package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
 *
 *
 */
public class BuildSpecificationNameValue implements NotifyStringValue
{
    public Comparable getValue(BuildResult result, User user)
    {
        return result.getSpecName().getName();
    }
}
