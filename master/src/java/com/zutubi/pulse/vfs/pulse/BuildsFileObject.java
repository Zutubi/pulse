package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.search.BuildResultExpressions;
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
public class BuildsFileObject extends AbstractPulseFileObject
{
    private long projectId;

    public BuildsFileObject(final FileName name, final long projectId, final AbstractFileSystem fs)
    {
        super(name, fs);
        
        this.projectId = projectId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String buildId = fileName.getBaseName();
        return new BuildFileObject(fileName, Long.parseLong(buildId), pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        SearchQuery<Long> query = pfs.getQueries().getIds(BuildResult.class);
        query.setProjection(Projections.id());
        query.add(BuildResultExpressions.projectEq(projectId));

        List<Long> buildIds = query.list();

        String[] children = new String[buildIds.size()];
        int i = 0;
        for (Long id : buildIds)
        {
            children[i++] = Long.toString(id);
        }

        return children;
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