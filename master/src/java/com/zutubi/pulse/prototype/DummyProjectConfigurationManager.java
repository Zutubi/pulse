package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.*;

import java.util.*;

/**
 *
 *
 */
public class DummyProjectConfigurationManager implements ProjectConfigurationManager
{
    private Map<String, Map<String, TemplateRecord>> store = new HashMap<String, Map<String, TemplateRecord>>();

    private Map<String, String> projectConfigurationRoot = new HashMap<String, String>();

    private RecordTypeRegistry recordTypeRegistry;

    public DummyProjectConfigurationManager()
    {
    }

    public void init() throws InvalidRecordTypeException
    {
        recordTypeRegistry.register("svnConfig", SvnConfiguration.class);
        recordTypeRegistry.register("cvsConfig", CvsConfiguration.class);
        recordTypeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);
        recordTypeRegistry.register("generalConfig", GeneralConfiguration.class);

        Record r = new SingleRecord("svnConfig");
        r.put("url", "http://www.zutubi.com");
        r.put("password", "secret");
        r.put("name", "Duuude");
        r.put("filterPaths", Arrays.asList("a", "b", "c"));

        TemplateRecord tr = new TemplateRecord(r, "1");
        getProjectStore("1").put("svn", tr);

        r = new SingleRecord("cvsConfig");
        r.put("branch", "BRANCH");
        r.put("root", ":ext:cvstester@cinnamonbob.com:/cvsroots");
        r.put("password", "cvs");
        r.put("module", "a");

        tr = new TemplateRecord(r, "1");
        getProjectStore("1").put("cvs", tr);

        r = new SingleRecord("generalConfig");
        r.put("name", "project name");
        r.put("description", "a simple dummy project for testing");
        r.put("url", "http://www.zutubi.com/roxor");

        tr = new TemplateRecord(r, "1");
        getProjectStore("1").put("general", tr);

        projectConfigurationRoot.put("general", "generalConfig");
        projectConfigurationRoot.put("cleanup", "cleanupRuleConfig");
        projectConfigurationRoot.put("svn", "svnConfig");
        projectConfigurationRoot.put("cvs", "cvsConfig");
    }

    public ProjectConfiguration getProject(long projectId)
    {
        return null;
    }

    public List<String> getProjectConfigurationRoot()
    {
        return new LinkedList<String>(projectConfigurationRoot.keySet());
    }

    public String getSymbolicName(String path)
    {
        // resolve the path into an associated info, and if it is the correct type, return its symbolic name.
        String[] pathElements = path.split("/");

        // the path starts with the built in project root configurations.
        String symbolicName = projectConfigurationRoot.get(pathElements[0]);
        if (pathElements.length == 1)
        {
            return symbolicName;
        }

        // navigate through the type tree extracting the info as we go.
        RecordTypeInfo typeInfo = recordTypeRegistry.getInfo(symbolicName);
        for (int i = 1; i < pathElements.length; i++)
        {
            RecordPropertyInfo propertyInfo = typeInfo.getProperty(pathElements[i]);
            if (propertyInfo instanceof SubrecordRecordPropertyInfo)
            {
                typeInfo = ((SubrecordRecordPropertyInfo)propertyInfo).getSubrecordType();
            }
            else if (propertyInfo instanceof RecordMapRecordPropertyInfo)
            {
                typeInfo = ((RecordMapRecordPropertyInfo)propertyInfo).getRecordType();
            }
            else
            {
                return null;
            }
        }
        return typeInfo.getSymbolicName();
    }

    // Get a specific record within a project, referenced by a path made up
    // of field names and map keys (i.e. subrecord names)
    public TemplateRecord getRecord(long projectId, String path)
    {
        return getProjectStore(String.valueOf(projectId)).get(path);
    }

    public void setRecord(long projectId, String path, Map data)
    {
        TemplateRecord record = getRecord(projectId, path);
        if (record == null)
        {
            Record r = new SingleRecord(getSymbolicName(path));
            record = new TemplateRecord(r, String.valueOf(projectId));
            getProjectStore(String.valueOf(projectId)).put(path, record);
        }
        record.putAll(data);
    }

    private Map<String, TemplateRecord> getProjectStore(String scope)
    {
        if (!store.containsKey(scope))
        {
            store.put(scope, new HashMap<String, TemplateRecord>());
        }
        return store.get(scope);
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}
