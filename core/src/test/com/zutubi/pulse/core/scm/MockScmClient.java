package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.config.ResourceProperty;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 *
 *
 */
public class MockScmClient implements ScmClient, DataCacheAware
{
    private boolean throwError = false;
    protected Map<Object, Object> cache;
    protected String cacheId = "mock";

    public MockScmClient()
    {
    }

    public MockScmClient(boolean throwError)
    {
        this.throwError = throwError;
    }

    public void setThrowError(boolean throwError)
    {
        this.throwError = throwError;
    }

    public Set<ScmCapability> getCapabilities()
    {
        return new HashSet<ScmCapability>(Arrays.asList(ScmCapability.values()));
    }

    public Map<String, String> getServerInfo() throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public String getUid() throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public String getLocation()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void testConnection() throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Revision checkout(String id, File toDirectory, Revision revision, ScmEventHandler handler) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public InputStream retrieve(String path, Revision revision) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getChanges(Revision from, Revision to) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Revision> getRevisions(Revision from, Revision to) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public boolean hasChangedSince(Revision since) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Revision getLatestRevision() throws ScmException
    {
        if(throwError)
        {
            throw new ScmException("test");
        }
        else
        {
            return new Revision(null, null, null, "1");
        }
    }

    public List<ScmFile> browse(String path) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void update(String id, File workDir, Revision rev, ScmEventHandler handler) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws ScmException
    {
        throw new RuntimeException("Method not implemented");
    }

    public List<ResourceProperty> getProperties(String id, File dir) throws ScmException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public FileStatus.EOLStyle getEOLPolicy() throws ScmException
    {
        return FileStatus.EOLStyle.BINARY;
    }

    public Revision getRevision(String revision) throws ScmException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public String getCacheId()
    {
        return "mock";
    }

    public void setCache(Map<Object, Object> cache)
    {
        this.cache = cache;
    }
}
