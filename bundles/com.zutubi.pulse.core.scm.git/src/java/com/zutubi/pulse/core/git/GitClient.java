package com.zutubi.pulse.core.git;

import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmCapability;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.ScmEventHandler;
import com.zutubi.pulse.core.scm.FileStatus;
import com.zutubi.pulse.core.scm.ScmFile;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.Changelist;

import java.util.Set;
import java.util.List;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class GitClient implements ScmClient
{

    public void close()
    {

    }

    public Set<ScmCapability> getCapabilities()
    {
        return null;
    }

    public String getUid() throws ScmException
    {
        return null;
    }

    public String getLocation() throws ScmException
    {
        return null;
    }

    public Revision checkout(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        return null;
    }

    public Revision update(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        return null;
    }

    public InputStream retrieve(String path, Revision revision) throws ScmException
    {
        return null;
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {

    }

    public FileStatus.EOLStyle getEOLPolicy() throws ScmException
    {
        return null;
    }

    public Revision getLatestRevision() throws ScmException
    {
        return null;
    }

    public List<Revision> getRevisions(Revision from, Revision to) throws ScmException
    {
        return null;
    }

    public List<Changelist> getChanges(Revision from, Revision to) throws ScmException
    {
        return null;
    }

    public List<ScmFile> browse(String path, Revision revision) throws ScmException
    {
        return null;
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws ScmException
    {

    }

    public Revision getRevision(String revision) throws ScmException
    {
        return null;
    }
}
