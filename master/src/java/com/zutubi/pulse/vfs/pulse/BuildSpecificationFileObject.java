package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildSpecification;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <class comment/>
 */
public class BuildSpecificationFileObject extends AbstractPulseFileObject implements BuildSpecificationProvider
{
    private static final Map<String, Class> nodesDefinitions = new HashMap<String, Class>();
    {
        // setup the default root node definitions.
        nodesDefinitions.put("latest", LatestBuildFileObject.class);
        nodesDefinitions.put("latestsuccessful", LatestSuccessfulBuildFileObject.class);
        nodesDefinitions.put("successful", LatestSuccessfulBuildFileObject.class);
    }

    private long buildSpecificationId;

    public BuildSpecificationFileObject(final FileName name, final long buildSpecificationId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.buildSpecificationId = buildSpecificationId;
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

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> rootPaths = nodesDefinitions.keySet();
        return rootPaths.toArray(new String[rootPaths.size()]);
    }

    public BuildSpecification getBuildSpecification()
    {
        return projectManager.getBuildSpecification(getBuildSpecificationId());
    }

    public long getBuildSpecificationId()
    {
        return buildSpecificationId;
    }
}
