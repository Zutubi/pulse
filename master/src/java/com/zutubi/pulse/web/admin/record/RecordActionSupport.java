package com.zutubi.pulse.web.admin.record;

import com.zutubi.pulse.prototype.Scopes;
import com.zutubi.pulse.prototype.TemplateManager;
import com.zutubi.pulse.prototype.TemplateRecord;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;

/**
 * The base for actions dealing with project (and template) records.
 */
public class RecordActionSupport extends ActionSupport
{
    /**
     * Id of the project or template that owns the record.
     */
    private long projectId;
    /**
     * Path of the record we are addressing.
     */
    private String path;

    private TemplateRecord record;
    private TemplateManager templateManager;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public TemplateRecord getRecord()
    {
        if(record == null)
        {
            record = templateManager.load(Scopes.PROJECTS, Long.toString(projectId), path);
            if(record == null)
            {
                throw new LookupErrorException("Request for non-existant record '" + path + "' in project [" + projectId + "]");
            }
        }
        
        return record;
    }

    public void setTemplateManager(TemplateManager templateManager)
    {
        this.templateManager = templateManager;
    }
}
