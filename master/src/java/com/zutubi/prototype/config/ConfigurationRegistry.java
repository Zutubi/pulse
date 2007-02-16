package com.zutubi.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.Traversable;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.prototype.CleanupRuleConfiguration;
import com.zutubi.pulse.prototype.CommitMessageConfiguration;
import com.zutubi.pulse.prototype.CustomCommitMessageConfiguration;
import com.zutubi.pulse.prototype.CvsConfiguration;
import com.zutubi.pulse.prototype.GeneralConfiguration;
import com.zutubi.pulse.prototype.JiraCommitMessageConfiguration;
import com.zutubi.pulse.prototype.ScmConfiguration;
import com.zutubi.pulse.prototype.SvnConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 *
 */
public class ConfigurationRegistry
{
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;

    private Map<String, Traversable> scopes = new HashMap<String, Traversable>();

    public void init() throws TypeException
    {
        // setup the initial configuration.
        typeRegistry.register("scmConfig", ScmConfiguration.class);
        typeRegistry.register("svnConfig", SvnConfiguration.class);
        typeRegistry.register("cvsConfig", CvsConfiguration.class);
        typeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);
        typeRegistry.register("generalConfig", GeneralConfiguration.class);
        typeRegistry.register("commitConfig", CommitMessageConfiguration.class);
        typeRegistry.register("jiraCommitConfig", JiraCommitMessageConfiguration.class);
        typeRegistry.register("customCommitConfig", CustomCommitMessageConfiguration.class);

        // sort out the extensions.
        CompositeType scmConfig = typeRegistry.getType("scmConfig");
        scmConfig.addExtension("svnConfig");
        scmConfig.addExtension("cvsConfig");

        CompositeType commitConfig = typeRegistry.getType("commitConfig");
        commitConfig.addExtension("jiraCommitConfig");
        commitConfig.addExtension("customCommitConfig");

        // generated dynamically as new components are registered.
        CompositeType projectConfig = new CompositeType(String.class, "projectConfig");
        projectConfig.addProperty("scm", typeRegistry.getType("scmConfig"), null, null);
        projectConfig.addProperty("general", typeRegistry.getType("generalConfig"), null, null);
        projectConfig.addProperty("cleanup", typeRegistry.getType("cleanupRuleConfig"), null, null);
        projectConfig.addProperty("commit", typeRegistry.getType("commitConfig"), null, null);

        // define the root level scope.
        MapType projectCollection = new MapType(HashMap.class);
        projectCollection.setCollectionType(projectConfig);

        typeRegistry.register("projectConfig", projectConfig);

        scopes.put("project", projectCollection);

        dummyData();
    }

    private void dummyData()
    {
        // setup default data.
        Record scm = new Record();
        scm.putMetaProperty("symbolicName", "svnConfig");
        scm.put("url", "http://www.zutubi.com");
        scm.put("password", "secret");
        scm.put("name", "Duuude");
        Record filters = new Record();
        filters.put("0", "a");
        filters.put("1", "b");
        filters.put("2", "c");
        scm.put("filterPaths", filters);

        recordManager.store("project/1/scm", scm);

    }

    public String getSymbolicName(String path)
    {
        List<String> pathElements = new LinkedList<String>();
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            pathElements.add(tokens.nextToken());
        }

        String scope = pathElements.get(0);
        Traversable traversableType = scopes.get(scope);

        Type type = traversableType.getType(pathElements.subList(1, pathElements.size()));
        if (type != null && type instanceof CompositeType)
        {
            return ((CompositeType)type).getSymbolicName();
        }
        
        return null;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
