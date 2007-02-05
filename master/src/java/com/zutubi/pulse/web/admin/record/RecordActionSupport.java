package com.zutubi.pulse.web.admin.record;

import com.zutubi.pulse.prototype.*;
import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.prototype.record.SingleRecord;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

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

    protected TemplateRecord record;

    protected static ProjectConfigurationManager projectConfigurationManager = new MyProjectConfigurationManager();

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

    private static class MyProjectConfigurationManager implements ProjectConfigurationManager
    {
        private Map<String, TemplateRecord> store = new HashMap<String, TemplateRecord>();

        public MyProjectConfigurationManager()
        {
            Record r = new SingleRecord("svnConfiguration");
            r.put("url", "http://www.zutubi.com");
            r.put("password", "secret");
            r.put("name", "Duuude");
            r.put("filterPaths", Arrays.asList("a", "b", "c"));

            TemplateRecord tr = new TemplateRecord(r, "1");
            store.put("some/path", tr);

            r = new SingleRecord("cvsConfiguration");
            r.put("branch", "BRANCH");
            r.put("root", ":ext:cvstester@cinnamonbob.com:/cvsroots");
            r.put("password", "cvs");
            r.put("module", "a");

            tr = new TemplateRecord(r, "1");
            store.put("some/other/path", tr);
        }

        public ProjectConfiguration getProject(long projectId)
        {
            return null;
        }

        // Get a specific record within a project, referenced by a path made up
        // of field names and map keys (i.e. subrecord names)
        public TemplateRecord getRecord(long projectId, String path)
        {
            return store.get(path);
        }

        public void setRecord(long projectId, String path, Map data)
        {
            TemplateRecord record = getRecord(projectId, path);
            record.putAll(data);
        }
    }
}
