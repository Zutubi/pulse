package com.zutubi.pulse.scm.clearcase;

import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.filesystem.remote.RemoteFile;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class ClearCaseServer implements SCMServer
{
    private String url;

    public ClearCaseServer(String url)
    {
        this.url = url;
    }

    public Map<String, String> getServerInfo() throws SCMException
    {
        return new HashMap<String, String>();
    }

    public String getUid() throws SCMException
    {
        return null;
    }

    public String getLocation()
    {
        return url;
    }

    public void testConnection() throws SCMException
    {

    }

    public Revision checkout(String id, File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        return null;
    }

    public String checkout(Revision revision, String file) throws SCMException
    {
        return null;
    }

    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        return null;
    }

    public List<Revision> getRevisionsSince(Revision from) throws SCMException
    {
        return null;
    }

    public boolean hasChangedSince(Revision since) throws SCMException
    {
        return false;
    }

    public Revision getLatestRevision() throws SCMException
    {
        return null;
    }

    public RemoteFile getFile(String path) throws SCMException
    {
        return null;
    }

    public List<RemoteFile> getListing(String path) throws SCMException
    {
        return null;
    }

    public void update(String id, File workDir, Revision rev, List<Change> changes) throws SCMException
    {

    }

    public boolean supportsUpdate()
    {
        return false;
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
    {

    }

    public void writeConnectionDetails(File outputDir) throws SCMException, IOException
    {

    }
}
