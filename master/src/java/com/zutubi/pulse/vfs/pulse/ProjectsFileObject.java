package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.search.SearchQuery;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.hibernate.criterion.Projections;

import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;

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
        Long projectId = Long.parseLong(fileName.getBaseName());

        return objectFactory.buildBean(ProjectFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, projectId, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        SearchQuery<Long> query = pfs.getQueries().getIds(Project.class);
        query.setProjection(Projections.id());
        List<String> children = new LinkedList<String>();
        for (long id : query.list())
        {
            children.add(Long.toString(id));
        }
        return children.toArray(new String[children.size()]);
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