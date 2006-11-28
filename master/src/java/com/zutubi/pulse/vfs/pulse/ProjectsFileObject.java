package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.search.SearchQuery;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.hibernate.criterion.Projections;

import java.io.InputStream;
import java.util.List;

/**
 * <class comment/>
 */
public class ProjectsFileObject extends AbstractPulseFileObject
{
    public ProjectsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String projectName = fileName.getBaseName();
        return new ProjectFileObject(fileName, projectName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        SearchQuery<String> query = pfs.getQueries().getStrings(Project.class);
        query.setProjection(Projections.property("name"));
        List<String> projectNames = query.list();
        return projectNames.toArray(new String[projectNames.size()]);
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }
}