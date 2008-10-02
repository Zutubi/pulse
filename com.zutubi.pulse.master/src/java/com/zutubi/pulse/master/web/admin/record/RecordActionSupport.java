package com.zutubi.pulse.master.web.admin.record;

import com.zutubi.pulse.master.web.ActionSupport;

/**
 * The base for actions dealing with project (and template) records.
 *
 * @deprecated
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

/*
    protected TemplateRecord record;

    protected ProjectConfigurationManager projectConfigurationManager;

    public RecordActionSupport()
    {
    }

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
        if (record == null)
        {
            record = projectConfigurationManager.getRecord(projectId, path);
            if (record == null)
            {
                throw new LookupErrorException("Request for non-existant record '" + path + "' in project [" + projectId + "]");
            }
        }
        return record;
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager pcm)
    {
        this.projectConfigurationManager = pcm;
    }
*/
}
