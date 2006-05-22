/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Revision;

import java.util.Properties;

/**
 */
public abstract class PulseFileDetails extends Entity
{
    public abstract PulseFileDetails copy();

    public abstract boolean isBuiltIn();

    public abstract String getType();

    public abstract Properties getProperties();

    public abstract String getPulseFile(long id, Project project, Revision revision);

}
