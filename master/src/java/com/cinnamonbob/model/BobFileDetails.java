package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.core.model.Revision;

/**
 */
public abstract class BobFileDetails extends Entity
{
    public abstract String getBobFile(Project project, Revision revision);
}
