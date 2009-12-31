package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.vfs.provider.pulse.reference.ReferenceRootFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * The root file object of the pulse file system. This file object defines the root
 * 'directories/folders' available within the file system.
 */
public class RootFileObject extends StaticMappingFileObject
{
    public static final String NODE_ARTIFACTS       = "artifacts";
    public static final String NODE_BUILDS          = "builds";
    public static final String NODE_CONFIG          = "config";
    public static final String NODE_PLUGINS         = "plugins";
    public static final String NODE_PROJECTS        = "projects";
    public static final String NODE_PROJECT_CONFIGS = "cprojects";
    public static final String NODE_REFERENCE       = "reference";
    public static final String NODE_TEMPLATES       = "templates";
    public static final String NODE_WIZARDS         = "wizards";

    {
        // setup the default root node definitions.
        nodesDefinitions.put(NODE_ARTIFACTS, ArtifactsFileObject.class);
        nodesDefinitions.put(NODE_BUILDS, BuildsFileObject.class);
        nodesDefinitions.put(NODE_CONFIG, ConfigFileObject.class);
        nodesDefinitions.put(NODE_PLUGINS, PluginsFileObject.class);
        nodesDefinitions.put(NODE_PROJECTS, ProjectsFileObject.class);
        nodesDefinitions.put(NODE_PROJECT_CONFIGS, ProjectConfigsFileObject.class);
        nodesDefinitions.put(NODE_REFERENCE, ReferenceRootFileObject.class);
        nodesDefinitions.put(NODE_TEMPLATES, TemplateScopesFileObject.class);
        nodesDefinitions.put(NODE_WIZARDS, WizardsFileObject.class);
    }

    public RootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }
}
