package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.core.model.Revision;

import java.util.Properties;

/**
 */
public abstract class BobFileDetails extends Entity
{
    public abstract String getType();

    public abstract Properties getProperties();

    public abstract String getBobFile(long id, Project project, Revision revision);
}
