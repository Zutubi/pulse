package com.zutubi.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.*;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.admin.JabberConfiguration;
import com.zutubi.pulse.prototype.config.admin.LDAPConfiguration;
import com.zutubi.pulse.prototype.config.admin.LicenseConfiguration;
import com.zutubi.pulse.prototype.config.admin.LoggingConfiguration;

import java.util.HashMap;

/**
 *
 *
 */
public class ConfigurationRegistry
{
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public void init() throws TypeException
    {
        // scm configuration
        CompositeType scmConfig = (CompositeType) typeRegistry.register("scmConfig", ScmConfiguration.class);
        typeRegistry.register("svnConfig", SvnConfiguration.class);
        typeRegistry.register("cvsConfig", CvsConfiguration.class);
        typeRegistry.register("perforceConfig", PerforceConfiguration.class);

        // sort out the extensions.
        scmConfig.addExtension("svnConfig");
        scmConfig.addExtension("cvsConfig");
        scmConfig.addExtension("perforceConfig");

        CompositeType typeConfig = (CompositeType) typeRegistry.register("typeConfig", ProjectTypeConfiguration.class);
        typeRegistry.register("antConfig", AntTypeConfiguration.class);
        typeRegistry.register("mavenConfig", MavenTypeConfiguration.class);

        typeConfig.addExtension("antConfig");
        typeConfig.addExtension("mavenConfig");

        // general project configuration
        typeRegistry.register("generalConfig", GeneralConfiguration.class);

        // cleanup rule configuration
        typeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);

        // commit message processors.
        CompositeType commitConfig = (CompositeType) typeRegistry.register("commitConfig", CommitMessageConfiguration.class);
        typeRegistry.register("jiraCommitConfig", JiraCommitMessageConfiguration.class);
        typeRegistry.register("customCommitConfig", CustomCommitMessageConfiguration.class);

        commitConfig.addExtension("jiraCommitConfig");
        commitConfig.addExtension("customCommitConfig");

        // change view configuration
        CompositeType changeViewerConfig = (CompositeType) typeRegistry.register("changeViewerConfig", ChangeViewerConfiguration.class);
        typeRegistry.register("fisheyeChangeViewerConfig", FisheyeConfiguration.class);
        typeRegistry.register("customChangeViewerConfig", CustomChangeViewerConfiguration.class);

        changeViewerConfig.addExtension("fisheyeChangeViewerConfig");
        changeViewerConfig.addExtension("customChangeViewerConfig");
        
        CompositeType artifactConfig = (CompositeType) typeRegistry.register("artifactConfig", ArtifactConfiguration.class);
        typeRegistry.register("fileArtifactConfig", FileArtifactConfiguration.class);
        typeRegistry.register("directoryArtifactConfig", DirectoryArtifactConfiguration.class);

        artifactConfig.addExtension("fileArtifactConfig");
        artifactConfig.addExtension("directoryArtifactConfig");


        // generated dynamically as new components are registered.
        CompositeType projectConfig = new CompositeType(Project.class, "projectConfig");
        projectConfig.addProperty(new TypeProperty("scm", typeRegistry.getType("scmConfig")));
        projectConfig.addProperty(new TypeProperty("type", typeRegistry.getType("typeConfig")));
        projectConfig.addProperty(new TypeProperty("general", typeRegistry.getType("generalConfig")));
        projectConfig.addProperty(new TypeProperty("cleanup", typeRegistry.getType("cleanupRuleConfig")));
        projectConfig.addProperty(new TypeProperty("changeViewer", typeRegistry.getType("changeViewerConfig")));
        
        ListType artifacts = new ListType();
        artifacts.setTypeRegistry(typeRegistry);
        artifacts.setCollectionType(typeRegistry.getType("artifactConfig"));
        projectConfig.addProperty(new TypeProperty("artifact", artifacts));

        MapType commitTransformers = new MapType();
        commitTransformers.setTypeRegistry(typeRegistry);
        commitTransformers.setCollectionType(typeRegistry.getType("commitConfig"));
        projectConfig.addProperty(new TypeProperty("commit", commitTransformers));

        // define the root level scope.
        MapType projectCollection = new MapType(HashMap.class);
        projectCollection.setTypeRegistry(typeRegistry);
        projectCollection.setCollectionType(projectConfig);

        typeRegistry.register("projectConfig", projectConfig);

        configurationPersistenceManager.register("project", projectCollection);

        // setup the global configuration options.
        typeRegistry.register("generalAdminConfig", GeneralAdminConfiguration.class);
        typeRegistry.register("loggingConfig", LoggingConfiguration.class);
        typeRegistry.register("emailConfig", EmailConfiguration.class);
        typeRegistry.register("ldapConfig", LDAPConfiguration.class);
        typeRegistry.register("jabberConfig", JabberConfiguration.class);
        typeRegistry.register("licenseConfig", LicenseConfiguration.class);

        CompositeType globalConfig = new CompositeType(Object.class, "globalConfig");
        globalConfig.addProperty(new TypeProperty("general", typeRegistry.getType("generalAdminConfig")));
        globalConfig.addProperty(new TypeProperty("logging", typeRegistry.getType("loggingConfig")));
        globalConfig.addProperty(new TypeProperty("email", typeRegistry.getType("emailConfig")));
        globalConfig.addProperty(new TypeProperty("ldap", typeRegistry.getType("ldapConfig")));
        globalConfig.addProperty(new TypeProperty("jabber", typeRegistry.getType("jabberConfig")));
        globalConfig.addProperty(new TypeProperty("license", typeRegistry.getType("licenseConfig")));

        configurationPersistenceManager.register("global", globalConfig);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
