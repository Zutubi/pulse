package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.*;
import com.zutubi.prototype.PrototypePath;

import java.util.*;

/**
 *
 *
 */
public class DummyProjectConfigurationManager implements ProjectConfigurationManager
{
    private Map<String, Map<String, TemplateRecord>> store = new HashMap<String, Map<String, TemplateRecord>>();

    private PrototypeConfigRegistry configRegistry;
    
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
        recordTypeRegistry.register("scmConfig", ScmConfiguration.class);
        
        // this is behaviour that will be moved into the scm configuration extension point manager.
        RecordTypeInfo scmTypeInfo = recordTypeRegistry.getInfo("scmConfig");
        scmTypeInfo.addExtension(recordTypeRegistry.getInfo("svnConfig"));
        scmTypeInfo.addExtension(recordTypeRegistry.getInfo("cvsConfig"));

        Record r = new SingleRecord("svnConfig");
        r.put("url", "http://www.zutubi.com");
        r.put("password", "secret");
        r.put("name", "Duuude");
        r.put("filterPaths", Arrays.asList("a", "b", "c"));

        TemplateRecord tr = new TemplateRecord(r, "1");
        getProjectStore("1").put("scm", tr);

        r = new SingleRecord("generalConfig");
        r.put("name", "project name");
        r.put("description", "a simple dummy project for testing");
        r.put("url", "http://www.zutubi.com/roxor");

        tr = new TemplateRecord(r, "1");
        getProjectStore("1").put("general", tr);

        // configuration setup.
        Map<String, String> projectScope = configRegistry.addScope("project");
        projectScope.put("general", "generalConfig");
        projectScope.put("cleanup", "cleanupRuleConfig");
        projectScope.put("scm", "scmConfig");
    }

    public ProjectConfiguration getProject(long projectId)
    {
        return null;
    }

    public List<String> getProjectConfigurationRoot()
    {
        return configRegistry.getRoot("project");
    }

    public String getSymbolicName(PrototypePath path)
    {
        // resolve the path into an associated info, and if it is the correct type, return its symbolic name.
        List<String> pathElements = path.getPathElements();

        // the path starts with the built in project root configurations.
        String symbolicName = configRegistry.getScope("project").get(pathElements.get(0));
        if (pathElements.size() == 1)
        {
            return symbolicName;
        }

        // navigate through the type tree extracting the info as we go.
        RecordTypeInfo typeInfo = recordTypeRegistry.getInfo(symbolicName);
        for (int i = 1; i < pathElements.size(); i++)
        {
            RecordPropertyInfo propertyInfo = typeInfo.getProperty(pathElements.get(i));
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
    public TemplateRecord getRecord(PrototypePath path)
    {
        return getProjectStore(String.valueOf(path.getScopeId())).get(path.getPath());
    }

    public void setRecord(PrototypePath path, Map data)
    {
        TemplateRecord record = getRecord(path);
        if (record == null)
        {
            Record r = new SingleRecord(getSymbolicName(path));
            record = new TemplateRecord(r, String.valueOf(path.getScopeId()));
            getProjectStore(String.valueOf(path.getScopeId())).put(path.getPath(), record);
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

    public void setConfigRegistry(PrototypeConfigRegistry configRegistry)
    {
        this.configRegistry = configRegistry;
    }
}
