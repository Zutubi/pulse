package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Represents a condition that filters project build notifications.  All
 * conditions boil down to a single boolean expression, however common are
 * expressions are configured with custom interfaces for usability.  The
 * condition objects capture the type of expression that has been configured.
 */
public abstract class ProjectBuildCondition extends Entity
{
    public abstract String getType();
    public abstract String getExpression();
}
