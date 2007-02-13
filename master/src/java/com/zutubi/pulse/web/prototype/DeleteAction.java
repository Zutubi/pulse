package com.zutubi.pulse.web.prototype;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.pulse.prototype.TemplateRecord;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.prototype.model.Config;
import com.zutubi.prototype.ConfigurationDescriptorFactory;
import com.zutubi.prototype.ConfigurationDescriptor;
import com.zutubi.prototype.PrototypePath;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

import java.util.*;

/**
 *
 *
 */
public class DeleteAction extends ActionSupport
{
    private PrototypePath path;

    private ProjectConfigurationManager projectConfigurationManager;

    public void setPath(String path)
    {
        this.path = new PrototypePath(path);
    }

    public String getPath()
    {
        return path.toString();
    }

    public String getBasePath()
    {
        return path.getBasePath();
    }

    public String execute() throws Exception
    {
        projectConfigurationManager.getRecord(path);

        return SUCCESS;
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager projectConfigurationManager)
    {
        this.projectConfigurationManager = projectConfigurationManager;
    }
}
