package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

    public void close()
    {
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

    public Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public boolean hasChangedSince(Revision since) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        if(throwError)
        {
            throw new ScmException("test");
        }
        else
        {
            return new Revision("1");
        }
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Revision update(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void tag(ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException
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

    public EOLStyle getEOLPolicy(ScmContext context) throws ScmException
    {
        return EOLStyle.BINARY;
    }

    public Revision getRevision(String revision) throws ScmException
    {
        return parseRevision(revision);
    }

    public Revision parseRevision(String revision) throws ScmException
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
