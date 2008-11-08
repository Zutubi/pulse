package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MockScmClient implements ScmClient, DataCacheAware
{
    public static String TEST_PROPERTY = "scm.prop";
    public static String TEST_VALUE    = "scm.val";
    
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

    public void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
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

    public List<ResourceProperty> getProperties(ExecutionContext context) throws ScmException
    {
        return Arrays.asList(new ResourceProperty(TEST_PROPERTY, TEST_VALUE));
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

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {
        throw new RuntimeException("Method not implemented.");
    }

    public EOLStyle getEOLPolicy(ScmContext context) throws ScmException
    {
        return EOLStyle.BINARY;
    }

    public Revision parseRevision(ScmContext context, String revision) throws ScmException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Revision getPreviousRevision(ScmContext context, Revision fileRevision, boolean isFile) throws ScmException
    {
        throw new RuntimeException("Method not yet implemented");
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
