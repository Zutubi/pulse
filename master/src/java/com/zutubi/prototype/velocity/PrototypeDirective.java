package com.zutubi.prototype.velocity;

import com.zutubi.pulse.velocity.AbstractDirective;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.prototype.PrototypePath;
import freemarker.template.Configuration;

/**
 *
 *
 */
public abstract class PrototypeDirective extends AbstractDirective
{
    protected Configuration configuration;

    protected RecordTypeRegistry recordTypeRegistry;

    protected ProjectConfigurationManager projectConfigurationManager;

    protected PrototypePath path;

    public PrototypeDirective()
    {
        ComponentContext.autowire(this);
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }

    public void setPath(String path)
    {
        this.path = new PrototypePath(path);
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager projectConfigurationManager)
    {
        this.projectConfigurationManager = projectConfigurationManager;
    }

}
