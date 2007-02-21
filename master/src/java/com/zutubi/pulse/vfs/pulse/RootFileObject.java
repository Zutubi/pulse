package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The root file object of the pulse file system. This file object defines the root
 * 'directories/folders' available within the file system.
 *
 */
public class RootFileObject extends AbstractPulseFileObject
{
    private static final Map<String, Class> nodesDefinitions = new HashMap<String, Class>();
    {
        // setup the default root node definitions.
        nodesDefinitions.put("artifacts", ArtifactsFileObject.class);
        nodesDefinitions.put("builds", BuildsFileObject.class);
        nodesDefinitions.put("plugins", PluginsFileObject.class);
        nodesDefinitions.put("projects", ProjectsFileObject.class);
    }

    public RootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> rootPaths = nodesDefinitions.keySet();
        return rootPaths.toArray(new String[rootPaths.size()]);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String name = fileName.getBaseName();
        if (nodesDefinitions.containsKey(name))
        {
            Class clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }
}
