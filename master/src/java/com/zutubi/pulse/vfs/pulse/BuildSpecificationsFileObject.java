package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class BuildSpecificationsFileObject extends AbstractPulseFileObject
{
    public BuildSpecificationsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        long id = convertToSpecificationId(fileName.getBaseName());
        return objectFactory.buildBean(BuildSpecificationFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, id, pfs}
        );
    }

    private long convertToSpecificationId(String baseName)
    {
        // is it a name of an id?
        try
        {
            return Long.parseLong(baseName);
        }
        catch (NumberFormatException e)
        {
            try
            {
                Project project = getProject();
                BuildSpecification spec = project.getBuildSpecification(baseName);
                if (spec != null)
                {
                    return spec.getId();
                }
                return -1;
            }
            catch (FileSystemException e1)
            {
                return -1;
            }
        }
    }

    private Project getProject() throws FileSystemException
    {
        ProjectProvider provider = (ProjectProvider) getAncestor(ProjectProvider.class);
        return provider.getProject();
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> children = new LinkedList<String>();

        Project project = getProject();
        for (BuildSpecification spec : project.getBuildSpecifications())
        {
            children.add(spec.getName());
        }
        return children.toArray(new String[children.size()]);
    }
}
